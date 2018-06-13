<!DOCTYPE html>
<html>
	<head>
		<title>Species list | FishMap | Atlas of Living Australia</title>
		<meta name="breadcrumbs" content="${createLink(controller: 'search')},FishMap | Search" />
		<meta name="breadcrumb" content="${request.'javax.servlet.error.status_code'}" />
		<meta name="layout" content="main"/>
		<asset:stylesheet src="application"/>
		<asset:stylesheet src="tview"/>
		<asset:javascript src="tviewer"/>

		<style  type="text/css">
			#main {
				margin-top: 20px;
				padding-top: 0px;
				padding-left: 40px;
			}

			.detailedError {
				padding: 20px;
				-moz-box-sizing: border-box;
				box-sizing: border-box;
				border: solid 1px;
				border-color: lightgrey;
            }

	</style>

	</head>

	<body>

		<p>
			<h2>Oops! Something has gone wrong!</h2>
			Please contact <a href="mailto:support@ala.org.au?subject=Fishmap%20Error">support@ala.org.au</a>
		</p>
		<br />
		<p>
			<a style="color: darkslategrey" data-toggle="collapse" href="#collapseError" role="button" aria-expanded="false" >Click to see more details on the error</a>
		</p>

		<br />
		<div class="collapse detailedError" id="collapseError">
			<div class="card card-body">
				<h3>Grails Runtime Exception</h3>
				<h3>Error Details</h3>
				<strong>Error ${request.'javax.servlet.error.status_code'}:</strong> ${request.'javax.servlet.error.message'.encodeAsHTML()}<br/>
				<strong>Servlet:</strong> ${request.'javax.servlet.error.servlet_name'}<br/>
				<strong>URI:</strong> ${request.'javax.servlet.error.request_uri'}<br/>
				<g:if test="${exception}">
					<strong>Exception Message:</strong> ${exception.message?.encodeAsHTML()} <br />
					<strong>Caused by:</strong> ${exception.cause?.message?.encodeAsHTML()} <br />
					<strong>Class:</strong> ${exception.className} <br />
					<strong>At Line:</strong> [${exception.lineNumber}] <br />
					<strong>Code Snippet:</strong><br />
					<div class="snippet">
						<g:each var="cs" in="${exception.codeSnippet}">
							${cs?.encodeAsHTML()}<br />
						</g:each>
					</div>
				</g:if>
			</div>
			<g:if test="${exception}">
				<br />
				<h3>Stack Trace</h3>
				<div class="stack">
					<pre><g:each in="${exception.stackTraceLines}">${it.encodeAsHTML()}<br/></g:each></pre>
				</div>
			</g:if>
		</div>

	</body>

</html>