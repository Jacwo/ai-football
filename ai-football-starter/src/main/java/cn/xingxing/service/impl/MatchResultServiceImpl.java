package cn.xingxing.service.impl;


import cn.xingxing.dto.MatchResultDetailDto;
import cn.xingxing.entity.MatchResultDetail;
import cn.xingxing.mapper.MatchResultDetailMapper;
import cn.xingxing.service.MatchInfoService;
import cn.xingxing.service.MatchResultService;
import cn.xingxing.vo.MatchInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: yangyuanliang
 * @Date: 2026-03-26
 * @Version: 1.0
 */
@Service
public class MatchResultServiceImpl  extends ServiceImpl<MatchResultDetailMapper, MatchResultDetail> implements MatchResultService {
    @Autowired
    private MatchInfoService matchInfoService;
    
    @Override
    public List<MatchResultDetailDto> listMatchResult() {
        // 计算三天前的时间
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);

        // 查询最近三天的比赛结果
        LambdaQueryWrapper<MatchResultDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(MatchResultDetail::getCreateTime, threeDaysAgo)
                    .orderByDesc(MatchResultDetail::getCreateTime);

        List<MatchResultDetail> matchResultDetails = this.list(queryWrapper);

        // 转换为DTO
        return matchResultDetails.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 将实体转换为DTO
     */
    private MatchResultDetailDto convertToDto(MatchResultDetail entity) {
        MatchResultDetailDto dto = new MatchResultDetailDto();
        BeanUtils.copyProperties(entity, dto);
        MatchInfoVo matchById = matchInfoService.findMatchById(entity.getMatchId().toString());
        if(matchById!=null){
            dto.setHomeName(matchById.getHomeTeamAbbName());
            dto.setAwayName(matchById.getAwayTeamAbbName());
            dto.setLeagueName(matchById.getLeagueAbbName());
        }
        return dto;
    }
}
