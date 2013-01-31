/**
 * User: markew
 * Date: 13/02/12
 * Time: 3:12 PM
 *
 * This handles widget interaction for the expert distribution search page.
 *
 * Map and map controls are handled in selection-map.js
 */

// Handles the custom depth widgets -----------------------------------------------------------------------------------
var customDepth = {
    $minDepth: null,
    $maxDepth: null,
    $slider: null,
    $plus: null,
    $bathome: null,
    bathomeValue: function () {
        return this.$bathome.val();
    },
    init: function () {
        var that = this;

        // cache jquery objects after the page is rendered
        this.$minDepth = $('#minDepth');
        this.$maxDepth = $('#maxDepth');
        this.$slider = $('#depthRangeSlider');
        this.$plus = $('#plusMarker');
        this.$bathome = $('#bathome');

        // setup slider
        this.$slider.slider({
            range: true,
            min: 0,
            max: 2000,
            values: [0, 40],
            step: 10,
            slide: function (event, ui) { that.sliderChange(event, ui); }
        });

        // set bathome value based on isCustom
        this.updateBathomeList(false);

        // note that bathome may be null when we come from back button history if the value was custom
        var initialBathome = this.$bathome.val();
        if (initialBathome === null) {
            // fire input change to pick up browser-restored depth values
            this.inputChange();
            // set plus marker
            this.$plus.html(this.$maxDepth.val() == 2000 ? '+' : '');
        } else {
            this.setCustomToBathome();
        }

        // bind listeners
        $('#minDepth, #maxDepth').on('change', function () { that.inputChange() });
        this.$bathome.on('change', function () { that.bathomeChange() });
    },
    updateBathomeList: function (isCustom) {
        var $lastOption = this.$bathome.find('option:last-child'),
            customPresent = $lastOption.attr('value') === "custom depth range";
        if (isCustom) {
            if (!customPresent) {
                this.$bathome.append($('<option value="custom depth range">custom depth range</option>'));
            }
            this.$bathome.val("custom depth range");
        } else {
            if (customPresent) {
                $lastOption.remove();
            }
        }
    },
    inputChange: function () {
        var min = this.$minDepth.val(), max = this.$maxDepth.val();
        if (min > 0 || max > 0 && this.$bathome.val() !== "custom depth range") {
            this.updateBathomeList(true);
        }
        this.$slider.slider('values', [min,max]);
    },
    bathomeChange: function () {
        if (this.$bathome.val() !== "custom depth range") {
            // adjust option list
            this.updateBathomeList(false);
            // update custom depth display
            this.setCustomToBathome();
        }
    },
    sliderChange: function (event, ui) {
        // update input fields
        this.setMinDepth(ui.values[0]);
        this.setMaxDepth(ui.values[1]);
        this.updateBathomeList(true);
    },
    setMinDepth: function(m) {
        this.$minDepth.val(m);
    },
    setMaxDepth: function(m) {
        this.$maxDepth.val(m);
        // set plus marker
        this.$plus.html(m == 2000 ? '+' : '');
    },
    // set custom widgets to reflect selected bathome
    setCustomToBathome: function () {
        var matcher = this.$bathome.val().match(/(\d+)-(\d+)/);
        this.setMinDepth(matcher[1]);
        this.setMaxDepth(matcher[2]);
        this.$slider.slider('values', [matcher[1],matcher[2]]);
    },
    setFromQuery: function (minDepth, maxDepth) {
        var $bathome = $('#bathome'),
                depth = "" + minDepth + "-" + maxDepth;
        switch (depth) {
            case "undefined-40":
                $bathome.val('coastal/shallow water (0-40m)'); this.bathomeChange(); break;
            case "undefined-200":
                $bathome.val('shelf (0-200m)'); this.bathomeChange(); break;
            case "undefined-500":
                $bathome.val('shelf+slope (0-500m)'); this.bathomeChange(); break;
            case "200-500":
                $bathome.val('slope only (200-500m)'); this.bathomeChange(); break;
            case "undefined-undefined":
                $bathome.val('any (0-2000+m)'); this.bathomeChange(); break;
            default:
                $bathome.val('custom depth range');
                this.setMinDepth(minDepth);
                this.setMaxDepth(maxDepth);
                this.inputChange();
        }
    },
    // returns true if advanced search criteria exist
    isAdvanced: function () {
        return this.$bathome.val() === "custom depth range";
    }
};

