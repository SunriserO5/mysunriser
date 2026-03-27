package com.mysunriser.backend.dto;



class PageItems
{
    private String slug;
    private String title;
    private String summary;
    private String status;
    private int publishTime;
}
public class PageResponse {
    private int page;
    private int pageSize;
    private PageItems[] items=new PageItems[pageSize];
    

}
