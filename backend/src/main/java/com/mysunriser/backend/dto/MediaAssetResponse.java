package com.mysunriser.backend.dto;

import com.mysunriser.backend.entity.MediaAsset;

import java.time.LocalDateTime;

public record MediaAssetResponse(
        Long id,
        String assetType,
        String originalFilename,
        String mimeType,
        Long sizeBytes,
        String accessLevel,
        String uploadedBy,
        LocalDateTime createdAt,
        String contentUrl,
        String downloadUrl,
        String markdown
) {
    public static MediaAssetResponse of(MediaAsset asset) {
        String contentUrl = "/api/media/" + asset.getId() + "/content";
        String downloadUrl = "/api/media/" + asset.getId() + "/download";
        boolean image = MediaAssetType.IMAGE.name().equals(asset.getAssetType());
        String markdown = image
                ? "![" + markdownAlt(asset.getOriginalFilename()) + "](" + contentUrl + ")"
                : "[" + markdownAlt(asset.getOriginalFilename()) + "](" + downloadUrl + ")";

        return new MediaAssetResponse(
                asset.getId(),
                asset.getAssetType(),
                asset.getOriginalFilename(),
                asset.getMimeType(),
                asset.getSizeBytes(),
                asset.getAccessLevel(),
                asset.getUploadedBy(),
                asset.getCreatedAt(),
                contentUrl,
                downloadUrl,
                markdown
        );
    }

    private static String markdownAlt(String value) {
        if (value == null || value.isBlank()) {
            return "media";
        }

        return value.replace("[", "\\[").replace("]", "\\]");
    }
}
