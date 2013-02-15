<!DOCTYPE html>
<html>
<head>
    <title>Expert distribution search</title>
    <meta name="layout" content="ala2"/>
    <link rel="stylesheet" type="text/css" media="screen" href="${resource(dir:'css',file:'expert.css')}" />
    <g:javascript library="expert"/>
    <script type="text/javascript" src="http://maps.google.com/maps/api/js?sensor=false&libraries=drawing"></script>
    <g:javascript library="selection-map"/>
</head>
<body>
<header id="page-header">
    <div class="inner">
        <nav id="breadcrumb"><ol><li><a href="${grailsApplication.config.ala.baseUrl}">Home</a></li> <li class="last">Expert distribution search</li></ol></nav>
        <hgroup>
            <h1>ALA Fish Finder - Spatial Search</h1>
            <h2>Test drawing polygons with Google maps</h2>
        </hgroup>
    </div>
</header>
<section>
    <div id="map-wrap">
        <div id="map-canvas"></div>
        <div id="map-controls">
            <ul>
                <li class="active">
                    <img src="${resource(dir:'images/map',file:'pointer.jpeg')}" alt="pointer" id="pointer"/>
                    Move and zoom map
                </li>
                <li>
                    <img src="${resource(dir:'images/map',file:'centre-and-radius.png')}" alt="center and radius" id="circle"/>
                    Draw a circle
                </li>
                <li>
                    <img src="${resource(dir:'images/map',file:'rectangle.png')}" alt="rectangle" id="rectangle"/>
                    Draw a rectangle
                </li>
                <li>
                    <img src="${resource(dir:'images/map',file:'polygon.png')}" alt="polygon" id="polygon"/>
                    Draw a polygon
                </li>
                <li>
                    <img src="${resource(dir:'images/map',file:'clear.jpeg')}" alt="polygon" id="clear"/>
                    Clear the areas
                </li>
            </ul>
            <div id="drawnArea">
                <div id="circleArea">
                    Circle<br/>
                    <label for="circleLat">Lat:</label><input type="text" id="circleLat"/><br/>
                    <label for="circleLon">Lon:</label><input type="text" id="circleLon"/><br/>
                    <label for="circleRadius">Radius:</label><input type="text" id="circleRadius"/>
                </div>
                <div id="rectangleArea">
                    Rectangle<br/>
                    <label for="swLat">SW Lat:</label><input type="text" id="swLat"/><br/>
                    <label for="swLon">SW Lon:</label><input type="text" id="swLon"/><br/>
                    <label for="neLat">NE Lat:</label><input type="text" id="neLat"/><br/>
                    <label for="neLon">NE Lon:</label><input type="text" id="neLon"/><br/>
                </div>
                <div id="polygonArea">
                    Polygon<br/>
                    <label for="lat0">SW Lat:</label><input type="text" id="lat0"/><br/>
                    <label for="lon0">SW Lon:</label><input type="text" id="lon0"/><br/>
                    <label for="lat1">SW Lat:</label><input type="text" id="lat1"/><br/>
                    <label for="lon1">SW Lon:</label><input type="text" id="lon1"/><br/>
                    <label for="lat2">SW Lat:</label><input type="text" id="lat2"/><br/>
                    <label for="lon2">SW Lon:</label><input type="text" id="lon2"/><br/>
                    <label for="lat3">SW Lat:</label><input type="text" id="lat3"/><br/>
                    <label for="lon3">SW Lon:</label><input type="text" id="lon3"/><br/>
                    <label for="lat4">SW Lat:</label><input type="text" id="lat4"/><br/>
                    <label for="lon4">SW Lon:</label><input type="text" id="lon4"/><br/>
                </div>
            </div>
        </div>
    </div>
</section>
<script type="text/javascript">
    $(document).ready(function() {
        init_map({
            server: '${grailsApplication.config.grails.serverURL}',
            spatialService: '${grailsApplication.config.spatial.layers.service.url}',
            spatialWms: '${grailsApplication.config.spatial.wms.url}',
            spatialCache: '${grailsApplication.config.spatial.wms.cache.url}',
            mapContainer: 'map-canvas'
        });
    });
</script>
</body>
</html>