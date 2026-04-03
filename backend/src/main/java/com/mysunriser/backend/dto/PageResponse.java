package com.mysunriser.backend.dto;

import com.mysunriser.backend.entity.PageItems;

import java.util.List;


public class PageResponse {
    private int page;
    private int pageSize;
    private List<PageItems> items;

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }


    public List<PageItems> getItems() {
        return items;
    }
    public int getPageSize() {
        return pageSize;
    }

    public int getPage() {
        return page;
    }
    public PageResponse(int page,int pagesize,List<PageItems> items){
        this.page=page;
        this.pageSize=pagesize;
        this.items=items;
    }
    public static PageResponse of(int page,int pagesize,List<PageItems> items){

       return new PageResponse(
               page,pagesize,items
       );
    }
}
