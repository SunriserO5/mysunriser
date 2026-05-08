package com.mysunriser.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mysunriser.backend.Dao.PostDao;
import com.mysunriser.backend.dto.Codes;
import com.mysunriser.backend.dto.CreatePostRequest;
import com.mysunriser.backend.dto.PageResponse;
import com.mysunriser.backend.dto.PostResponse;
import com.mysunriser.backend.dto.UpdatePostRequest;
import com.mysunriser.backend.entity.PageItems;
import com.mysunriser.backend.entity.post;
import com.mysunriser.backend.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class postservice {

    @Autowired
    private PostDao postDao;

    @Autowired
    private MediaReferenceService mediaReferenceService;

    public PostResponse getPostBySlug(String slug, Authentication authentication){
        post postEntity = isAdmin(authentication) ? postDao.getBySlug(slug) : postDao.getPublishedBySlug(slug);
        if (postEntity == null) {
            throw new BizException(Codes.NOT_FOUND, "post not found");
        }
        return PostResponse.of(postEntity);
    }

    public PageResponse getPage(int pageNum ,int PageSize, Authentication authentication){
        Page<PageItems> page=new Page<>(pageNum,PageSize);
        Page<PageItems> result = isAdmin(authentication)
                ? (Page<PageItems>) postDao.selectPageItems(page)
                : (Page<PageItems>) postDao.selectPublishedPageItems(page);

        return PageResponse.of(pageNum, PageSize, result.getRecords());
    }

    @Transactional
    public String initPost(post newpost){
        boolean statues = postDao.insertOrUpdate(newpost);
        if(!statues)return "Create Failed";

        post savedPost = postDao.getBySlug(newpost.getSlug());
        if (savedPost != null) {
            mediaReferenceService.rebuildReferences(savedPost.getId(), savedPost.getContent());
        }

        return "Success!";

    }

    @Transactional
    public PostResponse createPost(CreatePostRequest request) {
        String slug = request.getSlug().trim();
        if (postDao.getBySlug(slug) != null) {
            throw new BizException(Codes.VALIDATION_ERROR, "post slug already exists");
        }

        post newPost = request.toCreateEntity();
        boolean created = postDao.insert(newPost) > 0;
        if (!created) {
            throw new BizException(Codes.INTERNAL_ERROR, "post create failed");
        }

        post savedPost = postDao.getBySlug(slug);
        mediaReferenceService.rebuildReferences(savedPost.getId(), savedPost.getContent());

        return PostResponse.of(savedPost);
    }

    @Transactional
    public PostResponse updatePost(String slug, UpdatePostRequest request) {
        post existingPost = postDao.getBySlug(slug);
        if (existingPost == null) {
            throw new BizException(Codes.NOT_FOUND, "post not found");
        }

        existingPost.setTitle(request.title().trim());
        existingPost.setContent(request.content());
        existingPost.setStatus(request.status().trim());
        existingPost.setPublished_at(request.publishedAt());

        boolean updated = postDao.updateById(existingPost) > 0;
        if (!updated) {
            throw new BizException(Codes.INTERNAL_ERROR, "post update failed");
        }

        post savedPost = postDao.getBySlug(slug);
        mediaReferenceService.rebuildReferences(savedPost.getId(), savedPost.getContent());

        return PostResponse.of(savedPost);
    }

    @Transactional
    public void deletePost(String slug) {
        post existingPost = postDao.getBySlug(slug);
        if (existingPost == null) {
            throw new BizException(Codes.NOT_FOUND, "post not found");
        }

        boolean deleted = postDao.deleteById(existingPost.getId()) > 0;
        if (!deleted) {
            throw new BizException(Codes.INTERNAL_ERROR, "post delete failed");
        }
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)
                && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }
}
