package au.org.ala.expert

class SearchService {

    def grailsApplication

    def buildQuery(SearchCommand cmd) {
        def criteria = ['dataResourceUid=' + grailsApplication.config.distribution.maps.dataResourceUid]
        switch (cmd.locationBasedOn) {
            case 'wkt': criteria << "wkt=" + cmd.wkt; break
            case ['circle','locality']:
                def circ = cmd.circle
                criteria << "lat=" + circ.lat
                criteria << "lon=" + circ.lng
                criteria << "radius=" + circ.radius
                break
            case 'marine area':
                criteria << "fid=" + cmd.myLayer
                criteria << "objectName=" + cmd.getMarineArea().imcra
        }

        if (cmd.depthBasedOn != 'all') {
            def dep = cmd.depthRange
            if (dep.minD) {
                criteria << "min_depth=" + dep.minD
            }
            if (dep.maxD) {
                criteria << "max_depth=" + dep.maxD
            }
        }

        if (cmd.groupBasedOn == 'fishGroup') {
            criteria << "groupName=" + formatGroupName(cmd.getGroup())
        }

        if (cmd.ecosystem) {
            criteria.addAll getEcosystemQuery(cmd.ecosystem)
        }

        if (cmd.families) {
            cmd.families.each {
                criteria << "family=" + it
            }
        }

        return criteria.join('&')
    }

    def formatGroupName(name) {
        // replace '&' with encoding
        return name.encodeAsURL()
    }

    def search(SearchCommand cmd) {
        println "location based on ${cmd.locationBasedOn}"
        println "Radius = ${cmd.radius}"
        cmd.families.each { log.debug it }
        //println "Families = ${cmd.families}"
        def results = []
        def query = buildQuery(cmd)
        log.debug "Query = " + query
        def servicePath = '/ws/distributions'
        if (cmd.locationBasedOn == 'circle' || cmd.locationBasedOn == 'locality') {
            servicePath += '/radius'
        }
        try {
            withHttp(uri: grailsApplication.config.spatial.baseURL) {
                def json = post(path: servicePath, body: query)
                //println json
                json.each {
                    results <<
                            [name: it.scientific,
                             common: it.common_nam,
                             caabCode: it.caab_species_number,
                             guid: it.lsid,
                             spcode: it.spcode,
                             family: it.family,
                             familyGuid: it.family_lsid,
                             familyCaabCode: it.caab_family_number,
                             genus: it.genus_name,
                             genusGuid: it.genus_lsid,
                             specific: it.specific_n,
                             group: it.group_name,
                             gidx: it.geom_idx,
                             authority: it.authority_,
                             imageQuality: it.image_quality,
                             wmsurl: it.wmsurl,
                             minDepth: it.min_depth,
                             maxDepth: it.max_depth,
                             primaryEcosystem: (it.pelagic_fl > 0 ? "p" : "") +
                                     (it.coastal_fl ? "c" : "") +
                                     (it.estuarine_fl ? "e" : "") +
                                     (it.desmersal_fl ? "d" : "")
                            ]
                }
            }
        } catch (Exception e) {
            return [error: "Spatial search: " + e.message, results: [], query: query]
        }

        return [results: results, query: query/*, error: "spatial webservice not available"*/]
    }

    def speciesListSummary(List species) {
        def summary = [total: species.size()]
        def clone = species.clone()
        def families = clone.unique { it.family }
        summary.familyCount = families.size()
        summary['families'] = families.collect { it.family }
        return summary
    }
    
    List getEcosystemQuery(system) {
        switch (system) {
            case 'estuarine': return ['estuarine=true']
            // NOTE mis-spelling of demersal to match DB column name!
            case 'demersal': return ['desmersal=true']
            case 'pelagic': return ['pelagic=true']
            case 'coastal': return ['coastal=true']
            default: return [""]
        }
    }
}
