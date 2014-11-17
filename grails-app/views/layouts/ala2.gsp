<!DOCTYPE html>
<html>
<head>
    <title><g:layoutTitle default="Grails" /></title>
    <meta name="app.version" content="${g.meta(name:'app.version')}"/>
    <meta name="app.build" content="${g.meta(name:'app.build')}"/>
    <link rel="stylesheet" href="http://ala.org.au/wp-content/themes/ala2011/style.css" type="text/css" media="screen" />
    <link rel="stylesheet" href="http://ala.org.au/wp-content/themes/ala2011/css/wp-styles.css" type="text/css" media="screen" />
    <link rel="stylesheet" href="http://ala.org.au/wp-content/themes/ala2011/css/buttons.css" type="text/css" media="screen" />
    <link rel="icon" type="image/x-icon" href="http://ala.org.au/wp-content/themes/ala2011/images/favicon.ico" />
    <link rel="shortcut icon" type="image/x-icon" href="http://ala.org.au/wp-content/themes/ala2011/images/favicon.ico" />
    <link rel="stylesheet" type="text/css" media="screen" href="http://ala.org.au/wp-content/themes/ala2011/css/jquery.autocomplete.css" />
    <link rel="stylesheet" type="text/css" media="screen" href="http://ala.org.au/wp-content/themes/ala2011/css/search.css" />
    <link rel="stylesheet" type="text/css" media="screen" href="http://ala.org.au/wp-content/themes/ala2011/css/skin.css" />
    <link rel="stylesheet" type="text/css" media="screen" href="http://ala.org.au/wp-content/themes/ala2011/css/sf.css" />

    <r:require module="html5" />
    <g:layoutHead />
    <script language="JavaScript" type="text/javascript" src="http://ala.org.au/wp-content/themes/ala2011/scripts/superfish/superfish.js"></script>
    %{-- This clashes with jqueryui autocomplete used on the search page -- <script language="JavaScript" type="text/javascript" src="http://ala.org.au/wp-content/themes/ala2011/scripts/jquery.autocomplete.js"></script>--}%
    <r:script>

        // initialise plugins

        jQuery(function(){
            jQuery('ul.sf').superfish( {
                delay:500,
                autoArrows:false,
                dropShadows:false
            });

            jQuery("form#search-form-2011 input#search-2011").autocomplete('http://bie.ala.org.au/search/auto.jsonp', {
                extraParams: {limit: 100},
                dataType: 'jsonp',
                parse: function(data) {
                    var rows = new Array();
                    data = data.autoCompleteList;
                    for(var i=0; i < data.length; i++){
                        rows[i] = {
                            data:data[i],
                            value: data[i].matchedNames[0],
                            result: data[i].matchedNames[0]
                        };
                    }
                    return rows;
                },
                matchSubset: false,
                formatItem: function(row, i, n) {
                    return row.matchedNames[0];
                },
                cacheLength: 10,
                minChars: 3,
                scroll: false,
                max: 10,
                selectFirst: false
            });
        });
    </r:script>
    <r:layoutResources />
</head>
<body class="fish ${pageProperty(name:'body.class')}">

<hf:banner logoutUrl="${grailsApplication.config.grails.serverURL}/taxon/logout"/>

%{--<hf:menu/>--}%

<g:layoutBody />

<hf:footer/>

<script type="text/javascript">
    // show warning if using IE6
    if ($.browser.msie && $.browser.version.slice(0,1) == '6') {
        $('#header').prepend($('<div style="text-align:center;color:red;">WARNING: This page is not compatible with IE6.' +
                ' Many functions will still work but layout and image transparency will be disrupted.</div>'));
    }
</script>
<script type="text/javascript">
    var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
    document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
</script>
<script>
    var pageTracker = _gat._getTracker("UA-4355440-1");
    pageTracker._initData();
    pageTracker._trackPageview();

    // show warning if using IE6
    if ($.browser && $.browser.msie && $.browser.version.slice(0,1) == '6') {
        $('#header').prepend($('<div style="text-align:center;color:red;">WARNING: This page is not compatible with IE6.' +
                ' Many functions will still work but layout and image transparency will be disrupted.</div>'));
    }
</script>

</body>
</html>