package cn.xingxing.dto;


import lombok.Builder;
import lombok.Data;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-31
 * @Version: 1.0
 */
@Data
@Builder
public class AnalysisResultDto {
    private long timestamp;
    private String aiAnalysis;
}
