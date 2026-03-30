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
 * 拼团成员实体类
 * @Author: yangyuanliang
 * @Date: 2026-03-30
 * @Version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("group_buy_member")
public class GroupBuyMember extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 拼团ID
     */
    private String groupId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 是否团长: 0-团员, 1-团长
     */
    private Integer isLeader;

    /**
     * 加入时间
     */
    private LocalDateTime joinTime;
}
