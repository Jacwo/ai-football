package cn.xingxing.service;

import cn.xingxing.entity.HadList;
import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 赔率深度分析工具
 * 提供专业的赔率转换和价值分析
 */
@Component
public class OddsAnalyzer {

    private static final double BOOKMAKER_MARGIN = 0.08; // 博彩公司抽水比例约8%

    /**
     * 分析赔率数据，生成结构化的分析报告
     */
    public OddsAnalysisResult analyze(List<HadList> hadLists, List<HadList> hhadLists) {
        OddsAnalysisResult result = new OddsAnalysisResult();

        if (CollectionUtils.isEmpty(hadLists)) {
            return result;
        }

        HadList latest = hadLists.getFirst();
        double h = parseOdds(latest.getH());
        double d = parseOdds(latest.getD());
        double a = parseOdds(latest.getA());

        // 1. 计算隐含概率(去除抽水)
        double totalProb = 1/h + 1/d + 1/a;
        double margin = totalProb - 1;
        result.setMargin(margin * 100);

        double homeProb = (1/h) / totalProb * 100;
        double drawProb = (1/d) / totalProb * 100;
        double awayProb = (1/a) / totalProb * 100;

        result.setHomeImpliedProb(homeProb);
        result.setDrawImpliedProb(drawProb);
        result.setAwayImpliedProb(awayProb);

        // 2. 分析赔率特征
        result.setOddsPattern(analyzeOddsPattern(h, d, a));

        // 3. 计算凯利指数参考值
        result.setHomeKelly(calculateKelly(homeProb/100, h));
        result.setDrawKelly(calculateKelly(drawProb/100, d));
        result.setAwayKelly(calculateKelly(awayProb/100, a));

        // 4. 分析赔率变化趋势
        if (hadLists.size() >= 2) {
            result.setOddsTrend(analyzeOddsTrend(hadLists));
        }

        // 5. 分析让球盘
        if (!CollectionUtils.isEmpty(hhadLists)) {
            result.setHandicapAnalysis(analyzeHandicap(hhadLists.getFirst()));
        }

        // 6. 生成关键信号
        result.setKeySignals(generateKeySignals(result, h, d, a));

        return result;
    }

    private double parseOdds(String odds) {
        try {
            return Double.parseDouble(odds);
        } catch (Exception e) {
            return 0;
        }
    }

    private String analyzeOddsPattern(double h, double d, double a) {
        StringBuilder pattern = new StringBuilder();

        // 判断比赛类型
        if (h < 1.5) {
            pattern.append("【强主场】主队绝对优势盘口，市场极度看好主队；");
        } else if (h < 1.8) {
            pattern.append("【主队优势】主队明显优势，但非碾压局；");
        } else if (a < 1.5) {
            pattern.append("【强客场】客队实力碾压，反客为主概率大；");
        } else if (a < 1.8) {
            pattern.append("【客队优势】客队占优，主场因素被弱化；");
        } else if (Math.abs(h - a) < 0.3) {
            pattern.append("【势均力敌】两队实力接近，结果难以预测；");
        }

        // 平局概率分析
        if (d < 3.0) {
            pattern.append("【平局热】平局赔率偏低，市场预期闷平可能；");
        } else if (d > 3.8) {
            pattern.append("【平局冷】平局概率被压低，预期会分胜负；");
        }

        // 特殊盘口识别
        double totalOdds = h + d + a;
        if (totalOdds < 7.0) {
            pattern.append("【低赔总和】博彩公司信心足，结果相对明朗；");
        } else if (totalOdds > 8.5) {
            pattern.append("【高赔总和】不确定性高，冷门概率增加；");
        }

        return pattern.toString();
    }

    private double calculateKelly(double prob, double odds) {
        // 凯利公式: f = (bp - q) / b
        // b = 赔率 - 1, p = 胜率, q = 1 - p
        double b = odds - 1;
        double q = 1 - prob;
        if (b <= 0) return 0;
        return (b * prob - q) / b;
    }

