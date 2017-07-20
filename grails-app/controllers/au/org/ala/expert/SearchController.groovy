package au.org.ala.expert

import grails.converters.JSON

class SearchController {

    def searchService, metadataService, bieService

    def index() {
        def model =
            [bathomeValues: metadataService.bathomeValues, imcras: metadataService.getMarineRegions(), myLayer: metadataService.getMyLayerRegions(),
             localities: metadataService.localitiesByState, allFamilies: metadataService.getAllFamilies(),
             fishGroups: metadataService.getFishGroups(), criteria: new SearchCommand()]

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

    /**
     * Main search service. Called via ajax.
     *
     * @param cmd the bound post data
     * @return
     */
    def ajaxSearch(SearchCommand cmd) {

        // families does not seem to bind automatically
        cmd.families = params.families

        // check to see if the same search is already in the results cache
        def key = searchService.buildQuery(cmd).encodeAsMD5()
        log.debug "Calculated key = ${key}"

        def result = searchService.ajaxSearch(cmd, key)

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
            if(params.pid.equals("cl21")) {
                render metadataService.getMarineRegions() as JSON
            } else if(params.pid.equals("cl1051")) {
                render metadataService.getCapad2014Regions() as JSON
            }
        }
        else {
            render "error: you must supply a layer pid"
        }
    }

    def allFamilies = {
        render metadataService.getAllFamilies() as JSON
    }

    // num is at least 4, 16 will be similar to a circle
    String getCircle(double X, double Y, double R, int num) {
        String circle = ""
        if (num < 4)
            num = 4
        double[] x = new double[num + 1]
        double[] y = new double[num + 1]
        double unit = 2 * Math.PI / num
        for (int i = 0; i < num + 1; i++) {
            x[i] = X + R * Math.sin(unit * i)
            y[i] = Y + R * Math.cos(unit * i)
            if (i != num)
                circle = circle + y[i] + " " + x[i] + ","
            else
                circle = circle + y[i] + " " + x[i]
        }

        return circle
    }
}