package au.org.ala.expert

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import grails.converters.JSON

class BieService {

    def webService, resultsService

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
            println "Image for species ${repSpp.name} will be used for family ${name}"
        }

        //println "family loop finished at ----- " + (System.currentTimeMillis() - startTime) / 1000 + " seconds"

        // bulk lookup by guid for families
        def famBieData = doBulkLookup(families.values().collect {it.guid})
        def sppBieData = doBulkLookup(families.values().collect {it.repSpeciesGuid})
        families.each { name, fam ->
            def famData = famBieData[fam.guid]
            if (famData) {
                fam.common = famData.common
                if (!ConfigurationHolder.config.expert.images.useConstructedUrls) {
                    fam.image = famData.image
                }
            }
            else {
                println "no common name found for ${name}"
            }
            def sppData = sppBieData[fam.repSpeciesGuid]
            if (sppData) {
                fam.image = sppData.image
            }
            else {
                println "no image found for ${name}"
            }
        }

        return families
    }

    def doBulkLookup(guids) {
        def url = ConfigurationHolder.config.bie.baseURL
        //println url
        def data = webService.doJsonPost(url,
                "species/guids/bulklookup.json", "", (guids as JSON).toString())
        //println data
        Map results = [:]
        data.searchDTOList.each {item ->
            results.put item.guid, [
                    common: item.commonNameSingle,
                    image: [largeImageUrl: item.largeImageUrl,
                            smallImageUrl: item.smallImageUrl,
                            thumbnailUrl: item.thumbnailUrl,
                            imageMetadataUrl: item.imageMetadataUrl]]
        }
        return results
    }

}
