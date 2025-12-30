package cn.xingxing.service.impl;


import cn.xingxing.domain.AiAnalysisResult;
import cn.xingxing.dto.AnalysisPageDTO;
import cn.xingxing.dto.PageVO;
import cn.xingxing.mapper.AiAnalysisResultMapper;
import cn.xingxing.service.AiAnalysisResultService;
import cn.xingxing.util.PageConvertUtils;
import cn.xingxing.vo.AiAnalysisResultVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-24
 * @Version: 1.0
 */
@Service
public class AiAnalysisResultServiceImpl  extends ServiceImpl<AiAnalysisResultMapper, AiAnalysisResult> implements AiAnalysisResultService {
    @Override
    public AiAnalysisResultVo findByMatchId(String matchId) {
        AiAnalysisResultVo  aiAnalysisResultVo = new AiAnalysisResultVo();
        LambdaQueryWrapper<AiAnalysisResult> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiAnalysisResult::getMatchId,matchId);
        AiAnalysisResult aiAnalysisResult = baseMapper.selectOne(queryWrapper);
        if(aiAnalysisResult!=null){
            BeanUtils.copyProperties(aiAnalysisResult,aiAnalysisResultVo);
            return aiAnalysisResultVo;
        }
        return null;
    }

    @Override
    public PageVO<AiAnalysisResult> matchInfoHistoryList(AnalysisPageDTO analysisPageDTO) {
        LambdaQueryWrapper<AiAnalysisResult> queryWrapper = new LambdaQueryWrapper<>();
        if(!StringUtils.isEmpty(analysisPageDTO.getMatchResult())){
            queryWrapper.like(AiAnalysisResult::getAiAnalysis, analysisPageDTO.getMatchResult());
        }

        if(!StringUtils.isEmpty(analysisPageDTO.getTeamName())){
            queryWrapper.like(AiAnalysisResult::getHomeTeam, analysisPageDTO.getTeamName());
        }

        if(!StringUtils.isEmpty(analysisPageDTO.getMatchResult())){
            queryWrapper.like(AiAnalysisResult::getAiAnalysis, analysisPageDTO.getMatchResult());
        }

        queryWrapper.isNotNull(AiAnalysisResult::getMatchResult);

        queryWrapper.orderByDesc(AiAnalysisResult::getCreateTime);

        IPage<AiAnalysisResult> page = new Page<>(analysisPageDTO.getPageNo(), analysisPageDTO.getPageSize());
        page = this.page(page,queryWrapper);
        return PageConvertUtils.convert(page, AiAnalysisResult.class);
    }


}
