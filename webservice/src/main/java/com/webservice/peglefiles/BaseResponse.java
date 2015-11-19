package com.webservice.peglefiles;

public class BaseResponse {
    private boolean success = true;
    private String message = null;

    public static BaseResponse ok() {
        BaseResponse obj = new BaseResponse();
        obj.setSuccess(true);
        obj.setMessage("OK");
        return obj;
    }

    public static BaseResponse error(String message) {
        BaseResponse obj = new BaseResponse();
        obj.setSuccess(false);
        obj.setMessage(message);
        return obj;
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

}
