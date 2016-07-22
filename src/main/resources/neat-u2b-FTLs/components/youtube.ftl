[#assign link = content.youtube!]
[#assign autoPlay = content.autoPlay!false]
[#assign startTime = content.startTime!]
[#assign hideInfo = content.hideInfo!false]
[#assign hideControls = content.hideControls!false]

[#if link?has_content]
    [#assign urlParams = ""]
    [#macro addToUrlParams add]
        [#if urlParams != ""]
            [#assign urlParams = urlParams + "&"]
        [#else]
            [#assign urlParams = urlParams + "?"]
        [/#if]
        [#assign urlParams = urlParams + add]
    [/#macro]

    [#assign youtubeID = link]

    [#if link?index_of("//youtu.be/") > -1]
        [#assign youtubeIDs = link?split("//youtu.be/")];
        [#assign youtubeID = youtubeIDs[1]]
    [#elseif link?index_of("//www.youtube.com/watch?v=") > -1]
        [#assign youtubeIDs = link?split("//www.youtube.com/watch?v=")]
        [#assign youtubeID = youtubeIDs[1]]
    [/#if]

    [#if youtubeID?index_of("&") > -1]
        [#list youtubeID?split("&") as x]
            [#if x_index == 0]
                [#assign youtubeID = x]
            [#else]
                [@addToUrlParams x /]
            [/#if]
        [/#list]
    [/#if]

    [#assign videoURL = "//www.youtube.com/embed/" + youtubeID]

    [#if autoPlay]
        [@addToUrlParams "autoplay=1"/]
    [/#if]
    [#if hideInfo]
        [@addToUrlParams "showinfo=0"/]
    [/#if]
    [#if hideControls]
        [@addToUrlParams "controls=0"/]
    [/#if]
    [#if startTime?has_content]
        [@addToUrlParams ("t=" + content.startTime)/]
    [/#if]

    <div class="embed-responsive embed-responsive-16by9" itemprop="video" itemscope itemtype="http://schema.org/VideoObject">

        <meta itemprop="name" content="${content.youtubeTitle!}" />
        <meta itemprop="description" content="${content.youtubeDescription!}" />
        <meta itemprop="duration" content="${content.youtubeDuration!}" />
        <meta itemprop="uploadDate" content="${content.youtubePublishedAt!}" />
        <meta itemprop="thumbnailUrl" content="${content.youtubeThumbs.medium.url!}" />
        <meta itemprop="embedURL" content="${videoURL}" />

        <iframe src='${videoURL + urlParams}' width="560" height="315" frameborder="0" allowfullscreen></iframe>

    </div>
[/#if]