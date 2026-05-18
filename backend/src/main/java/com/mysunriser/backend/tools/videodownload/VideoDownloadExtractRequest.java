package com.mysunriser.backend.tools.videodownload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VideoDownloadExtractRequest(
        @NotBlank(message = "url is required")
        @Size(max = 2048, message = "url must be at most 2048 characters")
        String url
) {
}
