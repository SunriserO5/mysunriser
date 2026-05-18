package com.mysunriser.backend.tools.videodownload;

import java.util.List;
import java.util.Map;

public record VideoDownloadExtractResponse(
        String text,
        List<Media> medias
) {
    public record Media(
            String mediaType,
            String resourceUrl,
            String previewUrl,
            List<Format> formats,
            Map<String, String> headers
    ) {
    }

    public record Format(
            Integer quality,
            String videoUrl,
            String videoExt,
            Long videoSize,
            String audioUrl,
            String audioExt,
            Long audioSize,
            Integer separate,
            String qualityNote
    ) {
    }
}
