package au.org.ala.tviewer

import grails.converters.JSON


class TviewerTagLib {
    static namespace = "tv"
    static final MAX_PAGE_LINKS = 22

    /**
     * Builds the unordered list that implements the appropriate pagination links
     * for the supplied values.
     *
     * Implements an algorithm to truncate the pagination list if it is too long.
     *
     * @attr total number of items
     * @attr pageSize items per page
     * @attr start the first item to display
     * @attr params other url params to be added if they have a value
     */
    def paginate = { attrs ->
        // total num items
        int total = attrs.total as int
        // num items per page
        int pageSize = attrs.pageSize as int ?: 10
        // the first item to display on this page
        int start = attrs.start as int ?: 0
        // the num of the page being displayed
        int currentPage = start / pageSize
        // the num of pages
        int totalPages = Math.ceil total / pageSize
        // the index of the last page (zero-based)
        int lastPage = totalPages - 1
        // whether we need to abbreviate the list of page links
        boolean abbreviateLinks = totalPages > MAX_PAGE_LINKS
        // in general the abbreviated list starts MAX_PAGE_LINKS/2 from the current page so that the current
        // page is in the centre of the list - however this value increases as we approach the last page (because
        // there are no more pages on the right end to fill the list) so that we maintain the same number
        // of links in the list - simple, yes?
        int startOffset = Math.max(currentPage - (lastPage - MAX_PAGE_LINKS), MAX_PAGE_LINKS / 2 as int)
        // the num of the page for the first link - 0 unless we are abbreviating the list on the left
        int startingPageLink = abbreviateLinks ? Math.max(currentPage - startOffset, 0) : 0
        // the num of the page for the last link - total/pageSize unless we are abbreviating the list on the right
        int endingPageLink = abbreviateLinks ? Math.min(startingPageLink + MAX_PAGE_LINKS, lastPage) : lastPage
        /* Because the algorithm requires the first and last page links to always be shown, we need another
         * set of pointers for the start and end of the inner set of page links */
        // if the first page is 0, skip it as already shown
        int innerStartingPageLink = startingPageLink == 0 ? 1 : startingPageLink
        // if the last page is the absolute last page, skip it as it will be shown
        int innerEndingPageLink = endingPageLink == lastPage ? endingPageLink - 1 : endingPageLink
        // this caters for the edge case where there are only 2 pages so no inner links need to be shown
        boolean needInnerLinks = innerEndingPageLink >= innerStartingPageLink

        // params to include in page links
        def otherParams = ""
        attrs.params.each { k, v ->
            if (v) {
                otherParams += "&${k}=${v}"
            }
        }
        // optional pageSize parameter for links
        def pageSizeParameter = pageSize == 10 ? "" : "&pageSize=${pageSize}"

        // closure to write a single page link
        // page numbers are 0-based but the displayed text is 1-based
        def writePageLink = { page ->
            if (page == currentPage) {
                out << "<li class='currentPage'>${page + 1}</li>"
            } else {
                out << "<li><a href='?start=${page * pageSize}${pageSizeParameter}${otherParams}'>${page + 1}</a></li>"
            }
        }

        if (total > pageSize) {
            out << "<ul>"

            // « Previous link
            if (start == 0) {
                out << "<li id='prevPage'>« Previous</li>"
            }
            else {
                out << "<li id='prevPage'><a href='?start=${start - pageSize}${pageSizeParameter}${otherParams}'>« Previous</a></li>"
            }

            // first page link (always show even if abbreviating)
            writePageLink 0

            // show ellipsis if we are abbreviating the list at the front
            if (startingPageLink > 0) {
                out << " ... "
            }

            // show the page links btw start and end - bearing in mind the absolute first and
            // last pages are shown separately
            if (needInnerLinks) {
                (innerStartingPageLink..innerEndingPageLink).each {
                    writePageLink it
                }
            }

            // show ellipsis if we are abbreviating the list at the end
            if (endingPageLink < lastPage) {
                out << " ... "
            }

            // last page link (always show even if abbreviating)
            writePageLink lastPage

            // Next » link
            if (currentPage == lastPage) {
                out << "<li id='nextPage'>Next »</li>"
            }
            else {
                out << "<li id='nextPage'><a href='?start=${start + pageSize}${pageSizeParameter}${otherParams}'>Next »</a></li>"
            }

            out << "</ul>"
        }
    }

    /**
     * Provides the plural form of the rank.
     * @attr rank
     */
    def pluraliseRank = { attrs ->
        def plural
        switch(attrs.rank) {
            case 'phylum': plural = 'phyla'; break
            case 'class': plural = 'classes'; break
            case 'family': plural = 'families'; break
            case 'genus': plural = 'genera'; break
            case 'species': plural = 'species'; break
            default: plural = attrs.rank + 's'
        }
        out << plural
    }

    def removeSpaces = { attrs ->
        if (attrs.str) {
            out << attrs.str.tokenize(' ').join()
        }
    }

    def displayPrimaryEcosystem = { attrs ->
        def codes = attrs.codes
        def text = []
        if (codes =~ 'e') {
            text << 'estuarine'
        }
        if (codes =~ 'c') {
            text << 'coastal'
        }
        if (codes =~ 'd') {
            text << 'demersal'
        }
        if (codes =~ 'p') {
            text << 'pelagic'
        }

        out << text.join(', ')
    }

    def notNull = { attrs, body ->
        if (attrs.val) {
            if (attrs.val == '') {
                return ""
            }
            return body()
        }
    }

    // splits a family caab number like '37 441' into ctg and fcde components
    def splitFamilyCaab = { attrs, body ->
        def caab = attrs.caab
        def ctg = ''
        def fcde = ''
        def bits = caab.tokenize(' ')
        if (bits.size() < 2) {
            // no space was present so take the first 2 chars as ctg
            if (caab.size() > 1) {
                ctg = caab[0..1]
            }
            if (caab.size() > 2) {
                fcde = caab[2..-1]
            }
        } else {
            ctg = bits[0]
            fcde = bits[1]
        }

        out << "ctg=${ctg}&fcde=${fcde}"
    }

    def toObjectLiteral = { attrs ->
        if (attrs.obj) {
            def j = attrs.obj as JSON
            out << j.toString()
        }
        else {
            out << "[]"
        }
    }
}
