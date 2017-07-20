package au.org.ala.expert

class ResultsController {

//    def resultsCacheService, resultsService
//    static allowedMethods = [submit:'POST']
//
//    def index() { }
//
//    /**
//     * Store the supplied results list with a generated key.
//     *
//     * @param post body is a map: property 'list' holds the results
//     * @key optional key to use - this is intended for development only
//     * @return the generated key
//     */
//    def submit = {
//        def bodyText = request.reader.text
//        def body = JSON.parse(bodyText as String)
//        def key = body.key ?: body.query.encodeAsMD5()
//        resultsCacheService.put key, [list: body.list, query: body.query, queryDescription: body.queryDescription, time: new Date()]
//        def result = [key: key]
//        render result as JSON
//    }
//
//    /**
//     * Retrieve the raw stored results for the supplied key.
//     *
//     * @param key
//     * @return
//     */
//    def getResults(String key) {
//        def results = resultsCacheService.get(key)
//        if (results) {
//            render results as JSON
//        }
//        else {
//            def error = [error: "no results with key = ${key}"]
//            render error as JSON
//        }
//    }
//
//    /**
//     * Returns a paginated list of the stored results.
//     *
//     * @param key the identifier of the results set in the cache
//     * @param start the pagination index of the first item to display (optional defaults to 0)
//     * @param pageSize the number of items to display per page (optional defaults to 10)
//     * @param sortBy the property to sort on (optional)
//     * @param sortOrder normal or reverse (optional defaults to normal)
//     * @param facets a comma separated list of facets to include (optional)
//     * @param includeFacetMembers if present the property specified of each item in a facet will be included
//     *  as a list (optional)
//     * @param returnAll overrides pagination if true (optional)
//     * @param noResults excludes the results list if true (optional)
//     * @param taxonHierarchy returns a breakdown by family and genus if true (optional)
//     *
//     * @return a json representation of the page of results
//     */
//    def getPage(String key) {
//
//        def query = resultsCacheService.get(key)?.query
//        def queryDescription = resultsCacheService.get(key)?.queryDescription
//        def list = resultsCacheService.get(key)?.list
//        if (list) {
//            def speciesList = list.results?.clone()
//
//            // calculate any facets
//            def facets = []
//            params.facets?.tokenize(',')?.each {
//                facets << facetFor(speciesList, it, params.includeFacetMembers)
//            }
//
//            // calculate the hierarchy
//            def hierarchy = []
//            if (params.taxonHierarchy) {
//                hierarchy = buildFamilyHierarchy(list)
//            }
//
//            // return the json
//            def results = [queryDescription: queryDescription, query: query]
//            if (!params.noResults) {
//                results.list = speciesList
//            }
//            if (facets) {
//                results.facetResults = facets
//            }
//            if (hierarchy) {
//                results.taxonHierarchy = hierarchy
//            }
//            render results as JSON
//        }
//        else {
//            def error = [error: "no results with key = ${key}"]
//            render error as JSON
//        }
//    }
//
//    /**
//     * Build the facet data for the specified facet.
//     *
//     * @param list the list to facet
//     * @param facetName the property to facet
//     * @param includeFacetMembers if present the property specified of each item in a facet will be included
//     *  as a list (optional)
//     * @return map representing the facet values
//     */
//    def facetFor(list, facetName, includeFacetMembers) {
//        def clone = list.clone()  // because unique mutates list
//        // list of items with unique facet values
//        def uniqueFacets = clone.unique { it[facetName] }
//        // list of just the facet values
//        def facetValues = uniqueFacets.collect {it[facetName]}
//        // add the details
//        def facets = [fieldName: facetName, fieldResult: []]
//        facetValues.each { facetValue ->
//            // get the count - number of times the facet value occurs
//            def count = list.count {it[facetName] == facetValue }
//            def facetInstance = [count: count, label: facetValue]
//            if (includeFacetMembers) {
//                // add the list of the specified property of the members
//                facetInstance.members = list.findAll({it[facetName] == facetValue}).collect{it[includeFacetMembers]}
//            }
//            // add to the list of facet values
//            facets.fieldResult << facetInstance
//        }
//        // sort by descending count
//        facets.fieldResult.sort{-it.count}
//        return facets
//    }
//
//    def buildFamilyHierarchy(list) {
//        def results = []
//
//        // get unique families
//        def families = list.families
//        def speciesRecords = list.results
//
//        // try gathering records by family in one pass rather than searching separately for each
//        // 100x faster for large results sets
//        def recordsByFamily = [:]
//        speciesRecords.each {
//            if (!recordsByFamily.containsKey(it.family)) {
//                recordsByFamily.put it.family, []
//            }
//            recordsByFamily[it.family] << it
//        }
//
//        // for each family
//        families.each { name, data ->
//            def genera = []
//            def genusRecords = recordsByFamily[name]
//
//            // find unique genera
//            def genusNames = []
//            genusRecords.each{
//                if (!genusNames.contains(it.genus)) {
//                    genusNames << it.genus
//                }
//            }
//
//            // for each genus
//            genusNames.each { genusName ->
//                def species = genusRecords.findAll {it.genus == genusName}
//                genera << [name: genusName, speciesCount: species.size(), guid: species[0].genusGuid,
//                        repSppGuid: resultsService.pickFirstBestImage(species)?.guid]
//            }
//            //processTime += System.currentTimeMillis() - startTime
//            results << [name: name, guid: data.guid, common: data.common, image: data.image,
//                    caabCode: data.caabCode, genera: genera.sort {it.name}]
//        }
//
//        return results
//    }
//
//    def dumpCache = {
//        def cache = resultsCacheService.cache
//        def results = [totalEntries: cache.size(), keys: []]
//        results.keys = cache.keySet().collect {
//            [key:  it, query: cache[it].query, time: cache[it].time,
//             view: grailsApplication.config.grails.serverURL + "/results/getResults?key=" + it]
//        }
//        if (params.key) {
//            def data = cache[params.key]
//            if (data) {
//                results[params.key] = data
//            }
//        }
//        results.keys = results.keys.sort({it.time}).reverse()
//        render results as JSON
//    }
//
//    def clearCache = {
//        resultsCacheService.clear()
//        render 'cleared'
//    }
}
