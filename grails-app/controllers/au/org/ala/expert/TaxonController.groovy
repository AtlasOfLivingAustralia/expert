package au.org.ala.expert

import grails.converters.JSON
import grails.util.Holders

class TaxonController {

    def resultsService, webService, bieService, searchService

    static defaultAction = "view"

    static timer = 0

    /**
     * Do logouts through this app so we can invalidate the session.
     *
     * @param casUrl the url for logging out of cas
     * @param appUrl the url to redirect back to after the logout
     */
    def logout = {
        session.invalidate()
        redirect(url: "${params.casUrl}?url=${params.appUrl}")
    }

    /**
     * Displays a page of the results specified by the results key.
     *
     * @params key the identifier of the results set to display
     * @param start the pagination index of the first item to display (optional defaults to 0)
     * @param pageSize the number of items to display per page (optional defaults to 10)
     * @param sortBy the property to sort on (optional)
     * @param sortOrder normal or reverse (optional defaults to normal)
     * @param debugModel
     *
     * @return model for a page of results
     */
    def view(String key) {

        // retrieve the required page from the results cache
        def data = resultsService.getResultsPage(key, "", "", true, [:])

        // check for errors
        if (data.error) {
            render view: 'error', model: [message   : data.error,
                                          searchPage: Holders.config.distribution.search.baseUrl]
        } else {

            def results = data.taxonHierarchy

            // sort by
            def sortBy = params.sortBy ?: 'name'
            results.sort { it[sortBy] }

            // sort order
            if (params.sortOrder == 'reverse') {
                results = results.reverse()
            }

            // pagination
            def total = results.size()
            def start = params.start ? params.start as int : 0
            def pageSize = params.pageSize ? params.pageSize as int : 10
            if (results) {
                results = results[start..(Math.min(start + pageSize, total) - 1)]
            }

            // inject remaining metadata only for the families to be displayed
            results = bieService.injectGenusMetadata(results)

            def model = [list            : results, total: total, rank: 'family', parentTaxa: "", key: key,
                         queryDescription: data.queryDescription, start: start, pageSize: pageSize,
                         sortBy          : sortBy, sortOrder: params.sortOrder, query: data.query,
                         searchPage      : Holders.config.distribution.search.baseUrl]

            if (params.debugModel == 'true') {
                render model as JSON
            } else {
                render(view: 'list', model: model)
            }
        }
    }

    /**
     * Displays a paginated list of species for the results specified by the results key.
     *
     * @params key the identifier of the results set to display
     * @params genus the name of a single genus to display (acts as a filter on the specified results)
     * @param taxa a list of family names (as this stage), comma separated
     * @param start the pagination index of the first taxon to display
     * @param pageSize the number of taxa to display per page
     * @param sortBy the column to sort on
     * @param sortOrder normal or reverse
     * @param debugModel
     *
     */
    def species(String key, String genus) {

        // retrieve the required page from the results cache
        def data = resultsService.getResultsPage(key, "", "", false, [:])
        if (!data || data.error) {
            render "No data " + data?.error
        } else {
            def results = data.list

            // apply optional filter by genus
            if (genus) {
                results = results.findAll { it.genus == genus }
            }

            // sort by
            def sortBy = params.sortBy ?: 'taxa'
            /*if (sortBy == 'taxa') {
                results.sort taxaSort
            } else {
                results.sort {it[sortBy]}
            }*/
            results.sort(sortBy == 'taxa' ? taxaSort : { it[sortBy] })

            // sort order
            if (params.sortOrder == 'reverse') {
                results = results.reverse()
            }

            // filter by taxa
            if (params.taxa) {
                results = filterList(results, params.taxa.tokenize(','))
            }

            // pagination
            def total = results.size()
            def start = params.start ? params.start as int : 0
            def pageSize = params.pageSize ? params.pageSize as int : 10
            if (results) {
                results = results[start..(Math.min(start + pageSize, total) - 1)]
            }

            // inject remaining metadata only for species to be displayed
            results = bieService.injectSpeciesMetadata(results)

            def model = [list            : results, total: total, taxa: params.taxa, start: start, key: key,
                         queryDescription: data.queryDescription, pageSize: pageSize, sortBy: sortBy,
                         sortOrder       : params.sortOrder, query: data.list.query, rank: 'species',
                         searchPage      : Holders.config.distribution.search.baseUrl]

            if (genus) {
                model.put 'genus', genus
            }

            if (params.debugModel == 'true') {
                render model as JSON
            } else {
                render(view: 'species', model: model)
            }
        }
    }

    /**
     * Displays a paginated list of species for the results specified by the results key.
     *
     * @params key the identifier of the results set to display
     * @param taxa a list of species names, comma separated
     * @param sortBy the column to sort on
     * @param sortOrder normal or reverse
     * @param debugModel
     */
    def data(String key) {

        // retrieve the required page from the results cache
        def data = resultsService.getResultsPage(key, "", "", true, [:])
        if (!data || data.error) {
            render "No data " + data.error
        } else {
            def results = data.list

            // sort by
            def sortBy = params.sortBy ?: 'taxa'
            results.sort(sortBy == 'taxa' ? taxaSort : { it[sortBy] })

            // sort order
            if (params.sortOrder == 'reverse') {
                results = results.reverse()
            }

            // filter by taxa
            if (params.taxa) {
                def filters = params.taxa.tokenize(',')
                results = results.findAll { it.spcode.toString() in filters }
            }

            def model = [list            : results, total: results.size(), taxa: params.taxa, key: key,
                         queryDescription: data.queryDescription, sortBy: sortBy,
                         sortOrder       : params.sortOrder, query: data.list.query,
                         searchPage      : Holders.config.distribution.search.baseUrl]

            if (params.debugModel == 'true') {
                render model as JSON
            } else {
                render(view: 'data', model: model)
            }
        }
    }

