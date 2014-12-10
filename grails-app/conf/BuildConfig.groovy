grails.servlet.version = "2.5" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6
grails.project.source.level = 1.6
grails.project.war.file = "target/${appName}-${appVersion}.war"
grails.project.groupId = "au.org.ala"

grails.project.dependency.resolver = "maven" // or ivy
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "error" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'

    repositories {
        mavenLocal()
        mavenRepo ("http://nexus.ala.org.au/content/groups/public/") {
            updatePolicy 'always'
        }
    }

    dependencies {
        runtime 'net.sf.opencsv:opencsv:2.1'
        compile group: 'au.org.ala',
                name: 'ala-cas-client',
                version:'2.1-SNAPSHOT',
                transitive:false
        compile 'org.jasig.cas.client:cas-client-core:3.1.12'
    }

    plugins {
        build ":tomcat:7.0.54"
        build  ":release:3.0.1"
        runtime ":resources:1.2.8"
        runtime ":rest:0.8"
        compile ':cache:1.1.1'
        compile ":cache-ehcache:1.0.0"
    }
}
