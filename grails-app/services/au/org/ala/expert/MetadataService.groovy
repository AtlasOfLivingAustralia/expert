package au.org.ala.expert

import au.com.bytecode.opencsv.CSVReader
import grails.converters.JSON
import org.codehaus.groovy.grails.commons.ConfigurationHolder

class MetadataService {

    def webService
    
    static bathomeValues = [
            'coastal/shallow water (0-40m)',
            'shelf (0-200m)',
            'shelf+slope (0-500m)',
            'slope only (200-500m)',
            'any (0-2000+m)']

    static fishGroups = [
            'Australian salmons','billfishes','boarfishes','breams','catfishes','chimaeras','cods', 'dories','eels',
            'emperors','flatfishes','flatheads','garfishes','gemfishes','gurnards & latchets','hagfishes','hakes',
            'herrings & anchovies','jewfishes','lampreys','leatherjackets','morwongs','mullets','oreos','pikes',
            'rays','redfishes','rockcods','roughies','seaperches','sharks','smelts','threadfin breams','trevallas',
            'trevallies','trumpeters','whalefishes','whitings','wrasses']

    def getFishGroups() {
        return [keys: fishGroups, display: fishGroups.collect {it[0].toUpperCase() + it[1..-1]}]
    }

    def getLocalityNames() {
        return localities.keySet().sort()
    }

    def locationOf(String locality) {
        localities[locality]
    }

    def getLocalities2() {
        def list = [
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
        println wkt
        return wkt
    }

    static familiesCache = []
    def getAllFamilies = {
        if (!familiesCache) {
            def all = webService.getJson(ConfigurationHolder.config.spatial.layers.service.url + "/distributions.json")
            familiesCache = all.collect({it.family}).unique().sort()
        }
        return familiesCache
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
        println (localities as JSON).toString()
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
