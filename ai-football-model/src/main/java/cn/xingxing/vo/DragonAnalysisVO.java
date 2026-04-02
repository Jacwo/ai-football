package cn.xingxing.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 长龙分析结果VO
 *
 * @Author: yangyuanliang
 * @Date: 2025-01-15
 * @Version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DragonAnalysisVO {

    /**
     * 分析的总场次
     */
    private Integer totalMatches;

    /**
     * 距离上次主胜多少场（0表示上一场就是主胜）
     */
    private Integer gapsSinceLastHomeWin;

    /**
     * 距离上次平局多少场
     */
    private Integer gapsSinceLastDraw;

    /**
     * 距离上次客胜多少场
     */
    private Integer gapsSinceLastAwayWin;

    /**
     * 距离上次出最高赔率多少场
     */
    private Integer gapsSinceLastMaxOdds;

    /**
     * 距离上次出最低赔率多少场
     */
    private Integer gapsSinceLastMinOdds;

    /**
     * 主胜最长未出场次（历史记录）
     */
    private Integer maxHomeWinDragon;

    /**
     * 平局最长未出场次（历史记录）
     */
    private Integer maxDrawDragon;

    /**
     * 客胜最长未出场次（历史记录）
     */
    private Integer maxAwayWinDragon;

    /**
     * 当前主胜连续未出场次
     */
    private Integer currentHomeWinDragon;

    /**
     * 当前平局连续未出场次
     */
    private Integer currentDrawDragon;

    /**
     * 当前客胜连续未出场次
     */
    private Integer currentAwayWinDragon;

    /**
     * 主胜出现次数
     */
    private Integer homeWinCount;

    /**
     * 平局出现次数
     */
    private Integer drawCount;

    /**
     * 客胜出现次数
     */
    private Integer awayWinCount;

    /**
     * 主胜出现率(%)
     */
    private Double homeWinRate;

    /**
     * 平局出现率(%)
     */
    private Double drawRate;

    /**
     * 客胜出现率(%)
     */
    private Double awayWinRate;

    /**
     * 最高赔率出现次数
     */
    private Integer maxOddsWinCount;

    /**
     * 最低赔率出现次数
     */
    private Integer minOddsWinCount;

    /**
     * 最高赔率命中率(%)
     */
    private Double maxOddsWinRate;

    /**
     * 最低赔率命中率(%)
     */
    private Double minOddsWinRate;

    /**
     * 详细比赛记录（按时间倒序，最新的在前）
     */
    private List<MatchDragonDetail> matchDetails;
}
