package cn.xingxing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 投注方案保存DTO
 * @Author: yangyuanliang
 * @Date: 2026-03-24
 * @Version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BetSchemeSaveDto {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 比赛选择列表
     */
    private List<MatchSelection> selections;

    /**
     * 过关方式(多个用逗号分隔,如:single,2_1)
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
     * 比赛选择
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatchSelection {
        /**
         * 比赛ID
         */
        private Long matchId;

        /**
         * 选项列表
         */
        private List<BetOption> options;
    }

    /**
     * 投注选项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BetOption {
        /**
         * 选项类型(had-胜平负,hhad-让球胜平负,ttg-总进球,hafu-半全场,crs-比分)
         */
        private String type;

        /**
         * 选项值(如:3,0:0,1:1等)
         */
        private String value;

        /**
         * 赔率
         */
        private Double odds;
        /**
         * 是否选中
         */
        private Boolean checked;
    }
}
