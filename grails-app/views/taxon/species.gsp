<!DOCTYPE html>
<html>
<head>
    <title>Taxon list</title>
    <meta name="layout" content="ala2"/>
    <link rel="stylesheet" type="text/css" media="screen" href="${resource(dir:'css',file:'tview.css')}" />
    <link rel="stylesheet" type="text/css" media="screen" href="${resource(dir:'css',file:'colorbox.css')}" />
    <script language="JavaScript" type="text/javascript" src="http://ala.org.au/wp-content/themes/ala2011/scripts/jquery.autocomplete.js"></script>
    <g:javascript library="jquery.ba-bbq.min"/>
    <g:javascript library="jquery.colorbox-min"/>
    <g:javascript library="tviewer"/>
</head>
<body>
<header id="page-header">
    <div class="inner">
        <nav id="breadcrumb"><ol><li><a href="${grailsApplication.config.grails.serverURL}">Home</a></li> <li class="last"><i>search</i></li></ol></nav>
        <hgroup>
            <h1>Visual explorer - species list</h1>
            <h2>Defined region: ${region ?: 'Australia'}</h2>
            %{--<h2>Taxon group - ${parentTaxa[0].rank}: <em>${parentTaxa[0].name}</em></h2>--}%
        </hgroup>
    </div>
    <div id="controls">
        <label for="sortBy">Sort by:</label>
        <g:select from="[[text:'Scientific name',id:'name'],[text:'Common name',id:'common'],[text:'CAAB code',id:'CAABCode']]"
                  name="sortBy" optionKey="id" optionValue="text"/>
        <label for="sortOrder">Sort order:</label>
        <g:select from="['normal','reverse']" name="sortOrder"/>
        <label for="perPage">Results per page:</label>
        <g:select from="[5,10,20,50,100]" name="perPage" value="10"/>
    </div>
</header>
<div id="context">
    <table class="taxonList">
        <colgroup>
            <col id="tlCheckbox"> <!-- checkbox -->
            <col id="tlName"> <!-- taxon name -->
            <col id="tlImage"> <!-- image -->
            <col id="tlGenera"> <!-- genera -->
        </colgroup>
        <thead>
        <tr><th></th><th>Scientific name<br/><span style="font-weight: normal;">CAAB code
            <a href="http://www.marine.csiro.au/caab/" class="external">more info</a></span></th>
            <th>Sample image</th>
            <th><span>Distribution</span></th></tr>
        </thead>
        <tbody>
        <g:each in="${list}" var="i">
            <tr>
                <!-- checkbox -->
                <td><input type="checkbox" alt="${i.guid}"/></td>
                <!-- name -->
                <td><em><a href="${grailsApplication.config.bie.baseURL}/species/${i.name}" title="Show ${rank} page">${i.name}</a></em>
                <!-- common -->
                <g:if test="${i.common && i.common.toString() != 'null'}">
                    <br/>${i.common}
                </g:if>
                <!-- family -->
                <br/>Family: ${i.family}
                <!-- CAAB code -->
                <g:if test="${i.CAABCode}">
                    <br/>CAAB: <a href="http://www.marine.csiro.au/caabsearch/caab_search.family_listing?ctg=${i.CAABCode[0..1]}&fcde=${i.CAABCode[4..5]}"
                            class="external" title="Lookup CAAB code">${i.CAABCode}</a>
                </g:if></td>
                <!-- image -->
                <td><div rel="list" class="imageContainer">
                    <g:if test="${i.image?.repo}">
                        <div>
                            <img class="list" src="${i.image.repo}" alt title="Click to view full size"/>
                            <details open="open">
                                <summary id="${i.name}">${i.name} (<em>${i.image?.title}</em>)</summary>
                                <dl>
                                    <dt>Image by</dt><dd class="creator">${i.image?.creator}</dd>
                                    <dt>License</dt><dd class="license">${i.image?.license}</dd>
                                    <dt>Rights</dt><dd class="rights">${i.image?.rights}</dd>
                                </dl>
                            </details>
                        </div>
                    </g:if>
                </div></td>
                <!-- distribution -->
                <td><div rel="dist" class="distributionImageContainer">
                    <div>
                        <g:if test="${i.distributionImage}">
                            <img class="dist" src="${i.distributionImage.repo}" alt title="Click to view full size"/>
                        </g:if>
                    </div>
                </div></td>
            </tr>
        </g:each>
        </tbody>
    </table>
    <section id="pagination">
        <tv:paginate start="${start}" pageSize="${pageSize}" total="${total}" query="${query}"/>
        <p>
            Total <tv:pluraliseRank rank="species"/>: ${total}
            <button id="selectAll" type="button">Select all</button>
            <button id="clearAll" type="button">Clear all</button>
        </p>
    </section>
</div>
<script type="text/javascript">
    $(document).ready(function () {
        tviewer.init();

        // wire lightbox for distribution images
        $('div.distributionImageContainer').colorbox({
            opacity: 0.5,
            html: function () {
                var content = $(this).find('div').clone(false);
                // need to clear max-width and max-height explicitly (seems to get written into element style somehow - cloning?)
                $(content).find('img').removeClass('list').removeAttr('title').css('max-width','none').css('max-height','none'); // remove max size constraints & title
                $(content).find('details').css('display','block'); // show details
                return content;
            }
        });
    });
</script>
</body>
</html>