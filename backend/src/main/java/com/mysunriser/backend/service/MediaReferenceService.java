package com.mysunriser.backend.service;

import com.mysunriser.backend.Dao.MediaAssetDao;
import com.mysunriser.backend.Dao.PostMediaReferenceDao;
import com.mysunriser.backend.dto.Codes;
import com.mysunriser.backend.exception.BizException;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MediaReferenceService {
    private static final Pattern MEDIA_URL_PATTERN = Pattern.compile("/api/media/(\\d+)/(?:content|download)");

    private final MediaAssetDao mediaAssetDao;
    private final PostMediaReferenceDao postMediaReferenceDao;

    public MediaReferenceService(MediaAssetDao mediaAssetDao, PostMediaReferenceDao postMediaReferenceDao) {
        this.mediaAssetDao = mediaAssetDao;
        this.postMediaReferenceDao = postMediaReferenceDao;
    }

    public void rebuildReferences(long postId, String markdown) {
        Set<Long> mediaIds = extractMediaIds(markdown);

        for (Long mediaId : mediaIds) {
            if (mediaAssetDao.selectById(mediaId) == null) {
                throw new BizException(Codes.VALIDATION_ERROR, "referenced media not found");
            }
        }

        postMediaReferenceDao.deleteByPostId(postId);
        for (Long mediaId : mediaIds) {
            postMediaReferenceDao.insertReference(postId, mediaId);
        }
    }

    private Set<Long> extractMediaIds(String markdown) {
        Set<Long> mediaIds = new LinkedHashSet<>();
        if (markdown == null || markdown.isBlank()) {
            return mediaIds;
        }

        Matcher matcher = MEDIA_URL_PATTERN.matcher(markdown);
        while (matcher.find()) {
            mediaIds.add(Long.valueOf(matcher.group(1)));
        }

        return mediaIds;
    }
}
