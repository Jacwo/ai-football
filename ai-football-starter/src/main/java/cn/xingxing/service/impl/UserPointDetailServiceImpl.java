package cn.xingxing.service.impl;

import cn.xingxing.dto.user.UserPointDetailDto;
import cn.xingxing.dto.user.UserPointDetailQueryDto;
import cn.xingxing.entity.UserPointDetail;
import cn.xingxing.enums.PointChangeType;
import cn.xingxing.mapper.UserPointDetailMapper;
import cn.xingxing.service.UserPointDetailService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户积分明细服务实现
 * @Author: yangyuanliang
 * @Date: 2026-03-30
 * @Version: 1.0
 */
@Service
public class UserPointDetailServiceImpl extends ServiceImpl<UserPointDetailMapper, UserPointDetail>
        implements UserPointDetailService {

    @Override
    public void savePointDetail(String userId, Long pointChange, Long pointBefore, Long pointAfter,
                                String changeType, String matchId, String remark) {
        UserPointDetail detail = UserPointDetail.builder()
                .userId(userId)
                .pointChange(pointChange)
                .pointBefore(pointBefore)
                .pointAfter(pointAfter)
                .changeType(changeType)
                .matchId(matchId)
                .remark(remark)
                .build();
        this.save(detail);
    }

    @Override
    public List<UserPointDetailDto> getUserPointDetailList(UserPointDetailQueryDto queryDto) {
        // 构建查询条件
        LambdaQueryWrapper<UserPointDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserPointDetail::getUserId, queryDto.getUserId());

        if (StringUtils.hasText(queryDto.getChangeType())) {
            queryWrapper.eq(UserPointDetail::getChangeType, queryDto.getChangeType());
        }

        queryWrapper.orderByDesc(UserPointDetail::getCreateTime);

        // 分页查询
        Page<UserPointDetail> page = new Page<>(queryDto.getPageNum(), queryDto.getPageSize());
        Page<UserPointDetail> resultPage = this.page(page, queryWrapper);

        // 构建类型描述映射
        Map<String, String> typeDescMap = Arrays.stream(PointChangeType.values())
                .collect(Collectors.toMap(PointChangeType::getCode, PointChangeType::getDesc));

        // 转换为DTO
        return resultPage.getRecords().stream()
                .map(detail -> UserPointDetailDto.builder()
                        .id(detail.getId())
                        .userId(detail.getUserId())
                        .pointChange(detail.getPointChange())
                        .pointBefore(detail.getPointBefore())
                        .pointAfter(detail.getPointAfter())
                        .changeType(detail.getChangeType())
                        .changeTypeDesc(typeDescMap.getOrDefault(detail.getChangeType(), "未知"))
                        .matchId(detail.getMatchId())
                        .remark(detail.getRemark())
                        .createTime(detail.getCreateTime() != null ? detail.getCreateTime().toString() : "")
                        .build())
                .collect(Collectors.toList());
    }
}
