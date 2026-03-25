package cn.xingxing.dto;

import lombok.Data;

import java.util.List;

/**
 * 比赛结果详情响应类
 * @Author: yangyuanliang
 * @Date: 2026-03-25
 * @Version: 1.0
 */
@Data
public class MatchResultDetailResponse {
    private String dataFrom;
    private Boolean emptyFlag;
    private String errorCode;
    private String errorMessage;
    private Boolean success;
    private MatchResultDetailValue value;

    @Data
    public static class MatchResultDetailValue {
        private List<Object> eventList;
        private Integer matchId;
        private String matchMinute;
        private String matchMinuteExtra;
        private String matchPhaseTc;
        private String matchPhaseTcName;
        private List<MatchResultItem> matchResultList;
        private String matchStatus;
        private String matchStatusName;
        private List<Object> poolList;
        private String sectionsExtra;
        private String sectionsNo1;
        private String sectionsNo999;
        private String sectionsPenalty;
    }

    @Data
    public static class MatchResultItem {
        /**
         * 组合代码 (H/D/A, H:H, 3:2, 5等)
         */
        private String combination;

        /**
         * 组合描述
         */
        private String combinationDesc;

        /**
         * 赔率
         */
        private String odds;

        /**
         * 玩法代码 (HAD, HHAD, TTG, HAFU, CRS)
         */
        private String poolCode;
    }
}
