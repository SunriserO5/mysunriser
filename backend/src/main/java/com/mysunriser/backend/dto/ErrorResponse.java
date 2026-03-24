package com.mysunriser.backend.dto;

public class ErrorResponse {
    private int code;
    private String message;
    private long timeStamp;
    //private String traceId;
    private ErrorResponse(int errCode,String errMsg){
        this.code=errCode;
        this.message=errMsg;
        this.timeStamp=System.currentTimeMillis();
    }

    public static ErrorResponse of(int errCode,String errMsg){
        return new ErrorResponse(errCode,errMsg);
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public long getTimeStamp() {
        return timeStamp;
    }
}
