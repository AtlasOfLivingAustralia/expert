<%@ page import="grails.converters.JSON; org.codehaus.groovy.grails.commons.ConfigurationHolder" contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Expert distribution search</title>
    <meta name="layout" content="ala2"/>
    <link rel="stylesheet" type="text/css" media="screen" href="${resource(dir:'css',file:'expert.css')}" />
    <link rel="stylesheet" href="${resource(dir:'css/smoothness',file:'jquery-ui-1.8.19.custom.css')}" type="text/css" media="screen"/>
    <script type="text/javascript" src="http://maps.google.com/maps/api/js?sensor=false&libraries=drawing"></script>
    <g:javascript library="jquery-ui-1.8.19.custom.min"/>
    <g:javascript library="wms"/>
    <g:javascript library="expert"/>
    <g:javascript library="selection-map"/>
    <g:javascript library="keydragzoom"/>
    <g:javascript library="jquery.ba-bbq.min"/>
    <g:javascript library="combobox"/>
</head>
<body class="fish landing">
<header id="page-header">
    <div class="inner">
        <nav id="breadcrumb"><ol><li><a href="${ConfigurationHolder.config.ala.baseUrl}">Home</a></li> <li class="last">Expert distribution search</li></ol></nav>
        <hgroup>
            <h1>FishMap - search expert distributions for marine fishes</h1>
            <h2>Search for fish which potentially occur in a defined area based on expert distributions
            <a href="">more info</a></h2>
        </hgroup>
    </div>
