package au.org.ala.expert

import groovyx.net.http.HTTPBuilder
import groovyx.net.http.ContentType
import grails.converters.JSON
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import grails.util.GrailsUtil

class SearchController {

    def searchService, metadataService, bieService, resultsCacheService

    def index() {
        def model =
            [bathomeValues: metadataService.bathomeValues, imcras: metadataService.getMarineRegions(), myLayer: metadataService.getMyLayerRegions(),
             localities: metadataService.localitiesByState, allFamilies: metadataService.getAllFamilies(),
             fishGroups: metadataService.getFishGroups(), criteria: new SearchCommand()]
        
        /*if (params.key) {
            model.key = params.key
            def res = resultsService.getResults(params.key)
            if (!res.error) {
                model.summary = [total: res.list.total, familyCount: res.list.familyCount]
                println "summary = ${model.summary}"
            }
        }*/

        if (params.debugModel == 'true') {
            model.criteria = null
            render model as JSON
        }
        else {
            model
        }
    }

    def distributionModelling() {
        [searchPage: grailsApplication.config.grails.serverURL]
    }

    static allSpecies = null

    def missingImages() {
        //if (!allSpecies) {
            def allCmd = new SearchCommand()
            //allCmd.fishGroup = 'flatheads'
            allSpecies = searchService.search(allCmd)
        //}
        def results = bieService.listMissingImages(allSpecies.results)
        [missing: results.matchedWithMissingImage,
         unmatched: results.unmatched,
         total: allSpecies.results.size(),
         searchPage: grailsApplication.config.grails.serverURL,
         namesOnly: params.namesOnly]
    }

    // deprecated
    def search(SearchCommand cmd) {

        // do the search
        def results = searchService.search(cmd)
        if (results.error) {
            log.error results.error
        }
        def list = results.results

        // register the results with the results service
        def error = ""
        def key = submitResults(list, cmd.queryDescription, "")
        if (key == null) {
            error = "Failed to save search results."
        }
        log.debug "stored key = ${key}"

        // create a summary
        def summary = searchService.speciesListSummary(list)

        // redisplay the search page with the results summary
        render(view: 'index', model: [summary: summary,
            localities: metadataService.localities,
            minDepth: cmd.minDepth ?: null,
            maxDepth: cmd.maxDepth ?: null,
            bathomeValues: metadataService.bathomeValues + ((cmd.minDepth || cmd.maxDepth) ? ['custom depth range'] : []),
            key: key,
            searchError: error,
            imcras: metadataService.getMarineRegions(),
            myLayer: metadataService.getMyLayerRegions(),
            query:  results.query,
            criteria: cmd])
    }

