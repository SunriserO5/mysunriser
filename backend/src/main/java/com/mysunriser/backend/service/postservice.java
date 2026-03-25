package com.mysunriser.backend.service;

import com.mysunriser.backend.Dao.PostDao;
import com.mysunriser.backend.dto.Codes;
import com.mysunriser.backend.dto.PostResponse;
import com.mysunriser.backend.entity.post;
import com.mysunriser.backend.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