</header>
<div class="inner">
<section id="search">
    <p class="searchInstructions">Select depth, fish group and location and press the 'Search' button below.
    Use <button type="button" style="padding: 0;" class="toggleAdvanced simpleButton">advanced search</button>.</p>
    <g:form action="search" class="searchGroup">
        <div id="depthTaxonContainer">
            <section id="depthSearch" class="searchGroup">
                <h3><span>Depth</span></h3>
                <div class="widgets">
                    <label for="bathome">Bathome</label>
                    <g:select name="bathome" from="${bathomeValues}"
                              value="${criteria.bathome ?: 'coastal/shallow water (0-40m)'}"/>
                    <div class="advanced" id="advancedDepthSearch">
                        <span>OR</span>
                        <label style="padding-right: 5px;">Custom depth range (m)</label>
                        Min: <g:textField name="minDepth" class="depthInput" value="${criteria.minDepth}"/>
                        Max: <g:textField name="maxDepth" class="depthInput" value="${criteria.maxDepth}"/>
                        <span id="plusMarker"></span>
                        <div id="depthRangeSlider"></div>
                    </div>
                </div>
            </section>
            <section id="taxonSearch" class="searchGroup">
                <h3><span>Fish types</span></h3>
                <div id="taxonLeft">
                    <div class="widgets">
                        <g:select name="fishGroup" from="${fishGroups.display}" value="${criteria.fishGroup}"
                                  keys="${fishGroups.keys}" noSelection="['':'All fishes']"/>
                    </div>
                    <div class="advanced widgets" id="advancedTaxonSearch">
                        <label for="ecosystem">Primary ecosystem</label><br/>
                        <g:select name="ecosystem" from="['estuarine','coastal','demersal','pelagic']"
                                  noSelection="['':'any']"/><br/>
                    </div>
                </div>
                <div id="taxonRight" class="advanced" style="display:none;">
                    <div id="family-widget">
                        <label for="family">Only these families</label>
                        <span class="hint">(Type a few letters or pick from list.)</span><br/>
                        <g:select title="select" name="family" from="${allFamilies}" noSelection="['':'']"/>
                        <button type="button" id="addFamily">
                        <img alt="add selected family" title="Add selected family to search criteria"
                             src="${resource(dir:'images/skin',file:'plus_icon.gif')}"/></button>
                    </div>
                    <g:hiddenField name="families"/>
                    <div><ul id="familyList"></ul></div>
                </div>
            </section>
        </div>
        <section id="regionSearch" class="searchGroup">
            <h3><span>Location</span></h3>
            <div class="widgets">
                <label style="margin-left: 20px;" for="locality">Locality</label>
                <select name="locality" id="locality"><option value="">any</option></select>
                <div style="display: inline-block;">
                    <label for="radiusSlider">Distance from locality</label>
                    <div id="radiusSlider"></div><span id="radiusDisplay">50km</span>
                </div>
                <div class="advanced" id="advancedRegionSearch">
                    <span style="padding-right: 10px;">OR</span>
                    <label for="imcra">Marine bioregion</label>
                    <select name="imcra" id="imcra">
                        <option value>any</option>
                        <g:each in="${imcras}" var="ix">
                            <option value="${ix.name}" id="${ix.pid}" ${ix.name == criteria.imcra ? "selected='selected'" : ""}>${ix.name}</option>
                        </g:each>
                    </select>
                </div>
                <g:hiddenField name="radius" value="${criteria.radius ?: 50000}"/>
                <g:hiddenField name="wkt" value="${criteria.wkt}"/>
                <g:hiddenField name="circleLat" value="${criteria.circleLat}"/>
                <g:hiddenField name="circleLon" value="${criteria.circleLon}"/>
                <g:hiddenField name="circleRadius" value="${criteria.circleRadius}"/>
                <g:hiddenField name="imcraPid" value="${criteria.imcraPid}"/>
            </div>
            <div id="map-wrap">
                <div id="map-canvas"></div>
                <div id="intro-text">This tool searches against 'expert distributions'. These are maps of the areas
                where a species is expected to be found (as opposed to records of where specimens have actually been
                recorded). The maps are developed by a person or group of people with
                expert knowledge of the domain. The source of these distributions is available in the metadata for each
                map. Read more about <a href="">expert distibutions</a>.</div>
                <div id="map-controls" style="display: none">
                    <p><span id="or">OR</span> <strong>draw an area on the map</strong></p>
                    <ul>
                        <li class="active" id="pointer">
                            <img src="${resource(dir:'images/map',file:'pointer.png')}" alt="pointer"/>
                            Move and zoom map
                        </li>
                        <li id="circle">
                            <img src="${resource(dir:'images/map',file:'circle.png')}" alt="center and radius"/>
                            Draw a circle
                        </li>
                        <li id="rectangle">
                            <img src="${resource(dir:'images/map',file:'rectangle.png')}" alt="rectangle"/>
                            Draw a rectangle
                        </li>
                        <li id="polygon">
                            <img src="${resource(dir:'images/map',file:'polygon.png')}" alt="polygon"/>
                            Draw a polygon
                        </li>
                        <li id="clear">
                            <img src="${resource(dir:'images/map',file:'clear.png')}" alt="clear"/>
                            Clear the areas
                        </li>
                        <li id="reset">
                            <img src="${resource(dir:'images/map',file:'reset.png')}" alt="reset map"/>
                            Reset the map
                        </li>
                    </ul>
                    <div id="drawnArea">
                        <div id="circleArea">
                            Circle<br/>
                            <label for="circLat">Lat:</label><input type="text" id="circLat"/><br/>
                            <label for="circLon">Lon:</label><input type="text" id="circLon"/><br/>
                            <label for="circRadius">Radius:</label><input type="text" id="circRadius"/>
                            <img id="circRadiusUndo" src="${resource(dir:'images/map',file:'undo.png')}"
                                 style="display:none"/>
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
                            <input type="text" id="lat0"/><input type="text" id="lon0"/><br/>
                            <input type="text" id="lat1"/><input type="text" id="lon1"/><br/>
                            <input type="text" id="lat2"/><input type="text" id="lon2"/><br/>
                            <input type="text" id="lat3"/><input type="text" id="lon3"/><br/>
                            <input type="text" id="lat4"/><input type="text" id="lon4"/><br/>
                            <input type="text" id="lat5"/><input type="text" id="lon5"/><br/>
                            <input type="text" id="lat6"/><input type="text" id="lon6"/><br/>
                        </div>
                    </div>
                </div>
            </div>%{--map-wrap--}%
            <div style="clear: both"></div>
        </section>
        <fieldset id="submit-buttons" class='one-line'>
            <button type="button" id="searchButton" class="search">Search</button>
            <img src="${resource(dir:'images',file:'spinner.gif')}" id="waiting" style="visibility:hidden"/>
            <button type="button" id="clearButton" class="clear">Clear</button>
            <button type="button" style="padding-left: 40px;" class="toggleAdvanced simpleButton">advanced search</button>
        </fieldset>
    </g:form>
    <section id="searchResults" class="searchGroup" style="display:${summary ? 'block' : 'none'}">
        <h3><span>Search results</span></h3>
        <div class="widgets">
            <g:if test="${searchError}">
                <p class='error'>${searchError}</p>
            </g:if>
            <g:else>
                <div id="resultsText">
                    <span class="results">Search found <strong id="total">${summary?.total}</strong> species in <strong id="familyCount">${summary?.familyCount}</strong> families.</span>
                    <a id="familiesLink" href="${ConfigurationHolder.config.explorer.baseUrl}/taxon/view?key=${key}">View results by family</a>
                    <a id="speciesLink" href="${ConfigurationHolder.config.explorer.baseUrl}/taxon/species?key=${key}">View results by species</a>
                    <a id="speciesDataLink" href="${ConfigurationHolder.config.explorer.baseUrl}/taxon/data?key=${key}">View results as a data table</a>
                    %{--<g:link action='briefList' params="${[key: key]}">View results by species</g:link>--}%
                    <p id="queryDescription">For the query: <span id="qDescription">${criteria?.queryDescription}</span>
                        <button style="color:#606060" type="button" class="simpleButton" id="showQueryToggle">show full query</button></p>
                    <p id="queryDisplay">(${query})</p>
                </div>
            </g:else>
        </div>
    </section>
