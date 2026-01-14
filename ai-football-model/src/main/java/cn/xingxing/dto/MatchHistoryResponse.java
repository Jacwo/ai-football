package cn.xingxing.dto;


import lombok.Data;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-23
 * @Version: 1.0
 */
@Data
public class MatchHistoryResponse {
    public String dataFrom;
    public boolean emptyFlag;
    public String errorCode;
    public String errorMessage;
    public boolean success;
    public MatchHistoryData value;
}
