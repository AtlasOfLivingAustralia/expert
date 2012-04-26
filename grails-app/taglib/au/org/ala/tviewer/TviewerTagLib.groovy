package au.org.ala.tviewer

import grails.converters.JSON

class TviewerTagLib {
    static namespace = "tv"

    /**
     * Builds the unordered list that implements the appropriate pagination links
     * for the supplied values.
     * @attr total number of items
     * @attr pageSize items per page
     * @attr start the first item to display
     * @query option query to add to links
     */
    def paginate = { attrs ->
        int total = attrs.total as int
        int pageSize = attrs.pageSize as int ?: 10
        def pageSizeParameter = pageSize == 10 ? "" : "&pageSize=${pageSize}"
        int start = attrs.start as int ?: 0
        def query = attrs.query ?: "q=*"
        //println "from ${start} to ${total} step ${pageSize}"
        if (total > pageSize) {
            out << "<ul>"
            if (start == 0) {
                out << "<li id='prevPage'>« Previous</li>"
            }
            else {
                out << "<li id='prevPage'><a href='?${query}&start=${start-pageSize}${pageSizeParameter}'>« Previous</a></li>"
            }
            (0..total - 1).step(pageSize, {
                int page = it == 0 ? 0 : it/pageSize
                if (it == start) {
                    out << "<li class='currentPage'>${page}</li>"
                }
                else {
                    out << "<li><a href='?${query}&start=${it}${pageSizeParameter}'>${page}</a></li>"
                }
            })
            if (start + pageSize >= total) {
                out << "<li id='nextPage'>Next »</li>"
            }
            else {
                out << "<li id='nextPage'><a href='?${query}&start=${start+pageSize}${pageSizeParameter}'>Next »</a></li>"
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
