package au.org.ala.expert

import grails.converters.JSON

class BieService {

    def webService, resultsService, grailsApplication

    /**
     * Returns a map of the unique families in the species list with relevant family metadata.
     *
     * @param list of species
     * @return map of families
     */
    def getFamilyMetadata(list/*, startTime*/) {

        // map to hold family metadata
        def families = [:]

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
            //println "Image for species ${repSpp.name} will be used for family ${name}"
        }

        //println "family loop finished at ----- " + (System.currentTimeMillis() - startTime) / 1000 + " seconds"

        // bulk lookup by guid for families
        def famBieData = doBulkLookup(families.values().collect {it.guid})
        def sppBieData = doBulkLookup(families.values().collect {it.repSpeciesGuid})
        families.each { name, fam ->
            def famData = famBieData[fam.guid]
            if (famData) {
                fam.common = famData.common
            }
            else {
                println "no common name found for ${name}"
            }
            def sppData = sppBieData[fam.repSpeciesGuid]
            if (sppData && sppData.image && sppData.image.largeImageUrl?.toString() != "null" &&
                        sppData.image.imageSource == grailsApplication.config.image.source.dataResourceUid) {
                fam.image = sppData.image
            }
            else {
                println "no image found for ${name}"
            }
        }

        return families
    }

    def doBulkLookup(guids) {
        def data = webService.doJsonPost(grailsApplication.config.bie.services.baseURL,
                "species/guids/bulklookup.json", "", (guids as JSON).toString())
        Map results = [:]
        data.searchDTOList.each {item ->
            results.put item.guid, [
                    common: item.commonNameSingle,
                    image: [largeImageUrl: item.largeImageUrl,
                            smallImageUrl: item.smallImageUrl,
                            thumbnailUrl: item.thumbnailUrl,
                            imageMetadataUrl: item.imageMetadataUrl,
                            imageSource: item.imageSource]]
        }
        return results
    }

    def listMissingImages(list) {
        def matchedWithMissingImage = []
        def unmatched = []
        def buckets = 0..(Math.ceil((list.size() as int)/1000) - 1)
        buckets.each { i ->
            def upper = Math.min(999 + i*1000, list.size() - 1)
            println "processing records ${i*1000} to ${upper}"
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
        println "${matchedWithMissingImage.size()} matched species have no image"
        println "${unmatched.size()} guids could not be matched in the BIE"

        matchedWithMissingImage.sort {it.name}

        return [matchedWithMissingImage: matchedWithMissingImage, unmatched: unmatched]
    }
}
