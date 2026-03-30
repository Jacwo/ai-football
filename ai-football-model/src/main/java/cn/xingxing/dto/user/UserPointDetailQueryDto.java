package cn.xingxing.dto.user;

import lombok.Data;

/**
 * 用户积分明细查询DTO
 * @Author: yangyuanliang
 * @Date: 2026-03-30
 * @Version: 1.0
 */
@Data
public class UserPointDetailQueryDto {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 变化类型（可选）
     */
    private String changeType;

    /**
     * 页码（默认1）
     */
    private Integer pageNum = 1;

    /**
     * 每页数量（默认20）
     */
    private Integer pageSize = 20;
}
