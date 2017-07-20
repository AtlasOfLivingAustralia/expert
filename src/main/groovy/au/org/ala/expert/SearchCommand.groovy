package au.org.ala.expert

import grails.util.Holders
import grails.validation.Validateable

class SearchCommand implements Validateable {

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

    String getLocationBasedOn() {
        if (wkt) {
            return "wkt"
        } else if (locality) {
            return "locality"
        } else if (circleLat && circleLon && circleRadius) {
            return "circle"
        } else if (imcra && imcra != 'any') {
            return "marine area"
        } else if (state) {
            return "state"
        } else {
            return "all"
        }
    }

    String getDepthBasedOn() {
        if (bathome == Holders.config.depthMeasuresAll) {
            return 'all'
        } else if (bathome && !(bathome == 'custom depth range')) {
            return "bathome"
        } else if (minDepth && maxDepth) {
            return "min and max"
        } else if (minDepth) {
            return "min"
        } else if (maxDepth) {
            return "max"
        } else {
            return "all"
        }
    }

    String getGroupBasedOn() {
        if (fishGroup) {
            return "fishGroup"
        }
        return "all"
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
                def match = Holders.config.depthMeasures.find() { depth ->
                    depth.label == bathome
                }
                if (match) {
                    return [minD: match.min, maxD: match.max]
                } else {
                    return [minD: 0, maxD: 0]
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
        } else if (locationBasedOn == 'locality') {
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
                        "@ ${(circleRadius / 1000).round(2)}km)"; break
            case 'marine area': loc = imcra; break
            case 'locality': loc = parseLocality().name + " (${radius / 1000}km)"; break
        //case 'state': loc = state; break
            case 'wkt': loc = "user defined area"; break
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
        return [loc, dep, grp, eco, fam].findAll({ it }).join(', ')
    }
}