// handles interaction between locality widgets -----------------------------------------------------------------------
var locationWidgets = {
    $locality: null,
    $imcra: null,
    $slider: null,
    init: function (initialRadius, localities) {
        var that = this,
            str = "";

        // initialise cached jquery objects after the page is rendered
        this.$locality = $('#locality');
        this.$imcra = $('#imcra');
        this.$slider = $('#radiusSlider');

        // init locations list
        $.each(localities, function (i,state) {
            str += "<optgroup class='category' label='-- " + state.state + "'/>";
            $.each(state.localities, function (j, obj) {
                str += '<option class="option" value="' + obj.name + '|' + obj.lat + '|' + obj.lng + '">' +
                        obj.name + '</option>';
            });
        });
        this.$locality.append(str);

        // bind listeners
        this.$locality.on('change', function () { that.localityChange(); });
        this.$imcra.on('change', function () { that.imcraChange(); });

        // initialise slider for locality radius
        this.$slider.slider({
            min: 25000,
            max: 100000,
            value: initialRadius,
            step: 5000,
            slide: function (event, ui) {
                // update input field
                $('#radius').val(ui.value);
                // show radius
                that.showRadius(ui.value);
                // change radius on map
                if (that.getLocality() !== null) {
                    updateMap('radius', ui.value);
                }
            }
        });
        // show initial radius
        $('#radiusDisplay').html(initialRadius/1000 + "km");
    },
    localityChange: function () {
        var loc = this.getLocality();
        clearMap();
        clearData();
        if (loc !== null) {
            this.$imcra.val('');
            // display on map
            showOnMap('locality', loc.lat, loc.lng, this.getRadius());
        }
    },
    imcraChange: function () {
        if (this.$imcra.val()) {
            this.$locality.val('');
            clearData();
            var selectedId = this.$imcra.find('option[value="' + this.$imcra.val() + '"]').attr('id');
            showOnMap('wktFromPid', selectedId);//selected.id);
            $('#imcraPid').val(selectedId);
        } else {
            // has been set back to 'any'
            clearMap();
            clearData();
            clearSessionData('imcra');
        }
    },
    getLocality: function () {
        return this.parseLocality($('#locality').val());
    },
    getRadius: function () {
        return this.$slider.slider('value'); // convert to meters
    },
    setRadius: function (meters) {
        this.$slider.slider('value',meters);
        this.showRadius(meters);
    },
    showRadius: function (meters) {
        $('#radiusDisplay').html(meters/1000 + "km");
    },
    parseLocality: function (locality) {
        if (locality) {
            var nameLatLng = locality.split('|');
            return {name: nameLatLng[0], lat: nameLatLng[1], lng: nameLatLng[2]}
        }
        return null;
    },
    // clears all location widgets but not user-drawn shapes
    clear: function () {
        this.$locality.val('');
        this.$imcra.val('');
    },
    hasImcra: function () {
        return this.$imcra.val() !== '';
    },
    // returns true if advanced search criteria exist
    isAdvanced: function () {
        return this.hasImcra();
    }

};

// handles searching for specific families ----------------------------------------------------
var familyWidget = {
    $list: null,
    $fishGroup: null,
    init: function () {
        var that = this;

        // set the family picker as a combobox
        $("#family").combobox();

        // cache the display list
        this.$list = $('#familyList');
        // cache the fish groups widget
        this.$fishGroup = $('#fishGroup');

        // bind add button
        $('#addFamily').click(function () {
            var fam = $('#family-widget input').val();
            //todo: clear list & hidden
            if (fam && $('#familyList li:contains("' + fam + '")').length === 0) {
                that.$list.append('<li id="' + fam + '">' + fam + '<span class="delete">d</span></li>');
                that.setHiddenValue();
                that.setTypeState();
                $('#family-widget input').val('');
            }
        });

        // bind remove buttons
        $('#familyList').on('click', 'span.delete', function () {
            $(this).parent().remove();
            that.setHiddenValue();
            that.setTypeState();
        });
    },
    clear: function () {
        this.$list.empty();
    },
    setHiddenValue: function () {
        var fams = [];
        $.each(this.$list.find('li'), function(i,obj) {
            fams.push(obj.id);
        });
        $('#families').val(fams.join());
    },
    setList: function (stringList) {
        var list = stringList.split(','),
            i = 0, max;
        for (i = 0, max = list.length; i < max; i++) {
            this.$list.append('<li id="' + list[i] + '">' + list[i] + '<span class="delete">d</span></li>');
        }
        this.setHiddenValue();
        this.setTypeState();
        $('#family-widget input').val('');
    },
    // controls the value and enablement of the fish types widget to reflect the state of the families list.
    // if any families are selected, type must be 'all' and disabled
    setTypeState: function () {
        if (this.$list.find('li').length > 0) {
            this.$fishGroup.val('All fishes');
            this.$fishGroup.attr('disabled','disabled');
        }
        else {
            this.$fishGroup.removeAttr('disabled');
        }
    }
};

