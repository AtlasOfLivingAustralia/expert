package au.org.ala.expert

import grails.converters.JSON

class BieService {

    def webService, resultsService, grailsApplication, imageService, collectionsService

    /**
     * Returns a map of the unique families in the species list with relevant family metadata.
     *
     * @param list of species
     * @return map of families
     */
    def getFamilyMetadata(list) {

        // map to hold family metadata
        def families = [:]

        if (!list) {
            return families
        }

        // get unique families + known metadata
        list.each {
            if (!families.containsKey(it.family)) {
                families.put it.family, [
                        guid: it.familyGuid,
                        caabCode: it.familyCaabCode
                ]
            }
        }

        // find the first species in the results set for this family with the highest rated image
        families.each { name, fam ->
            def spp = list.findAll { it.family == name }
            def repSpp = resultsService.pickFirstBestImage(spp)
            fam.repSpeciesGuid = repSpp?.guid
        }

        // bulk lookup by guid for families
        def famBieData = doBulkLookup(families.values().collect {it.guid})
        def sppBieData = doBulkLookup(families.values().collect {it.repSpeciesGuid})
        families.each { name, fam ->
            def famData = famBieData[fam.guid]
            if (famData) {
                fam.common = famData.common
            }
            else {
                log.debug "no common name found for ${name}"
            }
            def sppData = sppBieData[fam.repSpeciesGuid]
            if (sppData && sppData.image && sppData.image.largeImageUrl?.toString() != "null" &&
                        sppData.image.imageSource == grailsApplication.config.image.source.dataResourceUid) {
                fam.image = sppData.image
            }
            else {
                log.debug "no image found for ${name}"
            }
        }

        return families
    }

    def doBulkLookup(guids) {
        def data = JSON.parse(webService.doJsonPost(grailsApplication.config.bie.services.baseURL,
                "/species/guids/bulklookup.json", "", (guids as JSON).toString(), 'application/json'))
        Map results = [:]
        data?.searchDTOList.each { item ->
            if (item?.guid) {
                results.put item.guid, [
                        common: item.commonNameSingle,
                        image : [largeImageUrl   : item.largeImageUrl,
                                 smallImageUrl   : item.smallImageUrl,
                                 thumbnailUrl    : item.thumbnailUrl,
                                 imageMetadataUrl: item.imageMetadataUrl,
                                 imageSource     : item.imageSource]]
            }
        }
        return results
    }

    def listMissingImages(list) {
        def matchedWithMissingImage = []
        def unmatched = []
        def buckets = 0..(Math.ceil((list.size() as int)/1000) - 1)
        buckets.each { i ->
            def upper = Math.min(999 + i*1000, list.size() - 1)
            log.debug "processing records ${i*1000} to ${upper}"
            def guids = list[i*1000..upper].collect {it.guid}
            def res = doBulkLookup(guids)

            // find guids that did not have a bie match
            guids.each { guid ->
                if (!res.containsKey(guid)) {
                    unmatched << [guid: guid, name: list.find({it.guid == guid}).name]
                }
            }

            // how many matched species have no image
            res.each { guid, rec ->
                if (!rec.image?.largeImageUrl ||
                        rec.image.imageSource != grailsApplication.config.image.source.dataResourceUid) {
                    matchedWithMissingImage << [guid: guid, common: rec.common, name: list.find({it.guid == guid}).name]
                }
            }
        }
        log.debug "${matchedWithMissingImage.size()} matched species have no image"
        log.debug "${unmatched.size()} guids could not be matched in the BIE"

        matchedWithMissingImage.sort {it.name}

        return [matchedWithMissingImage: matchedWithMissingImage, unmatched: unmatched]
    }

    def injectSpeciesMetadata(list) {

        // build a list of guids to lookup
        def guids = []
        list.each { sp ->
            if (sp.guid) {
                guids << sp.guid
            }
        }

        // look up the metadata
        def md = betterBulkLookup(guids)

        // inject the metadata
        list.each { sp ->
            def data = md[sp.guid]
            if (data) {
                //sp.common = data.common  // don't override common name with name from bie as CMAR is more authoritative
                if (data.image && data.image.largeImageUrl?.toString() != "null") {
                    def imageMetadata = imageService.getInfo(data.image.largeImageUrl)
                    if (imageMetadata && imageMetadata?.dataResourceUid == grailsApplication.config.image.source.dataResourceUid) {
                        sp.image = data.image
                        sp.image.putAll(imageMetadata)
                    }

                }
            } else {
                log.debug "No metadata found for species ${sp.name} (guid = ${sp.guid})"
            }
        }

        return list
    }

    def betterBulkLookup(list) {
        def url = grailsApplication.config.bie.baseURL + "/ws/species/guids/bulklookup.json"
        def data = JSON.parse(webService.doJsonPost(url, "", null, (list as JSON).toString(), 'application/json'))
        Map results = [:]
        for (int i = 0; i < list.size(); i++) {
            def item = null
            if (data?.searchDTOList != null && data.searchDTOList.size() > i) {
                item = data.searchDTOList.get(i)
            }

            if (item) {
                def imageMetadata = imageService.getInfo(item.largeImageUrl)
                if (imageMetadata.dataResourceUid) {
                    def imageDataResourceMetadata = collectionsService.getInfo(imageMetadata.dataResourceUid)
                    if (imageDataResourceMetadata) {
                        results.put(item.guid, [
                                common: item.commonNameSingle,
                                image : [largeImageUrl   : item.largeImageUrl,
                                         smallImageUrl   : item.smallImageUrl,
                                         thumbnailUrl    : item.thumbnailUrl,
                                         imageMetadataUrl: item.largeImageUrl ? item.largeImageUrl.replace('proxyImage', 'details') : null,
                                         imageSource     : imageDataResourceMetadata.name]])
                    }
                }
            }
        }
        return results
    }

    def injectGenusMetadata(list) {

        // build a list of genus guids to lookup
        def guids = []
        list.each { fam ->
            fam.genera.each { gen ->
                if (gen.guid) {
                    guids << gen.guid
                }
                if (gen.repSppGuid) {
                    guids << gen.repSppGuid
                }
            }
        }

        // look up the metadata
        def md = betterBulkLookup(guids)

        // inject the metadata
        list.each { fam ->
            fam.genera.each { gen ->
                def genData = md[gen.guid]
                if (genData) {
                    gen.common = genData.common
                } else {
                    log.debug "No metadata found for genus ${gen.name} (guid = ${gen.guid})"
                }
                def sppData = md[gen.repSppGuid]
                if (sppData) {
                    if (sppData.image && sppData.image.largeImageUrl?.toString() != "null") {
                        def imageMetadata = imageService.getInfo(sppData.image.largeImageUrl)
                        if (imageMetadata && imageMetadata?.dataResourceUid == grailsApplication.config.image.source.dataResourceUid) {
                            gen.image = sppData.image
                            gen.image.putAll(imageMetadata)

                            if (!fam.image) fam.image = gen.image
                        }
                    }
                } else {
                    log.debug "No image found for genus ${gen.name} (guid = ${gen.guid})"
                }
            }
        }

        return list
    }
}
