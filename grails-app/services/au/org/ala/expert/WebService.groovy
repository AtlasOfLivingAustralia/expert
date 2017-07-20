package au.org.ala.expert

import grails.converters.JSON
import org.grails.web.converters.exceptions.ConverterException

class WebService {

    def get(String url) {
        try {
            return new URL(url).text
        } catch (SocketTimeoutException e) {
            def error = [error: "Timed out calling web service. URL= \${url}."]
            log.error(error.error,e)
            return error as JSON
        } catch (Exception e) {
            def error = [error: "Failed calling web service. ${e.getClass()} ${e.getMessage()} URL= ${url}."]
            log.error(error.error,e)
            return error as JSON
        }
    }

    def getJson(String url) {
        try {
            def json = new URL(url).text
            return JSON.parse(json)
        } catch (ConverterException e) {
            def error = ['error': "Failed to parse json. ${e.getClass()} ${e.getMessage()} URL= ${url}."]
            log.error(error.error,e)
            return error
        } catch (SocketTimeoutException e) {
            def error = [error: "Timed out getting json. URL= \${url}."]
            log.error(error.error,e)
            return error
        } catch (Exception e) {
            def error = [error: "Failed to get json from web service. ${e.getClass()} ${e.getMessage()} URL= ${url}."]
            log.error(error.error,e)
            return error
        }
    }

    def doJsonPost(String url, String path, String port, String postBody, String contentType) {
        log.debug url + (port ?: '') + (path ?: '')
        log.debug postBody
        def conn = new URL(url + (port ?: '') + (path ?: '')).openConnection()
        try {
            conn.setConnectTimeout(10000)
            conn.setReadTimeout(50000)
            conn.setRequestProperty('Content-Type', contentType)
            conn.setDoOutput(true)
            conn.setDoInput(true)
            def sw = new OutputStreamWriter(conn.outputStream)
            sw.write(postBody)
            sw.flush()

            return conn.content.text
        } catch (SocketTimeoutException e) {
            def error = [error: "Timed out calling web service. URL= \${url}."]
            log.error(error.error, e)
            return error as JSON
        } catch (Exception e) {
            def error = [error: "Failed calling web service. ${e.getClass()} ${e.getMessage()} URL= ${url}."]
            log.error(error.error, e)
            return error as JSON
        }
    }
}
