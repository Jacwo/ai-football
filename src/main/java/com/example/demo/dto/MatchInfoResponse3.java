package com.example.demo.dto;

import java.util.List;

public class MatchInfoResponse3 {
    private String dataFrom;
    private boolean emptyFlag;
    private String errorCode;
    private String errorMessage;
    private boolean success;
    private MatchInfoValue3 value;

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

    public MatchInfoValue3 getValue() {
        return value;
    }

    public void setValue(MatchInfoValue3 value) {
        this.value = value;
    }
}

