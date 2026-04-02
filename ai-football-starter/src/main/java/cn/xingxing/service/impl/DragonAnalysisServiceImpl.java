package cn.xingxing.service.impl;

import cn.xingxing.entity.HadList;
import cn.xingxing.entity.MatchResultDetail;
import cn.xingxing.entity.SubMatchInfo;
import cn.xingxing.mapper.HadListMapperMapper;
import cn.xingxing.mapper.MatchInfoMapper;
import cn.xingxing.mapper.MatchResultDetailMapper;
import cn.xingxing.service.DragonAnalysisService;
import cn.xingxing.vo.DragonAnalysisVO;
import cn.xingxing.vo.MatchDragonDetail;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 长龙分析Service实现类
 *
 * @Author: yangyuanliang
 * @Date: 2025-01-15
 * @Version: 1.0
 */
@Slf4j
@Service
public class DragonAnalysisServiceImpl implements DragonAnalysisService {

    @Autowired
    private MatchInfoMapper matchInfoMapper;

    @Autowired
    private MatchResultDetailMapper matchResultDetailMapper;

    @Autowired
    private HadListMapperMapper hadListMapper;

    @Override
    public DragonAnalysisVO analyzeDragon(Integer sampleSize) {
        if (sampleSize == null || sampleSize <= 0) {
            sampleSize = 30; // 默认30场
        }

        log.info("开始分析近{}场单关比赛的长龙情况", sampleSize);

        // 1. 查询近N场单关比赛（按matchId倒序，最新的在前）
        LambdaQueryWrapper<SubMatchInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SubMatchInfo::getIsSingleMatch, true)
                .orderByDesc(SubMatchInfo::getMatchId)
                .last("LIMIT " + sampleSize);

        List<SubMatchInfo> matchInfoList = matchInfoMapper.selectList(queryWrapper);

        if (matchInfoList.isEmpty()) {
            log.warn("没有找到单关比赛数据");
            return DragonAnalysisVO.builder()
                    .totalMatches(0)
                    .matchDetails(new ArrayList<>())
                    .build();
        }

        log.info("查询到{}场单关比赛", matchInfoList.size());

        // 2. 构建详细数据列表
        List<MatchDragonDetail> detailList = new ArrayList<>();

        for (int i = 0; i < matchInfoList.size(); i++) {
            SubMatchInfo matchInfo = matchInfoList.get(i);
            Integer matchId = matchInfo.getMatchId();

            // 查询开奖结果
            MatchResultDetail resultDetail = matchResultDetailMapper.selectById(matchId);
            if (resultDetail == null || StringUtils.isBlank(resultDetail.getHadResult())) {
                log.warn("比赛ID: {} 没有开奖结果，跳过", matchId);
                continue;
            }

            // 查询赔率信息
            LambdaQueryWrapper<HadList> hadWrapper = new LambdaQueryWrapper<>();
            hadWrapper.eq(HadList::getMatchId, String.valueOf(matchId))
                    .orderByDesc(HadList::getUpdateTime)
                    .last("LIMIT 1");
            HadList hadList = hadListMapper.selectOne(hadWrapper);

            if (hadList == null) {
                log.warn("比赛ID: {} 没有赔率信息，跳过", matchId);
                continue;
            }

            // 解析赔率
            BigDecimal homeOdds = parseOdds(hadList.getH());
            BigDecimal drawOdds = parseOdds(hadList.getD());
            BigDecimal awayOdds = parseOdds(hadList.getA());

            // 确定最高和最低赔率
            BigDecimal maxOdds = homeOdds.max(drawOdds).max(awayOdds);
            BigDecimal minOdds = homeOdds.min(drawOdds).min(awayOdds);

            // 确定中奖赔率
            String result = resultDetail.getHadResult();
            BigDecimal winningOdds = null;
            if ("H".equals(result)) {
                winningOdds = homeOdds;
            } else if ("D".equals(result)) {
                winningOdds = drawOdds;
            } else if ("A".equals(result)) {
                winningOdds = awayOdds;
            }

            // 构建详情对象
            MatchDragonDetail detail = MatchDragonDetail.builder()
                    .matchId(matchId)
                    .matchNumStr(matchInfo.getMatchNumStr())
                    .homeTeam(matchInfo.getHomeTeamAbbName())
                    .awayTeam(matchInfo.getAwayTeamAbbName())
                    .matchTime(matchInfo.getMatchTime())
                    .fullScore(resultDetail.getSectionsNo999())
                    .result(result)
                    .resultDesc(getResultDesc(result))
                    .homeOdds(homeOdds)
                    .drawOdds(drawOdds)
                    .awayOdds(awayOdds)
                    .winningOdds(winningOdds)
                    .isMaxOdds(winningOdds != null && winningOdds.compareTo(maxOdds) == 0)
                    .isMinOdds(winningOdds != null && winningOdds.compareTo(minOdds) == 0)
                    .gapFromNow(i)
                    .build();

            detailList.add(detail);
        }

