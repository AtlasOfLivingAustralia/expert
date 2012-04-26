/**
 * Created by IntelliJ IDEA.
 * User: markew
 * Date: 13/01/12
 * Time: 10:59 AM
 * To change this template use File | Settings | File Templates.
 */
function buildUrl() {
    var params = $.deparam.querystring(true),
            sortBy = $('#sortBy').val(),
            sortOrder = $('#sortOrder').val(),
            pageSize = $('#perPage').val(),
            url;
    if (sortOrder === 'reverse') {
        params.sortOrder = 'reverse';
    }
    else if (params.sortOrder !== undefined) {
        delete params.sortOrder;
    }
    if (pageSize == '10') {
        delete params.pageSize;
    }
    else {
        params.pageSize = pageSize;
    }
    if (sortBy === 'name') {
        delete params.sortBy;
    }
    else {
        params.sortBy = sortBy;
    }
    url = $.param.querystring("", params);
    if ($.bbq.getState('showGenera') === 'true') {
        url = $.param.fragment(url, 'showGenera=true');
    }
    return url;
}
var tviewer = {
    init: function () {
        var params = $.deparam.querystring(true);

        // set search controls based on url params - !this must be done before binding events
        if (params.pageSize !== undefined) {
            $('#perPage').val(params.pageSize);
        }
        if (params.sortBy !== undefined) {
            $('#sortBy').val(params.sortBy);
        }
        if (params.sortOrder !== undefined) {
            $('#sortOrder').val(params.sortOrder);
        }

        // select all checkboxes
        $('#selectAll').click(function () {
            $('input[type="checkbox"]').attr('checked','checked');
        });

        // clear all checkboxes
        $('#clearAll').click(function () {
            $('input[type="checkbox"]').removeAttr('checked');
        });

        // handle change to search controls
        $('#controls select').change(function () {
            document.location.href = buildUrl();
        });

        // wire lightbox for images
        $('div.imageContainer').colorbox({
            rel: 'list',
            opacity: 0.5,
            html: function () {
                var content = $(this).find('div').clone(false);
                // need to clear max-width and max-height explicitly (seems to get written into element style somehow - cloning?)
                $(content).find('img').removeClass('list').removeAttr('title').css('max-width','none').css('max-height','none'); // remove max size constraints & title
                $(content).find('details').css('display','block'); // show details
                return content;
            }
        });
    }
}