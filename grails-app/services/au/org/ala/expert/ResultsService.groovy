package au.org.ala.expert

class ResultsService {

    def webService

    def getResultsPage(String key, String facets, String includeFacetMembers, boolean includeHierarchy, Map params) {
        def model = [:]
        if (key) {
            def action = "looking up page from results cache"
            def url = grailsApplication.config.results.cache.baseUrl + '/getPage' +
                    "?key=${key}"
            if (facets) { url += "&facets=${facets}" }
            if (includeFacetMembers) { url += "&includeFacetMembers=${includeFacetMembers}" }
            if (includeHierarchy) { url += "&taxonHierarchy=true" }
            ['start','pageSize','sortBy','sortOrder'].each {
                if (params[it]) {
                    url += "&${it}=${params[it]}"
                }
            }
            //println "url = " + url
            def resp = webService.getJson(url)
            if (resp.error) {
                model.error = resp.error
            }
            else {
                model = [key: key, query: resp.query, queryDescription: resp.queryDescription,
                        list: resp.list,
                        facets: resp.facetResults, taxonHierarchy: resp.taxonHierarchy]
            }
        }
        else {
            model.error = 'no key passed'
        }

        return model
    }

    def getResults(String key) {
        def model = [:]
        if (key) {
            def action = "looking up results from results cache"
            def url = grailsApplication.config.results.cache.baseUrl + "/getResults?key=${key}"
            assert webService
            def resp = webService.getJson(url)
            if (resp.error) {
                model.error = resp.error
            }
            else {
                model = [key: key, query: resp.query, queryDescription: resp.queryDescription,
                        list: resp.list]
            }
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
}
