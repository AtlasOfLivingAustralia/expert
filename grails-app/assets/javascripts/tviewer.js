/**
 * Created by IntelliJ IDEA.
 * User: markew
 * Date: 13/01/12
 * Time: 10:59 AM
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
    params.sortBy = sortBy;

    url = $.param.querystring("", params);
    return url;
}
var tviewer = {
    serverUrl: null,
    init: function (serverUrl) {
        this.serverUrl = serverUrl;

        var that = this,
            params = $.deparam.querystring(true),
            colorboxOptions = {
                opacity: 0.5,
                inline: true,
                onLoad: function () {
                    var $popup = $(this.hash),
                        mdUrl = $popup.find('div.details').data('mdurl');

                    if (mdUrl) {
                        // add 'loading..' status
                        $popup.find('dd').html('loading..');
                        // load and inject metadata
                        that.injectImageMetadata(mdUrl, this);
                    }
                }
            };

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

        // wire selection of all checkboxes
        $('#selectAll').click(function () {
            $('input[type="checkbox"]').attr('checked', 'checked');
        });

        // wire clearing of all checkboxes
        $('#clearAll').click(function () {
            $('input[type="checkbox"]').removeAttr('checked');
        });

        // handle change to search controls
        $('#controls select').change(function () {
            document.location.href = buildUrl();
        });

        // wire lightbox for images
        $('.lightbox').colorbox(colorboxOptions);

        // change main image on mouseover of genera images
        $('img.thumb').on('mouseenter', function () {
            var $mainImageTd = $(this).closest('table.genera').parent().prev(),
                $genusTd = $(this).parent().parent(),
                $mainImageLink = $mainImageTd.find('a.imageContainer'),
                $mainImage = $mainImageLink.find('img'),
                $popupContent = $mainImageTd.find('div.popupContent'),
                $popImage = $popupContent.find('img'),
                newImageSrc = $(this).attr('src'),
                mdUrl = $genusTd.find('div.details').data('mdurl');

            // handle case where the initial image is 'no image available'
            if ($mainImageLink.hasClass('no-image')) {
                // remove 'no-image'
                $mainImageLink.removeClass('no-image');
                // make sure the image is included in the colorbox group
                $mainImageLink.addClass('lightbox');
                $mainImageLink.attr('rel', 'list');
                $mainImageLink.colorbox(colorboxOptions);
            }

            // change the image src
            $mainImage.attr('src', newImageSrc);
            // change the popup img
            $popImage.attr('src', newImageSrc);
            // change the metadata url
            $popupContent.find('div.details').data('mdurl', mdUrl);
            that.injectImageMetadata(mdUrl, $(this).parent());
        });
    },
    // asynchronous loading of image metadata
    injectImageMetadata: function (mdUrl, box) {
        $.getJSON(this.serverUrl + "/taxon/imageMetadataLookup", {url: mdUrl}, function (data) {
            $(box.hash).find('.creator').html(data["http://purl.org/dc/elements/1.1/creator"]);
            $(box.hash).find('.license').html(data["http://purl.org/dc/elements/1.1/license"]);
            $(box.hash).find('.rights').html(data["http://purl.org/dc/elements/1.1/rights"]);
            $(box).colorbox.resize();
        });
    }
};