    /**
     * Main search service. Called via ajax.
     *
     * @param cmd the bound post data
     * @return
     */
    def ajaxSearch(SearchCommand cmd) {

        def result

        // families does not seem to bind automatically
        cmd.families = params.families

        // check to see if the same search is already in the results cache
        def key = searchService.buildQuery(cmd).encodeAsMD5()
        log.debug "Calculated key = ${key}"
        if (resultsCacheService.hasKey(key)) {
            def cachedResult = resultsCacheService.get(key)
            // bundle the results
            result = [summary: [total: cachedResult.list.total,
                    familyCount: cachedResult.list.familyCount], query: cachedResult.query,
                    queryDescription: cmd.queryDescription, key: key, cached: true]
            log.debug "serving results from cache"
        }
        else {
            // do the search
            /*long startTime = System.currentTimeMillis();
            log.debug "timing----- 0"*/

            def searchResults = searchService.search(cmd)
            //log.debug "search took ----- " + (System.currentTimeMillis() - startTime) / 1000 + " seconds"
            if (searchResults.error) {
                log.debug searchResults.error
            }

            // add the family metadata (image, common name, etc)
            /*startTime = System.currentTimeMillis();
            log.debug "timing----- 0"*/
            searchResults.families = bieService.getFamilyMetadata(searchResults.results/*, startTime*/)
            /*log.debug "family processing took ----- " + (System.currentTimeMillis() - startTime) / 1000 + " seconds"

            startTime = System.currentTimeMillis();*/

            // create a summary
            def summary = [total: searchResults.results.size(),
                    familyCount: searchResults.families.size()]
            //searchService.speciesListSummary(searchResults.results)


            log.debug(summary)


            // inject some summary counts into the results
            searchResults.total = summary.total
            searchResults.familyCount = summary.familyCount

            //log.debug "summary at ----- " + (System.currentTimeMillis() - startTime) / 1000 + " seconds"

            // register the results with the results service
            def storeError = ""
            if (!searchResults.error) {
                key = submitResults(searchResults, cmd.queryDescription, "")
                if (key == null) {
                    storeError = "Failed to save search results."
                }
                log.debug "stored key = ${key}"
            }
            //println "stored at ----- " + (System.currentTimeMillis() - startTime) / 1000 + " seconds"

            // bundle the results
            result = [summary: summary, query: searchResults.query,
                    queryDescription: cmd.queryDescription, key: key]
            if (storeError) {
                result.error = storeError
            }
            if (searchResults.error) {
                result.error = searchResults.error
            }

            //println "remaining processing took ----- " + (System.currentTimeMillis() - startTime) / 1000 + " seconds"
        }

        //println "sending response with ${result.summary.total} species"
        render result as JSON
    }
    
    def getWkt = {
        log.debug "SearchController::getWkt = ${params.pid}"

        if (params.pid) {
            render metadataService.getImcraPolyAsWkt(params.pid)
        }
        else {
            render "error: you must supply a pid"
        }
    }

    def getMyLayer = {
        log.debug "SearchController::getMyLayer = ${params.pid}"

        if (params.pid) {
            response.setHeader("Access-Control-Allow-Origin", "*");

            if(params.pid.equals("cl21")) {
                render metadataService.getMarineRegions() as JSON;
            } else if(params.pid.equals("cl1051")) {
                render metadataService.getCapad2014Regions() as JSON;
            }
        }
        else {
            render "error: you must supply a layer pid"
        }
    }

    def test = {
        def html = searchService.test()
        render html
    }

    def briefList(String key) {
        def model = [:]
        if (key) {
            withHttp(uri: grailsApplication.config.results.cache.baseUrl + '/',
                    contentType: groovyx.net.http.ContentType.JSON) {
                def query = [key: key]
                ['start','pageSize','sortBy','sortOrder'].each {
                    if (params[it]) {query[it] = params[it]}
                }
                def resp = get(path: 'getPage', query: query)
                if (resp.error) {
                    model.error = resp.error
                }
                else {
                    //println "SEARCH:: list = " + resp.getClass()
                    model = [key: key, list: resp.list]
                }
            }
        }
        else {
            model.error = 'no key passed'
        }
        model
    }

    String submitResults(list, queryDescription, key) {
        println grailsApplication.config.results.cache.baseUrl

        def http = new HTTPBuilder(grailsApplication.config.results.cache.baseUrl + '/')
        http.request( groovyx.net.http.Method.POST, groovyx.net.http.ContentType.JSON) {
            //Modified by Alan on for fetching multiple layers on 30/07/2014 --- START

            /*
            uri.path = 'submit'

            def bodyMap = [ list: list, queryDescription: queryDescription, query: list.query]

            if (key) {bodyMap.key = key}

            def formDataStr = bodyMap as JSON

            def stream = new ByteArrayOutputStream()
            stream.write( formDataStr.toString().getBytes("UTF-8"))

            body = stream
            requestContentType = ContentType.URLENC
            */
            //Modified by Alan --- END

            uri.path = 'submit'
            body = [ list: list, queryDescription: queryDescription, query: list.query]
            if (key) {body.key = key}
            requestContentType = ContentType.URLENC

            response.success = { resp, json ->
                return json.key
            }

            response.failure = { resp ->
                log.debug "Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
                return null
            }
        }
    }

