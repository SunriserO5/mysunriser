package com.mysunriser.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mysunriser.backend.Dao.PostDao;
import com.mysunriser.backend.dto.Codes;
import com.mysunriser.backend.dto.PageResponse;
import com.mysunriser.backend.dto.PostResponse;
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
}
