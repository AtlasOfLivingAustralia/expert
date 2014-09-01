package au.org.ala.expert

import au.com.bytecode.opencsv.CSVReader
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray

class MetadataService {

    def webService, grailsApplication
    
    static bathomeValues = [
            'coastal/shallow water (0-40m)',
            'shelf (0-200m)',
            'shelf + upper slope (0-500m)',
            'upper slope only (200-500m)',
            'any (0-2000+m)']

    def getFishGroups() {
        def fg = getAllGroups()
        return [keys: fg, display: fg.collect {it[0].toUpperCase() + it[1..-1]}]
    }

    def getLocalitiesByState() {
        [
                [state: 'New South Wales', localities: localities.values().findAll({it.state == 'NSW'})],
                [state: 'Northern Territory', localities: localities.values().findAll({it.state == 'NT'})],
                [state: 'Queensland', localities: localities.values().findAll({it.state == 'Qld'})],
                [state: 'South Australia', localities: localities.values().findAll({it.state == 'SA'})],
                [state: 'Tasmania', localities: localities.values().findAll({it.state == 'Tas'})],
                [state: 'Victoria', localities: localities.values().findAll({it.state == 'Vic'})],
                [state: 'Western Australia', localities: localities.values().findAll({it.state == 'WA'})]
        ]
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

    static familiesCache = []
    static groupsCache = []

    def loadFamiliesAndGroups() {
        def all = webService.getJson(grailsApplication.config.spatial.layers.service.url +
                "/distributions.json?dataResourceUid=" +
                grailsApplication.config.distribution.maps.dataResourceUid)
        // protect against an error response
        if (!(all instanceof JSONArray)) {
            return []
        }
        // clear
        familiesCache = []
        groupsCache = []
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
        familiesCache.sort()
        groupsCache.sort()
    }

    def getAllFamilies = {
        if (!familiesCache) {
            loadFamiliesAndGroups()
        }
        return familiesCache
    }

    def getAllGroups = {
        if (!groupsCache) {
            loadFamiliesAndGroups()
        }
        return groupsCache
    }

    static localities = [
            "Albany":["state":"WA","name":"Albany","lat":-35.0030,"lng":117.8658],
            "Bateman's Bay":["state":"NSW","name":"Bateman's Bay","lat":-35.7316,"lng":150.2177],
            "Brisbane":["state":"Qld","name":"Brisbane","lat":-27.4711,"lng":153.0241],
            "Broome":["state":"WA","name":"Broome","lat":-17.9616,"lng":122.2363],
            "Cairns":["state":"Qld","name":"Cairns","lat":-16.9230,"lng":145.7663],
            "Cape Jaffa":["state":"SA","name":"Cape Jaffa","lat":-36.9533,"lng":139.6733],
            "Cape Leeuwin":["state":"WA","name":"Cape Leeuwin","lat":-34.3766,"lng":115.1358],
            "Cape Londonderry":["state":"WA","name":"Cape Londonderry","lat":-13.7411,"lng":126.9644],
            "Carnarvon":["state":"WA","name":"Carnarvon","lat":-24.8808,"lng":113.6594],
            "Coffs Harbour": ["state":"NSW","name":"Coffs Harbour","lat":-30.2963,"lng":153.1136],
            "Darwin":["state":"NT","name":"Darwin","lat":-12.4572,"lng":130.8366],
            "Devonport":["state":"Tas","name":"Devonport","lat":-41.1769,"lng":146.3513],
            "Eden":["state":"NSW","name":"Eden","lat":-37.0633,"lng":149.9038],
            "Esperance":["state":"WA","name":"Esperance","lat":-33.86,"lng":121.8825],
            "Exmouth":["state":"WA","name":"Exmouth","lat":-21.9402,"lng":114.125],
            "Eyre":["state":"WA","name":"Eyre","lat":-32.2458,"lng":126.3041],
            "Fraser Island":["state":"Qld","name":"Fraser Island","lat":-25.2502,"lng":153.1669],
            "Geelong":["state":"Vic","name":"Geelong","lat":-38.1533,"lng":144.3580],
            "Geraldton":["state":"WA","name":"Geraldton","lat":-28.7791,"lng":114.6144],
            "Gladstone":["state":"Qld","name":"Gladstone","lat":-23.8475,"lng":151.2563],
            "Groote Eylandt":["state":"NT","name":"Groote Eylandt","lat":-13.9330,"lng":136.6],
            "Hobart":["state":"Tas","name":"Hobart","lat":-42.8830,"lng":147.3316],
            "Kangaroo Island":["state":"SA","name":"Kangaroo Island","lat":-35.8116,"lng":137.2052],
            "Karumba":["state":"Qld","name":"Karumba","lat":-17.4888,"lng":140.8383],
            "Lakes Entrance":["state":"Vic","name":"Lakes Entrance","lat":-37.8766,"lng":147.9941],
            "Mackay":["state":"Qld","name":"Mackay","lat":-21.1533,"lng":149.1655],
            "Newcastle":["state":"NSW","name":"Newcastle","lat":-32.9316,"lng":151.7847],
            "Perth":["state":"WA","name":"Perth","lat":-31.9522,"lng":115.8613],
            "Port Hedland":["state":"WA","name":"Port Hedland","lat":-20.3122,"lng":118.6105],
            "Port Lincoln":["state":"SA","name":"Port Lincoln","lat":-34.7263,"lng":135.8744],
            "Port Macquarie":["state":"NSW","name":"Port Macquarie","lat":-31.4316,"lng":152.9177],
            "Princess Charlotte Bay":["state":"Qld","name":"Princess Charlotte Bay","lat":-14.2169,"lng":143.9680],
            "South East Cape":["state":"Tas","name":"South East Cape","lat":-43.64,"lng":146.82],
            "St Marys":["state":"Tas","name":"St Marys","lat":-41.5791,"lng":148.1872],
            "Strahan":["state":"Tas","name":"Strahan","lat":-42.1519,"lng":145.3272],
            "Sydney":["state":"NSW","name":"Sydney","lat":-33.8652,"lng":151.2097],
            "Thursday Island":["state":"Qld","name":"Thursday Island","lat":-10.5808,"lng":142.2197],
            "Townsville":["state":"Qld","name":"Townsville","lat":-19.2663,"lng":146.8055],
            "Warrnambool":["state":"Vic","name":"Warrnambool","lat":-38.3816,"lng":142.4880],
            "Weipa":["state":"Qld","name":"Weipa","lat":-12.25,"lng":142.05],
            "Wessel Islands":["state":"NT","name":"Wessel Islands","lat":-11.6044,"lng":136.3288],
            "Wilsons Promontory":["state":"Vic","name":"Wilsons Promontory","lat":-39.0480,"lng":146.3886]
    ]

    def getMarineRegions() {
        return imcras
    }

    // some imcras removed as out of scope for distribution maps
    static imcras = [
            [name: "Bass Strait Shelf Province", pid: "3742572"],
            [name: "Cape Province", pid: "3742569"],
            [name: "Central Eastern Province", pid: "3742575"],
            [name: "Central Eastern Shelf Province", pid: "3742590"],
            [name: "Central Eastern Shelf Transition", pid: "3742571"],
            [name: "Central Eastern Transition", pid: "3742573"],
            [name: "Central Western Province", pid: "3742561"],
            [name: "Central Western Shelf Province", pid: "3742570"],
            [name: "Central Western Shelf Transition", pid: "3742576"],
            [name: "Central Western Transition", pid: "3742562"],
            /*[name: "Christmas Island Province", pid: "3742578"],*/
            /*[name: "Cocos (Keeling) Island Province", pid: "3742598"],*/
            [name: "Great Australian Bight Shelf Transition", pid: "3742577"],
            /*[name: "Kenn Province", pid: "3742581"],*/
            /*[name: "Kenn Transition", pid: "3742586"],*/
            /*[name: "Lord Howe Province", pid: "3742579"],
            [name: "Macquarie Island Province", pid: "3742596"],
            [name: "Norfolk Island Province", pid: "3742567"],*/
            [name: "Northeast Province", pid: "3742560"],
            [name: "Northeast Shelf Province", pid: "3742589"],
            [name: "Northeast Shelf Transition", pid: "3742597"],
            [name: "Northeast Transition", pid: "3742595"],
            [name: "Northern Shelf Province", pid: "3742588"],
            [name: "Northwest Province", pid: "3742587"],
            [name: "Northwest Shelf Province", pid: "3742585"],
            [name: "Northwest Shelf Transition", pid: "3742563"],
            [name: "Northwest Transition", pid: "3742582"],
            [name: "Southeast Shelf Transition", pid: "3742568"],
            [name: "Southeast Transition", pid: "3742583"],
            [name: "Southern Province", pid: "3742592"],
            [name: "Southwest Shelf Province", pid: "3742565"],
            [name: "Southwest Shelf Transition", pid: "3742580"],
            [name: "Southwest Transition", pid: "3742559"],
            [name: "Spencer Gulf Shelf Province", pid: "3742591"],
            [name: "Tasman Basin Province", pid: "3742584"],
            [name: "Tasmania Province", pid: "3742564"],
            [name: "Tasmanian Shelf Province", pid: "3742566"],
            [name: "Timor Province", pid: "3742594"],
            [name: "Timor Transition", pid: "3742599"],
            [name: "West Tasmania Transition", pid: "3742593"],
            [name: "Western Bass Strait Shelf Transition", pid: "3742574"]
    ]

    //Modified by Alan on for fetching multiple layers on 30/07/2014 --- START
    def getMyLayerRegions() {
        return myLayer
    }

    // some imcras removed as out of scope for distribution maps
    static myLayer = [
            [name: "Marine bioregion", pid: "cl21"],
            //[name: "2014 CAPAD bioregion", pid: "cl1050"]
            [name: "SE CMR Network", pid: "cl1051"]
    ]

    def getCapad2014Regions() {
        return capad2014
    }

    // Define the 2014 CAPAD bioregions
    static capad2014 = [
            //[name: "Abrolhos Islands", pid: "5746593"],
            //[name: "Adelaide Dolphin Sanctuary", pid: "5746474"],
            //[name: "Aldinga Reef", pid: "5746578"],
            //[name: "American River", pid: "5746560"],
            //[name: "Annan River", pid: "5746643"],
            //[name: "Annan River", pid: "5746637"],
            [name: "Apollo", pid: "5746550"],
            //[name: "Ashmore Reef", pid: "5746488"],
            //[name: "Baffle Creek", pid: "5746657"],
            //[name: "Bales Beach", pid: "5746588"],
            //[name: "Ball Bay - Sand Bay", pid: "5746693"],
            //[name: "Barker Inlet", pid: "5746634"],
            //[name: "Barr Creek", pid: "5746530"],
            //[name: "Barrenjoey", pid: "5746629"],
            //[name: "Barrow Island", pid: "5746610"],
            //[name: "Barrow Island", pid: "5746567"],
            //[name: "Bass Pyramid", pid: "5746525"],
            //[name: "Bassett Basin (Rev.1)", pid: "5746587"],
            //[name: "Batemans", pid: "5746501"],
            [name: "Beagle", pid: "5746603"],
            //[name: "Beware Reef", pid: "5746658"],
            //[name: "Beelbi", pid: "5746689"],
            //[name: "Beelbi", pid: "5746679"],
            //[name: "Blackman Rivulet", pid: "5746503"],
            //[name: "Blanche Harbour - Douglas Bank", pid: "5746665"],
            [name: "Boags", pid: "5746520"],
            //[name: "Boat Harbour", pid: "5746670"],
            //[name: "Bohle River", pid: "5746598"],
            //[name: "Booderee", pid: "5746518"],
            //[name: "Bowling Green Bay", pid: "5746524"],
            //[name: "Bowling Green Bay", pid: "5746611"],
            //[name: "Broad Sound", pid: "5746672"],
            //[name: "Bronte-Coogee", pid: "5746616"],
            //[name: "Burdekin", pid: "5746606"],
            //[name: "Burrum", pid: "5746596"],
            //[name: "Burrum", pid: "5746594"],
            //[name: "Bushrangers Bay", pid: "5746526"],
            //[name: "Cabbage Tree Bay", pid: "5746589"],
            //[name: "Cape Banks", pid: "5746528"],
            //[name: "Cape Byron", pid: "5746482"],
            //[name: "Cape Jaffa", pid: "5746669"],
            //[name: "Cape Palmerston - Rocky Dam", pid: "5746517"],
            //[name: "Cape Palmerston - Rocky Dam", pid: "5746485"],
            //[name: "Cartier Island", pid: "5746636"],
            //[name: "Cattle Creek", pid: "5746579"],
            //[name: "Cawarral Creek", pid: "5746645"],
            //[name: "Central Channel", pid: "5746674"],
            //[name: "Clairview Bluff - Carmilla Creek", pid: "5746659"],
            //[name: "Cleveland Bay", pid: "5746683"],
            //[name: "Cleveland Bay - Magnetic Island", pid: "5746580"],
            //[name: "Cod Grounds", pid: "5746602"],
            //[name: "Colosseum Inlet", pid: "5746633"],
            //[name: "Colosseum Inlet", pid: "5746539"],
            //[name: "Coobowie Bay", pid: "5746572"],
            //[name: "Cook Island", pid: "5746631"],
            //[name: "Coombabah", pid: "5746582"],
            //[name: "Coomera", pid: "5746542"],
            //[name: "Coringa-Herald", pid: "5746653"],
            //[name: "Corio Bay", pid: "5746595"],
            //[name: "Cottesloe Reef", pid: "5746662"],
            //[name: "Currumbin Creek", pid: "5746533"],
            //[name: "Dallachy Creek", pid: "5746493"],
            //[name: "Deception Bay", pid: "5746667"],
            [name: "East Gippsland", pid: "5746642"],
            //[name: "Eastern Spencer Gulf", pid: "5746516"],
            //[name: "Edgecumbe Bay", pid: "5746569"],
            //[name: "Edgecumbe Bay", pid: "5746656"],
            //[name: "Edgecumbe Bay – Bowen", pid: "5746608"],
            //[name: "Eight Mile Creek", pid: "5746506"],
            //[name: "Elizabeth and Middleton Reefs", pid: "5746540"],
            //[name: "Elliott River", pid: "5746478"],
            //[name: "Encounter", pid: "5746463"],
            //[name: "Escape River", pid: "5746576"],
            //[name: "Eurimbula", pid: "5746621"],
            //[name: "Far West Coast", pid: "5746522"],
            //[name: "Fitzroy River", pid: "5746583"],
            [name: "Flinders", pid: "5746483"],
            [name: "Franklin", pid: "5746639"],
            //[name: "Franklin Harbor", pid: "5746515"],
            //[name: "Fraser Island", pid: "5746680"],
            [name: "Freycinet", pid: "5746552"],
            //[name: "Gambier Islands Group", pid: "5746604"],
            //[name: "Garig Gunak Barlu", pid: "5746677"],
            //[name: "Gleesons Landing", pid: "5746650"],
            //[name: "Goose Island", pid: "5746666"],
            //[name: "Great Australian Bight", pid: "5746466"],
            //[name: "Great Australian Bight (Commonwealth Waters)", pid: "5746673"],
            //[name: "Great Australian Bight (Conservation Zone)", pid: "5746492"],
            //[name: "Great Australian Bight (Sanctuary Zone)", pid: "5746632"],
            //[name: "Great Barrier Reef", pid: "5746529"],
            //[name: "Great Barrier Reef Coast", pid: "5746622"],
            //[name: "Great Sandy", pid: "5746489"],
            //[name: "Half Moon Creek", pid: "5746577"],
            //[name: "Halifax", pid: "5746685"],
            //[name: "Hamelin Pool", pid: "5746558"],
            //[name: "Hay's Inlet", pid: "5746519"],
            //[name: "Heard Island and McDonald Islands", pid: "5746675"],
            //[name: "Hervey Bay - Tin Can Bay", pid: "5746486"],
            //[name: "Hinchinbrook", pid: "5746502"],
            //[name: "Hinchinbrook Island area", pid: "5746612"],
            //[name: "Hippolyte Rocks", pid: "5746546"],
            //[name: "HMAS Hobart", pid: "5746534"],
            //[name: "Hull River", pid: "5746495"],
            [name: "Huon", pid: "5746500"],
            //[name: "Ince Bay (Cape Palmerston - Allom Point)", pid: "5746635"],
            //[name: "Investigator", pid: "5746547"],
            //[name: "Jervis Bay", pid: "5746472"],
            //[name: "Jumpinpin-Broadwater", pid: "5746671"],
            //[name: "Jurien Bay", pid: "5746597"],
            //[name: "Kalbarri Blue Holes", pid: "5746692"],
            //[name: "Kauri Creek", pid: "5746523"],
            //[name: "Kinkuna", pid: "5746541"],
            //[name: "Kippa-Ring", pid: "5746512"],
            //[name: "Kolan River", pid: "5746655"],
            //[name: "Lancelin Island Lagoon", pid: "5746564"],
            //[name: "Lihou Reef", pid: "5746532"],
            //[name: "Llewellyn Bay", pid: "5746668"],
            //[name: "Long Reef", pid: "5746654"],
            //[name: "Lord Howe Island", pid: "5746553"],
            //[name: "Lower South East", pid: "5746615"],
            //[name: "Lower Yorke Peninsula", pid: "5746559"],
            //[name: "Lord Howe Island", pid: "5746498"],
            //[name: "Lucinda to Allingham - Halifax Bay", pid: "5746507"],
            //[name: "Maaroom", pid: "5746467"],
            //[name: "Macquarie Island", pid: "5746623"],
            //[name: "Margaret Bay \"Wuthathi\" (Rev.1)", pid: "5746575"],
            //[name: "Margaret Bay \"Wuthathi\" (Rev.1)", pid: "5746487"],
            //[name: "Margaret Brock Reef", pid: "5746601"],
            //[name: "Marmion", pid: "5746531"],
            //[name: "Maroochy", pid: "5746599"],
            //[name: "Marengo Reefs", pid: "5746543"],
            //[name: "Maroochy", pid: "5746496"],
            //[name: "Mermaid Reef", pid: "5746663"],
            //[name: "Meunga Creek", pid: "5746497"],
            //[name: "Miaboolya Beach", pid: "5746566"],
            //[name: "Midge", pid: "5746477"],
            //[name: "Monk Bay", pid: "5746551"],
            //[name: "Montebello Islands", pid: "5746627"],
            //[name: "Moreton Banks", pid: "5746617"],
            //[name: "Moreton Bay", pid: "5746508"],
            //[name: "Morning Inlet - Bynoe River", pid: "5746554"],
            //[name: "Muiron Islands", pid: "5746480"],
            //[name: "Murray River", pid: "5746499"],
            [name: "Murray", pid: "5746479"],
            //[name: "Myora - Amity Banks", pid: "5746591"],
            //[name: "Narrabeen", pid: "5746682"],
            //[name: "Nassau River", pid: "5746609"],
            [name: "Nelson", pid: "5746664"],
            //[name: "Neptune Islands Group", pid: "5746545"],
            //[name: "Ningaloo", pid: "5746510"],
            //[name: "Ningaloo (Commonwealth Waters)", pid: "5746641"],
            //[name: "Noosa River (Rev.2)", pid: "5746511"],
            //[name: "Noosa River (Rev.2)", pid: "5746574"],
            //[name: "North Sydney Harbour", pid: "5746592"],
            //[name: "Nuyts Archipelago", pid: "5746470"],
            //[name: "Palm Creek", pid: "5746581"],
            //[name: "Peel Island", pid: "5746681"],
            //[name: "Pimpama", pid: "5746678"],
            //[name: "Point Labatt", pid: "5746571"],
            //[name: "Point Quobba", pid: "5746646"],
            //[name: "Port Clinton (Reef Point - Cape Clinton)", pid: "5746644"],
            //[name: "Port Cygnet", pid: "5746555"],
            //[name: "Port Noarlunga Reef", pid: "5746600"],
            //[name: "Port of Gladstone - Rodds Bay", pid: "5746652"],
            //[name: "Port Stephens - Great Lakes", pid: "5746465"],
            //[name: "Princess Charlotte Bay", pid: "5746688"],
            //[name: "Pumicestone Channel", pid: "5746562"],
            //[name: "Pumicestone Channel", pid: "5746625"],
            //[name: "Reid Rocks", pid: "5746514"],
            //[name: "Repulse", pid: "5746468"],
            //[name: "Repulse Bay", pid: "5746544"],
            //[name: "Rivoli Bay (Including Penguin Island)", pid: "5746504"],
            //[name: "Roberts Point", pid: "5746607"],
            //[name: "Rodds Bay", pid: "5746563"],
            //[name: "Rodds Bay", pid: "5746481"],
            //[name: "Rowley Shoals", pid: "5746537"],
            //[name: "Sand Bay", pid: "5746535"],
            //[name: "Seal Bay", pid: "5746618"],
            //[name: "Seventeen Seventy-Round Hill", pid: "5746638"],
            //[name: "Shark Bay", pid: "5746475"],
            //[name: "Shiprock", pid: "5746630"],
            //[name: "Shoalwater Bay", pid: "5746471"],
            //[name: "Shoalwater Islands", pid: "5746660"],
            //[name: "Simpsons Point", pid: "5746605"],
            //[name: "Silver Plains", pid: "5746490"],
            //[name: "Sir Joseph Banks Group", pid: "5746628"],
            //[name: "Sloping Island", pid: "5746640"],
            //[name: "Solitary Islands", pid: "5746651"],
            //[name: "Solitary Islands (Commonwealth Waters)", pid: "5746509"],
            [name: "South Tasman Rise", pid: "5746464"],
            //[name: "Southern Kangaroo Island", pid: "5746624"],
            //[name: "Southern Spencer Gulf", pid: "5746505"],
            //[name: "St Kilda - Chapman Creek", pid: "5746476"],
            //[name: "Staaten-Gilbert", pid: "5746556"],
            //[name: "Starcke River (Ngulun)", pid: "5746620"],
            //[name: "Starcke River (Ngulun)", pid: "5746619"],
            //[name: "Stewart Peninsula - Newry Island - Ball Bay", pid: "5746484"],
            //[name: "Susan River", pid: "5746584"],
            //[name: "Swan Estuary", pid: "5746568"],
            //[name: "Swan Estuary", pid: "5746573"],
            //[name: "Swan Estuary – Milyu", pid: "5746473"],
            //[name: "Swan Estuary - Pelican Point", pid: "5746647"],
            //[name: "Tallebudgera Creek", pid: "5746538"],
            [name: "Tasman Fracture", pid: "5746690"],
            //[name: "Temple Bay", pid: "5746494"],
            //[name: "The Arches", pid: "5746613"],
            //[name: "Thorny Passage", pid: "5746491"],
            //[name: "Tin Can Inlet", pid: "5746648"],
            //[name: "Trinity Inlet", pid: "5746513"],
            //[name: "Trinity Inlet", pid: "5746691"],
            //[name: "Towra Point", pid: "5746614"],
            //[name: "Troubridge Hill", pid: "5746686"],
            //[name: "Tully River", pid: "5746684"],
            //[name: "Upper Gulf St Vincent", pid: "5746649"],
            //[name: "Upper South East", pid: "5746548"],
            //[name: "Upper Spencer Gulf", pid: "5746661"],
            //[name: "Upstart Bay", pid: "5746565"],
            //[name: "Walpole-Nornalup Inlets", pid: "5746549"],
            //[name: "Waterfall-Fortescue", pid: "5746469"],
            //[name: "West Coast Bays", pid: "5746561"],
            //[name: "West Hill", pid: "5746687"],
            //[name: "West Hill", pid: "5746570"],
            //[name: "West Island - Encounter Bay", pid: "5746590"],
            //[name: "Western Kangaroo Island", pid: "5746527"],
            //[name: "Whyalla - Cowleds Landing", pid: "5746462"],
            //[name: "Wreck Creek", pid: "5746586"],
            //[name: "Wright Rock", pid: "5746626"],
            //[name: "Yatala Harbour", pid: "5746536"],
            //[name: "Yorkeys Creek", pid: "5746585"],
            //[name: "Zanoni", pid: "5746676"],
            [name: "Zeehan", pid: "5746557"]
    ]
    //Added by Alan --- END

    def parseCsv() {
        CSVReader reader = new CSVReader(new StringReader(csv),',' as char)
        String [] nextLine;
        def raw = []

        while ((nextLine = reader.readNext()) != null) {
            raw << [state: nextLine[0], local: nextLine[1], lat: nextLine[2], lng: nextLine[3]]
        }
        def localities = [:]
        raw.each { loc ->
            def x = [state: loc.state, name: loc.local,
                    lat: convert(loc.lat), lng: convert(loc.lng)]
            localities.put loc.local, x
        }
        log.debug (localities as JSON).toString()
    }
    
    def convert = {
        def pattern = /^(.*)º (.*)' (.*)'' ./
        def matcher = (it =~ pattern)
        Double deg, min, sec
        assert matcher.matches()
        deg = (matcher[0][1] as String).toDouble()
        min = (matcher[0][2] as String).toDouble()
        sec = (matcher[0][3] as String).toDouble()
        if (deg < 0) {
            return deg - min/60 - sec/3600
        } else {
            return deg + min/60 + sec/3600
        }
    }

    static csv = """NSW,Coffs Harbour,-30º 17' 47'' S,153º 6' 49'' E
                    NSW,Port Macquarie,-31º 25' 54'' S,152º 55' 4'' E
                    NSW,Newcastle,-32º 55' 54'' S,151º 47' 5'' E
                    NSW,Sydney,-33º 51' 55'' S,151º 12' 35'' E
                    NSW,Bateman's Bay,-35º 43' 54'' S,150º 13' 4'' E
                    NSW,Eden,-37º 3' 48'' S,149º 54' 14'' E
                    NT,Wessel Islands,-11º 36' 16'' S,136º 19' 44'' E
                    NT,Darwin,-12º 27' 26'' S,130º 50' 12'' E
                    NT,Groote Eylandt,-13º 55' 59'' S,136º 36' 0'' E
                    Qld,Thursday Island,-10º 34' 51'' S,142º 13' 11'' E
                    Qld,Weipa,-12º 15' 0'' S,142º 3' 0'' E
                    Qld,Princess Charlotte Bay,-14º 13' 1'' S,143º 58' 5'' E
                    Qld,Cairns,-16º 55' 23'' S,145º 45' 59'' E
                    Qld,Karumba,-17º 29' 20'' S,140º 50' 18'' E
                    Qld,Townsville,-19º 15' 59'' S,146º 48' 20'' E
                    Qld,Mackay,-21º 9' 12'' S,149º 9' 56'' E
                    Qld,Gladstone,-23º 50' 51'' S,151º 15' 23'' E
                    Qld,Fraser Island,-25º 15' 1'' S,153º 10' 1'' E
                    Qld,Brisbane,-27º 28' 16'' S,153º 1' 27'' E
                    SA,Port Lincoln,-34º 43' 35'' S,135º 52' 28'' E
                    SA,Kangaroo Island,-35º 48' 42'' S,137º 12' 19'' E
                    SA,Cape Jaffa,-36º 57' 12'' S,139º 40' 24'' E
                    Tas,Devonport,-41º 10' 37'' S,146º 21' 5'' E
                    Tas,St Marys,-41º 34' 45'' S,148º 11' 14'' E
                    Tas,Hobart,-42º 52' 59'' S,147º 19' 54'' E
                    Tas,Strahan,-42º 9' 7'' S,145º 19' 38'' E
                    Tas,South East Cape,-43º 38' 24'' S,146º 49' 12'' E
                    Vic,Lakes Entrance,-37º 52' 36'' S,147º 59' 39'' E
                    Vic,Warrnambool,-38º 22' 54'' S,142º 29' 17'' E
                    Vic,Geelong,-38º 9' 12'' S,144º 21' 29'' E
                    Vic,Wilsons Promontory,-39º 2' 53'' S,146º 23' 19'' E
                    WA,Cape Londonderry,-13º 44' 28'' S,126º 57' 52'' E
                    WA,Broome,-17º 57' 42'' S,122º 14' 11'' E
                    WA,Port Hedland,-20º 18' 44'' S,118º 36' 38'' E
                    WA,Exmouth,-21º 56' 25'' S,114º 7' 30'' E
                    WA,Carnarvon,-24º 52' 51'' S,113º 39' 34'' E
                    WA,Geraldton,-28º 46' 45'' S,114º 36' 52'' E
                    WA,Perth,-31º 57' 8'' S,115º 51' 41'' E
                    WA,Eyre,-32º 14' 45'' S,126º 18' 15'' E
                    WA,Esperance,-33º 51' 36'' S,121º 52' 57'' E
                    WA,Cape Leeuwin,-34º 22' 36'' S,115º 8' 9'' E
                    WA,Albany,-35º 0' 11'' S,117º 51' 57'' E"""
}
