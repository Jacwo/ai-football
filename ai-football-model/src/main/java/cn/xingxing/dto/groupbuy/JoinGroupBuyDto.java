package cn.xingxing.dto.groupbuy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 加入拼团请求DTO
 * @Author: yangyuanliang
 * @Date: 2026-03-30
 * @Version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinGroupBuyDto {

    /**
     * 拼团ID
     */
    private String groupId;

    /**
     * 用户ID(团员)
     */
    private String userId;
}
