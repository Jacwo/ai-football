package cn.xingxing.service.impl;

import cn.xingxing.common.exception.CommonException;
import cn.xingxing.dto.BetSchemeSaveDto;
import cn.xingxing.dto.BetSchemeVo;
import cn.xingxing.entity.*;
import cn.xingxing.mapper.*;
import cn.xingxing.service.BetSchemeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 投注方案服务实现类
 * @Author: yangyuanliang
 * @Date: 2026-03-24
 * @Version: 1.0
 */
@Slf4j
@Service
public class BetSchemeServiceImpl extends ServiceImpl<BetSchemeMapper, BetScheme> implements BetSchemeService {

    @Autowired
    private BetSchemeMapper betSchemeMapper;

    @Autowired
    private BetSchemeDetailMapper betSchemeDetailMapper;

    @Autowired
    private BetSchemeOptionMapper betSchemeOptionMapper;

    @Autowired
    private MatchCalculatorMapper matchCalculatorMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveBetScheme(BetSchemeSaveDto saveDto) {
        // 1. 生成方案编号
        String schemeNo = generateSchemeNo(saveDto.getUserId());

        // 2. 计算总金额(每注2元)
        BigDecimal totalAmount = new BigDecimal(saveDto.getTotalBets())
                .multiply(new BigDecimal(saveDto.getMultiple()))
                .multiply(new BigDecimal("2.00"));

        // 3. 保存主表
        BetScheme betScheme = BetScheme.builder()
                .userId(saveDto.getUserId())
                .schemeNo(schemeNo)
                .passTypes(String.join(",", saveDto.getPassTypes()))
                .multiple(saveDto.getMultiple())
                .totalBets(saveDto.getTotalBets())
                .totalAmount(totalAmount)
                .status(0)
                .build();
        betSchemeMapper.insert(betScheme);

        Long schemeId = betScheme.getId();

        // 4. 保存明细和选项
        for (BetSchemeSaveDto.MatchSelection selection : saveDto.getSelections()) {
            // 查询比赛信息
            MatchCalculator matchCalculator = matchCalculatorMapper.selectById(selection.getMatchId());

            // 保存明细
            BetSchemeDetail detail = BetSchemeDetail.builder()
                    .schemeId(schemeId)
                    .matchId(selection.getMatchId())
                    .matchNumStr(matchCalculator != null ? matchCalculator.getMatchNumStr() : null)
                    .homeTeamName(matchCalculator != null ? matchCalculator.getHomeTeamAbbName() : null)
                    .awayTeamName(matchCalculator != null ? matchCalculator.getAwayTeamAbbName() : null)
                    .matchTime(matchCalculator != null ? matchCalculator.getMatchTime() : null)
                    .build();
            betSchemeDetailMapper.insert(detail);

            Long detailId = detail.getId();

            // 保存选项
            for (BetSchemeSaveDto.BetOption option : selection.getOptions()) {
                BetSchemeOption schemeOption = BetSchemeOption.builder()
                        .detailId(detailId)
                        .schemeId(schemeId)
                        .matchId(selection.getMatchId())
                        .optionType(option.getType())
                        .checked(option.getChecked())
                        .optionValue(option.getValue())
                        .odds(BigDecimal.valueOf(option.getOdds()))
                        .build();
                betSchemeOptionMapper.insert(schemeOption);
            }
        }

        log.info("保存投注方案成功, 方案编号: {}, 用户ID: {}", schemeNo, saveDto.getUserId());
        return schemeNo;
    }

