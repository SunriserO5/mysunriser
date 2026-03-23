package com.mysunriser.backend.dto;



public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;
    private long timeStamp;
    private String traceId;
    private ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timeStamp = System.currentTimeMillis();
    }
    public static <T> ApiResponse<T> success(T data){
        return new ApiResponse<>(Codes.OK,"ok",data);
    }
    public static <T> ApiResponse<T> error(int Code,String message){
        return new ApiResponse<>(Code,message,null);
    }
}

