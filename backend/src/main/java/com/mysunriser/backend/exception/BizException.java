//package com.mysunriser.backend.exception;
//
//public class BizException extends RuntimeException{
//    private final int code;
//    public BizException(int code,String message){
//        this.code=code;
//        super(message);
//    }
//
//    public int getCode() {
//        return code;
//    }
//}
package com.mysunriser.backend.exception;

public class BizException extends RuntimeException {
    private final int code;

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}