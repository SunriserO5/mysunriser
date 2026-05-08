package com.mysunriser.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mysunriser.backend.Dao.MediaAssetDao;
import com.mysunriser.backend.Dao.PostMediaReferenceDao;
import com.mysunriser.backend.dto.AdminMediaListResponse;
import com.mysunriser.backend.dto.Codes;
import com.mysunriser.backend.dto.MediaAccessLevel;
import com.mysunriser.backend.dto.MediaAssetResponse;
import com.mysunriser.backend.dto.MediaAssetType;
import com.mysunriser.backend.entity.MediaAsset;
import com.mysunriser.backend.exception.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class MediaService {
    private static final Map<String, String> IMAGE_MIME_BY_EXTENSION = Map.of(
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "gif", "image/gif",
            "webp", "image/webp"
    );

    private static final Map<String, Set<String>> ATTACHMENT_MIME_BY_EXTENSION = Map.ofEntries(
            Map.entry("pdf", Set.of("application/pdf")),
            Map.entry("zip", Set.of("application/zip", "application/x-zip-compressed")),
            Map.entry("txt", Set.of("text/plain")),
            Map.entry("md", Set.of("text/markdown", "text/plain", "application/octet-stream")),
            Map.entry("csv", Set.of("text/csv", "application/vnd.ms-excel")),
            Map.entry("doc", Set.of("application/msword", "application/octet-stream")),
            Map.entry("docx", Set.of("application/vnd.openxmlformats-officedocument.wordprocessingml.document")),
            Map.entry("xls", Set.of("application/vnd.ms-excel")),
            Map.entry("xlsx", Set.of("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
            Map.entry("ppt", Set.of("application/vnd.ms-powerpoint")),
            Map.entry("pptx", Set.of("application/vnd.openxmlformats-officedocument.presentationml.presentation"))
    );

    private final MediaAssetDao mediaAssetDao;
    private final PostMediaReferenceDao postMediaReferenceDao;
    private final MediaStorageService mediaStorageService;
    private final long imageMaxBytes;
    private final long attachmentMaxBytes;

    public MediaService(
            MediaAssetDao mediaAssetDao,
            PostMediaReferenceDao postMediaReferenceDao,
            MediaStorageService mediaStorageService,
            @Value("${media.image-max-bytes}") long imageMaxBytes,
            @Value("${media.attachment-max-bytes}") long attachmentMaxBytes
    ) {
        this.mediaAssetDao = mediaAssetDao;
        this.postMediaReferenceDao = postMediaReferenceDao;
        this.mediaStorageService = mediaStorageService;
        this.imageMaxBytes = imageMaxBytes;
        this.attachmentMaxBytes = attachmentMaxBytes;
    }

    public MediaAssetResponse upload(MultipartFile file, String accessLevelValue, Authentication authentication) {
        if (file == null || file.isEmpty()) {
            throw new BizException(Codes.VALIDATION_ERROR, "file is required");
        }

        MediaAccessLevel accessLevel = parseAccessLevel(accessLevelValue);
        String originalFilename = cleanOriginalFilename(file.getOriginalFilename());
        String extension = extensionOf(originalFilename);
        String mimeType = normalizeMimeType(file.getContentType());
        MediaAssetType assetType = detectAssetType(extension, mimeType);
        assertSizeAllowed(file.getSize(), assetType);

        String storageKey = mediaStorageService.store(file, assetType, extension);

        MediaAsset asset = new MediaAsset();
        asset.setAssetType(assetType.name());
        asset.setOriginalFilename(originalFilename);
        asset.setStorageKey(storageKey);
        asset.setMimeType(mimeType);
        asset.setSizeBytes(file.getSize());
        asset.setAccessLevel(accessLevel.name());
        asset.setUploadedBy(authentication == null ? "unknown" : authentication.getName());

        if (mediaAssetDao.insert(asset) <= 0) {
            mediaStorageService.delete(storageKey);
            throw new BizException(Codes.INTERNAL_ERROR, "media create failed");
        }

        return MediaAssetResponse.of(mediaAssetDao.selectById(asset.getId()));
    }

    public AdminMediaListResponse listAdminMedia(int pageNum, int pageSize) {
        int safePage = Math.max(1, pageNum);
        int safePageSize = Math.max(1, Math.min(pageSize, 100));
        Page<MediaAsset> page = new Page<>(safePage, safePageSize);
        Page<MediaAsset> result = mediaAssetDao.selectAdminPage(page);

        List<MediaAssetResponse> items = result.getRecords().stream()
                .map(MediaAssetResponse::of)
                .toList();

        return new AdminMediaListResponse(safePage, safePageSize, result.getTotal(), items);
    }

    public void deleteUnreferenced(long id) {
        MediaAsset asset = findAsset(id);
        if (postMediaReferenceDao.countByMediaAssetId(id) > 0) {
            throw new BizException(Codes.VALIDATION_ERROR, "referenced media cannot be deleted");
        }

        mediaStorageService.delete(asset.getStorageKey());
        mediaAssetDao.deleteById(id);
    }

    public ResponseEntity<org.springframework.core.io.Resource> content(long id, Authentication authentication) {
        MediaAsset asset = findAsset(id);
        assertCanRead(asset, authentication);
        StoredMediaResource stored = mediaStorageService.load(asset.getStorageKey());

        return ResponseEntity.ok()
                .contentType(parseMediaType(asset.getMimeType()))
                .contentLength(stored.contentLength())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(asset.getOriginalFilename(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(stored.resource());
    }

    public ResponseEntity<org.springframework.core.io.Resource> download(long id, Authentication authentication) {
        MediaAsset asset = findAsset(id);
        assertCanRead(asset, authentication);
        StoredMediaResource stored = mediaStorageService.load(asset.getStorageKey());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(stored.contentLength())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(asset.getOriginalFilename(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(stored.resource());
    }

    private MediaAsset findAsset(long id) {
        MediaAsset asset = mediaAssetDao.selectById(id);
        if (asset == null) {
            throw new BizException(Codes.NOT_FOUND, "media not found");
        }

        return asset;
    }

    private void assertCanRead(MediaAsset asset, Authentication authentication) {
        if (isAdmin(authentication)) {
            return;
        }

        if (postMediaReferenceDao.countPublishedReferences(asset.getId()) <= 0) {
            throw new BizException(Codes.FORBIDDEN, "media is not public");
        }

        if (MediaAccessLevel.AUTHENTICATED.name().equals(asset.getAccessLevel()) && !isRealAuthenticated(authentication)) {
            throw new BizException(Codes.UNAUTHORIZED, "login required");
        }
    }

    private boolean isAdmin(Authentication authentication) {
        return isRealAuthenticated(authentication)
                && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    private boolean isRealAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private MediaAccessLevel parseAccessLevel(String accessLevelValue) {
        try {
            return MediaAccessLevel.from(accessLevelValue);
        } catch (IllegalArgumentException e) {
            throw new BizException(Codes.VALIDATION_ERROR, "invalid media access level");
        }
    }

    private String cleanOriginalFilename(String originalFilename) {
        String value = originalFilename == null ? "upload" : PathName.filename(originalFilename);
        if (value.isBlank()) {
            return "upload";
        }

        return value.length() > 255 ? value.substring(value.length() - 255) : value;
    }

    private String extensionOf(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            throw new BizException(Codes.VALIDATION_ERROR, "file extension is required");
        }

        return filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private String normalizeMimeType(String mimeType) {
        if (mimeType == null || mimeType.isBlank()) {
            throw new BizException(Codes.VALIDATION_ERROR, "file mime type is required");
        }

        return mimeType.toLowerCase(Locale.ROOT);
    }

    private MediaAssetType detectAssetType(String extension, String mimeType) {
        String imageMime = IMAGE_MIME_BY_EXTENSION.get(extension);
        if (imageMime != null) {
            if (!imageMime.equals(mimeType)) {
                throw new BizException(Codes.VALIDATION_ERROR, "file mime type does not match extension");
            }
            return MediaAssetType.IMAGE;
        }

        Set<String> attachmentMimes = ATTACHMENT_MIME_BY_EXTENSION.get(extension);
        if (attachmentMimes != null && attachmentMimes.contains(mimeType)) {
            return MediaAssetType.ATTACHMENT;
        }

        throw new BizException(Codes.VALIDATION_ERROR, "unsupported file type");
    }

    private void assertSizeAllowed(long size, MediaAssetType assetType) {
        long maxBytes = assetType == MediaAssetType.IMAGE ? imageMaxBytes : attachmentMaxBytes;
        if (size > maxBytes) {
            throw new BizException(Codes.VALIDATION_ERROR, "file is too large");
        }
    }

    private MediaType parseMediaType(String mimeType) {
        try {
            return MediaType.parseMediaType(mimeType);
        } catch (RuntimeException e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private static final class PathName {
        private PathName() {
        }

        private static String filename(String path) {
            return java.nio.file.Path.of(path).getFileName().toString();
        }
    }
}
