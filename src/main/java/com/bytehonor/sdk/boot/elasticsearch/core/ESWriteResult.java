package com.bytehonor.sdk.boot.elasticsearch.core;

public class ESWriteResult {
    private boolean success;

    private String message;

    private String action;

    public ESWriteResult() {
        this(true, "success", "");
    }

    public ESWriteResult(String action) {
        this(true, "success", action);
    }

    public ESWriteResult(boolean success, String message, String action) {
        this.success = success;
        this.message = message;
        this.action = action;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

}
