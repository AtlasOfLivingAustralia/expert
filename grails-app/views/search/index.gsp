<%@ page import="grails.converters.JSON" contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>FishMap search</title>
    <meta name="layout" content="ala2"/>
    <link rel="stylesheet" type="text/css" media="screen" href="${resource(dir:'css',file:'expert.css')}" />
    <link rel="stylesheet" href="${resource(dir:'css/smoothness',file:'jquery-ui-1.8.19.custom.css')}" type="text/css" media="screen"/>
    <script type="text/javascript" src="http://maps.google.com/maps/api/js?sensor=false&libraries=drawing"></script>
    <r:require modules="jquery, jqueryui, application"/>
    <r:layoutResources />
</head>
<body class="search">
<header id="page-header">
    <div class="inner">
        <hgroup>
            <h1 title="fishmap - find Australian marine fishes"></h1>
        </hgroup>
        <nav id="breadcrumb"><ol><li class="last">Search</li></ol></nav>
    </div>
</header>
<div class="inner">
    <section id="search">
        <p class="searchInstructions">Select depth, fish group and location and press the 'Search' button below or
            use the <span style="padding: 0;" class="toggleAdvanced
            link">advanced search</span><span class="sea">&gt;</span>.<br>
            <span id="advWarning"><r:img uri="/images/skin/warning.png" style="padding-right:4px;"/>Some advanced criteria are hidden.</span>
        </p>
        <g:form action="search" class="searchGroup">
            <div class="search-block">
                <label for="bathome" class="mainLabel">Depth</label>
                <g:select name="bathome" from="${bathomeValues}"
                          value="${criteria.bathome ?: 'coastal/shallow water (0-40m)'}"/>
                <div class="advanced top-pad" id="advancedDepthSearch">
                    <span>OR</span>
                    <label style="padding-right: 5px;">Custom depth range (m)</label>
                    Min: <g:textField name="minDepth" class="depthInput" value="${criteria.minDepth}"/>
                    Max: <g:textField name="maxDepth" class="depthInput" value="${criteria.maxDepth}"/>
                    <span id="plusMarker"></span>
                    <div id="depthRangeSlider"></div>
                </div>
            </div>
            <div class="search-block">
                <label for="fishGroup" class="mainLabel">Fish group</label>
                <g:select name="fishGroup" from="${fishGroups.display}" value="${criteria.fishGroup}"
                          keys="${fishGroups.keys}" noSelection="['':'All fishes']"/>
                <div class="advanced top-pad" id="advancedTaxonSearch">
                    <label for="ecosystem">Primary ecosystem</label>
                    <g:select name="ecosystem" from="['estuarine','coastal','demersal','pelagic']"
                              noSelection="['':'any']" style="margin-top:0;"/><br/>
                    <div id="family-widget">
                        <label for="family">Only these families</label>
                        <g:select title="Type a few letters or pick from list." name="family" from="${allFamilies}" noSelection="['':'']"/>
                        <button type="button" id="addFamily">
                            <img alt="add selected family" title="Add selected family to search criteria"
                                 src="${resource(dir:'images/skin',file:'plus_icon.gif')}"/></button>
                    </div>
                    <g:hiddenField name="families"/>
                    <div><ul id="familyList"></ul></div>
                </div>

            </div>
            <div class="search-block">
                <label for="locality" class="mainLabel">Locality</label>
                <select name="locality" id="locality"><option value="">any</option></select>
                <div class="top-pad">
                    <label for="radiusSlider">Distance from locality</label>
                    <div id="radiusSlider"></div><span id="radiusDisplay">50km</span>
                </div>
                <div class="advanced top-pad" id="advancedRegionSearch">
                    <span>OR</span>
                    <label for="imcra">Marine bioregion</label>
                    <select name="imcra" id="imcra">
                        <option value="">any</option>
                        <g:each in="${imcras}" var="ix">
                            <option value="${ix.name}" id="${ix.pid}" ${ix.name == criteria.imcra ? "selected='selected'" : ""}>${ix.name}</option>
                        </g:each>
                    </select>
                    <span>OR use <a href="#">map tools</a> to draw a region.</span>
                </div>
            </div>
            <g:hiddenField name="radius" value="${criteria.radius ?: 50000}"/>
            <g:hiddenField name="wkt" value="${criteria.wkt}"/>
            <g:hiddenField name="circleLat" value="${criteria.circleLat}"/>
            <g:hiddenField name="circleLon" value="${criteria.circleLon}"/>
            <g:hiddenField name="circleRadius" value="${criteria.circleRadius}"/>
            <g:hiddenField name="imcraPid" value="${criteria.imcraPid}"/>
            <fieldset id="submit-buttons" class='one-line'>
                <button type="button" id="searchButton" class="search">Search</button>
                <img src="${resource(dir:'images',file:'spinner.gif')}" id="waiting" style="visibility:hidden"/>
                <button type="button" id="clearButton" class="clear">Clear</button>
            </fieldset>
        </g:form>
        <section id="searchResults" style="display:${summary ? 'block' : 'none'}">
            <h3>Search results</h3>
            <div class="widgets">
                <g:if test="${searchError}">
                    <p class='error'>${searchError}</p>
                </g:if>
                <g:else>
                    <div id="resultsText">
                        <span class="results">Search found <strong id="total">${summary?.total}</strong> species in <strong id="familyCount">${summary?.familyCount}</strong> families.</span>
                        <div id="resultsLinks">
                            <p style="font-weight:bold;padding-bottom:3px;padding-top:3px;">View results by:</p>
                            <a id="familiesLink" href="${grailsApplication.config.explorer.baseUrl}/taxon/view?key=${key}">family list</a>
                            <span class="sea">|</span>
                            <a id="speciesLink" href="${grailsApplication.config.explorer.baseUrl}/taxon/species?key=${key}">species list</a>
                            <span class="sep">|</span>
                            <a id="speciesDataLink" href="${grailsApplication.config.explorer.baseUrl}/taxon/data?key=${key}">species data</a>
                        </div>
                        <p id="queryDescription">For the query: <span id="qDescription">${criteria?.queryDescription}</span>
                            <span style="color:#606060;padding-left:10px;" class="link" id="showQueryToggle">show&nbsp;full&nbsp;query</span>
                        </p>
                        <p id="queryDisplay">(${query})</p>
                    </div>
                </g:else>
            </div>
        </section>
    </section>
    <section id="map">
        <div id="map-wrap">
            <div id="map-canvas"></div>
        </div>%{--map-wrap--}%
        <div id="intro-text">This tool searches against 'expert distributions'. These are maps of the areas
        where a species is expected to be found (as opposed to records of where specimens have actually been
        recorded). The maps are developed by a person or group of people with
        expert knowledge of the domain. The source of these distributions is available in the metadata for each
        map.</div>
        <div id="map-controls" style="display: none">
            <ul id="control-buttons">
                <li class="active" id="pointer">
                    <img src="${resource(dir:'images/map',file:'pointer.png')}" alt="pointer"/>
                    Move & zoom
                </li>
                <li id="circle">
                    <img src="${resource(dir:'images/map',file:'circle.png')}" alt="center and radius"/>
                    Draw circle
                </li>
                <li id="rectangle">
                    <img src="${resource(dir:'images/map',file:'rectangle.png')}" alt="rectangle"/>
                    Draw rect
                </li>
                <li id="polygon">
                    <img src="${resource(dir:'images/map',file:'polygon.png')}" alt="polygon"/>
                    Draw polygon
                </li>
                <li id="clear">
                    <img src="${resource(dir:'images/map',file:'clear.png')}" alt="clear"/>
                    Clear
                </li>
                <li id="reset">
                    <img src="${resource(dir:'images/map',file:'reset.png')}" alt="reset map"/>
                    Reset
                </li>
            </ul>
            <div id="drawnArea">
                <div id="circleArea">
                    Circle<br/>
                    <ul>
                        <li><label for="circLat">Lat:</label><input type="text" id="circLat"/></li>
                        <li><label for="circLon">Lon:</label><input type="text" id="circLon"/></li>
                        <li><label for="circRadius">Radius:</label><input type="text" id="circRadius"/></li>
                    </ul>
                    %{--<img id="circRadiusUndo" src="${resource(dir:'images/map',file:'undo.png')}"
                    style="display:none"/>--}%
                </div>
                <div id="rectangleArea">
                    Rectangle<br/>
                    <ul>
                        <li><label for="swLat">SW Lat:</label><input type="text" id="swLat"/></li>
                        <li><label for="swLon">SW Lon:</label><input type="text" id="swLon"/></li>
                        <li><label for="neLat">NE Lat:</label><input type="text" id="neLat"/></li>
                        <li><label for="neLon">NE Lon:</label><input type="text" id="neLon"/></li>
                    </ul>
                </div>
                <div id="polygonArea">
                    Polygon (lat | lon)<br/>
                    <ul>
                        <li><input type="text" id="lat0"/><input type="text" id="lon0"/></li>
                        <li><input type="text" id="lat1"/><input type="text" id="lon1"/></li>
                        <li><input type="text" id="lat2"/><input type="text" id="lon2"/></li>
                    </ul>
                </div>
            </div>
        </div>
    </section>
