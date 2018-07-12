<!DOCTYPE html>
<html>
<head>
    <g:render template="/layout/ga" />

    <title>Species data | FishMap | Atlas of Living Australia</title>
    <meta name="breadcrumbs" content="${createLink(controller: 'search')},FishMap | Search"/>
    <meta name="breadcrumb" content="Species data"/>
    <meta name="layout" content="main"/>

    <asset:stylesheet src="application"/>
    <asset:stylesheet src="tview"/>
    <asset:javascript src="tviewer"/>
</head>

<body class="species-data">
<header id="page-header">
    <div class="inner no-top">
        <hgroup>
            <h1 title="Data explorer - species data"></h1>
        </hgroup>
    </div>
</header>

<div class="inner">
    <h2 style="float:left;">Results for ${queryDescription ?: 'Australia'}</h2>

    <div id="controls">
        <label for="sortBy">Sort by:</label>
        <g:select
                from="[[text: 'Family/genus/spp', id: 'taxa'], [text: 'Scientific name', id: 'name'], [text: 'Common name', id: 'common'], [text: 'CAAB code', id: 'caabCode']]"
                name="sortBy" optionKey="id" optionValue="text"/>
        <label for="sortOrder">Sort order:</label>
        <g:select from="['normal', 'reverse']" name="sortOrder"/>
    </div>
    <table class="taxonData">
        <colgroup>
            <col id="tdCaabCode">
            <col id="tdFamily">
            <col id="tdSciName">
            <col id="tdCommon">
            <col id="tdGroup">
        </colgroup>
        <thead>
        <tr>
            <th>CAAB Code</th>
            <th>Family</th>
            <th>Scientific name</th>
            <th>Common name</th>
            <th>Fish group</th>
            <th>Min depth</th>
            <th>Max depth</th>
            <th>Primary ecosystem</th>
        </thead>
        <tbody>
        <g:each in="${list}" var="i">
            <tr>
                <!-- caab -->
                <td>${i.caabCode}</td>
                <!-- family -->
                <td>${i.family}</td>
                <!-- name -->
                <td><em><a href="${grailsApplication.config.bie.baseURL}/species/${i.name}"
                           title="Show ${rank} page">${i.name}</a></em></td>
                <!-- common -->
                <td>${i.common}</td>
                <!-- group -->
                <td>${i.group}</td>
                <!-- min depth -->
                <td>${i.minDepth}</td>
                <!-- max depth -->
                <td>${i.maxDepth}</td>
                <!-- ecosystem -->
                <td><tv:displayPrimaryEcosystem codes="${i.primaryEcosystem}"/></td>
            </tr>
        </g:each>
        </tbody>
    </table>
    <section id="pagination">
        <p id="viewLinks">
            Total <tv:pluraliseRank rank="species"/>: ${total}
            <g:link style="padding-left:20px;" action="view" params="[key: key]">Show all results by family</g:link>
            <g:link style="padding-left:20px;" action="species" params="[key: key]">Show all results by species</g:link>
        </p>
    </section>
</div>
<asset:script>
    $(document).ready(function () {
        tviewer.init("${grailsApplication.config.grails.serverURL}");
    });
</asset:script>
</body>
</html>