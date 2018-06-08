grails.project.groupId = "au.org.ala"

grails.serverURL = 'http://local.ala.org.au:8080'

ENV_NAME = "EXPERT_CONFIG"
appName = 'expert'
grails.config.locations = ["file:/data/${appName}/config/${appName}-config.properties",
                           "file:/data/${appName}/config/${appName}-config.yml",
                           "file:/data/${appName}/config/${appName}-config.groovy"]
if (System.getenv(ENV_NAME) && new File(System.getenv(ENV_NAME)).exists()) {
    println "[EXPERT] Including configuration file specified in environment: " + System.getenv(ENV_NAME);
    grails.config.locations = ["file:" + System.getenv(ENV_NAME)]
} else if (System.getProperty(ENV_NAME) && new File(System.getProperty(ENV_NAME)).exists()) {
    println "[EXPERT] Including configuration file specified on command line: " + System.getProperty(ENV_NAME);
    grails.config.locations = ["file:" + System.getProperty(ENV_NAME)]
} else {
    println "[EXPERT] Including default configuration files, if they exist: " + grails.config.locations
}

/******************************************************************************\
 *  APPLICATION SWITCHES
 \******************************************************************************/
if (!expert.images.useConstructedUrls) {
    expert.images.useConstructedUrls = false
}

grails.cors.enabled = true

/******************************************************************************\
 *  EXTERNAL SERVERS
 \******************************************************************************/
if (!bie.baseURL) {
    bie.baseURL = "http://bie.ala.org.au"
}
if (!bie.services.baseURL) {
    bie.services.baseURL = "http://bie.ala.org.au/ws"
}
/*
if (!bie.searchPath) {
    bie.searchPath = "/search"
}
*/
if (!biocache.baseURL) {
    biocache.baseURL = "http://biocache.ala.org.au"
}
if (!spatial.baseURL) {
    spatial.baseURL = "http://spatial.ala.org.au"
}
if (!ala.baseURL) {
    ala.baseURL = "http://www.ala.org.au"
}
// spatial services
if (!spatial.wms.url) {
    spatial.wms.url = spatial.baseURL + "/geoserver/ALA/wms?"
}
if (!spatial.wms.cache.url) {
    spatial.wms.cache.url = spatial.baseURL + "/geoserver/gwc/service/wms?"
}
if (!spatial.layers.service.url) {
    spatial.layers.service.url = spatial.baseURL + "/layers-service"
}
if (!headerAndFooter.baseURL) {
    headerAndFooter.baseURL = 'https://wpprod2017.ala.org.au/commonui-bs3-v2/commonui-bs3'
}

/******************************************************************************\
 *  APP CONFIG
 \******************************************************************************/
distribution.maps.dataResourceUid = 'dr803'
distribution.image.baseURL = 'http://spatial.ala.org.au/geoserver/ALA/wms?service=WMS&version=1.1.0&request=GetMap&layers=ALA:aus1,ALA:Distributions&styles=&bbox=109,-47,157,-7&srs=EPSG:4326&format=image/png&width=512&height=454&viewparams=s:'
image.source.dataResourceUid = 'dr660'
distribution.image.cache = "/data/expert/images"

/******************************************************************************\
 *  SECURITY
 \******************************************************************************/

ignoreCookie = 'true'
/*
security {
    cas {
        // appServerName is automatically set from grails.serverURL

        uriFilterPattern = '/alaAdmin.*'
        uriExclusionFilterPattern = '/assets/.*,/images/.*,/css/.*,/js/.*,/less/.*'

        //authenticateOnlyIfLoggedInPattern requires authenticateOnlyIfLoggedInPattern to identify 'logged in' when ignoreCookie='true'
        authenticateOnlyIfLoggedInPattern = '.*'
    }
}
*/

headerAndFooter.excludeApplicationJs = true
orgNameLong = 'Atlas of Living Australia'
collections.url = 'http://collections.ala.org.au'
image.ws.url = 'http://images.ala.org.au/ws'


depthMeasures = [
        [
                label: "coastal/shallow water (0-40m)",
                min  : 0,
                max  : 40
        ],
        [
                label: "shelf (0-200m)",
                min  : 0,
                max  : 200
        ],
        [
                label: "shelf + upper slope (0-500m)",
                min  : 0,
                max  : 500
        ],
        [
                label: "upper slope only (200-500m)",
                min  : 200,
                max  : 500
        ]
]

depthMeasuresAll = 'any (0-2000+m)'

localityStates = [
        [label: 'New South Wales', state: 'NSW'],
        [label: 'Northern Territory', state: 'NT'],
        [label: 'Queensland', state: 'Qld'],
        [label: 'South Australia', state: 'SA'],
        [label: 'Tasmania', state: 'Tas'],
        [label: 'Victoria', state: 'Vic'],
        [label: 'Western Australia', state: 'WA']
]

