package au.org.ala.tviewer

import grails.test.mixin.TestFor

/* *************************************************************************
 *  Copyright (C) 2011 Atlas of Living Australia
 *  All Rights Reserved.
 * 
 *  The contents of this file are subject to the Mozilla Public
 *  License Version 1.1 (the "License"); you may not use this file
 *  except in compliance with the License. You may obtain a copy of
 *  the License at http://www.mozilla.org/MPL/
 * 
 *  Software distributed under the License is distributed on an "AS
 *  IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  rights and limitations under the License.
 ***************************************************************************/

/**
 * User: markew
 * Date: 22/01/12
 */
@TestFor(TaxonController)
class TaxonControllerTests {
    
    void testBind() {
        params.asModel = "true"
        //params.search = 2
        controller.bind(2)
        assert model.sch == 2

        controller.bind(-2)
        assert model.sch == -2

        controller.bind()
        assert model.sch == 0
    }
    
    void testBindAsStringPosInt() {
        params.search = 2
        controller.bind()
        assert response.text == "search is a class java.lang.Integer and has value 2"
    }

    void testBindAsStringNegInt() {
        params.search = -2
        controller.bind()
        assert response.text == "search is a class java.lang.Integer and has value -2"
    }
    
    void testListWithPredefinedSearch() {
        controller.list(1)
        assert model.total == 4

        controller.list(2)
        assert model.total == 8

        controller.list(3)
        assert model.total == 12

    }
}
