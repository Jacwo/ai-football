package cn.xingxing.dto.groupbuy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 拼团成员VO
 * @Author: yangyuanliang
 * @Date: 2026-03-30
 * @Version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyMemberVo {

    /**
     * 成员ID
     */
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 是否团长
     */
    private Integer isLeader;

    /**
     * 加入时间
     */
    private String joinTime;
}