// handles switching between simple and advanced views ----------------------------------------
var searchMode = {
    $toggleAdvanced: null,
    $advancedContent: null,
    $intro: null,
    $mapControls: null,
    $taxonSearch: null,
    isAdvanced: function () {
        return $('span.toggleAdvanced').html() === "simple search"
    },
    hasAdvancedCriteria: function () {
        if ($('#ecosystem').val() !== "") {
            return true;
        }
        if ($('#families').val() !== "") {
            return true;
        }
        if ($('#imcra').val() !== "") {
            return true;
        }
        return false;
    },
    init: function (isAdvanced) {
        var that = this;
        this.$toggleAdvanced = $('span.toggleAdvanced');
        this.$advancedContent = $('div.advanced');
        this.$intro = $('#intro-text');
        this.$mapControls = $('#map-controls');
        this.$taxonSearch = $('#taxonSearch');
        this.$toggleAdvanced.click(function () {
            that.toggle();
        });
        if (isAdvanced) {
            this.toggle();
        }
        this.flagHiddenCriteria();
    },
    set: function (advanced) {
        if (advanced) {
            this.setAdvanced();
        } else {
            this.setSimple();
        }
    },
    flagHiddenCriteria: function () {
        if (!searchMode.isAdvanced() && searchMode.hasAdvancedCriteria()) {
            $('#advWarning').show();
        } else {
            $('#advWarning').hide();
        }
    },
    setSimple: function(headless) {
        var that = this;
        // set up for simple search
        this.$mapControls.slideUp(350, function () {
            that.$intro.slideDown(350);
        });
        this.$advancedContent.slideToggle(350);
        this.$taxonSearch.css('min-height', 0);
        this.$toggleAdvanced.html('advanced search'); // this changes all links
        if (window.sessionStorage) {
            window.sessionStorage.removeItem('advancedSearch');
        }
        // check whether we might hide active criteria
        this.flagHiddenCriteria();
    },
    setAdvanced: function() {
        var that = this;
        // swap intro text for map controls
        this.$intro.slideUp(350, function () {
            that.$mapControls.slideDown(350);
        });
        // set up for advanced search
        this.$advancedContent.slideToggle(350, function () {
            that.$taxonSearch.css('min-height', '113px');
        });
        this.$toggleAdvanced.html('simple search'); // this changes all links (not just 'this')
        if (window.sessionStorage) {
            window.sessionStorage.setItem('advancedSearch', true);
        }
        this.flagHiddenCriteria();
    },
    toggle: function (headless) {
        if (this.$toggleAdvanced.html() === "advanced search") {
            // in simple mode
            this.setAdvanced();
        } else {
            // in adv mode
            this.setSimple(headless)
        }
    }
};

// handles searching --------------------------------------------------------------------------
var searchInProgress = false;

// gatherers criteria and preforms a search (asynchronously)
// param headless if true will ignore interactive warnings
function search(headless) {

    var start;

    // check whether the user has selected a family but not added it to the search
    if (searchMode.isAdvanced() && !headless && $('#family-widget input.ui-autocomplete-input').val() !== "") {
        $('#search-confirm').dialog("open");
        return;
    }

    // don't allow concurrent searches
    if (searchInProgress) {
        return;
    }
    // show spinner
    $('#waiting').css('visibility','');
    searchInProgress = true;

    // time the search - note this is not highly accurate
    start = new Date().getTime();

    // do search
    $.post(serverUrl + "/search/ajaxSearch", $('form').serialize(), function (data) {

        searchInProgress = false;

        data.elapsedSearchTime = new Date().getTime() - start;

        // hide spinner
        $('#waiting').css('visibility','hidden');

        // check for errors
        if (data.error) {
            // show error
            $('#resultsText span.results').html('Search failed due to an error: ' + data.error);
            // hide links
            $('#resultsText a').css('display','none');
            // show results if hidden
            $('#searchResults').slideDown(350);
        }

        // else show results
        else {
            // store some values so we can re-establish them if the user returns to page via back
            if (window.sessionStorage) {
                storePageValues(data);
            }
            setResults(data);
        }

        // emphasise it
        $('#searchResults').animate({borderColor: '#DF4A21'/*'#f2bd99'*//*'#ff7154'*/,
            backgroundColor: '#f2bd99',color:'#DF4A21'}, 1000, function () {
            $('#searchResults').animate({borderColor: '#ccc', backgroundColor: 'white',
                color: '#333A3F'}, 1000);
        });
    });
}

