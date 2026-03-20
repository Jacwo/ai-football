package cn.xingxing.service.impl;



import cn.xingxing.dto.user.BatchCheckDto;
import cn.xingxing.dto.user.BatchCheckResponseDto;
import cn.xingxing.dto.user.UserMatchDto;
import cn.xingxing.entity.UserMatch;
import cn.xingxing.mapper.UserMatchMapper;
import cn.xingxing.service.UserMatchService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: yangyuanliang
 * @Date: 2026-01-09
 * @Version: 1.0
 */
@Service
public class UserMatchServiceImpl extends ServiceImpl<UserMatchMapper, UserMatch> implements UserMatchService {

    @Override
    public void saveUserMatch(UserMatchDto userMatchDto) {
        UserMatch userMatch =new UserMatch();
        userMatch.setMatchId(userMatchDto.getMatchId());
        userMatch.setUserId(userMatchDto.getUserId());
        this.save(userMatch);
    }

    @Override
    public BatchCheckResponseDto batchCheckUnlock(BatchCheckDto batchCheckDto) {
        List<String> matchIds = batchCheckDto.getMatchIds();
        String userId = batchCheckDto.getUserId();

        // 查询用户已解锁的比赛记录
        LambdaQueryWrapper<UserMatch> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserMatch::getUserId, userId)
                   .in(UserMatch::getMatchId, matchIds);
        List<UserMatch> userMatches = this.list(queryWrapper);

        // 构建已解锁的比赛ID集合
        List<String> unlockedMatchIds = userMatches.stream()
                .map(UserMatch::getMatchId)
                .toList();

        // 构建返回结果
        BatchCheckResponseDto response = new BatchCheckResponseDto();
        List<BatchCheckResponseDto.MatchUnlockStaus> result = matchIds.stream()
                .map(matchId -> {
                    BatchCheckResponseDto.MatchUnlockStaus status = new BatchCheckResponseDto.MatchUnlockStaus();
                    status.setMatchId(matchId);
                    status.setIsUnlocked(unlockedMatchIds.contains(matchId));
                    return status;
                })
                .toList();
        response.setResult(result);

        return response;
    }
}
