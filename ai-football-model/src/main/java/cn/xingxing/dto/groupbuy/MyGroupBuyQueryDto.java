package cn.xingxing.dto.groupbuy;

import lombok.Data;

/**
 * 我的拼团查询DTO
 * @Author: yangyuanliang
 * @Date: 2026-03-30
 * @Version: 1.0
 */
@Data
public class MyGroupBuyQueryDto {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 拼团状态（可选）: 0-进行中, 1-成功, 2-失败
     */
    private Integer status;

    /**
     * 页码（默认1）
     */
    private Integer pageNum = 1;

    /**
     * 每页数量（默认10）
     */
    private Integer pageSize = 10;
}
