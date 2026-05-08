package com.mysunriser.backend.service;

import com.mysunriser.backend.dto.Codes;
import com.mysunriser.backend.dto.MediaAssetType;
import com.mysunriser.backend.exception.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class MediaStorageService {
    private final Path storageRoot;

    public MediaStorageService(@Value("${media.storage-root}") String storageRoot) {
        this.storageRoot = Path.of(storageRoot).toAbsolutePath().normalize();
    }

    public String store(MultipartFile file, MediaAssetType assetType, String extension) {
        String storageKey = assetType.name().toLowerCase() + "/" + java.util.UUID.randomUUID() + "." + extension;
        Path target = resolveStoragePath(storageKey);

        try {
            Files.createDirectories(target.getParent());
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new BizException(Codes.INTERNAL_ERROR, "media store failed");
        }

        return storageKey;
    }

    public StoredMediaResource load(String storageKey) {
        Path path = resolveStoragePath(storageKey);

        try {
            UrlResource resource = new UrlResource(path.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new BizException(Codes.NOT_FOUND, "media file not found");
            }

            return new StoredMediaResource(resource, Files.size(path));
        } catch (MalformedURLException e) {
            throw new BizException(Codes.VALIDATION_ERROR, "invalid media path");
        } catch (IOException e) {
            throw new BizException(Codes.INTERNAL_ERROR, "media read failed");
        }
    }

    public void delete(String storageKey) {
        Path path = resolveStoragePath(storageKey);

        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new BizException(Codes.INTERNAL_ERROR, "media delete failed");
        }
    }

    private Path resolveStoragePath(String storageKey) {
        Path path = storageRoot.resolve(storageKey).normalize();
        if (!path.startsWith(storageRoot)) {
            throw new BizException(Codes.VALIDATION_ERROR, "invalid media path");
        }

        return path;
    }
}