    @Override
    public List<BetSchemeVo> getUserSchemes(String userId) {
        // 查询用户的方案列表
        LambdaQueryWrapper<BetScheme> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(item->item.eq(BetScheme::getUserId, userId).or().eq(BetScheme::getId,userId))
                .orderByDesc(BetScheme::getCreateTime);
        List<BetScheme> betSchemes = betSchemeMapper.selectList(wrapper);
        return processBetSchemes(betSchemes);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteScheme(String id) {
        try {
            Long schemeId = Long.parseLong(id);

            // 1. 删除方案选项
            LambdaQueryWrapper<BetSchemeOption> optionWrapper = new LambdaQueryWrapper<>();
            optionWrapper.eq(BetSchemeOption::getSchemeId, schemeId);
            betSchemeOptionMapper.delete(optionWrapper);

            // 2. 删除方案明细
            LambdaQueryWrapper<BetSchemeDetail> detailWrapper = new LambdaQueryWrapper<>();
            detailWrapper.eq(BetSchemeDetail::getSchemeId, schemeId);
            betSchemeDetailMapper.delete(detailWrapper);

            // 3. 删除方案主表
            int rows = betSchemeMapper.deleteById(schemeId);

            log.info("删除投注方案成功, 方案ID: {}, 影响行数: {}", id, rows);
            return rows > 0;
        } catch (Exception e) {
            log.error("删除投注方案失败, 方案ID: {}, 错误: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Boolean recommendScheme(String id) {
        Long schemeId = Long.parseLong(id);
        BetScheme byId = this.getById(schemeId);

        if(byId!=null){
            if(byId.getRecommend()==1){
                throw new CommonException(10007,"不能重复推荐");
            }
            byId.setRecommend(1);
            this.updateById(byId);
        }
        return true;
    }

    @Override
    public List<BetSchemeVo> listSchemes() {
        // 查询用户的方案列表
        LambdaQueryWrapper<BetScheme> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BetScheme::getRecommend, 1)
                .orderByDesc(BetScheme::getCreateTime);
        List<BetScheme> betSchemes = betSchemeMapper.selectList(wrapper);
        return processBetSchemes(betSchemes);

    }

    private List<BetSchemeVo> processBetSchemes(List<BetScheme> betSchemes) {
        if (betSchemes.isEmpty()) {
            return Collections.emptyList();
        }

        // 组装返回数据
        List<BetSchemeVo> result = new ArrayList<>();
        for (BetScheme betScheme : betSchemes) {
            String userId = betScheme.getUserId();
            User user = userMapper.selectById(userId);
            BetSchemeVo vo = BetSchemeVo.builder()
                    .id(betScheme.getId())
                    .userId(userId)
                    .userName(user!=null? user.getUserName():"")
                    .schemeNo(betScheme.getSchemeNo())
                    .passTypes(Arrays.asList(betScheme.getPassTypes().split(",")))
                    .multiple(betScheme.getMultiple())
                    .totalBets(betScheme.getTotalBets())
                    .totalAmount(betScheme.getTotalAmount())
                    .status(betScheme.getStatus())
                    .statusDesc(getStatusDesc(betScheme.getStatus()))
                    .createTime(betScheme.getCreateTime())
                    .build();

            // 查询方案明细
            LambdaQueryWrapper<BetSchemeDetail> detailWrapper = new LambdaQueryWrapper<>();
            detailWrapper.eq(BetSchemeDetail::getSchemeId, betScheme.getId());
            List<BetSchemeDetail> details = betSchemeDetailMapper.selectList(detailWrapper);

            List<BetSchemeVo.MatchDetail> matchDetails = new ArrayList<>();
            for (BetSchemeDetail detail : details) {
                // 查询选项
                LambdaQueryWrapper<BetSchemeOption> optionWrapper = new LambdaQueryWrapper<>();
                optionWrapper.eq(BetSchemeOption::getDetailId, detail.getId());
                List<BetSchemeOption> options = betSchemeOptionMapper.selectList(optionWrapper);

                List<BetSchemeVo.OptionDetail> optionDetails = options.stream()
                        .map(option -> BetSchemeVo.OptionDetail.builder()
                                .optionType(option.getOptionType())
                                .optionTypeDesc(getOptionTypeDesc(option.getOptionType()))
                                .optionValue(option.getOptionValue())
                                .odds(option.getOdds())
                                .checked(option.getChecked())
                                .matchResult(option.getMatchResult())
                                .matchResultDesc(option.getMatchResultDesc())
                                .checkTime(option.getCheckTime())
                                .resultOdds(option.getResultOdds())
                                .isHit(option.getIsHit())
                                .build())
                        .collect(Collectors.toList());

                BetSchemeVo.MatchDetail matchDetail = BetSchemeVo.MatchDetail.builder()
                        .matchId(detail.getMatchId())
                        .matchNumStr(detail.getMatchNumStr())
                        .homeTeamName(detail.getHomeTeamName())
                        .awayTeamName(detail.getAwayTeamName())
                        .matchTime(detail.getMatchTime())
                        .options(optionDetails)
                        .build();
                matchDetails.add(matchDetail);
            }

            vo.setMatchDetails(matchDetails);
            result.add(vo);
        }

        return result;
    }

    /**
     * 生成方案编号
     * @param userId 用户ID
     * @return 方案编号
     */
    private String generateSchemeNo(String userId) {
        // 格式: BS + 日期(yyyyMMdd) + 用户ID后6位 + 随机4位数
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String date = LocalDateTime.now().format(formatter);
        String userIdSuffix = userId.length() > 6 ? userId.substring(userId.length() - 6) : userId;
        String random = String.format("%04d", new Random().nextInt(10000));
        return "BS" + date + userIdSuffix + random;
    }

    /**
     * 获取状态描述
     * @param status 状态码
     * @return 状态描述
     */
    private String getStatusDesc(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0:
                return "待开奖";
            case 1:
                return "已中奖";
            case 2:
                return "未中奖";
            case 3:
                return "已取消";
            default:
                return "未知";
        }
    }

    /**
     * 获取选项类型描述
     * @param optionType 选项类型
     * @return 选项类型描述
     */
    private String getOptionTypeDesc(String optionType) {
        if (optionType == null) {
            return "未知";
        }
        switch (optionType.toLowerCase()) {
            case "had":
                return "胜平负";
            case "hhad":
                return "让球胜平负";
            case "ttg":
                return "总进球";
            case "hafu":
                return "半全场";
            case "crs":
                return "比分";
            default:
                return optionType;
        }
    }
}
