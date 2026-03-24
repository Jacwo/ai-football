package cn.xingxing.dto;

import lombok.Data;

/**
 * 比赛计算器API响应
 * @Author: yangyuanliang
 * @Date: 2026-03-24
 * @Version: 1.0
 */
@Data
public class MatchCalculatorResponse {
    private String dataFrom;
    private boolean emptyFlag;
    private String errorCode;
    private String errorMessage;
    private boolean success;
    private MatchCalculatorValue value;
}
