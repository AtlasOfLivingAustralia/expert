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
            <h1>Visual explorer - ${rank} list</h1>
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
            <tr><th></th><th>Family name<br/><span style="font-weight: normal;">Common name<br/>CAAB code
                <a href="http://www.marine.csiro.au/caab/" class="external">more info</a></span></th>
                <th>Sample image</th>
                <th><span id="toggleGenera" class='link'>Show genera</span></th></tr>
            </thead>
            <tbody>
            <g:each in="${list}" var="i">
                <tr>
                    <!-- checkbox -->
                    <td><input type="checkbox" id="${i.name}" alt="${i.guid}"/></td>
                    <!-- name -->
                    <td><a href="${grailsApplication.config.bie.baseURL}/species/${i.name}" title="Show ${rank} page">${i.name}</a>
                    <!-- common name -->
                    <g:if test="${i.common && i.common.toString() != 'null'}">
                        ${i.common}
                    </g:if>
                    <!-- CAAB code -->
                    <g:if test="${i.CAABCode}">
                        <br/><a href="http://www.marine.csiro.au/caabsearch/caab_search.family_listing?ctg=${i.CAABCode[0..1]}&fcde=${i.CAABCode[4..5]}"
                                class="external" title="Lookup CAAB code">${i.CAABCode}</a>
                    </g:if>
                    </td>
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
                    <!-- genera -->
                    <td>
                        <table class="genera">
                            <g:each in="${i.genera}" var="g" status="count">
                                <g:if test="${count % 2 == 0}">
                                    <tr>
                                </g:if>
                                <td><div>
                                    <img class="thumb" src="${g.image?.repo}"/>
                                    <a href="${grailsApplication.config.bie.baseURL}/species/${g.guid}">${g.name}</a>
                                    <details open="open">
                                        <summary id="${g.name}">${g.name} (<em>${g.image?.title}</em>)</summary>
                                        <dl>
                                            <dt>Image by</dt><dd class="creator">${g.image?.creator}</dd>
                                            <dt>License</dt><dd class="license">${g.image?.license}</dd>
                                            <dt>Rights</dt><dd class="rights">${g.image?.rights}</dd>
                                        </dl>
                                    </details>
                                </div></td>
                                <g:if test="${count % 2 == 1 || count == i.genera.size()}">
                                    </tr>
                                </g:if>
                            </g:each>
                        </table>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
        <section id="pagination">
            <tv:paginate start="${start}" pageSize="${pageSize}" total="${total}" query="${query}"/>
            <p>
                Total <tv:pluraliseRank rank="${rank}"/>: ${total}
                <span class="link" id="speciesList">Show species list for checked <tv:pluraliseRank rank="${rank}"/></span><br/>
                <button id="selectAll" type="button">Select all</button>
                <button id="clearAll" type="button">Clear all</button>
            </p>
        </section>
    </div>
    <script type="text/javascript">
        $(document).ready(function () {
            var $toggleGenera = $('#toggleGenera'),
                params = $.deparam.querystring(true);
            // toggle display of genera via 'show genera' link
            $toggleGenera.click(function () {
                if ($toggleGenera.html() === 'Show genera') {
                    $('.genera').css('display','block');
                    $toggleGenera.html('Hide genera');
                    $('img.list').css('max-width','220px').css('max-height','150px')
                    $('#tlImage').css('width', '25%');
                    $('tlGenera').css('width', '20%');
                    $.bbq.pushState({showGenera:'true'});
                    $('#pagination a').fragment({showGenera:'true'});
                } else {
                    $('.genera').css('display','none');
                    $toggleGenera.html('Show genera');
                    $('img.list').css('max-width','285px').css('max-height','150px');
                    $('#tlImage').css('width', '35%');
                    $('tlGenera').css('width', '10%');
                    $.bbq.removeState('showGenera');
                    $('#pagination a').fragment('',2);
                }
            });
            // show genera if url has this state
            if ($.bbq.getState('showGenera') == 'true') {
                $toggleGenera.click();
            }
            // change main image on mouseover of genera images
            $('img.thumb').on('mouseenter', function () {
                var $mainImageData = $(this).closest('table.genera').parent().prev().find('div'),
                    $mainImageSummary = $mainImageData.find('summary'),
                    $sourceData = $(this).parent(),
                    $sourceSummary = $sourceData.find('summary'),
                    rankName = $mainImageSummary.attr('id'),
                    title = $sourceSummary.find('em').text();
                // change the image src (easier than swapping the image tag and having to correct display size)
                $mainImageData.find('img').attr('src',$(this).attr('src'));
                // change the metadata
                $mainImageSummary.html(rankName + " (<em>" + title + "</em>)");
                $mainImageData.find('dd.creator').html($sourceData.find('dd.creator').text());
                $mainImageData.find('dd.license').html($sourceData.find('dd.license').text());
                $mainImageData.find('dd.rights').html($sourceData.find('dd.rights').text());
                //$mainImageData.find('details').html($(this).parent().find('details').html());
            });
            // wire link to species list
            $('#speciesList').click(function () {
                // collect the selected ranks
                var checked = "";
                $('input[type="checkbox"]:checked').each(function () {
                    checked += (checked === "" ? '' : ',') + $(this).attr('id');
                });
                if (checked === "") {
                    alert("No families selected");
                }
                else {
                    document.location.href = "${grailsApplication.config.grails.serverURL}" +
                    "/taxon/species?taxa=" + checked;
                }
            });
            tviewer.init();
        });
    </script>
</body>
</html>