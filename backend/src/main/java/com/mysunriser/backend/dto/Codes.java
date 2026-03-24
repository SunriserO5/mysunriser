package com.mysunriser.backend.dto;

public final class Codes {
    public static final int OK = 0;
    public static final int VALIDATION_ERROR = 10001;
    public static final int UNAUTHORIZED = 10002;
    public static final int FORBIDDEN = 10003;
    public static final int NOT_FOUND = 10004;
    public static final int TOO_MANY_REQUESTS = 10005;
    public static final int INTERNAL_ERROR = 20000;
    public static final int OTHER_ERROR = 9999;
    private Codes() {}


}