localities = [
        "Albany"                : ["state": "WA", "name": "Albany", "lat": -35.0030, "lng": 117.8658],
        "Bateman's Bay"         : ["state": "NSW", "name": "Bateman's Bay", "lat": -35.7316, "lng": 150.2177],
        "Brisbane"              : ["state": "Qld", "name": "Brisbane", "lat": -27.4711, "lng": 153.0241],
        "Broome"                : ["state": "WA", "name": "Broome", "lat": -17.9616, "lng": 122.2363],
        "Cairns"                : ["state": "Qld", "name": "Cairns", "lat": -16.9230, "lng": 145.7663],
        "Cape Jaffa"            : ["state": "SA", "name": "Cape Jaffa", "lat": -36.9533, "lng": 139.6733],
        "Cape Leeuwin"          : ["state": "WA", "name": "Cape Leeuwin", "lat": -34.3766, "lng": 115.1358],
        "Cape Londonderry"      : ["state": "WA", "name": "Cape Londonderry", "lat": -13.7411, "lng": 126.9644],
        "Carnarvon"             : ["state": "WA", "name": "Carnarvon", "lat": -24.8808, "lng": 113.6594],
        "Coffs Harbour"         : ["state": "NSW", "name": "Coffs Harbour", "lat": -30.2963, "lng": 153.1136],
        "Darwin"                : ["state": "NT", "name": "Darwin", "lat": -12.4572, "lng": 130.8366],
        "Devonport"             : ["state": "Tas", "name": "Devonport", "lat": -41.1769, "lng": 146.3513],
        "Eden"                  : ["state": "NSW", "name": "Eden", "lat": -37.0633, "lng": 149.9038],
        "Esperance"             : ["state": "WA", "name": "Esperance", "lat": -33.86, "lng": 121.8825],
        "Exmouth"               : ["state": "WA", "name": "Exmouth", "lat": -21.9402, "lng": 114.125],
        "Eyre"                  : ["state": "WA", "name": "Eyre", "lat": -32.2458, "lng": 126.3041],
        "Fraser Island"         : ["state": "Qld", "name": "Fraser Island", "lat": -25.2502, "lng": 153.1669],
        "Geelong"               : ["state": "Vic", "name": "Geelong", "lat": -38.1533, "lng": 144.3580],
        "Geraldton"             : ["state": "WA", "name": "Geraldton", "lat": -28.7791, "lng": 114.6144],
        "Gladstone"             : ["state": "Qld", "name": "Gladstone", "lat": -23.8475, "lng": 151.2563],
        "Groote Eylandt"        : ["state": "NT", "name": "Groote Eylandt", "lat": -13.9330, "lng": 136.6],
        "Hobart"                : ["state": "Tas", "name": "Hobart", "lat": -42.8830, "lng": 147.3316],
        "Kangaroo Island"       : ["state": "SA", "name": "Kangaroo Island", "lat": -35.8116, "lng": 137.2052],
        "Karumba"               : ["state": "Qld", "name": "Karumba", "lat": -17.4888, "lng": 140.8383],
        "Lakes Entrance"        : ["state": "Vic", "name": "Lakes Entrance", "lat": -37.8766, "lng": 147.9941],
        "Mackay"                : ["state": "Qld", "name": "Mackay", "lat": -21.1533, "lng": 149.1655],
        "Newcastle"             : ["state": "NSW", "name": "Newcastle", "lat": -32.9316, "lng": 151.7847],
        "Perth"                 : ["state": "WA", "name": "Perth", "lat": -31.9522, "lng": 115.8613],
        "Port Hedland"          : ["state": "WA", "name": "Port Hedland", "lat": -20.3122, "lng": 118.6105],
        "Port Lincoln"          : ["state": "SA", "name": "Port Lincoln", "lat": -34.7263, "lng": 135.8744],
        "Port Macquarie"        : ["state": "NSW", "name": "Port Macquarie", "lat": -31.4316, "lng": 152.9177],
        "Princess Charlotte Bay": ["state": "Qld", "name": "Princess Charlotte Bay", "lat": -14.2169, "lng": 143.9680],
        "South East Cape"       : ["state": "Tas", "name": "South East Cape", "lat": -43.64, "lng": 146.82],
        "St Marys"              : ["state": "Tas", "name": "St Marys", "lat": -41.5791, "lng": 148.1872],
        "Strahan"               : ["state": "Tas", "name": "Strahan", "lat": -42.1519, "lng": 145.3272],
        "Sydney"                : ["state": "NSW", "name": "Sydney", "lat": -33.8652, "lng": 151.2097],
        "Thursday Island"       : ["state": "Qld", "name": "Thursday Island", "lat": -10.5808, "lng": 142.2197],
        "Townsville"            : ["state": "Qld", "name": "Townsville", "lat": -19.2663, "lng": 146.8055],
        "Warrnambool"           : ["state": "Vic", "name": "Warrnambool", "lat": -38.3816, "lng": 142.4880],
        "Weipa"                 : ["state": "Qld", "name": "Weipa", "lat": -12.25, "lng": 142.05],
        "Wessel Islands"        : ["state": "NT", "name": "Wessel Islands", "lat": -11.6044, "lng": 136.3288],
        "Wilsons Promontory"    : ["state": "Vic", "name": "Wilsons Promontory", "lat": -39.0480, "lng": 146.3886]
]

imcras = [
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

marine = [
        [name: "Marine bioregion", pid: "cl21"],
        //[name: "2014 CAPAD bioregion", pid: "cl1050"]
        [name: "SE CMR Network", pid: "cl1051"]
]

capad = [
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

grails.cache.enabled = true
grails.cache.config = {
    provider {
        updateCheck false
    }

    cache {
        maxEntriesLocalHeap 0
        name 'metadata'
        eternal false
        overflowToDisk false
        maxElementsOnDisk 0
    }

    defaultCache {
        maxEntriesLocalHeap 0
        eternal false
        overflowToDisk false
        maxElementsOnDisk 0
    }

    defaults {
        maxEntriesLocalHeap 0
        eternal false
        overflowToDisk false
        maxElementsOnDisk 0
    }
}