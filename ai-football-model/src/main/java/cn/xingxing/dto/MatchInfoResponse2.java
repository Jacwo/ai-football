package cn.xingxing.dto;

public class MatchInfoResponse2 {
    private String dataFrom;
    private boolean emptyFlag;
    private String errorCode;
    private String errorMessage;
    private boolean success;
    private MatchInfoValue2 value;

    // Getter and Setter methods
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

    public MatchInfoValue2 getValue() {
        return value;
    }

    public void setValue(MatchInfoValue2 value) {
        this.value = value;
    }
}

