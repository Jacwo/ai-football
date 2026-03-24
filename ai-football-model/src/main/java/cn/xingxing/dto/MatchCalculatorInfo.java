package cn.xingxing.dto;

import lombok.Data;
import java.util.List;

/**
 * 比赛计算器信息
 * @Author: yangyuanliang
 * @Date: 2026-03-24
 * @Version: 1.0
 */
@Data
public class MatchCalculatorInfo {
    private String businessDate;
    private List<SubMatchCalculator> subMatchList;
    private String weekday;
    private Integer matchCount;
    private String matchNumDate;
}
