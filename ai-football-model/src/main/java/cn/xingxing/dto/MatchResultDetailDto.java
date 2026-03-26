package cn.xingxing.dto;


import lombok.Data;

import java.math.BigDecimal;

/**
 * 比赛结果详情实体类
 * @Author: yangyuanliang
 * @Date: 2026-03-25
 * @Version: 1.0
 */
@Data
public class MatchResultDetailDto {

    private Integer matchId;
    /**
     * 联赛名称
     */
    private String leagueName;

    private String matchDate;

    private String matchTime;
    /**
     * 主队名称
     */
    private String homeName;
    /**
     * 客队名称
     */
    private String awayName;


    /**
     * 比赛状态码
     */
    private String matchStatus;

    /**
     * 比赛状态名称
     */
    private String matchStatusName;

    /**
     * 比赛进行时间
     */
    private String matchMinute;

    /**
     * 补时分钟
     */
    private String matchMinuteExtra;

    /**
     * 比赛阶段代码
     */
    private String matchPhaseTc;

    /**
     * 比赛阶段名称
     */
    private String matchPhaseTcName;

    /**
     * 半场比分
     */
    private String sectionsNo1;

    /**
     * 全场比分
     */
    private String sectionsNo999;

    /**
     * 加时赛比分
     */
    private String sectionsExtra;

    /**
     * 点球比分
     */
    private String sectionsPenalty;

    /**
     * 胜平负开奖结果(H/D/A)
     */
    private String hadResult;

    /**
     * 胜平负结果描述
     */
    private String hadCombinationDesc;

    /**
     * 胜平负赔率
     */
    private BigDecimal hadOdds;

    /**
     * 让球胜平负开奖结果
     */
    private String hhadResult;

    /**
     * 让球胜平负结果描述
     */
    private String hhadCombinationDesc;

    /**
     * 让球胜平负赔率
     */
    private BigDecimal hhadOdds;

    /**
     * 总进球开奖结果
     */
    private String ttgResult;

    /**
     * 总进球结果描述
     */
    private String ttgCombinationDesc;

    /**
     * 总进球赔率
     */
    private BigDecimal ttgOdds;

    /**
     * 半全场开奖结果
     */
    private String hafuResult;

    /**
     * 半全场结果描述
     */
    private String hafuCombinationDesc;

    /**
     * 半全场赔率
     */
    private BigDecimal hafuOdds;

    /**
     * 比分开奖结果
     */
    private String crsResult;

    /**
     * 比分结果描述
     */
    private String crsCombinationDesc;

    /**
     * 比分赔率
     */
    private BigDecimal crsOdds;
}
