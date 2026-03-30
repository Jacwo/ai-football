package cn.xingxing.service;

import cn.xingxing.dto.user.UserPointDetailDto;
import cn.xingxing.dto.user.UserPointDetailQueryDto;
import cn.xingxing.entity.UserPointDetail;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 用户积分明细服务接口
 * @Author: yangyuanliang
 * @Date: 2026-03-30
 * @Version: 1.0
 */
public interface UserPointDetailService extends IService<UserPointDetail> {

    /**
     * 保存积分明细
     * @param userId 用户ID
     * @param pointChange 积分变化（正数为增加，负数为扣除）
     * @param pointBefore 变化前积分
     * @param pointAfter 变化后积分
     * @param changeType 变化类型
     * @param matchId 关联赛事ID（可选）
     * @param remark 备注说明（可选）
     */
    void savePointDetail(String userId, Long pointChange, Long pointBefore, Long pointAfter,
                         String changeType, String matchId, String remark);

    /**
     * 查询用户积分明细列表
     * @param queryDto 查询条件
     * @return 积分明细列表
     */
    List<UserPointDetailDto> getUserPointDetailList(UserPointDetailQueryDto queryDto);
}
