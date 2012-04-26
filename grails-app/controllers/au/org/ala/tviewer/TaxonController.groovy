package au.org.ala.tviewer

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import grails.converters.JSON
import org.apache.ivy.core.module.descriptor.License
import javax.xml.stream.events.StartDocument

class TaxonController {

    static defaultAction = "index"
    
    static timer = 0
    static bieNameGuidCache = [:]  // temp cache while services are made more efficient

    def index = { }

    /**
     * Do logouts through this app so we can invalidate the session.
     *
     * @param casUrl the url for logging out of cas
     * @param appUrl the url to redirect back to after the logout
     */
    def logout = {
        session.invalidate()
        redirect(url:"${params.casUrl}?url=${params.appUrl}")
    }

    /**
     * Displays a paginated list of taxa at the specified rank for the specified query.
     *
     * @param targetRank the rank to display - defaults to family
     * @param start the pagination index of the first taxon to display
     * @param pageSize the number of taxa to display per page
     * @param sortBy the column to sort on
     * @param sortOrder normal or reverse
     * @param search overrides the selection of taxa - for devt purposes
     * 
     */
    def list(int search) {
        timer = new Date().getTime()
        def targetRank = params.targetRank ?: 'family'
        def start = params.start ? params.start as int : 0
        def pageSize = params.pageSize ? params.pageSize as int : 10
        def query = ""
        def taxon = ""
        def results = [:]
        //println search
        //int search = params.search ?: -1
        if (search) {
            results = searchBiocache(search, params)
            query = "search=${search}" +
                    ((params.sortBy == 'common' || params.sortBy == 'CAABCode') ? "&sortBy=${params.sortBy}" : "") +
                    (params.sortOrder == 'reverse' ? "&sortOrder=${params.sortOrder}" : "")
        }
        else {
            int taxonListId = params.id ? params.id as int : 1
            if (params.rank && params.name) {
                taxon = [[rank: params.rank, name: params.name]]
                query = "rank=${params.rank}&name=${params.name}"
            }
            else {
                taxon = lookupTaxonList(taxonListId)
                query = "rank=${taxon.rank}&name=${taxon.name}"
            }
            // build list of target ranks with this list of taxa
            results = listMembers(taxon, targetRank)
        }
/*
        // sort
        def order = params.order ?: 'name'
        results.taxa.sort {it[order]}
*/
        render (view: 'list', model:
                [list: results.taxa, total: results.total, rank: targetRank, parentTaxa: taxon,
                region: lookupRegion(search), start: start, pageSize: pageSize, query: query])
    }

    def bind(int search) {
        int s = search //params.search ?: 0
        if (params.asModel == 'true') {
            render(view: 'bind', model: [sch: s])
        }
        else {
            render "search is a ${s.getClass()} and has value ${s}"
        }
    }

    def bind2 = {
        int s = params.search ? params.search as int : 1//params.search ?: 0
        if (params.asModel == 'true') {
            render(view: 'bind', model: [sch: s])
        }
        else {
            render "search is a ${s.getClass()} and has value ${s}"
        }
    }

    // dummy service to return a list of taxa from an id
    def lookupTaxonList(int id) {
        switch (id) {
            case 1: return [[rank:'class', name:'Insecta']]
            case 2: return [[rank: 'class', name:  'Chondrichthyes']]
            case 3: return [[rank: 'order', name:  'Lamniformes']]
            case 4: return [[rank: 'genus', name: 'Notomys']]
            default: return []
        }
    }

    def lookupRegion(search) {
        if (!search) { return "" }
        switch (search) {
            case -12..1: return 'Central Eastern Province'
            case 2: return 'Carnarvon'
            case 3: return 'Tasmanian Shelf Province'
            default: return ""
        }
    }

    /**
     * Displays a paginated list of species for the specified list of taxa.
     *
     * @param taxa a list of family names (as this stage), comma separated
     * @param start the pagination index of the first taxon to display
     * @param pageSize the number of taxa to display per page
     * @param sortBy the column to sort on
     * @param sortOrder normal or reverse
     *
     */
    def species = {
        def list = params.taxa ? params.taxa.tokenize(',') : []
        def start = params.start ? params.start as int : 0
        def pageSize = params.pageSize ? params.pageSize as int : 10
        def query = ""

        [list: speciesData.Lamnidae, total: speciesData.Lamnidae.size(), taxa: params.taxa, start: start,
                pageSize: pageSize, query: query]
    }
    
    def listTargetRankFromSearch(search, targetRank, start, pageSize) {
        def query = ""
        switch (search) {
            case 1: query = 'q=imcra:"Central%20Eastern%20Province"&fq=species_group:Fish'; break;
            case 2: query = 'q=ibra:"Carnarvon"&fq=species_group:Fish'; break;
            case 3: query = 'q=ibra:"Tasmanian%20Shelf%20Province"&fq=species_group:Fish'; break;
            default: query = 'q=imcra:"Central%20Eastern%20Province"&fq=species_group:Fish'; break;
        }
        query += "&facets=${targetRank == 'order' ? 'bioOrder' : targetRank}&pageSize=0"
        return getRanksForQuery(query, targetRank, start, pageSize)
    }

