package cn.xingxing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 投注方案查询VO
 * @Author: yangyuanliang
 * @Date: 2026-03-24
 * @Version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BetSchemeVo {

    /**
     * 方案ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private String userId;
    /**
     * 用户名称
     */
    private String userName;

    /**
     * 方案编号
     */
    private String schemeNo;

    /**
     * 过关方式
     */
    private List<String> passTypes;

    /**
     * 倍数
     */
    private Integer multiple;

    /**
     * 总注数
     */
    private Integer totalBets;

    /**
     * 总金额(元)
     */
    private BigDecimal totalAmount;

    /**
     * 方案状态(0-待开奖,1-已中奖,2-未中奖,3-已取消)
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 比赛明细列表
     */
    private List<MatchDetail> matchDetails;

    /**
     * 比赛明细
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatchDetail {
        /**
         * 比赛ID
         */
        private Long matchId;

        /**
         * 比赛编号
         */
        private String matchNumStr;

        /**
         * 主队名称
         */
        private String homeTeamName;

        /**
         * 客队名称
         */
        private String awayTeamName;

        /**
         * 比赛时间
         */
        private String matchTime;

        /**
         * 选项列表
         */
        private List<OptionDetail> options;
    }

    /**
     * 选项详情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionDetail {
        /**
         * 选项类型
         */
        private String optionType;

        /**
         * 选项类型描述
         */
        private String optionTypeDesc;

        /**
         * 选项值
         */
        private String optionValue;

        /**
         * 赔率
         */
        private BigDecimal odds;

        /**
         * 是否命中(0-未命中,1-命中,NULL-未开奖)
         */
        private Integer isHit;


        /**
         * 比赛开奖结果(根据option_type存储对应玩法的结果)
         */
        private String matchResult;

        /**
         * 开奖结果描述
         */
        private String matchResultDesc;

        /**
         * 开奖赔率
         */
        private BigDecimal resultOdds;

        /**
         * 开奖校验时间
         */
        private LocalDateTime checkTime;
    }
}
