package cn.xingxing.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 比赛长龙详情
 *
 * @Author: yangyuanliang
 * @Date: 2025-01-15
 * @Version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchDragonDetail {

    /**
     * 比赛ID
     */
    private Integer matchId;

    /**
     * 比赛编号
     */
    private String matchNumStr;

    /**
     * 主队名称
     */
    private String homeTeam;

    /**
     * 客队名称
     */
    private String awayTeam;

    /**
     * 比赛时间
     */
    private String matchTime;

    /**
     * 全场比分
     */
    private String fullScore;

    /**
     * 开奖结果 (H/D/A)
     */
    private String result;

    /**
     * 结果描述（主胜/平局/客胜）
     */
    private String resultDesc;

    /**
     * 主胜赔率
     */
    private BigDecimal homeOdds;

    /**
     * 平局赔率
     */
    private BigDecimal drawOdds;

    /**
     * 客胜赔率
     */
    private BigDecimal awayOdds;

    /**
     * 开奖赔率（中奖的赔率）
     */
    private BigDecimal winningOdds;

    /**
     * 是否最高赔率
     */
    private Boolean isMaxOdds;

    /**
     * 是否最低赔率
     */
    private Boolean isMinOdds;

    /**
     * 距离现在第几场（0表示最新一场）
     */
    private Integer gapFromNow;
}
