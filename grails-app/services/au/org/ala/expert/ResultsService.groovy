package au.org.ala.expert

class ResultsService {

    def webService
    def grailsApplication
    def searchService

    def getResultsPage(String key, String facets, String includeFacetMembers, boolean includeHierarchy, Map params) {
        def model = [:]
        if (key) {

            def cache = searchService.ajaxSearch(null, key)
            if (!cache) {
                return null
            }
            def list = cache?.results
            def speciesList = list?.results?.clone()

            facets?.tokenize(',')?.each {
                facets << facetFor(speciesList, it, includeFacetMembers)
            }

            // calculate the hierarchy
            def hierarchy = []
            if (includeHierarchy) {
                hierarchy = buildFamilyHierarchy(list)
            }

            // return the json
            def results = [queryDescription: cache.queryDescription, query: cache.query]
            if (!params.noResults) {
                results.list = speciesList
            }
            if (facets) {
                results.facetResults = facets
            }
            if (hierarchy) {
                results.taxonHierarchy = hierarchy
            }

            model = [key   : key, query: results.query, queryDescription: results.queryDescription,
                     list  : speciesList,
                     facets: results.facetResults, taxonHierarchy: results.taxonHierarchy]

        }
        else {
            model.error = 'no key passed'
        }

        return model
    }

    def pickFirstBestImage(list) {
        // use for loop so we can bail early as possible
        for (Map s in list) {
            if (s.imageQuality == 'E') {
                return s
            }
        }
        for (Map s in list) {
            if (s.imageQuality == 'G') {
                return s
            }
        }
        for (Map s in list) {
            if (s.imageQuality == 'A') {
                return s
            }
        }
        for (Map s in list) {
            if (s.imageQuality == 'P') {
                return s
            }
        }
        return list ? list[0] : null
    }

    /**
     * Build the facet data for the specified facet.
     *
     * @param list the list to facet
     * @param facetName the property to facet
     * @param includeFacetMembers if present the property specified of each item in a facet will be included
     *  as a list (optional)
     * @return map representing the facet values
     */
    def facetFor(list, facetName, includeFacetMembers) {
        def clone = list.clone()  // because unique mutates list
        // list of items with unique facet values
        def uniqueFacets = clone.unique { it[facetName] }
        // list of just the facet values
        def facetValues = uniqueFacets.collect { it[facetName] }
        // add the details
        def facets = [fieldName: facetName, fieldResult: []]
        facetValues.each { facetValue ->
            // get the count - number of times the facet value occurs
            def count = list.count { it[facetName] == facetValue }
            def facetInstance = [count: count, label: facetValue]
            if (includeFacetMembers) {
                // add the list of the specified property of the members
                facetInstance.members = list.findAll({ it[facetName] == facetValue }).collect {
                    it[includeFacetMembers]
                }
            }
            // add to the list of facet values
            facets.fieldResult << facetInstance
        }
        // sort by descending count
        facets.fieldResult.sort { -it.count }
        return facets
    }

    def buildFamilyHierarchy(list) {
        def results = []

        // get unique families
        def families = list.families
        def speciesRecords = list.results

        // try gathering records by family in one pass rather than searching separately for each
        // 100x faster for large results sets
        def recordsByFamily = [:]
        speciesRecords.each {
            if (!recordsByFamily.containsKey(it.family)) {
                recordsByFamily.put it.family, []
            }
            recordsByFamily[it.family] << it
        }

        // for each family
        families.each { name, data ->
            def genera = []
            def genusRecords = recordsByFamily[name]

            // find unique genera
            def genusNames = []
            genusRecords.each {
                if (!genusNames.contains(it.genus)) {
                    genusNames << it.genus
                }
            }

            // for each genus
            genusNames.each { genusName ->
                def species = genusRecords.findAll { it.genus == genusName }
                genera << [name      : genusName, speciesCount: species.size(), guid: species[0].genusGuid,
                           repSppGuid: pickFirstBestImage(species)?.guid]
            }
            //processTime += System.currentTimeMillis() - startTime
            results << [name    : name, guid: data.guid, common: data.common, image: data.image,
                        caabCode: data.caabCode, genera: genera.sort { it.name }]
        }

        return results
    }
}
