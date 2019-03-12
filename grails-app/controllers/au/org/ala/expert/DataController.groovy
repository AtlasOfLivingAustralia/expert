package au.org.ala.expert

class DataController {

    def images(String id) {
        if (id.toString().matches('dist[0-9]+')) {
            def img = new File(grailsApplication.config.distribution.image.cache + '/' + id.toString().substring(4) + '.png')
            if (img.exists()) {
                response.setHeader('Cache-Control', 'max-age=' + 60 * 60 * 24 * 30)
                response.setContentType('image/png')
                response.outputStream << img.bytes
            } else {
                render status: 404, text: ''
            }
        } else {
            render status: 404, text: ''
        }
    }

    def sld = DataController.classLoader.getResourceAsStream('dist.sld').text

    def dist() {
        render text: sld, contentType: 'application/xml'
    }
}