    def taxaSort = { a, b ->
        (a.family <=> b.family) ?: (a.genus <=> b.genus) ?: (a.name <=> b.name)
    }

    def filterList(list, filters) {
        return list.findAll { it.family in filters }
    }

    /**
     * Displays a paginated list of taxa at the specified rank for the specified query.
     *
     * @param targetRank the rank to display - defaults to family
     * @param start the pagination index of the first taxon to display
     * @param pageSize the number of taxa to display per page
     * @param sortBy the column to sort on
     * @param sortOrder normal or reverse
     *
     */
    def list(int search) {
        timer = new Date().getTime()
        def targetRank = params.targetRank ?: 'family'
        def start = params.start ? params.start as int : 0
        def pageSize = params.pageSize ? params.pageSize as int : 10
        def taxon = ""
        def query = ''

        int taxonListId = params.id ? params.id as int : 1
        if (params.rank && params.name) {
            taxon = [[rank: params.rank, name: params.name]]
            query = "rank=${params.rank}&name=${params.name}"
        } else {
            taxon = lookupTaxonList(taxonListId)
            query = "rank=${taxon.rank}&name=${taxon.name}"
        }
        // build list of target ranks with this list of taxa
        def results = listMembers(taxon, targetRank)

        render(view: 'list', model:
                [list  : results.taxa, total: results.total, rank: targetRank, parentTaxa: taxon,
                 region: lookupRegion(search), start: start, pageSize: pageSize, query: query])
    }

    def imageMetadataLookup(String url) {
        render webService.get(url)
    }

    // dummy service to return a list of taxa from an id
    def lookupTaxonList(int id) {
        switch (id) {
            case 1: return [[rank: 'class', name: 'Insecta']]
            case 2: return [[rank: 'class', name: 'Chondrichthyes']]
            case 3: return [[rank: 'order', name: 'Lamniformes']]
            case 4: return [[rank: 'genus', name: 'Notomys']]
            default: return []
        }
    }

    def lookupRegion(search) {
        if (!search) {
            return ""
        }
        switch (search) {
            case -12..1: return 'Central Eastern Province'
            case 2: return 'Carnarvon'
            case 3: return 'Tasmanian Shelf Province'
            default: return ""
        }
    }

    def listMembers(list, targetRank) {

        // currently need to make a request for each taxon in list
        def results = [taxa: []]  // map holding the list to build
        list.each { taxon ->

            // TODO: only getting first 10 for now
            def url = Holders.config.bie.baseURL + "/search.json?q=*&pageSize=10" +
                    "&fq=rank:${targetRank}" +
                    "&fq=${taxon.rank == 'order' ? 'bioOrder' : taxon.rank}:${taxon.name}"
            def conn = new URL(url).openConnection()
            try {
                conn.setConnectTimeout(10000)
                conn.setReadTimeout(50000)
                def json = conn.content.text

                def resp = JSON.parse(json).searchResults.results
                results.total = resp.size()

                resp.each {
                    results.taxa << [name : it.name, guid: it.guid, common: it.commonNameSingle,
                                     image: urlForBestImage(it.guid, targetRank, it.name)]
                }
            } catch (SocketTimeoutException e) {
                log.warn "Timed out looking up taxon breakdown. URL= ${url}."
            } catch (Exception e) {
                log.warn "Failed to lookup taxon breakdown. ${e.getClass()} ${e.getMessage()} URL= ${url}."
            }
        }
        return results
    }

    /**
     * Find the best image for the specified taxon. Search child taxa if necessary.
     *
     * @param guid of the taxon
     * @param rank of the taxon
     * @param name of the taxon
     * @return image properties or null if none found
     */
    def urlForBestImage(guid, rank, name) {

        if (!guid) {
            return ""
        }
        // get more info for taxon via guid
        def url = Holders.config.bie.baseURL + "/species/moreInfo/${guid}.json"

        def conn = new URL(url).openConnection()
        try {
            conn.setConnectTimeout(10000)
            conn.setReadTimeout(50000)

            def json = conn.content.text
            def imageList = JSON.parse(json).images

            // look for preferred image first
            def image = imageList.find { it.preferred && !it.isBlackListed }
            // else take the first image
            if (!image && imageList) {
                image = imageList[0]
            }

            // if none found look at child species
            if (!image && rank != 'species') {

                def sppUrl = Holders.config.bie.baseURL + "/search.json?q=*&pageSize=1" +
                        "&fq=rank:species&fq=hasImage:true" +
                        "&fq=${rank == 'order' ? 'bioOrder' : rank}:${name}"

                def sppConn = new URL(sppUrl).openConnection()
                try {
                    conn.setConnectTimeout(10000)
                    def result = JSON.parse(sppConn.content.text).searchResults?.results

                    if (result) {
                        // just take the first one
                        image = [repoLocation: Holders.config.bie.baseURL + "/repo" + result[0].image[9..-1],
                                 thumbnail   : Holders.config.bie.baseURL + "/repo" + result[0].thumbnail[9..-1],
                                 rights      : null]
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e)
                }
            }

            if (image) {
                return [repoLocation: image.repoLocation, thumbnail: image.thumbnail, rights: image.rights]
            } else {
                return null
            }
        } catch (SocketTimeoutException e) {
            log.error("Timed out looking up taxon image. URL= ${url}.", e)
        } catch (Exception e) {
            log.error("Failed to lookup taxon image. ${e.getClass()} ${e.getMessage()} URL= ${url}.", e)
        }
    }

    def cacheManager

    def clearCache() {
        cacheManager.evictAll()
    }
}
