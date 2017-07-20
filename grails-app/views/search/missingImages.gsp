<!DOCTYPE HTML>
<html>
<head>
    <title>Species missing ANFC images | FishMap | Atlas of Living Australia</title>

    <meta name="layout" content="main"/>

    <asset:stylesheet src="expert"/>
</head>
<body class="search">
    <header id="page-header">
        <div class="inner">
            <hgroup>
                <h1 title="fishmap - species missing ANFC images"></h1>
            </hgroup>
            <nav id="breadcrumb"><ol>
                <li><a href="${searchPage}">Search</a></li>
                <li class="last"><i>Species missing ANFC images</i></li></ol>
            </nav>
        </div>
    </header>
    <div class="inner">

        <h2>Species missing ANFC images</h2>
        <h3>Total species = ${total}</h3>
        <h3>Unable to match in BIE = ${unmatched.size()}</h3>
        <ul>
            <g:each in="${unmatched}" var="u">
                <li>
                    <b><a href="${grailsApplication.config.bie.baseURL}/species/${u.name}">${u.name}</a></b>
                    <a href="${grailsApplication.config.bie.baseURL}/species/${u.guid}">${u.guid}</a>
                </li>
            </g:each>
        </ul>

        <h3>Matched with no image = ${missing.size()}</h3>
        <table>
            <g:each in="${missing}" var="sp">
                <tr><td><b><a href="${grailsApplication.config.bie.baseURL}/species/${sp.name}">${sp.name}</a></b></td>
                    <g:if test="${!(namesOnly == 'true')}">
                        <td>(${sp.common})</td><td><a href="${grailsApplication.config.bie.baseURL}/species/${sp.guid}">${sp.guid}</a></td>
                    </g:if>
                </tr>
            </g:each>
        </table>
    </div>
</body>
</html>