    def allFamilies = {
        render metadataService.getAllFamilies() as JSON
    }

    def debugSearch() {

        SearchCommand cmd = new SearchCommand()
        cmd.maxDepth = 40
        
        // do the search
        def results = searchService.search(cmd)
        
        render results as JSON
    }

    def reloadConfig = {
        def resolver = new PathMatchingResourcePatternResolver()
        def resource = resolver.getResource(grailsApplication.config.reloadable.cfgs[0])
        def stream

        try {
            stream = resource.getInputStream()
            ConfigSlurper configSlurper = new ConfigSlurper(GrailsUtil.getEnvironment())
            if(resource.filename.endsWith('.groovy')) {
                def newConfig = configSlurper.parse(stream.text)
                grailsApplication.getConfig().merge(newConfig)
                render "Config reloaded"
            }
            else if(resource.filename.endsWith('.properties')) {
                def props = new Properties()
                props.load(stream)
                def newConfig = configSlurper.parse(props)
                grailsApplication.getConfig().merge(newConfig)
                render "Config reloaded: " + props.toString()
            }
        }
        catch (GroovyRuntimeException gre) {
            log.error "Unable to reload configuration. Please correct problem and try again: " + gre.getMessage()
            render "Unable to reload configuration - " + gre.getMessage()
        }
        finally {
            stream?.close()
        }
    }

    def googleTest = {}

    // num is at least 4, 16 will be similar to a circle
    public String getCircle(double X, double Y, double R, int num) {
        String circle = "";
        if (num < 4)
            num = 4;
        double[] x = new double[num + 1];
        double[] y = new double[num + 1];
        double unit = 2*Math.PI / num;
        for (int i = 0; i < num + 1; i++) {
            x[i] = X + R * Math.sin(unit * i);
            y[i] = Y + R * Math.cos(unit * i);
            if (i != num)
                circle = circle + y[i] + " " + x[i] + ",";
            else
                circle = circle + y[i] + " " + x[i];
        }

        return circle;
    }

    def clearTemplateCache = {
        hf.clearCache()
        render "Done"
    }
}

class SearchCommand {

    def metadataService

    String wkt
    String state
    String locality // name|lat|lng
    float radius
    String bathome
    int minDepth
    int maxDepth
    String fishGroup
    boolean endemic
    String ecosystem
    String families
    String imcra
    String imcraPid
    //Added by Alan on for fetching multiple layers on 30/07/2014 --- START
    String myLayer
    String myLayerPid
    //Added by Alan --- END
    String circleLat
    String circleLon
    float circleRadius
    String search
    String clear

    static constraints = {
        wkt(nullable: true)
        state(nullable: true)
        locality(nullable: true)
        bathome(nullable: true)
        imcra(nullable: true)
        myLayer(nullable: true)
        fishGroup(nullable: true)
        endemic(nullable: true)
        ecosystem(nullable: true)
        families(nullable: true)
        imcraPid(nullable: true)
        myLayerPid(nullable: true)
        circleLat(nullable: true)
        circleLon(nullable: true)
        search(nullable: true)
        clear(nullable: true)
    }

    String getLocationBasedOn() {
        if (wkt) {
            return "wkt"
        }
        else if (locality) {
            return "locality"
        }
        else if (circleLat && circleLon && circleRadius) {
            return "circle"
        }
        else if (imcra && imcra != 'any') {
            return "marine area"
        }
        else if (state) {
            return "state"
        }
        else {
            return "all"
        }
    }
    
    boolean getIsAdvanced() {
        return (getLocationBasedOn() in ['wkt','circle','marine area'] ||
                getIsCustomDepth() || getIsCustomTaxonomy())
    }