function setResults(data) {
    // set counts
    $('#resultsText span.results').html('Search found <strong>' + data.summary.total +
            '</strong> species in <strong>' + data.summary.familyCount +
            '</strong> families.</span>');
    // create links with the returned key
    $.each($('#resultsText a'), function (i, obj) {
        var href = $(obj).attr('href'),
                toKey = href.indexOf('key=') + 4;
        $(obj).attr('href',href.substr(0,toKey) + data.key);
    });
    // make sure links are visible unless there are no results
    if (data.summary.total == 0) {  // deliberately using coercion here
        $('#resultsLinks').css('display','none');
    } else {
        $('#resultsLinks').css('display','block');
    }
    // set query
    $('#qDescription').html(data.queryDescription);
    $('#queryDisplay').html('(' + wrappable(data.query) + ')<br>' +
        displayElapsedTime(data.elapsedSearchTime, data.cached));
    // show it if hidden
    $('#searchResults').slideDown(350);
}

// inserts spaces into raw query string so the string will wrap on page
function wrappable(query) {
    if (query !== undefined && query !== null) {
        query = query.replace(new RegExp("&","gm"),", ");
    }
    return query;
}

function displayElapsedTime(millis, cached) {
    var quantity = millis,
        units = 'milliseconds';

    if (millis === undefined || millis === null) {
        return '';
    }

    if (millis > 1000) {
        quantity = (millis / 1000).toFixed(1);
        units = 'seconds';
    }
    if (millis > 2 * 1000 * 60) { // 2 minutes
        quantity = (millis / (1000 * 60)).toFixed(1);
        units = 'minutes';
    }

    return 'Search took ' + quantity + ' ' + units + (cached ? ' (cached)' : '');
}

function setPageValues() {
    existingData = {
        summary: {
            familyCount: window.sessionStorage.getItem('familyCount'),
            total: window.sessionStorage.getItem('total')
        },
        key: window.sessionStorage.getItem('key'),
        query: window.sessionStorage.getItem('query'),
        queryDescription: window.sessionStorage.getItem('queryDescription'),
        advancedSearch: window.sessionStorage.getItem('advancedSearch'),
        elapsedSearchTime: window.sessionStorage.getItem('elapsedSearchTime')
    };

    // only do this if there is a query to represent
    if (existingData.query) {
        // re-establish query settings from query string
        var queryParams = $.deparam(existingData.query, true);

        // taxon group
        if (queryParams.groupName) {
            $('#fishGroup').val(queryParams.groupName);
        }

        // depth range
        customDepth.setFromQuery(queryParams.min_depth, queryParams.max_depth);

        // imcra
        if (queryParams.objectName) {
            $('#imcra').val(queryParams.objectName);
            locationWidgets.imcraChange();
        }

        // ecosystem
        $.each(['estuarine','coastal','demersal','pelagic'], function (i,eco) {
            if (queryParams[eco]) {
                $('#ecosystem').val(eco);
            }
        });

        // user-drawn polygon or rectangle
        if (queryParams.wkt) {
            showOnMap('wkt', queryParams.wkt);
        }

        // user-drawn circle
        if (queryParams.lat && queryParams.lon && queryParams.radius) {
            showOnMap('circle', queryParams.lat, queryParams.lon, queryParams.radius);
        }

        // re-establish other page values
        $.each(['locality'], function (i, key) {
            var value = window.sessionStorage.getItem(key);
//console.log(key + " = " + value);
            if (value) {
                $('#' + key).val(value);
            }
        });
        var families = window.sessionStorage.getItem('families');
        if (families) {
            familyWidget.setList(families);
        }
        var radius = window.sessionStorage.getItem('radius');
        if (radius) {
//console.log("radius = " + radius);
            locationWidgets.setRadius(radius);
        }

        setResults(existingData);
    }
}

function storePageValues(data) {
    // these are data returned from the search
    window.sessionStorage.setItem('key',data.key);
    window.sessionStorage.setItem('total',data.summary.total);
    window.sessionStorage.setItem('familyCount',data.summary.familyCount);
    window.sessionStorage.setItem('query',data.query);
    window.sessionStorage.setItem('queryDescription',data.queryDescription);
    window.sessionStorage.setItem('elapsedSearchTime',data.elapsedSearchTime);

    // these are data that may not be available from the search but are required to
    // re-establish the original search
    $.each(['locality','families'], function (i, key) {
        var value = $('#' + key).val();
        if (value === "") {
            // clear any existing value
            window.sessionStorage.removeItem(key);
        } else {
            //console.log("storing " + key + " = " + value);
            window.sessionStorage.setItem(key, value);
        }
    });
    var radius = locationWidgets.getRadius();
    if (radius === 50000) {
        window.sessionStorage.removeItem('radius');
    } else {
        //console.log("storing radius = " + radius);
        window.sessionStorage.setItem('radius',radius);
    }
}

function clearSessionData(key) {
    // preserve simple/advanced state
    var isAdv = window.sessionStorage ? window.sessionStorage.getItem('advancedSearch') : false;
    if (window.sessionStorage) {
        if (key !== undefined) {
            window.sessionStorage.removeItem(key);
        } else {
            window.sessionStorage.clear();
            searchMode.set(isAdv);
        }
    }
}