package cn.xingxing.dto.groupbuy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建拼团请求DTO
 * @Author: yangyuanliang
 * @Date: 2026-03-30
 * @Version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupBuyDto {

    /**
     * 用户ID(团长)
     */
    private String userId;

    /**
     * 需要的团员数量(默认2人)
     */
    private Integer groupSize;
}
