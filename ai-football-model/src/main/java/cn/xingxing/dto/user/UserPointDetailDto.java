package cn.xingxing.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户积分明细DTO
 * @Author: yangyuanliang
 * @Date: 2026-03-30
 * @Version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPointDetailDto {

    /**
     * 明细ID
     */
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 积分变化（正数为增加，负数为扣除）
     */
    private Long pointChange;

    /**
     * 变化前积分
     */
    private Long pointBefore;

    /**
     * 变化后积分
     */
    private Long pointAfter;

    /**
     * 变化类型代码
     */
    private String changeType;

    /**
     * 变化类型描述
     */
    private String changeTypeDesc;

    /**
     * 关联赛事ID
     */
    private String matchId;

    /**
     * 备注说明
     */
    private String remark;

    /**
     * 创建时间
     */
    private String createTime;
}
