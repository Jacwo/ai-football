package cn.xingxing.service;

import cn.xingxing.dto.groupbuy.ClaimRewardDto;
import cn.xingxing.dto.groupbuy.CreateGroupBuyDto;
import cn.xingxing.dto.groupbuy.GroupBuyVo;
import cn.xingxing.dto.groupbuy.JoinGroupBuyDto;
import cn.xingxing.dto.groupbuy.MyGroupBuyQueryDto;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 拼团服务接口
 * @Author: yangyuanliang
 * @Date: 2026-03-30
 * @Version: 1.0
 */
public interface GroupBuyService {

    /**
     * 发起拼团
     * @param createDto 创建拼团DTO
     * @return 拼团信息
     */
    GroupBuyVo createGroupBuy(CreateGroupBuyDto createDto);

    /**
     * 加入拼团
     * @param joinDto 加入拼团DTO
     * @return 拼团信息
     */
    GroupBuyVo joinGroupBuy(JoinGroupBuyDto joinDto);

    /**
     * 获取拼团详情
     * @param groupId 拼团ID
     * @return 拼团信息
     */
    GroupBuyVo getGroupBuyDetail(String groupId);

    /**
     * 查询我的拼团列表（分页）
     * @param queryDto 查询条件
     * @return 分页结果
     */
    Page<GroupBuyVo> getMyGroupBuyList(MyGroupBuyQueryDto queryDto);

    /**
     * 团长领取积分奖励
     * @param claimDto 领取请求
     * @return 是否成功
     */
    Boolean claimReward(ClaimRewardDto claimDto);
}
