package cn.xingxing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户积分明细实体类
 * @Author: yangyuanliang
 * @Date: 2026-03-30
 * @Version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_point_detail")
public class UserPointDetail extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
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
     * 变化类型：DEDUCT_MATCH(赛事扣除)、DEDUCT_INFO(情报扣除)、SIGN(签到)、REGISTER(注册赠送)、BIND_PHONE(绑定手机)
     */
    private String changeType;

    /**
     * 关联赛事ID
     */
    private String matchId;

    /**
     * 备注说明
     */
    private String remark;
}
