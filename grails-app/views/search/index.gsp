<%@ page import="grails.converters.JSON" contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>FishMap | Atlas of Living Australia</title>
    <meta name="breadcrumb" content="FishMap"/>

    <asset:stylesheet src="jquery"/>

    <script src="https://maps.google.com/maps/api/js?key=${grailsApplication.config.google.apikey}&libraries=drawing"></script>

    <meta name="layout" content="main"/>

    <asset:javascript src="application"/>
    <asset:stylesheet src="application"/>

    <style>
    .custom-combobox {
        position: relative;
        display: inline-block;
    }

    .custom-combobox-toggle {
        position: absolute;
        top: 0;
        bottom: 0;
        margin-left: -1px;
        padding: 0;
    }

    .custom-combobox-input {
        margin: 0;
        height: 22px;
        font-size: 12px;
    }
    </style>

</head>
<body class="search">
<header id="page-header">
    <div class="inner">
        <hgroup>
            <h1 title="fishmap - find Australian marine fishes"></h1>
        </hgroup>

        <h2>Search</h2>
    </div>
</header>

<div class="row">
    <div class="col-md-4" id="search">
        <p class="searchInstructions">Select depth, fish group and location and press the 'Search' button below or
        use the <span style="padding: 0;" class="toggleAdvanced link">advanced search</span><span
                class="sea"></span>.<br>
            <span id="advWarning">
                <asset:image src='skin/warning.png'
                             style="padding-right:4px;"/>Some advanced criteria are hidden.</span>
        </p>
        <g:form class="searchGroup">
            <div class="search-block">
                <g:set var="bathomeTitle" value="Select broad depth category or use advanced search to set custom depth range."/>
                <h4 for="bathome" class="mainLabel " title="${bathomeTitle}">Depth</h4>
                <g:select name="bathome" class="" from="${bathomeValues}" title="${bathomeTitle}"
                          value="${criteria.bathome ?: 'coastal/shallow water (0-40m)'}"/>
                <div class="advanced top-pad " id="advancedDepthSearch"
                     title="Set custom depth range using the sliders or the value boxes.">
                    <span>OR</span>
                    <label style="padding-right: 5px;">Custom depth range (m)</label>
                    Min: <g:textField name="minDepth" class="depthInput" value="${criteria.minDepth}"/>
                    Max: <g:textField name="maxDepth" class="depthInput" value="${criteria.maxDepth}"/>
                    <span id="plusMarker"></span>
                    <div id="depthRangeSlider"></div>
                </div>
            </div>
            <div class="search-block">
                <g:set var="groupTitle" value="Commonly recognisable fish ‘groupings’ are included. To search for a specific family or multiple families click on advanced search link above and select from ‘only these families’ pull-down menu, then click ‘+’ button."/>
                <h4 for="fishGroup" class="mainLabel " title="${groupTitle}">Fish group</h4>
                <g:select name="fishGroup" from="${fishGroups.display}" value="${criteria.fishGroup}" class=""
                          keys="${fishGroups.keys}" noSelection="['':'All fishes']" title="${groupTitle}"/>
                <div class="advanced top-pad" id="advancedTaxonSearch">
                    <g:set var="ecosystemTitle" value="Restrict search to the selected ecosystem or choose ‘any’ to include all options."/>
                    <label for="ecosystem" title="${ecosystemTitle}" class="">Primary ecosystem</label>
                    <g:select name="ecosystem" from="['estuarine', 'coastal', 'demersal', 'pelagic']" class=""
                              noSelection="['':'any']" style="margin-top:0;" title="${ecosystemTitle}"/><br/>

                    <div id="family-widget" class="top-pad"
                         title="Restrict the search to specified families. Type a few letters or select from pulldown menu. Select any number of families but ensure you click the + button after selecting each family to add it to the search list. Remove a family from a completed search by clicking the red X or click the ‘clear’ button below to remove all criteria.">
                        <label for="family">Only these families</label>
                        <g:select title="Type a few letters or pick from list." name="family" class=""
                                  from="${allFamilies}" noSelection="['': '']"/>
                        <button type="button" id="addFamily">
                            <asset:image alt="add selected family" title="Add selected family to search criteria"
                                         src='skin/plus_icon.gif'/></button>
                    </div>

                    <div class="top-pad">
                        <label for="endemic" class="advanced">
                            Only include <abbr class=""
                                               title="Species is not found in any other areas">Endemic</abbr> species
                        </label>
                        <g:checkBox name="endemic" id="endemic" value="${params.endemic}"/>
                    </div>
                    <g:hiddenField name="families"/>
                    <div><ul id="familyList"></ul></div>
                </div>

            </div>
            <div class="search-block">
                <g:set var="localityTitle" value="If area of interest is not listed, use map tools in advanced search to draw region of interest."/>
                <h4 for="locality" class="mainLabel " title="${localityTitle}">Locality</h4>
                <select name="locality" id="locality" class="" title="${localityTitle}"><option value="">any</option>
                </select>

                <div class="top-pad "
                     title="Choose the radius of the area around the selected locality. You can only adjust slider bar if a locality is selected.">
                    <label for="radiusSlider">Distance from locality</label>
                    <div id="radiusSlider"></div><span id="radiusDisplay">50km</span>
                </div>
                <div class="advanced top-pad" id="advancedRegionSearch">
                    <span>OR</span>
                    <!-- Added by Alan on for fetching multiple layers on 30/07/2014 --- START -->
                    <select name="myLayer" id="myLayer" class="" title="Select a layer">
                        <g:each in="${myLayer}" var="ix">
                            <option value="${ix.pid}" id="${ix.pid}" ${ix.name == criteria.imcra ? "selected='selected'" : ""}>${ix.name}</option>
                        </g:each>
                        <!--
                        <option value="cl21">Marine bioregion</option>
                        <option value="cl1050">2014 CAPAD bioregion</option>
                        -->
                    </select>
                    <span></span>
                    <!-- Added by Alan Lin --- END -->
                    <g:set var="imcraTitle" value="Restrict your search to the following bioregion selected from the list or choose ‘any’ to include all areas. Bioregion will appear on map when selected."/>
                    <select name="imcra" id="imcra" class="" title="${imcraTitle}">
                        <option value="">any</option>
                        <g:each in="${imcras}" var="ix">
                            <option value="${ix.name}" id="${ix.pid}" ${ix.name == criteria.imcra ? "selected='selected'" : ""}>${ix.name}</option>
                        </g:each>
                    </select>
                </div>

                <div class="advanced top-pad" id="advancedRegionSearch"></div>

                <div class="advanced top-pad" id="advancedRegionSearch">
                    <span class="top-pad">OR use the tools below the map to draw a region.</span>
                </div>
            </div>
            <g:hiddenField name="radius" value="${criteria.radius ?: 50000}"/>
            <g:hiddenField name="wkt" value="${criteria.wkt}"/>
            <g:hiddenField name="circleLat" value="${criteria.circleLat}"/>
            <g:hiddenField name="circleLon" value="${criteria.circleLon}"/>
            <g:hiddenField name="circleRadius" value="${criteria.circleRadius}"/>
            <g:hiddenField name="imcraPid" value="${criteria.imcraPid}"/>
            <g:hiddenField name="myLayerPid" value="${criteria.myLayerPid}"/>
            <fieldset id="submit-buttons" class='one-line'>
                <button type="button" id="searchButton" class="search btn btn-primary"
                        title="Search using the selected criteria.">Search</button>
                <asset:image src='spinner.gif' id="waiting" style="visibility:hidden"/>
                <button type="button" id="clearButton" class="clear btn btn-primary"
                        title="Clear all search criteria and results.">Clear</button>
            </fieldset>
        </g:form>
        <section id="searchResults" class="well" style="display:${summary ? 'block' : 'none'}">
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
                            <a id="familiesLink"
                               href="${grailsApplication.config.grails.serverURL}/taxon/view?key=${key}">family list</a>
                            <span class="sea">|</span>
                            <a id="speciesLink"
                               href="${grailsApplication.config.grails.serverURL}/taxon/species?key=${key}">species list</a>
                            <span class="sep">|</span>
                            <a id="speciesDataLink"
                               href="${grailsApplication.config.grails.serverURL}/taxon/data?key=${key}">species data</a>
                        </div>

                        <p id="queryDescription" class="top-pad">For the query: <span
                                id="qDescription">${criteria?.queryDescription}</span>
                            <a href="#" style="padding-left:10px;" class="link"
                               id="showQueryToggle">show&nbsp;full&nbsp;query</a>
                        </p>

                        <p id="queryDisplay" class="top-pad" style="display:none;">(${query})</p>
                    </div>
                </g:else>
            </div>
        </section>
    </div>

    <div class="col-md-8" id="map">
        <div id="map-wrap">
            <div id="map-canvas"></div>
        </div>%{--map-wrap--}%
        <div id="intro-text">This tool searches ‘compiled distributions' for marine fishes inhabiting Australia’s
    continental shelf and slope waters. These are maps of the areas where a species may be expected to be found
    (rather than searching only collection or observation records which have false absences, and may contain
    identifications that are out of date). The maps are developed by a person or persons with expert knowledge
    of the group. Read more <g:link target="_maps" action="distributionModelling">here</g:link>.</div>
        <div id="map-controls" style="display: none">
            <ul id="control-buttons">
                <li class="active " id="pointer" title="Drag to move. Double click or use the zoom control to zoom.">
                    <asset:image src='map/pointer.png' alt="pointer"/>
                    Move & zoom
                </li>
                <li id="circle" class=""
                    title="Click at centre and drag the desired radius. Values can be adjusted in the boxes.">
                    <asset:image src='map/circle.png' alt="center and radius"/>
                    Draw circle
                </li>
                <li id="rectangle" class="" title="Click and drag a rectangle.">
                    <asset:image src='map/rectangle.png' alt="rectangle"/>
                    Draw rect
                </li>
                <li id="polygon" class=""
                    title="Click any number of times to draw a polygon. Double click to close the polygon.">
                    <asset:image src='map/polygon.png' alt="polygon"/>
                    Draw polygon
                </li>
                <li id="clear" class="" title="Clear the region from the map.">
                    <asset:image src='map/clear.png' alt="clear"/>
                    Clear
                </li>
                <li id="reset" class="" title="Zoom and centre on Australia.">
                    <asset:image src='map/reset.png' alt="reset map"/>
                    Reset
                </li>
            </ul>
            <div id="drawnArea">
                <div id="circleArea">
                    <h4>Circle</h4>
                    <ul>
                        <li><label for="circLat">Lat:</label><input type="text" id="circLat"/></li>
                        <li><label for="circLon">Lon:</label><input type="text" id="circLon"/></li>
                        <li><label for="circRadius">Radius:</label><input type="text" id="circRadius"/></li>
                    </ul>
                    %{--<img id="circRadiusUndo" src="${resource(dir:'images/map',file:'undo.png')}"
                    style="display:none"/>--}%
                </div>
                <div id="rectangleArea">
                    <h4>Rectangle</h4>
                    <ul>
                        <li><label for="swLat">SW Lat:</label><input type="text" id="swLat"/></li>
                        <li><label for="swLon">SW Lon:</label><input type="text" id="swLon"/></li>
                        <li><label for="neLat">NE Lat:</label><input type="text" id="neLat"/></li>
                        <li><label for="neLon">NE Lon:</label><input type="text" id="neLon"/></li>
                    </ul>
                </div>
                <div id="polygonArea">
                    <h4>Polygon (lat | lon)</h4>
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

        $.widget("custom.combobox", {
            _create: function () {
                this.wrapper = $("<span>")
                    .addClass("custom-combobox")
                    .insertAfter(this.element);

                this.element.hide();
                this._createAutocomplete();
                this._createShowAllButton();
            },

            _createAutocomplete: function () {
                var selected = this.element.children(":selected"),
                    value = selected.val() ? selected.text() : "";

                this.input = $("<input>")
                    .appendTo(this.wrapper)
                    .val(value)
                    .attr("title", "")
                    .addClass("custom-combobox-input ui-widget ui-widget-content ui-state-default ui-corner-left")
                    .autocomplete({
                        delay: 0,
                        minLength: 0,
                        source: $.proxy(this, "_source")
                    })
                    .tooltip({
                        classes: {
                            "ui-tooltip": "ui-state-highlight"
                        }
                    });

                this._on(this.input, {
                    autocompleteselect: function (event, ui) {
                        ui.item.option.selected = true;
                        this._trigger("select", event, {
                            item: ui.item.option
                        });
                    },

                    autocompletechange: "_removeIfInvalid"
                });
            },

            _createShowAllButton: function () {
                var input = this.input,
                    wasOpen = false;

                var a = $("<a>")
                    .attr("tabIndex", -1)
                    .attr("title", "Show All Items")
                    .tooltip()
                    .appendTo(this.wrapper)
                    .button({
                        icons: {
                            primary: "ui-icon-triangle-1-s"
                        },
                        text: false
                    })
                    .removeClass("ui-corner-all")
                    .addClass("combobox-button ui-button ui-widget ui-state-default ui-button-icon-only ui-corner-right ui-button-icon")
                    .on("mousedown", function () {
                        wasOpen = input.autocomplete("widget").is(":visible");
                    })
                    .on("click", function () {
                        input.trigger("focus");

                        // Close if already visible
                        if (wasOpen) {
                            return;
                        }

                        // Pass empty string as value to search for, displaying all results
                        input.autocomplete("search", "");
                    });

                $('<span>').addClass('ui-button-icon-primary ui-icon ui-icon-triangle-1-s')
                    .appendTo(a);
            },

            _source: function (request, response) {
                var matcher = new RegExp($.ui.autocomplete.escapeRegex(request.term), "i");
                response(this.element.children("option").map(function () {
                    var text = $(this).text();
                    if (this.value && ( !request.term || matcher.test(text) ))
                        return {
                            label: text,
                            value: text,
                            option: this
                        };
                }));
            },

            _removeIfInvalid: function (event, ui) {

                // Selected an item, nothing to do
                if (ui.item) {
                    return;
                }

                // Search for a match (case-insensitive)
                var value = this.input.val(),
                    valueLowerCase = value.toLowerCase(),
                    valid = false;
                this.element.children("option").each(function () {
                    if ($(this).text().toLowerCase() === valueLowerCase) {
                        this.selected = valid = true;
                        return false;
                    }
                });

                // Found a match, nothing to do
                if (valid) {
                    return;
                }

                // Remove invalid value
                this.input
                    .val("")
                    .attr("title", value + " didn't match any item")
                    .tooltip("open");
                this.element.val("");
                this._delay(function () {
                    this.input.tooltip("close").attr("title", "");
                }, 2500);
                this.input.autocomplete("instance").term = "";
            },

            _destroy: function () {
                this.wrapper.remove();
                this.element.show();
            }
        });

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
        locationWidgets.init(50000, ${raw(tv.toObjectLiteral(obj:localities))});

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
            document.location.href = "${createLink()}";
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

        // wire simple/advanced toggle and set initial state
        var advanced = window.sessionStorage ? window.sessionStorage.getItem('advancedSearch') : false;
        searchMode.init(advanced);

        //$('.').ster({maxWidth: 450, delay: 2000});
    });

    function clearData() {
        $('#drawnArea > div').css('display','none');
        $('#drawnArea input').val("");
        $('#wkt').val("");
        $('#circleLat').val("");
        $('#circleLon').val("");
        $('#circleRadius').val("");
        $('#imcraPid').val("");
        $('#myLayerPid').val("");
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