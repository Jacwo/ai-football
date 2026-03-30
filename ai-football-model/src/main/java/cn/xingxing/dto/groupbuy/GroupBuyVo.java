package cn.xingxing.dto.groupbuy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 拼团响应VO
 * @Author: yangyuanliang
 * @Date: 2026-03-30
 * @Version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyVo {

    /**
     * 拼团ID
     */
    private String id;

    /**
     * 团长ID
     */
    private String leaderId;

    /**
     * 团长名称
     */
    private String leaderName;

    /**
     * 需要的团员数量
     */
    private Integer groupSize;

    /**
     * 当前团员数量
     */
    private Integer currentSize;

    /**
     * 拼团状态: 0-进行中, 1-成功, 2-失败
     */
    private Integer status;

    /**
     * 拼团状态描述
     */
    private String statusDesc;

    /**
     * 过期时间
     */
    private String expireTime;

    /**
     * 成功时间
     */
    private String successTime;

    /**
     * 积分是否已发放: 0-未发放, 1-已发放
     */
    private Integer rewardDistributed;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 成员列表
     */
    private List<GroupBuyMemberVo> members;
}
