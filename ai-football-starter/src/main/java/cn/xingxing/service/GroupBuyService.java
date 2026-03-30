package cn.xingxing.service;

import cn.xingxing.dto.groupbuy.CreateGroupBuyDto;
import cn.xingxing.dto.groupbuy.GroupBuyVo;
import cn.xingxing.dto.groupbuy.JoinGroupBuyDto;

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
}
