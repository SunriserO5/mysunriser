package com.mysunriser.backend.tools.videodownload;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tools/video-download")
public class VideoDownloadController {
    private final VideoDownloadService videoDownloadService;

    public VideoDownloadController(VideoDownloadService videoDownloadService) {
        this.videoDownloadService = videoDownloadService;
    }

    @PostMapping("/extract")
    public VideoDownloadExtractResponse extract(@Valid @RequestBody VideoDownloadExtractRequest request) {
        return videoDownloadService.extract(request);
    }
}
