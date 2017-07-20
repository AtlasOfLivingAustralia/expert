package au.org.ala.expert

import grails.converters.JSON
import grails.plugin.cache.Cacheable


class SearchService {

    def grailsApplication
    def webService
    def bieService

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

        if (cmd.endemic) {
            criteria << "endemic=true"
        }

        return criteria.join('&')
    }

    def formatGroupName(name) {
        // replace '&' with encoding
        return name.encodeAsURL()
    }

    def search(SearchCommand cmd) {

        def results = []
        def query = buildQuery(cmd)

        log.debug "Query = " + query

        def servicePath = '/ws/distributions'
        if (cmd.locationBasedOn == 'circle' || cmd.locationBasedOn == 'locality') {
            servicePath += '/radius'
        }

        try {
            def json = JSON.parse(webService.doJsonPost(grailsApplication.config.spatial.baseURL, servicePath, null, query, 'application/x-www-form-urlencoded'))

            json.each {
                results <<
                        [name            : it.scientific,
                         common          : it.common_nam,
                         caabCode        : it.caab_species_number,
                         guid            : it.lsid,
                         spcode          : it.spcode,
                         family          : it.family,
                         familyGuid      : it.family_lsid,
                         familyCaabCode  : it.caab_family_number,
                         genus           : it.genus_name,
                         genusGuid       : it.genus_lsid,
                         specific        : it.specific_n,
                         group           : it.group_name,
                         gidx            : it.geom_idx,
                         authority       : it.authority_,
                         imageQuality    : it.image_quality,
                         wmsurl          : it.wmsurl,
                         minDepth        : it.min_depth,
                         maxDepth        : it.max_depth,
                         endemic         : it.endemic,
                         primaryEcosystem: (it.pelagic_fl > 0 ? "p" : "") +
                                 (it.coastal_fl ? "c" : "") +
                                 (it.estuarine_fl ? "e" : "") +
                                 (it.desmersal_fl ? "d" : "")
                        ]
            }
        } catch (Exception e) {
            return [error: "Spatial search: " + e.message, results: [], query: query]
        }

        log.info "results = ${results}"

        return [results: results, query: query]
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

    @Cacheable(value = "searchService", key = { key ?: cmd ? buildQuery(cmd).encodeAsMD5() : '' })
    def ajaxSearch(SearchCommand cmd, String key) {
        //test for cache hit failure
        if (cmd == null) {
            log.error('search cache hit failure for key:' + key)
            return null
        }

        def result

        // do the search
        def searchResults = search(cmd)
        if (searchResults.error) {
            log.debug searchResults.error
        }

        // add the family metadata (image, common name, etc)
        searchResults.families = bieService.getFamilyMetadata(searchResults.results)

        // create a summary
        def summary = [total      : searchResults.results.size(),
                       familyCount: searchResults.families.size()]

        // inject some summary counts into the results
        searchResults.total = summary.total
        searchResults.familyCount = summary.familyCount

        // register the results with the results service
        def storeError = ""
        if (!searchResults.error) {
            key = searchResults.query.encodeAsMD5()

            log.debug "stored key = ${key}"
        }

        // bundle the results
        result = [summary: summary, query: searchResults.query, queryDescription: cmd.queryDescription,
                  key    : key, families: searchResults.families, results: searchResults]
        if (storeError) {
            result.error = storeError
        }
        if (searchResults.error) {
            result.error = searchResults.error
        }

        result
    }
}
