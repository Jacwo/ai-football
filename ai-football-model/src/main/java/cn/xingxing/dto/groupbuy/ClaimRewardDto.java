package cn.xingxing.dto.groupbuy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 领取积分奖励DTO
 * @Author: yangyuanliang
 * @Date: 2026-03-30
 * @Version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimRewardDto {

    /**
     * 拼团ID
     */
    private String groupId;

    /**
     * 团长用户ID
     */
    private String userId;
}
