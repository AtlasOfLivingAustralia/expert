package au.org.ala.expert

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import grails.converters.JSON

class BieService {

    def webService

    /**
     * Returns a map of the unique families in the species list with relevant family metadata.
     *
     * @param list of species
     * @return map of families
     */
    def getFamilyMetadata(list) {

        // map to hold family metadata
        def families = [:]

        // list of guids to look up
        def familyGuids = []
        def speciesGuids = []

        // get unique families + known metadata
        def uniqueByFamily = list.clone().unique({it.family})
        uniqueByFamily.each {
            families.put it.family, [
                    guid: it.familyGuid,
                    anySpeciesGuid: it.guid,
                    caabCode: it.familyCaabCode
            ]
            if (ConfigurationHolder.config.expert.images.useConstructedUrls) {
                families[it.family].image = [largeImageUrl: ConfigurationHolder.config.bie.baseURL +
                        '/species/image/large/' + it.guid]
            }
            if (it.familyGuid) {
                familyGuids << it.familyGuid
            }
            if (it.guid) {
                speciesGuids << it.guid
            }
        }

        // bulk lookup by guid for families
        def data = doBulkLookup(familyGuids)
        families.each { name, fam ->
            def famData = data[fam.guid]
            if (famData) {
                fam.common = famData.common
                if (!ConfigurationHolder.config.expert.images.useConstructedUrls) {
                    fam.image = famData.image
                }
            }
            else {
                println "no common name found for ${name}"
            }
        }

/*
        // bulk lookup by guid for species
        def spdata = doBulkLookup(speciesGuids)
        families.each { name, fam ->
            def spData = spdata[fam.anySpeciesGuid]
            if (spData) {
                fam.image = spData.image
            }
            else {
                println "no image data found for ${name}"
            }
        }
*/

        return families
    }

    def doBulkLookup(guids) {
        def url = ConfigurationHolder.config.bie.baseURL
        println url
        def data = webService.doJsonPost(url,
                "species/guids/bulklookup.json", "", (guids as JSON).toString())
        println data
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