    def getRanksForQuery(query, targetRank, start, pageSize) {
        def results = [taxa: [], query: query]
        def url = ConfigurationHolder.config.biocache.baseURL + "/ws/occurrences/search.json?" + query
        //println url
        def conn = new URL(url).openConnection()
        try {
            conn.setConnectTimeout(10000)
            conn.setReadTimeout(50000)
            def json = conn.content.text
            //println json
            def facets = JSON.parse(json).facetResults
            def facet = facets.find {it.fieldName == (targetRank == 'order' ? 'bioOrder' : targetRank)}
            def facetValues = facet.fieldResult
            results.total = facetValues.size()
            // TODO: paginate - first 10 for now
            facetValues.sort {it.label}
            // paginate
            def first = Math.min(start, facetValues.size() - 1)
            def last = Math.min(first + pageSize, facetValues.size())
            def ranks = facetValues[first..last - 1]
            //println "${new Date().getTime() - timer}ms - parsed target taxa"
            // dummy family
            //results.taxa << bieLookup('Lamnidae', family)
            ranks.each {
                results.taxa << bieLookup(it.label, targetRank)
            }
        } catch (SocketTimeoutException e) {
            println "Timed out searching. URL= \${url}."
        } catch (Exception e) {
            println "Failed search. ${e.getClass()} ${e.getMessage()} URL= ${url}."
        }
        return results
    }

    def bieLookup(name, rank) {
        // check cache first
        if (bieNameGuidCache[name]) {
            return bieNameGuidCache[name]
        }
        def url = ConfigurationHolder.config.bie.baseURL + "/species/" + name + ".json"
        def conn = new URL(url).openConnection()
        try {
            conn.setConnectTimeout(10000)
            conn.setReadTimeout(50000)
            def json = conn.content.text
            def resp = JSON.parse(json)
            //println resp
            def image = extractBestImage(resp.images, rank, name)
            //println "${new Date().getTime() - timer}ms - looked up ${name}"
            def details = [name: name, guid: resp.taxonConcept.guid,
                    common: extractBestCommonName(resp.commonNames),
                    image: image]
            bieNameGuidCache[name] = details
            return details
        } catch (SocketTimeoutException e) {
            println "Timed out searching. URL= \${url}."
        } catch (Exception e) {
            println "Failed to search for child species. ${e.getClass()} ${e.getMessage()} URL= ${url}."
        }
        return []
    }

    def extractBestCommonName(names) {
        if (!names || names.size() == 0) { return "" }
        def preferred = names.findAll {it.preferred}
        // return first preferred name
        if (preferred) { return preferred[0].nameString}
        // else return first name
        return names[0].nameString
    }

    def extractBestImage(images, rank, name) {
        if (!images || images.size() == 0) {
            // if none found look at child species
            if (rank != 'species') {
                //println "${new Date().getTime() - timer}ms - searching children"
                def sppUrl = ConfigurationHolder.config.bie.baseURL + "/search.json?q=*&pageSize=1" +
                        "&fq=rank:species&fq=hasImage:true" +
                        "&fq=${rank == 'order' ? 'bioOrder' : rank}:${name}"
                //println sppUrl
                def sppConn = new URL(sppUrl).openConnection()
                try {
                    sppConn.setConnectTimeout(10000)
                    def result = JSON.parse(sppConn.content.text).searchResults?.results
                    //println "${new Date().getTime() - timer}ms - received and parsed children"
                    //println "${result.size()} spp returned for ${name}"
                    if (result) {
                        // just take the first one
                        return [repo: ConfigurationHolder.config.bie.baseURL + "/repo" + result[0].image[9..-1],
                                thumbnail: ConfigurationHolder.config.bie.baseURL + "/repo" + result[0].thumbnail[9..-1],
                                rights: null]
                    }
                } catch (Exception e) {
                    println e
                }
            }
        }
        else {
            def preferred = images.findAll {it.preferred}
            // return first preferred name
            if (preferred) {
                return [repo: preferred[0].repoLocation, thumbnail: preferred[0].thumbnail, rights: preferred[0].rights]
            }
            // else return first name
            return [repo: images[0].repoLocation, thumbnail: images[0].thumbnail, rights: images[0].rights]
        }
        return [:]
    }

