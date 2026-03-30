package cn.xingxing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 拼团实体类
 * @Author: yangyuanliang
 * @Date: 2026-03-30
 * @Version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("group_buy")
public class GroupBuy extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 团长ID(发起人)
     */
    private String leaderId;

    /**
     * 需要的团员数量
     */
    private Integer groupSize;

    /**
     * 当前团员数量(含团长)
     */
    private Integer currentSize;

    /**
     * 拼团状态: 0-进行中, 1-成功, 2-失败
     */
    private Integer status;

    /**
     * 过期时间(2小时后)
     */
    private LocalDateTime expireTime;

    /**
     * 成功时间
     */
    private LocalDateTime successTime;

    /**
     * 积分是否已发放: 0-未发放, 1-已发放
     */
    private Integer rewardDistributed;
}