    String getDepthBasedOn() {
        if (bathome == 'any (0-2000+m)') {
            return 'all'
        }
        else if (bathome && !(bathome == 'custom depth range')) {
            return "bathome"
        }
        else if (minDepth && maxDepth) {
            return "min and max"
        }
        else if (minDepth) {
            return "min"
        }
        else if (maxDepth) {
            return "max"
        }
        else {
            return "all"
        }
    }

    boolean getIsCustomDepth() {
        return getDepthBasedOn() in ['min', 'max', 'min and max']
    }

    String getGroupBasedOn() {
        if (fishGroup) {
            return "fishGroup"
        }
        return "all"
    }

    boolean getIsCustomTaxonomy() {
        return ecosystem as boolean || families as boolean
    }

    def getFamilies() {
        return families ? families.tokenize(',') : []
    }

    def getMarineArea() {
        return [imcra: imcra, pid: imcraPid]
    }

    def getDepthRange() {
        switch (depthBasedOn) {
            case 'bathome':
                switch (bathome) {
                    // TODO: use a common source for these strings and the values in MetadataService
                    case "coastal/shallow water (0-40m)": return [minD:0, maxD:40]
                    case "shelf (0-200m)": return [minD:0, maxD:200]
                    case "shelf + upper slope (0-500m)": return [minD:0, maxD:500]
                    case "upper slope only (200-500m)": return [minD:200, maxD:500]
                    default: return [minD:0, maxD:0]
                }
                break
            case 'min and max': return [minD: minDepth, maxD: maxDepth]
            case 'min': return [minD: minDepth, maxD: 0]
            case 'max': return [minD: 0, maxD: maxDepth]
            case 'all': return null  // shouldn't happen
        }
    }
    
    def getCircle() {
        if (locationBasedOn == 'circle') {
            return [lat: circleLat, lng: circleLon, radius: circleRadius]
        }
        else if (locationBasedOn == 'locality') {
            //def coords = metadataService.locationOf(locality)
            def loc = parseLocality()
            return [lat: loc.lat, lng: loc.lng, radius: radius]
        }
    }

    def getGroup() {
        switch (groupBasedOn) {
            case 'fishGroup': return fishGroup
            case 'all': return ""
        }
    }
    
    Map parseLocality() {
        def bits = locality.tokenize('|')
        return [name: bits[0], lat: bits[1], lng: bits[2]]
    }
    
    String getQueryDescription() {
        def loc = ""
        switch (locationBasedOn) {
            case 'circle':
                loc = "Circle (${circleLat.toFloat().round(3)}, ${circleLon.toFloat().round(3)} " +
                            "@ ${(circleRadius/1000).round(2)}km)"; break
            case 'marine area': loc = imcra; break
            case 'locality': loc = parseLocality().name + " (${radius/1000}km)"; break
            //case 'state': loc = state; break
            case 'wkt': loc = "user defined area"/*condenseWkt(wkt)*/; break
            default: loc = "Australia"
        }
        def dep = ""
        switch (depthBasedOn) {
            case 'bathome': dep = bathome; break
            case 'min and max': dep = "minDepth=${minDepth} maxDepth=${maxDepth}"; break
            case 'min': dep = "minDepth=${minDepth}"; break
            case 'max': dep = "maxDepth=${maxDepth}"; break
        }
        def grp = ""
        switch (groupBasedOn) {
            case 'fishGroup': grp = fishGroup; break
        }
        def eco = ecosystem
        def fam = families
        return [loc, dep, grp, eco, fam].findAll({it}).join(', ')
    }
    
    String condenseWkt(wkt) {
        def bits = wkt.tokenize('.')  // split on decimal place
        bits[1..-1].eachWithIndex { bit, i ->
            def fifth = -1
            String str = ''
            boolean found = false
            bit.eachWithIndex { ch, j ->
                if (ch in [' ',',',')']) {
                    found = true
                }
                if (found || j < 4) {
                    str = str + ch  // add first 4 chars + chars after the number has finished
                }
            }
            bits[i+1] = str
        }
        return bits.join('.')
    }
}