</section>
</div>
<script type="text/javascript">

    var serverUrl = "${ConfigurationHolder.config.grails.serverURL}";

$(document).ready( function () {

    // init some widgets before we try to set their values from stored state
    // set up depth interactions
    customDepth.init();

    // set up locality interactions
    locationWidgets.init(50000, ${tv.toObjectLiteral(obj:localities)});

    // set up family widget
    familyWidget.init();

    // create map and controls
    init_map({
        server: serverUrl,
        spatialService: '${ConfigurationHolder.config.spatial.layers.service.url}',
        spatialWms: '${ConfigurationHolder.config.spatial.wms.url}',
        spatialCache: '${ConfigurationHolder.config.spatial.wms.cache.url}',
        mapContainer: 'map-canvas'
    });

    // this sets the function to call when the user draws a shape
    setCurrentShapeCallback(shapeDrawn);
    // set search criteria and results from any stored data
    if (window.sessionStorage) {
        setPageValues();
    }

    // wire search button
    $('#searchButton').click( function () {
        search();
    });

    // wire clear button
    $('#clearButton').click( function () {
        clearSessionData();
    });

    // wire show query toggle
    var $queryDisplay = $('#queryDisplay'),
        $queryToggle = $('#showQueryToggle');
    $queryToggle.click(function () {
        if ($queryDisplay.css('display') === 'block') {
            $queryDisplay.css('display','none');
            $queryToggle.html('show full query');
        } else {
            $queryDisplay.css('display','block');
            $queryToggle.html('hide full query');
        }
    });

    // clear button action
    $('.clear').click(function () {
        document.location.href = "${createLink()}";
    });

    // wire simple/advanced toggle and set initial state
    var advanced = window.sessionStorage ? window.sessionStorage.getItem('advancedSearch') : false;
    toggle(serverUrl, advanced);


    // redraw the location for the current query if present
    /*var basedOn = "${criteria.getLocationBasedOn()}";
    switch (basedOn) {
        case 'locality':
            var loc = locationWidgets.parseLocality("${criteria.locality}");
            showOnMap('circle', loc.lat, loc.lng, "${criteria.radius}");
            break;
        case 'circle':
            showOnMap('circle', ${criteria.circleLat ?: '""'}, ${criteria.circleLon ?: '""'}, ${criteria.circleRadius ?: '""'});
            break;
        case 'marine area':
            showOnMap('wktFromPid', ${criteria.imcraPid ?: '""'});
            break;
        case 'wkt':
            showOnMap('wkt', "${criteria.wkt}");
            break;
    }*/

    // redraw based on widget values that are preserved on browser back-button
    var initialImcra = $('#imcra').val();
    if (initialImcra) {
        locationWidgets.imcraChange();
        /*var selectedId = $('#imcra').find('option[value="' + initialImcra + '"]').attr('id');
        showOnMap('wktFromPid', selectedId);*/
    }
    var initialLocality = $('#locality').val();
    if (initialLocality) {
        locationWidgets.localityChange();
        /*var loc = this.getLocality();
        if (loc !== null) {
            showOnMap('locality', loc.lat, loc.lng, this.getRadius());
        }*/
    }

});

