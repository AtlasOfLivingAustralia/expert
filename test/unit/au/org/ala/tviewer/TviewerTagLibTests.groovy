package au.org.ala.tviewer

import grails.test.mixin.TestFor

@TestFor(TviewerTagLib)
class TviewerTagLibTests {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testPlurals() {
        assert applyTemplate("<tv:pluraliseRank rank='phylum'/>") == 'phyla'
        assert applyTemplate("<tv:pluraliseRank rank='class'/>") == 'classes'
        assert applyTemplate("<tv:pluraliseRank rank='order'/>") == 'orders'
        assert applyTemplate("<tv:pluraliseRank rank='family'/>") == 'families'
        assert applyTemplate("<tv:pluraliseRank rank='genus'/>") == 'genera'
        assert applyTemplate("<tv:pluraliseRank rank='species'/>") == 'species'
    }
    
    void testPagination() {
        // no pagination needed
        assertOutputEquals("", "<tv:paginate total='5' pageSize='10' start='0'/>")
        // next link
        assertOutputEquals("<ul><li id='prevPage'>« Previous</li><li class='currentPage'>0</li><li><a href='?q=*&start=10'>1</a></li><li id='nextPage'><a href='?q=*&start=10'>Next »</a></li></ul>",
                "<tv:paginate total='15' pageSize='10' start='0'/>")
        // prev link
        assertOutputEquals("<ul><li id='prevPage'><a href='?q=*&start=0'>« Previous</a></li><li><a href='?q=*&start=0'>0</a></li><li class='currentPage'>1</li><li id='nextPage'>Next »</li></ul>",
                "<tv:paginate total='15' pageSize='10' start='10'/>")

    }
}
