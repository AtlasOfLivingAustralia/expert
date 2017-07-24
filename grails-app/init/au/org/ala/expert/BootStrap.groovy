package au.org.ala.expert

import groovy.json.JsonSlurper

class BootStrap {
    def grailsApplication

    def init = { servletContext ->

        //fetch distribution images that are missing
        new Thread() {
            @Override
            void run() {
                def baseUrl = grailsApplication.config.distribution.image.baseURL

                def json = new URL("${grailsApplication.config.spatial.layers.service.url}/distributions?dataResourceUid=${grailsApplication.config.distribution.maps.dataResourceUid}").text
                def result = new JsonSlurper().parseText(json)


                def sld = '&sld_body=' + URLEncoder.encode(DataController.classLoader.getResourceAsStream('dist.sld').text, "UTF-8")

                try {
                    result.eachWithIndex { it, i ->
                        def file = new File(grailsApplication.config.distribution.image.cache + '/' + it.geom_idx + ".png")
                        if (!file.exists()) {
                            file.withOutputStream { os ->
                                new URL(baseUrl + it.geom_idx + sld).withInputStream { is ->
                                    os << is
                                }
                            }
                        }
                    }
                } catch (InterruptedException ie) {

                } catch (Exception e) {
                    log.error 'failed to fetch distribution images. ' + e.getMessage(), e
                }
            }
        }.start()

    }
    def destroy = {
    }
}
