package au.org.ala.expert

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import grails.converters.JSON
import net.sf.json.JSONNull

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

        // get unique families + known metadata
        def uniqueByFamily = list.clone().unique({it.family})
        uniqueByFamily.each {
            families.put it.family, [
                    guid: it.familyGuid,
                    caabCode: it.familyCaabCode
            ]
            familyGuids << it.familyGuid
        }

        // bulk lookup by guid
        def data = doBulkLookup(familyGuids)

        // inject extra metadata
        families.each { name, fam ->
            def famData = data[name.toUpperCase()]
            if (famData) {
                fam.common = famData.common
                fam.image = famData.image
            }
            else {
                println "no data found for ${name}"
            }
        }

        return families
    }

    def doBulkLookup(list) {
        def url = ConfigurationHolder.config.bie.baseURL
        def data = webService.doJsonPost(url,
                "species/guids/bulklookup.json", "", (list as JSON).toString())
        Map results = [:]
        data.searchDTOList.each {family ->
            def name = family.name
            if (family.acceptedConceptName != 'null') { name = family.acceptedConceptName }
            def image = [:]
            results.put name.toString().toUpperCase(), [
                    common: family.commonNameSingle,
                    guid: family.guid,
                    image: [largeImageUrl: family.largeImageUrl,
                            smallImageUrl: family.smallImageUrl,
                            thumbnailUrl: family.thumbnailUrl,
                            imageMetadataUrl: family.imageMetadataUrl]]
        }
        return results
    }

}
