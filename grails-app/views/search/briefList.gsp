<!DOCTYPE html>
<html>
<head>
    <title>Expert distribution search</title>
    <meta name="layout" content="ala2"/>
    <link rel="stylesheet" type="text/css" media="screen" href="${resource(dir:'css',file:'expert.css')}" />
    <g:javascript library="expert"/>
</head>
<body>
<header id="page-header">
    <div class="inner">
        <nav id="breadcrumb"><ol>
            <li><a href="${grailsApplication.config.ala.baseUrl}">Home</a></li>
            <li><a href="${grailsApplication.config.grails.serverURL}/search">Expert distribution search</a></li>
            <li class="last">Expert distribution results</li></ol></nav>
        <hgroup>
            <h1>ALA Fish Finder - Spatial Search Results</h1>
            <h2>This is a simple list for debugging</h2>
        </hgroup>
    </div>
</header>
<section>
    <g:if test="${error}">${error}</g:if>
    <g:else>
        <ul style="list-style-type:none;">
            <g:each in="${list}" var="obj">
                <li>
                    <a href="${grailsApplication.config.bie.baseURL}/species/${obj.guid}">${obj.name}</a>
                    (${obj.family})
                </li>
            </g:each>
        </ul>
    </g:else>
</section>
</body>
</html>