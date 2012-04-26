package au.org.ala.expert

import grails.test.mixin.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(SearchController)
class SearchControllerTests {

    void testGetCircle() {
        def wkt = controller.getCircle(10.0,10.0,5,6)
        println wkt
        assert wkt == "10.0 15.0,14.330127018922193 12.5,14.330127018922195 7.500000000000001,10.0 5.0,5.669872981077807 7.499999999999998,5.669872981077805 12.499999999999996,9.999999999999998 15.0"

        wkt = controller.getCircle(10.0,10.0,5,4)
        println wkt
        assert wkt == "10.0 15.0,15.0 10.0,10.0 5.0,5.0 9.999999999999998,9.999999999999998 15.0"

        wkt = controller.getCircle(-27.467,153.023,0.4,4)
        println wkt
        assert wkt == "-27.467 153.423,-27.067 153.023,-27.467 152.623,-27.866999999999997 153.023,-27.467 153.423"
    }
}
