package com.mysunriser.backend.service;

import com.mysunriser.backend.Dao.PostDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class postservice {

    @Autowired
    private PostDao postDao;

    public Boolean getPost(){
        return false;
    }
}
