package cn.xingxing.dto;


import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-30
 * @Version: 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AnalysisPageDTO extends CommonPageDTO{
    private String startDate;
    private String endDate;
    private String teamName;
    private String matchResult;
    private String searchKeyword;
}