        // 3. 进行长龙分析
        return performDragonAnalysis(detailList);
    }

    /**
     * 执行长龙分析
     */
    private DragonAnalysisVO performDragonAnalysis(List<MatchDragonDetail> detailList) {
        int totalMatches = detailList.size();

        // 统计变量
        int homeWinCount = 0;
        int drawCount = 0;
        int awayWinCount = 0;
        int maxOddsWinCount = 0;
        int minOddsWinCount = 0;

        // 长龙相关变量
        Integer gapsSinceLastHomeWin = null;
        Integer gapsSinceLastDraw = null;
        Integer gapsSinceLastAwayWin = null;
        Integer gapsSinceLastMaxOdds = null;
        Integer gapsSinceLastMinOdds = null;

        int currentHomeWinDragon = 0;
        int currentDrawDragon = 0;
        int currentAwayWinDragon = 0;

        int maxHomeWinDragon = 0;
        int maxDrawDragon = 0;
        int maxAwayWinDragon = 0;

        int tempHomeWinDragon = 0;
        int tempDrawDragon = 0;
        int tempAwayWinDragon = 0;

        // 遍历比赛（从最新到最旧）
        for (int i = 0; i < detailList.size(); i++) {
            MatchDragonDetail detail = detailList.get(i);
            String result = detail.getResult();

            // 统计各类结果出现次数
            if ("H".equals(result)) {
                homeWinCount++;
                if (gapsSinceLastHomeWin == null) {
                    gapsSinceLastHomeWin = i;
                }
                // 重置主胜长龙计数
                maxHomeWinDragon = Math.max(maxHomeWinDragon, tempHomeWinDragon);
                tempHomeWinDragon = 0;
                tempDrawDragon++;
                tempAwayWinDragon++;
            } else if ("D".equals(result)) {
                drawCount++;
                if (gapsSinceLastDraw == null) {
                    gapsSinceLastDraw = i;
                }
                // 重置平局长龙计数
                maxDrawDragon = Math.max(maxDrawDragon, tempDrawDragon);
                tempDrawDragon = 0;
                tempHomeWinDragon++;
                tempAwayWinDragon++;
            } else if ("A".equals(result)) {
                awayWinCount++;
                if (gapsSinceLastAwayWin == null) {
                    gapsSinceLastAwayWin = i;
                }
                // 重置客胜长龙计数
                maxAwayWinDragon = Math.max(maxAwayWinDragon, tempAwayWinDragon);
                tempAwayWinDragon = 0;
                tempHomeWinDragon++;
                tempDrawDragon++;
            }

            // 统计最高最低赔率
            if (Boolean.TRUE.equals(detail.getIsMaxOdds())) {
                maxOddsWinCount++;
                if (gapsSinceLastMaxOdds == null) {
                    gapsSinceLastMaxOdds = i;
                }
            }

            if (Boolean.TRUE.equals(detail.getIsMinOdds())) {
                minOddsWinCount++;
                if (gapsSinceLastMinOdds == null) {
                    gapsSinceLastMinOdds = i;
                }
            }
        }

        // 更新最后的长龙记录
        maxHomeWinDragon = Math.max(maxHomeWinDragon, tempHomeWinDragon);
        maxDrawDragon = Math.max(maxDrawDragon, tempDrawDragon);
        maxAwayWinDragon = Math.max(maxAwayWinDragon, tempAwayWinDragon);

        // 当前连续未出场次就是temp变量的值
        currentHomeWinDragon = tempHomeWinDragon;
        currentDrawDragon = tempDrawDragon;
        currentAwayWinDragon = tempAwayWinDragon;

        // 如果整个样本中都没有出现某个结果，距离设为样本大小
        if (gapsSinceLastHomeWin == null) gapsSinceLastHomeWin = totalMatches;
        if (gapsSinceLastDraw == null) gapsSinceLastDraw = totalMatches;
        if (gapsSinceLastAwayWin == null) gapsSinceLastAwayWin = totalMatches;
        if (gapsSinceLastMaxOdds == null) gapsSinceLastMaxOdds = totalMatches;
        if (gapsSinceLastMinOdds == null) gapsSinceLastMinOdds = totalMatches;

        // 计算出现率
        double homeWinRate = totalMatches > 0 ? (homeWinCount * 100.0 / totalMatches) : 0;
        double drawRate = totalMatches > 0 ? (drawCount * 100.0 / totalMatches) : 0;
        double awayWinRate = totalMatches > 0 ? (awayWinCount * 100.0 / totalMatches) : 0;
        double maxOddsWinRate = totalMatches > 0 ? (maxOddsWinCount * 100.0 / totalMatches) : 0;
        double minOddsWinRate = totalMatches > 0 ? (minOddsWinCount * 100.0 / totalMatches) : 0;

        // 构建返回结果
        return DragonAnalysisVO.builder()
                .totalMatches(totalMatches)
                .gapsSinceLastHomeWin(gapsSinceLastHomeWin)
                .gapsSinceLastDraw(gapsSinceLastDraw)
                .gapsSinceLastAwayWin(gapsSinceLastAwayWin)
                .gapsSinceLastMaxOdds(gapsSinceLastMaxOdds)
                .gapsSinceLastMinOdds(gapsSinceLastMinOdds)
                .maxHomeWinDragon(maxHomeWinDragon)
                .maxDrawDragon(maxDrawDragon)
                .maxAwayWinDragon(maxAwayWinDragon)
                .currentHomeWinDragon(currentHomeWinDragon)
                .currentDrawDragon(currentDrawDragon)
                .currentAwayWinDragon(currentAwayWinDragon)
                .homeWinCount(homeWinCount)
                .drawCount(drawCount)
                .awayWinCount(awayWinCount)
                .homeWinRate(round(homeWinRate, 2))
                .drawRate(round(drawRate, 2))
                .awayWinRate(round(awayWinRate, 2))
                .maxOddsWinCount(maxOddsWinCount)
                .minOddsWinCount(minOddsWinCount)
                .maxOddsWinRate(round(maxOddsWinRate, 2))
                .minOddsWinRate(round(minOddsWinRate, 2))
                .matchDetails(detailList)
                .build();
    }

    /**
     * 解析赔率
     */
    private BigDecimal parseOdds(String oddsStr) {
        try {
            if (StringUtils.isBlank(oddsStr)) {
                return BigDecimal.ZERO;
            }
            return new BigDecimal(oddsStr.trim());
        } catch (Exception e) {
            log.warn("解析赔率失败: {}", oddsStr, e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 获取结果描述
     */
    private String getResultDesc(String result) {
        if ("H".equals(result)) {
            return "主胜";
        } else if ("D".equals(result)) {
            return "平局";
        } else if ("A".equals(result)) {
            return "客胜";
        }
        return "未知";
    }

    /**
     * 四舍五入保留小数位
     */
    private Double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