    private String analyzeOddsTrend(List<HadList> hadLists) {
        StringBuilder trend = new StringBuilder();

        HadList first = hadLists.getFirst();  // 最新
        HadList last = hadLists.getLast();   // 最早

        double hChange = parseOdds(first.getH()) - parseOdds(last.getH());
        double dChange = parseOdds(first.getD()) - parseOdds(last.getD());
        double aChange = parseOdds(first.getA()) - parseOdds(last.getA());

        if (Math.abs(hChange) > 0.1) {
            trend.append(hChange > 0 ? "主胜赔率上升(降热)，" : "主胜赔率下降(升热)，");
        }
        if (Math.abs(dChange) > 0.1) {
            trend.append(dChange > 0 ? "平局赔率上升，" : "平局赔率下降(关注平局)，");
        }
        if (Math.abs(aChange) > 0.1) {
            trend.append(aChange > 0 ? "客胜赔率上升(降热)，" : "客胜赔率下降(升热)，");
        }

        if (trend.isEmpty()) {
            trend.append("赔率稳定，市场共识明确");
        }

        return trend.toString();
    }

    private String analyzeHandicap(HadList handicap) {
        StringBuilder analysis = new StringBuilder();
        String goalLine = handicap.getGoalLine();

        if (goalLine != null) {
            analysis.append("让球数: ").append(goalLine).append("；");

            double hProb = parseOdds(handicap.getH());
            double aProb = parseOdds(handicap.getA());

            if (hProb > 0 && aProb > 0) {
                if (hProb < aProb) {
                    analysis.append("让球方被看好能赢盘；");
                } else if (aProb < hProb) {
                    analysis.append("受让方更被看好；");
                } else {
                    analysis.append("让球盘两边均势；");
                }
            }
        }

        return analysis.toString();
    }

    private String generateKeySignals(OddsAnalysisResult result, double h, double d, double a) {
        StringBuilder signals = new StringBuilder();

        // 冷门信号检测
        if (result.getHomeImpliedProb() > 50 && h > 2.0) {
            signals.append("⚠️ 主队隐含概率与赔率背离，警惕冷门；");
        }

        if (result.getDrawImpliedProb() > 30 && d > 3.5) {
            signals.append("⚠️ 平局概率被低估，关注闷平可能；");
        }

        // 凯利值信号
        if (result.getHomeKelly() > 0.1) {
            signals.append("📈 主胜存在正期望值；");
        }
        if (result.getDrawKelly() > 0.1) {
            signals.append("📈 平局存在正期望值；");
        }
        if (result.getAwayKelly() > 0.1) {
            signals.append("📈 客胜存在正期望值；");
        }

        // 极端赔率警告
        if (h < 1.3 || a < 1.3) {
            signals.append("⚡ 极低赔率，冷门价值高但风险大；");
        }

        return signals.isEmpty() ? "暂无特殊信号" : signals.toString();
    }

    @Data
    public static class OddsAnalysisResult {
        private double homeImpliedProb;  // 主胜隐含概率
        private double drawImpliedProb;  // 平局隐含概率
        private double awayImpliedProb;  // 客胜隐含概率
        private double margin;           // 博彩公司抽水比例
        private String oddsPattern;      // 赔率特征分析
        private double homeKelly;        // 主胜凯利值
        private double drawKelly;        // 平局凯利值
        private double awayKelly;        // 客胜凯利值
        private String oddsTrend;        // 赔率变化趋势
        private String handicapAnalysis; // 让球盘分析
        private String keySignals;       // 关键信号

        @Override
        public String toString() {
            return String.format("""

                ## 赔率深度分析

                ### 隐含概率(去除抽水%.1f%%)
                | 主胜 | 平局 | 客胜 |
                |------|------|------|
                | %.1f%% | %.1f%% | %.1f%% |

                ### 赔率特征
                %s

                ### 凯利指数参考
                - 主胜凯利值: %.3f %s
                - 平局凯利值: %.3f %s
                - 客胜凯利值: %.3f %s

                ### 赔率变动趋势
                %s

                ### 让球盘分析
                %s

                ### 关键信号
                %s
                """,
                margin,
                homeImpliedProb, drawImpliedProb, awayImpliedProb,
                oddsPattern != null ? oddsPattern : "暂无",
                homeKelly, homeKelly > 0 ? "(有价值)" : "",
                drawKelly, drawKelly > 0 ? "(有价值)" : "",
                awayKelly, awayKelly > 0 ? "(有价值)" : "",
                oddsTrend != null ? oddsTrend : "暂无数据",
                handicapAnalysis != null ? handicapAnalysis : "暂无数据",
                keySignals != null ? keySignals : "暂无"
            );
        }
    }
}
