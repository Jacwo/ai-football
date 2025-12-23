package cn.xingxing.dto;

public class MatchInfoResponse {
    private String dataFrom;
    private boolean emptyFlag;
    private String errorCode;
    private String errorMessage;
    private boolean success;
    private MatchInfoValue value;

    // Getters and setters
    public String getDataFrom() {
        return dataFrom;
    }

    public void setDataFrom(String dataFrom) {
        this.dataFrom = dataFrom;
    }

    public boolean isEmptyFlag() {
        return emptyFlag;
    }

    public void setEmptyFlag(boolean emptyFlag) {
        this.emptyFlag = emptyFlag;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public MatchInfoValue getValue() {
        return value;
    }

    public void setValue(MatchInfoValue value) {
        this.value = value;
    }
}

