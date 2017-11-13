package au.org.ala.expert

import grails.plugin.cache.Cacheable
import grails.util.Holders

class MetadataService {

    def webService, grailsApplication

    def getBathomeValues() {
        Holders.config.depthMeasures.collect() { it -> it.label } + Holders.config.depthMeasuresAll
    }

    def getFishGroups() {
        def fg = getAllGroups()
        return [keys: fg, display: fg.collect {it[0].toUpperCase() + it[1..-1]}]
    }

    def getLocalitiesByState() {
        Holders.config.localityStates.collect() { it ->
            [state: it.label, localities: localities.values().findAll({ it.state == 'NSW' })]
        }
    }
    
    static imcraWktCache = [:]
    
    def getImcraPolyAsWkt(pid) {
        if (imcraWktCache[pid]) {
            return imcraWktCache[pid]
        }

        def wkt = webService.get("http://spatial.ala.org.au/ws/shape/wkt/${pid}")

        imcraWktCache.put pid, wkt
        log.debug wkt
        return wkt
    }

    @Cacheable("metadataService")
    def loadFamiliesAndGroups() {
        def all = webService.getJson(grailsApplication.config.spatial.layers.service.url +
                "/distributions.json?dataResourceUid=" +
                grailsApplication.config.distribution.maps.dataResourceUid)
        // protect against an error response
        if (!(all instanceof List)) {
            return []
        }

        def familiesCache = []
        def groupsCache = []

        // add each unique name to cache
        all.each {
            // families
            if (it.family == "") log.debug it.scientific + " has blank family"
            if (it.family == null) log.debug it.scientific + " has null family"
            if (!familiesCache.contains(it.family) && it.family != "" && it.family != null) {
                familiesCache << it.family
            }
            // groups
            if (it.group_name != null && it.group_name != "" && !groupsCache.contains(it.group_name)) {
                groupsCache << it.group_name
            }
        }
        [families: familiesCache.sort(), groups: groupsCache.sort()]
    }

    def getAllFamilies = {
        loadFamiliesAndGroups().families
    }

    def getAllGroups = {
        loadFamiliesAndGroups().groups
    }

    def getLocalities() {
        Holders.config.localities
    }

    def getMarineRegions() {
        Holders.config.imcras
    }

    //Modified by Alan on for fetching multiple layers on 30/07/2014 --- START
    def getMyLayerRegions() {
        return myLayer
    }

    // some imcras removed as out of scope for distribution maps
    def getMyLayer() {
        Holders.config.marine
    }

    def getCapad2014Regions() {
        Holders.config.capad
    }
}
