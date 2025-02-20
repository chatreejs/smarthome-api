package dev.chatree.smarthomeapi.constant;

public class MessageConstants {

    public static final String LOG_REQUEST_PATTERN;
    public static final String LOG_USER_REQUEST_PATTERN;
    public static final String LOG_REQUEST_PARAMETER_PATTERN;
    public static final String LOG_REQUEST_BODY_PATTERN;
    public static final String SUCCESS_RESPONSE;
    public static final String ACCESS_DENIED;

    private MessageConstants() {
    }

    static {
        LOG_REQUEST_PATTERN = "Received {} request with url {}";
        LOG_USER_REQUEST_PATTERN = "Received {} request with url {} from user {}";
        LOG_REQUEST_PARAMETER_PATTERN = "Request parameter: {} = {}";
        LOG_REQUEST_BODY_PATTERN = "Request body: {}";
        SUCCESS_RESPONSE = "Request Success";
        ACCESS_DENIED = "Access Denied";
    }
}
