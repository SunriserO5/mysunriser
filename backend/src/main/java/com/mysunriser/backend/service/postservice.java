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
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class postservice {

    @Autowired
    private PostDao postDao;

    public PostResponse getPostBySlug(String slug){
        post postEntity = postDao.getBySlug(slug);
        if (postEntity == null) {
            throw new BizException(Codes.NOT_FOUND, "post not found");
        }
        return PostResponse.of(postEntity);
    }

    public PageResponse getPage(int pageNum ,int PageSize){
        Page<PageItems> page=new Page<>(pageNum,PageSize);
        Page<PageItems> result=(Page<PageItems>) postDao.selectPageItems(page);

        return PageResponse.of(pageNum, PageSize, result.getRecords());
    }

    public String initPost(post newpost){
        boolean statues = postDao.insertOrUpdate(newpost);
        if(!statues)return "Create Failed";
        else return "Success!";

    }

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

        return PostResponse.of(postDao.getBySlug(slug));
    }

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

        return PostResponse.of(postDao.getBySlug(slug));
    }
}
