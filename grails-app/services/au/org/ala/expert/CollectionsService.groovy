package au.org.ala.expert

import grails.plugin.cache.Cacheable
import groovy.json.JsonSlurper

class CollectionsService {

    def grailsApplication

    @Cacheable(value = "collectionMetadata", key = { dataResourceUid })
    def getInfo(dataResourceUid) {
        def md = [:]

        try {
            def jsonSlurper = new JsonSlurper()
            md = jsonSlurper.parseText(new URL(grailsApplication.config.collections.url + '/ws/dataResource/' + dataResourceUid).text)
        } catch (err) {
            log.error("failed to get collection metadata for: " + dataResourceUid, err)
        }

        return md
    }
}