    def listMembers(list, targetRank) {
        //println "${new Date().getTime() - timer}ms"
        // currently need to make a request for each taxon in list
        def results = [taxa: []]  // map holding the list to build
        list.each { taxon ->
            //println "${new Date().getTime() - timer}ms - request target taxa"
            // TODO: only getting first 10 for now
            def url = ConfigurationHolder.config.bie.baseURL + "/search.json?q=*&pageSize=10" +
                    "&fq=rank:${targetRank}" +
                    "&fq=${taxon.rank == 'order' ? 'bioOrder' : taxon.rank}:${taxon.name}"
            def conn = new URL(url).openConnection()
            try {
                conn.setConnectTimeout(10000)
                conn.setReadTimeout(50000)
                def json = conn.content.text
                //println "${new Date().getTime() - timer}ms - received target taxa"
                def resp = JSON.parse(json).searchResults.results
                results.total = resp.size()
                //println "${new Date().getTime() - timer}ms - parsed target taxa"
                resp.each {
                    results.taxa << [name: it.name, guid: it.guid, common: it.commonNameSingle,
                            image: urlForBestImage(it.guid, targetRank, it.name)]
                }
            } catch (SocketTimeoutException e) {
                log.warn "Timed out looking up taxon breakdown. URL= ${url}."
                println "Timed out looking up taxon breakdown."
            } catch (Exception e) {
                log.warn "Failed to lookup taxon breakdown. ${e.getClass()} ${e.getMessage()} URL= ${url}."
                println "Failed to lookup taxon breakdown. ${e.getClass()} ${e.getMessage()} URL= ${url}."
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
        //println "${new Date().getTime() - timer}ms - find best image for ${rank}:${name}"
        if (!guid) { return "" }
        // get more info for taxon via guid
        def url = ConfigurationHolder.config.bie.baseURL + "/species/moreInfo/${guid}.json"
        //println url
        def conn = new URL(url).openConnection()
        try {
            conn.setConnectTimeout(10000)
            conn.setReadTimeout(50000)
            //println "${new Date().getTime() - timer}ms - more info received"
            def json = conn.content.text
            def imageList = JSON.parse(json).images
            // debug
/*
            println guid
            imageList.each {
                println "${it.preferred} - ${it.ranking} ${it.thumbnail ?: ''} ${it.repoLocation ? ' has raw' : ''}"
            }
*/
            // look for preferred image first
            def image = imageList.find { it.preferred && !it.isBlackListed }
            // else take the first image
            if (!image && imageList) {
                image = imageList[0]
            }

            // if none found look at child species
            if (!image && rank != 'species') {
                //println "${new Date().getTime() - timer}ms - searching children"
                def sppUrl = ConfigurationHolder.config.bie.baseURL + "/search.json?q=*&pageSize=1" +
                        "&fq=rank:species&fq=hasImage:true" +
                        "&fq=${rank == 'order' ? 'bioOrder' : rank}:${name}"
                //println sppUrl
                def sppConn = new URL(sppUrl).openConnection()
                try {
                    conn.setConnectTimeout(10000)
                    def result = JSON.parse(sppConn.content.text).searchResults?.results
                    //println "${new Date().getTime() - timer}ms - received and parsed children"
                    //println "${result.size()} spp returned for ${name}"
                    if (result) {
                        // just take the first one
                        image = [repoLocation: ConfigurationHolder.config.bie.baseURL + "/repo" + result[0].image[9..-1],
                                thumbnail: ConfigurationHolder.config.bie.baseURL + "/repo" + result[0].thumbnail[9..-1],
                                rights: null]
                        //println image
                    }
                } catch (Exception e) {
                    println e
                }
            }

            //println "${new Date().getTime() - timer}ms - done searching children"
            if (image) {
                return [repo: image.repoLocation, thumbnail: image.thumbnail, rights: image.rights]
            }
            else {
                return null
            }
        } catch (SocketTimeoutException e) {
            log.warn "Timed out looking up taxon image. URL= ${url}."
            println "Timed out looking up taxon image."
        } catch (Exception e) {
            log.warn "Failed to lookup taxon image. ${e.getClass()} ${e.getMessage()} URL= ${url}."
            println "Failed to lookup taxon image. ${e.getClass()} ${e.getMessage()} URL= ${url}."
        }
    }

    /*** the following dummy services replicate what good bie/biocache services should look like ***/
    // used for development only
    
    /**
     * Occurrence search
     *
     * @param search the search phrase - a q and optional fq's
     * @param targetRank the rank we want to display - defaults to family
     * @param start the pagination index of the first taxon to display
     * @param pageSize the number of taxa to display per page
     * @param sortBy the column to sort on
     * @param sortOrder normal or reverse
     * @return json containing all required metadata
     */
    def searchBiocache(search, params) {
        def start = params.start ? params.start as int : 0
        def pageSize = params.pageSize ? params.pageSize as int : 10
        def sortBy = params.sortBy ?: 'name'
        def sortOrder = params.sortOrder ?: 'normal'
        def results = [taxa: [], total: 0]

        def add = {taxon ->
            results.taxa << [
                    name: taxon.taxonConcept.nameString,
                    guid: taxon.taxonConcept.guid,
                    CAABCode: taxon.CAABCode,
                    common: taxon.preferredCommonName,
                    image: [title: taxon.preferredImage.title,
                            repo: taxon.preferredImage.repoLocation,
                            thumbnail: taxon.preferredImage.thumbnail,
                            rights: taxon.preferredImage.rights,
                            creator: taxon.preferredImage.creator,
                            license: taxon.preferredImage.license],
                    genera: taxon.genera.collect {[
                            name: it.taxonConcept.nameString,
                            guid:  it.taxonConcept.guid,
                            common: it.preferredCommonName,
                            image: [title: it.preferredImage.title,
                                    repo: it.preferredImage?.repoLocation,
                                    rights: it.preferredImage?.rights,
                                    creator: it.preferredImage?.creator,
                                    license: it.preferredImage?.license]]}]
            results.total++
        }

        search1.taxa.each(add)

        if (search > 1) {
            search2.taxa.each(add)
        }

        if (search > 2) {
            search3.taxa.each(add)
        }

        // sort
        results.taxa.sort {it[sortBy]}

        // order
        if (sortOrder == 'reverse') {
            results.taxa = results.taxa.reverse()
        }

        // paginate
        //println "total = ${results.total}, pageSize = ${params.pageSize} start=${start} end=${(Math.min(start + pageSize, results.total) - 1)}"
        results.taxa = results.taxa[start..(Math.min(start + pageSize, results.total) - 1)]
        //results.taxa.each { println it }
        return results
    }

    // temp
    def showData = {
        render search1 as JSON
    }
    def showSpeciesData = {
        render speciesData as JSON
    }

    // a same search result
    def search1 = [total: 4, taxa: [
            [taxonConcept: [nameString: 'Lamnidae', rankString: 'family',
                    guid: 'urn:lsid:biodiversity.org.au:afd.taxon:af7628a0-288a-457a-8e3e-ee2ea99ef01b'],
                    classification: [order: 'Lamniformes'],
                    CAABCode: '37 010',
                    preferredCommonName: 'Mackerel Sharks',
                    preferredImage: [ContentType: 'image/jpeg',
                            title: 'Isurus oxyrinchus',
                            thumbnail: 'http://bie.ala.org.au/repo/1111/174/1740514/thumbnail.jpg',
                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740514/raw.jpg',
                            license: 'Creative Common Attribution 3.0 Australia',
                            rights: 'Australian National Fish Collection, CSIRO',
                            creator: 'Australian National Fish Collection, CSIRO'],
             genera: [
                     [taxonConcept: [nameString: 'Carcharodon', rankString: 'genus',
                             guid: 'urn:lsid:biodiversity.org.au:afd.taxon:8ac2e870-beb9-4660-a982-fe1eab81ad54'],
                             preferredCommonName: '',
                             preferredImage: [ContentType: 'image/jpeg',
                                     title: 'Carcharodon carcharias',
                                     thumbnail: 'http://bie.ala.org.au/repo/1111/174/1740554/thumbnail.jpg',
                                     repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740554/raw.jpg',
                                     license: 'Creative Common Attribution 3.0 Australia',
                                     rights: 'Australian National Fish Collection, CSIRO, Image enhancement funded by CSIRO/FRDC',
                                     creator: 'Australian National Fish Collection, CSIRO']],
                     [taxonConcept: [nameString: 'Isurus', rankString: 'genus',
                             guid: 'urn:lsid:biodiversity.org.au:afd.taxon:2c199852-03da-4e37-8bad-d40980d9c16d'],
                             preferredCommonName: '',
                             preferredImage: [ContentType: 'image/jpeg',
                                     title: 'Isurus oxyrinchus',
                                     thumbnail: '',
                                     repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740514/raw.jpg',
                                     license: 'Creative Common Attribution 3.0 Australia',
                                     rights: 'Australian National Fish Collection, CSIRO',
                                     creator: 'Australian National Fish Collection, CSIRO']],
                     [taxonConcept: [nameString: 'Lamna', rankString: 'genus',
                             guid: 'urn:lsid:biodiversity.org.au:afd.taxon:25978990-ab5f-485f-b31e-000369d73a3b'],
                             preferredCommonName: '',
                             preferredImage: [ContentType: 'image/jpeg',
                                     title: 'Lamna nasus',
                                     thumbnail: '',
                                     repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740716/raw.jpg',
                                     license: 'Creative Common Attribution 3.0 Australia',
                                     rights: 'Australian National Fish Collection, CSIRO, Image enhancement funded by CSIRO/FRDC',
                                     creator: 'Australian National Fish Collection, CSIRO']]
             ]],
            [taxonConcept: [nameString: 'Rajidae', rankString: 'family',
                    guid: 'urn:lsid:biodiversity.org.au:afd.taxon:71003289-737d-429f-8301-79ac008a1ebc'],
                    classification: [order: 'Rajiformes'],
                    CAABCode: '37 031',
                    preferredCommonName: 'Hardnose Skates',
                    preferredImage: [ContentType: 'image/jpeg',
                            title: 'Amblyraja hyperborea',
                            thumbnail: 'http://bie.ala.org.au/repo/1111/174/1740706/thumbnail.jpg',
                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740706/raw.jpg',
                            license: 'Creative Common Attribution 3.0 Australia',
                            rights: 'Australian National Fish Collection, CSIRO, Image enhancement funded by CSIRO/FRDC',
                            creator: 'Australian National Fish Collection, CSIRO'],
                    genera: [
                            [taxonConcept: [nameString: 'Amblyraja', rankString: 'genus',
                                    guid: 'urn:lsid:biodiversity.org.au:afd.taxon:538fab84-27bd-4d35-8abd-391234dc199f'],
                                    preferredCommonName: '',
                                    preferredImage: [ContentType: 'image/jpeg',
                                            title: 'Amblyraja hyperborea',
                                            thumbnail: '',
                                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740706/raw.jpg',
                                            license: 'Creative Common Attribution 3.0 Australia',
                                            rights: 'Australian National Fish Collection, CSIRO, Image enhancement funded by CSIRO/FRDC',
                                            creator: 'Australian National Fish Collection, CSIRO']],
                            /*[taxonConcept: [nameString: 'Arhynchobatis', rankString: 'genus',
                                    guid: 'urn:lsid:catalogueoflife.org:taxon:d7f69ad0-29c1-102b-9a4a-00304854f820:ac2010'],
                                    preferredCommonName: '',
                                    preferredImage: [ContentType: 'image/jpeg',
                                            thumbnail: '',
                                            repoLocation: '',
                                            license: 'Creative Common Attribution 3.0 Australia',
                                            rights: 'Australian National Fish Collection, CSIRO, Image enhancement funded by CSIRO/FRDC',
                                            creator: 'Australian National Fish Collection, CSIRO']],*/
                            [taxonConcept: [nameString: 'Dentiraja', rankString: 'genus',
                                    guid: 'urn:lsid:biodiversity.org.au:afd.taxon:fec0a7dd-7534-43c2-9ab4-50b31bbab593'],
                                    preferredCommonName: '',
                                    preferredImage: [ContentType: 'image/jpeg',
                                            title: 'Dentiraja flindersi',
                                            thumbnail: '',
                                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740556/raw.jpg',
                                            license: 'Creative Common Attribution 3.0 Australia',
                                            rights: 'Australian National Fish Collection, CSIRO, Image enhancement funded by CSIRO/FRDC',
                                            creator: 'Australian National Fish Collection, CSIRO']],
                            [taxonConcept: [nameString: 'Dipturus', rankString: 'genus',
                                    guid: 'urn:lsid:biodiversity.org.au:afd.taxon:725cd37e-3b89-4810-a29d-91938d897a7e'],
                                    preferredCommonName: '',
                                    preferredImage: [ContentType: 'image/jpeg',
                                            title: 'Dipturus acrobelus',
                                            thumbnail: '',
                                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740699/raw.jpg',
                                            license: 'Creative Common Attribution 3.0 Australia',
                                            rights: 'Australian National Fish Collection, CSIRO, Image enhancement funded by CSIRO/FRDC',
                                            creator: 'Australian National Fish Collection, CSIRO']],
                            [taxonConcept: [nameString: 'Leucoraja', rankString: 'genus',
                                    guid: 'urn:lsid:biodiversity.org.au:afd.taxon:a987faff-3840-46d5-b7c2-0c3b7307b61f'],
                                    preferredCommonName: '',
                                    preferredImage: [ContentType: 'image/jpeg',
                                            title: 'Leucoraja pristispina',
                                            thumbnail: '',
                                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740562/raw.jpg',
                                            license: 'Creative Common Attribution 3.0 Australia',
                                            rights: 'Australian National Fish Collection, CSIRO, Image enhancement funded by CSIRO/FRDC',
                                            creator: 'Australian National Fish Collection, CSIRO']],
                            [taxonConcept: [nameString: 'Okamejei', rankString: 'genus',
                                    guid: 'urn:lsid:biodiversity.org.au:afd.taxon:0e9bb88d-5925-47e8-a521-dc8a4e3d92ab'],
                                    preferredCommonName: '',
                                    preferredImage: [ContentType: 'image/jpeg',
                                            title: 'Okamejei arafurensis',
                                            thumbnail: '',
                                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740563/raw.jpg',
                                            license: 'Creative Common Attribution 3.0 Australia',
                                            rights: 'Australian National Fish Collection, CSIRO, Image enhancement funded by CSIRO/FRDC',
                                            creator: 'Australian National Fish Collection, CSIRO']],
                            /*[taxonConcept: [nameString: 'Raja', rankString: 'genus',
                                    guid: '103178322'],
                                    preferredCommonName: '',
                                    preferredImage: null],*/
                            [taxonConcept: [nameString: 'Rajella', rankString: 'genus',
                                    guid: 'urn:lsid:biodiversity.org.au:afd.taxon:07160b95-8b56-4a27-b067-c4e6e8efe6ab'],
                                    preferredCommonName: '',
                                    preferredImage: [ContentType: 'image/jpeg',
                                            title: 'Rajella challengeri',
                                            thumbnail: '',
                                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740707/raw.jpg',
                                            license: 'Creative Common Attribution 3.0 Australia',
                                            rights: 'Australian National Fish Collection, CSIRO, Image enhancement funded by CSIRO/FRDC',
                                            creator: 'Australian National Fish Collection, CSIRO']],
                            [taxonConcept: [nameString: 'Spiniraja', rankString: 'genus',
                                    guid: 'urn:lsid:biodiversity.org.au:afd.taxon:72099fd6-b36a-4718-9614-34d2f9b9ed2d'],
                                    preferredCommonName: '',
                                    preferredImage: [ContentType: 'image/jpeg',
                                            title: 'Spiniraja whitleyi',
                                            thumbnail: '',
                                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740569/raw.jpg',
                                            license: 'Creative Common Attribution 3.0 Australia',
                                            rights: 'Australian National Fish Collection, CSIRO, Image enhancement funded by CSIRO/FRDC',
                                            creator: 'Australian National Fish Collection, CSIRO']],
                            [taxonConcept: [nameString: 'Zearaja', rankString: 'genus',
                                    guid: 'urn:lsid:biodiversity.org.au:afd.taxon:61ee6330-2e56-4069-a5cd-ce3e3a41904e'],
                                    preferredCommonName: '',
                                    preferredImage: [ContentType: 'image/jpeg',
                                            title: 'Zearaja maugeana',
                                            thumbnail: '',
                                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740700/raw.jpg',
                                            license: 'Creative Common Attribution 3.0 Australia',
                                            rights: 'Australian National Fish Collection, CSIRO, Image enhancement funded by CSIRO/FRDC',
                                            creator: 'Australian National Fish Collection, CSIRO']]
            ]],
            [taxonConcept: [nameString: 'Orectolobidae', rankString: 'family',
                    guid: 'urn:lsid:biodiversity.org.au:afd.taxon:5dc1e320-0329-4fae-8047-8da4fd792dd0'],
                    classification: [order: 'Orectolobiformes'],
                    CAABCode: '37 013 (part)',
                    preferredCommonName: 'Western Wobbegong',
                    preferredImage: [ContentType: 'image/jpeg',
                            thumbnail: 'http://bie.ala.org.au/repo/1111/174/1740542/thumbnail.jpg',
                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740542/raw.jpg',
                            license: 'Creative Common Attribution 3.0 Australia',
                            rights: 'Australian National Fish Collection, CSIRO',
                            creator: 'Australian National Fish Collection, CSIRO'],
                    genera: [
                            [taxonConcept: [nameString: 'Eucrossorhinus', rankString: 'genus',
                                    guid: 'urn:lsid:biodiversity.org.au:afd.taxon:7e4c3bf3-bc57-4335-b1af-9519fb38f9a4'],
                                    preferredCommonName: '',
                                    preferredImage: [ContentType: 'image/jpeg',
                                            title: 'Eucrossorhinus dasypogon',
                                            thumbnail: '',
                                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740622/raw.jpg',
                                            license: 'Creative Common Attribution 3.0 Australia',
                                            rights: 'Australian National Fish Collection, CSIRO, Image enhancement funded by CSIRO/FRDC',
                                            creator: 'Australian National Fish Collection, CSIRO']],
                            [taxonConcept: [nameString: 'Orectolobus', rankString: 'genus',
                                    guid: 'urn:lsid:biodiversity.org.au:afd.taxon:177b147e-db03-4834-ba1a-4aa56f24392e'],
                                    preferredCommonName: '',
                                    preferredImage: [ContentType: 'image/jpeg',
                                            title: 'Orectolobus floridus',
                                            thumbnail: '',
                                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740543/raw.jpg',
                                            license: 'Creative Common Attribution 3.0 Australia',
                                            rights: 'Australian National Fish Collection, CSIRO, Image enhancement funded by CSIRO/FRDC',
                                            creator: 'Australian National Fish Collection, CSIRO']],
                            [taxonConcept: [nameString: 'Sutorectus', rankString: 'genus',
                                    guid: 'urn:lsid:biodiversity.org.au:afd.taxon:91c925b3-62e1-489f-9ae4-be0ec2095397'],
                                    preferredCommonName: '',
                                    preferredImage: [ContentType: 'image/jpeg',
                                            title: 'Sutorectus tentaculatus',
                                            thumbnail: '',
                                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740737/raw.jpg',
                                            license: 'Creative Common Attribution 3.0 Australia',
                                            rights: 'Australian National Fish Collection, CSIRO, Image enhancement funded by CSIRO/FRDC',
                                            creator: 'Australian National Fish Collection, CSIRO']]
            ]],
            [taxonConcept: [nameString: 'Rhinopteridae', rankString: 'family',
                    guid: 'urn:lsid:biodiversity.org.au:afd.taxon:a8a0508d-e22f-44a8-a9df-d76358d2329e'],
                    classification: [order: 'Myliobatiformes'],
                    CAABCode: '37 040',
                    preferredCommonName: 'Cownose Rays',
                    preferredImage: [ContentType: 'image/jpeg',
                            thumbnail: 'http://bie.ala.org.au/repo/1111/174/1740809/thumbnail.jpg',
                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740809/raw.jpg',
                            license: 'Creative Common Attribution 3.0 Australia',
                            rights: 'Australian National Fish Collection, CSIRO',
                            creator: 'Australian National Fish Collection, CSIRO'],
                    genera: [
                            [taxonConcept: [nameString: 'Rhinoptera', rankString: 'genus',
                                    guid: 'urn:lsid:biodiversity.org.au:afd.taxon:346c30da-89d7-4d9a-b082-c320877d8a9c'],
                                    preferredCommonName: '',
                                    preferredImage: [ContentType: 'image/jpeg',
                                            title: 'Rhinoptera javanica',
                                            thumbnail: '',
                                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740809/raw.jpg',
                                            license: 'Creative Common Attribution 3.0 Australia',
                                            rights: 'Australian National Fish Collection, CSIRO, Image enhancement funded by CSIRO/FRDC',
                                            creator: 'Australian National Fish Collection, CSIRO']]
            ]]
    ]]

    def search2 = [total: 4, taxa: [
            [taxonConcept: [nameString: 'Carcharhinidae', rankString: 'family',
                    guid: 'urn:lsid:biodiversity.org.au:afd.taxon:62d6d958-e694-4038-a072-07f7d1f3fda2'],
                    classification: [order: 'Carcharhiniformes'],
                    CAABCode: '37 018 (part)',
                    preferredCommonName: 'Silvertip Shark',
                    preferredImage: [ContentType: 'image/jpeg',
                            title: 'Carcharhinus albimarginatus',
                            thumbnail: 'http://bie.ala.org.au/repo/1111/174/1740711/thumbnail.jpg',
                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740711/raw.jpg',
                            license: 'Creative Common Attribution 3.0 Australia',
                            rights: 'Australian National Fish Collection, CSIRO, Image enhancement funded by CSIRO/FRDC',
                            creator: 'Australian National Fish Collection, CSIRO']],
            [taxonConcept: [nameString: 'Callorhinchidae', rankString: 'family',
                    guid: 'urn:lsid:biodiversity.org.au:afd.taxon:537c51b2-c05a-42bc-9091-58f0030e63a7'],
                    classification: [order: 'Chimaeriformes'],
                    CAABCode: '37 043',
                    preferredCommonName: 'Elephant Fish',
                    preferredImage: [ContentType: 'image/jpeg',
                            title: 'Callorhinchus milii',
                            thumbnail: 'http://bie.ala.org.au/repo/1111/174/1740594/thumbnail.jpg',
                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740594/raw.jpg',
                            license: 'Creative Common Attribution 3.0 Australia',
                            rights: 'Ken Graham',
                            creator: 'Ken Graham']],
            [taxonConcept: [nameString: 'Heterodontidae', rankString: 'family',
                    guid: 'urn:lsid:biodiversity.org.au:afd.taxon:7fa709ac-94c2-4a0e-a11b-2cce7d893d2e'],
                    classification: [order: 'Heterodontiformes'],
                    CAABCode: '37 007',
                    preferredCommonName: 'Crested Bull Shark',
                    preferredImage: [ContentType: 'image/jpeg',
                            title: 'Heterodontus galeatus',
                            thumbnail: 'http://bie.ala.org.au/repo/1111/174/1740550/thumbnail.jpg',
                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740550/raw.jpg',
                            license: 'Creative Common Attribution 3.0 Australia',
                            rights: 'Australian National Fish Collection, CSIRO',
                            creator: 'Australian National Fish Collection, CSIRO']],
            [taxonConcept: [nameString: 'Chlamydoselachidae', rankString: 'family',
                    guid: 'urn:lsid:biodiversity.org.au:afd.taxon:e6d3eee4-566c-4603-86f7-772a2a21af4f'],
                    classification: [order: 'Hexanchiformes'],
                    CAABCode: '37 006',
                    preferredCommonName: 'Frill Sharks',
                    preferredImage: [ContentType: 'image/jpeg',
                            title: 'Chlamydoselachus anguineus',
                            thumbnail: 'http://bie.ala.org.au/repo/1111/174/1740584/thumbnail.jpg',
                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740584/raw.jpg',
                            license: 'Creative Common Attribution 3.0 Australia',
                            rights: 'Australian National Fish Collection, CSIRO',
                            creator: 'Australian National Fish Collection, CSIRO']],
    ]]

    def search3 = [total: 4, taxa: [
            [taxonConcept: [nameString: 'Hexanchidae', rankString: 'family',
                    guid: 'urn:lsid:biodiversity.org.au:afd.taxon:3e0e15e9-dd24-46d8-9ea2-be9a7130fc5f'],
                    classification: [order: 'Hexanchiformes'],
                    CAABCode: '37 005',
                    preferredCommonName: 'Cow Sharks',
                    preferredImage: [ContentType: 'image/jpeg',
                            title: 'Heptranchias perlo',
                            thumbnail: 'http://bie.ala.org.au/repo/1111/174/1740511/thumbnail.jpg',
                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740511/raw.jpg',
                            license: 'Creative Common Attribution 3.0 Australia',
                            rights: 'Australian National Fish Collection, CSIRO',
                            creator: 'Australian National Fish Collection, CSIRO']],
            [taxonConcept: [nameString: 'Dasyatidae', rankString: 'family',
                    guid: 'urn:lsid:biodiversity.org.au:afd.taxon:43c35340-509c-48d9-bb6f-ad87916da26b'],
                    classification: [order: 'Myliobatiformes'],
                    CAABCode: '37 035',
                    preferredCommonName: 'Stingrays',
                    preferredImage: [ContentType: 'image/jpeg',
                            title: 'Dasyatis brevicaudata',
                            thumbnail: 'http://bie.ala.org.au/repo/1111/174/1740546/thumbnail.jpg',
                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740546/raw.jpg',
                            license: 'Creative Common Attribution 3.0 Australia',
                            rights: 'Australian National Fish Collection, CSIRO',
                            creator: 'Australian National Fish Collection, CSIRO']],
            [taxonConcept: [nameString: 'Urolophidae', rankString: 'family',
                    guid: 'urn:lsid:biodiversity.org.au:afd.taxon:371ed595-dec8-4d5b-8f4d-61165ee0edd8'],
                    classification: [order: 'Myliobatiformes'],
                    CAABCode: '37 038 (part)',
                    preferredCommonName: 'Stingarees',
                    preferredImage: [ContentType: 'image/jpeg',
                            title: 'Trygonoptera galba',
                            thumbnail: 'http://bie.ala.org.au/repo/1111/174/1740604/thumbnail.jpg',
                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740604/raw.jpg',
                            license: 'Creative Common Attribution 3.0 Australia',
                            rights: 'Australian National Fish Collection, CSIRO',
                            creator: 'Australian National Fish Collection, CSIRO']],
            [taxonConcept: [nameString: 'Pristiophoridae', rankString: 'family',
                    guid: 'urn:lsid:biodiversity.org.au:afd.taxon:633b7065-df8c-469a-b97f-6f8b88115299'],
                    classification: [order: 'Pristiophoriformes'],
                    CAABCode: '37 023',
                    preferredCommonName: 'Sawsharks',
                    preferredImage: [ContentType: 'image/jpeg',
                            title: 'Pristiophorus delicatus',
                            thumbnail: 'http://bie.ala.org.au/repo/1111/174/1740672/thumbnail.jpg',
                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740672/raw.jpg',
                            license: 'Creative Common Attribution 3.0 Australia',
                            rights: 'Australian National Fish Collection, CSIRO, Image enhancement funded by CSIRO/FRDC',
                            creator: 'Australian National Fish Collection, CSIRO']],
    ]]

    /*     def search2 = [total: 4, taxa: [
            [taxonConcept: [nameString: 'Carcharhinidae', rankString: 'family',
                    guid: 'urn:lsid:biodiversity.org.au:afd.taxon:62d6d958-e694-4038-a072-07f7d1f3fda2'],
                    classification: [order: 'Carcharhiniformes'],
                    preferredCommonName: 'Silvertip Shark',
                    preferredImage: [ContentType: 'image/jpeg',
                            thumbnail: 'http://bie.ala.org.au/repo/1111/174/1740711/thumbnail.jpg',
                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740711/raw.jpg',
                            license: 'Creative Common Attribution 3.0 Australia',
                            rights: 'Australian National Fish Collection, CSIRO, Image enhancement funded by CSIRO/FRDC',
                            creator: 'Australian National Fish Collection, CSIRO']],

                            [taxonConcept: [nameString: '', rankString: 'genus',
                                    guid: ''],
                                    preferredCommonName: '',
                                    preferredImage: [ContentType: 'image/jpeg',
                                            thumbnail: '',
                                            repoLocation: '',
                                            license: 'Creative Common Attribution 3.0 Australia',
                                            rights: 'Australian National Fish Collection, CSIRO, Image enhancement funded by CSIRO/FRDC',
                                            creator: 'Australian National Fish Collection, CSIRO']],



     */

    def speciesData = [
            Lamnidae: [
                    [name: 'Isurus oxyrinchus',
                     guid: 'urn:lsid:biodiversity.org.au:afd.taxon:d315a8f6-b990-4a0a-aee1-055efae7bc1f',
                     common:  'Shortfin Mako',
                     CAABCode: '37 010001',
                     family: 'Lamnidae',
                     image: [repo: 'http://bie.ala.org.au/repo/1111/174/1740514/raw.jpg',
                             license: 'Creative Common Attribution 3.0 Australia',
                             rights: 'Australian National Fish Collection, CSIRO, Image enhancement funded by CSIRO/FRDC',
                             creator: 'Australian National Fish Collection, CSIRO'],
                     distributionImage: [repo: 'http://bie.ala.org.au/repo/1011/38/382186/raw.png',
                             license: null,
                             rights: null,
                             creator: null]],
                    [name: 'Carcharodon carcharias',
                     guid: 'urn:lsid:biodiversity.org.au:afd.taxon:ec40a215-7127-4555-b314-abaa3de66155',
                     common:  'Great White Shark',
                     CAABCode: '37 010003',
                     family: 'Lamnidae',
                     image: [repo: 'http://bie.ala.org.au/repo/1111/174/1740554/raw.jpg',
                             license: 'Creative Common Attribution 3.0 Australia',
                             rights: 'Australian National Fish Collection, CSIRO, Image enhancement funded by CSIRO/FRDC',
                             creator: 'Australian National Fish Collection, CSIRO']],
                    [name: 'Isurus paucus',
                     guid: 'urn:lsid:biodiversity.org.au:afd.taxon:88390b35-784a-4e55-ab24-48f401d331d2',
                     common:  'Longfin Mako',
                     CAABCode: '37 010002',
                     family: 'Lamnidae',
                     image: [repo: 'http://bie.ala.org.au/repo/1111/174/1740589/raw.jpg',
                             license: 'Creative Common Attribution 3.0 Australia',
                             rights: 'Australian National Fish Collection',
                             creator: 'Australian National Fish Collection, CSIRO']],
                    [name: 'Lamna nasus',
                     guid: 'urn:lsid:biodiversity.org.au:afd.taxon:f57b6325-e2ca-4cc1-8813-ec7a26f0f50a',
                     common:  'Mackerel Shark',
                     CAABCode: '37 010004',
                     family: 'Lamnidae',
                     image: [repo: 'http://bie.ala.org.au/repo/1111/174/1740716/raw.jpg',
                             license: 'Creative Common Attribution 3.0 Australia',
                             rights: 'Australian National Fish Collection, CSIRO, Image enhancement funded by CSIRO/FRDC',
                             creator: 'Australian National Fish Collection, CSIRO']]/*,
                    [name: '',
                     guid: '',
                     common:  '',
                     CAABCode: '',
                     image: [repo: '',
                             license: 'Creative Common Attribution 3.0 Australia',
                             rights: 'Australian National Fish Collection, CSIRO, Image enhancement funded by CSIRO/FRDC',
                             creator: 'Australian National Fish Collection, CSIRO']]*/
            ]

    ]
}