</div>
<!-- Dialogs -->
<div id="search-confirm" title="Selected family has not been added">
    <p><span class="ui-icon ui-icon-alert" style="float: left; margin: 0 7px 20px 0;"></span>
        You have selected a family but have not added it to the search.<br>
        Press the + button next to the family selector to add it to the search criteria.
    </p>
</div>

<script>

    var serverUrl = "${grailsApplication.config.grails.serverURL}";

    $(document).ready( function () {

        $('#search-confirm').dialog({
            resizable: false,
            modal: true,
            width: 350,
            autoOpen: false,
            buttons: {
                "Search anyway": function() {
                    $(this).dialog( "close" );
                    search(true);
                },
                Cancel: function() {
                    $(this).dialog( "close" );
                }
            }
        });

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
            spatialService: '${grailsApplication.config.spatial.layers.service.url}',
            spatialWms: '${grailsApplication.config.spatial.wms.url}',
            spatialCache: '${grailsApplication.config.spatial.wms.cache.url}',
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
                $queryToggle.html('show&nbsp;full&nbsp;query');
            } else {
                $queryDisplay.css('display','block');
                $queryToggle.html('hide&nbsp;full&nbsp;query');
            }
        });

        // clear button action
        $('.clear').click(function () {
            document.location.href = "${createLink()}";
        });

        // wire simple/advanced toggle and set initial state
        var advanced = window.sessionStorage ? window.sessionStorage.getItem('advancedSearch') : false;
        searchMode.init(advanced);
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
                    /*
                     * Note that the path received from the drawing manager does not end by repeating the starting
                     * point (number coords = number vertices). However the path derived from a WKT does repeat
                     * (num coords = num vertices + 1). So we need to check whether the last coord is the same as the
                     * first and if so ignore it.
                     */
                    var path = shape.getPath(),
                            $lat = null,
                            $ul = $('#polygonArea ul'),
                            realLength = 0,
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
                            $ul.find('li').remove();
                            realLength = path.getLength();
                            if (path.getAt(0).equals(path.getAt(path.length - 1))) {
                                realLength = realLength - 1;
                            }
                            for (i = 0; i < realLength; i++) {
                                // check whether widget exists
                                $lat = $('#lat' + i);
                                if ($lat.length === 0) {
                                    // doesn't so create it
                                    $lat = $('<li><input type="text" id="lat' + i +
                                            '"/><input type="text" id="lon' + i + '"/></li>')
                                            .appendTo($ul);
                                }
                                $('#lat' + i).val(round(path.getAt(i).lat()));
                                $('#lon' + i).val(round(path.getAt(i).lng()));
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