function clearData() {
    $('#drawnArea > div').css('display','none');
    $('#drawnArea input').val("");
    $('#wkt').val("");
    $('#circleLat').val("");
    $('#circleLon').val("");
    $('#circleRadius').val("");
    $('#imcraPid').val("");
}

function shapeDrawn(source, type, shape) {
    if (source === 'user-drawn') {
        locationWidgets.clear();
    }
    if (source === 'clear') {
        clearData();
        clearSessionData('drawnShapes');
    } else {
        switch (type) {
            case google.maps.drawing.OverlayType.CIRCLE:
                /*// don't show or set circle props if source is a locality
                if (source === "user-drawn") {*/
                    var center = shape.getCenter();
                    // set coord display
                    $('#circLat').val(round(center.lat()));
                    $('#circLon').val(round(center.lng()));
                    $('#circRadius').val(round(shape.getRadius()/1000,2) + "km");
                    $('#circleArea').css('display','block');
                    // set hidden inputs
                    $('#circleLat').val(center.lat());
                    $('#circleLon').val(center.lng());
                    $('#circleRadius').val(shape.getRadius());
                /*}*/
                break;
            case google.maps.drawing.OverlayType.RECTANGLE:
                var bounds = shape.getBounds(),
                    sw = bounds.getSouthWest(),
                    ne = bounds.getNorthEast();
                // set coord display
                $('#swLat').val(round(sw.lat()));
                $('#swLon').val(round(sw.lng()));
                $('#neLat').val(round(ne.lat()));
                $('#neLon').val(round(ne.lng()));
                $('#rectangleArea').css('display','block');
                // set hidden inputs
                $('#wkt').val(rectToWkt(sw, ne));
                break;
            case google.maps.drawing.OverlayType.POLYGON:
                var path = shape.getPath(),
                    i = 0;
                    isRect = representsRectangle(path);
                if (!locationWidgets.hasImcra()) {
                    // set coord display
                    if (isRect) {
                        $('#swLat').val(round(path.getAt(0).lat()));
                        $('#swLon').val(round(path.getAt(0).lng()));
                        $('#neLat').val(round(path.getAt(2).lat()));
                        $('#neLon').val(round(path.getAt(2).lng()));
                        $('#rectangleArea').css('display','block');
                    } else if (!locationWidgets.hasImcra()) {
                        while (i < 7 && i < path.getLength()) {
                            $('#lat' + i).val(round(path.getAt(i).lat()));
                            $('#lon' + i).val(round(path.getAt(i).lng()));
                            i += 1;
                        }
                        $('#polygonArea').css('display','block');
                    }
                    // set hidden inputs
                    $('#wkt').val(polygonToWkt(path));
                }
                break;
        }
    }
}

function rectToWkt(sw, ne) {
    var swLat = sw.lat(),
        swLng = sw.lng(),
        neLat = ne.lat(),
        neLng = ne.lng(),
        wkt = "POLYGON((";
    wkt += swLng + " " + swLat + ',' +
           swLng + " " + neLat + ',' +
           neLng + " " + neLat + ',' +
           neLng + " " + swLat + ',' +
           swLng + " " + swLat;

    return wkt + "))";
}

function polygonToWkt(path) {
    var wkt = "POLYGON((",
        firstPoint = path.getAt(0),
        points = [];
    path.forEach(function (obj, i) {
        points.push(obj.lng() + " " + obj.lat());
    });
    // a polygon array from the drawingManager will not have a closing point
    // but one that has been drawn from a wkt will have - so only add closing
    // point if the first and last don't match
    if (!firstPoint.equals(path.getAt(path.length -1))) {
        // add first points at end
        points.push(firstPoint.lng() + " " + firstPoint.lat());
    }
    wkt += points.join(',') + "))";
    //console.log(wkt);
    return wkt;
}

function round(number, places) {
    var p = places || 4;
    return places === 0 ? number.toFixed() : number.toFixed(p);
}

function representsRectangle(path) {
    // must have 5 points
    if (path.getLength() !== 5) { return false; }
    var arr = path.getArray();
    if ($.isArray(arr[0])) { return false; }  // must be multipolygon (array of arrays)
    if (arr[0].lng() != arr[1].lng()) { return false; }
    if (arr[2].lng() != arr[3].lng()) { return false; }
    if (arr[0].lat() != arr[3].lat()) { return false; }
    if (arr[1].lat() != arr[2].lat()) { return false; }
    return true
}

</script>
</body>
</html>