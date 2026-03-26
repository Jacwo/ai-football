package cn.xingxing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 投注方案主表实体类
 * @Author: yangyuanliang
 * @Date: 2026-03-24
 * @Version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("bet_scheme")
public class BetScheme extends BaseEntity {

    /**
     * 方案ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 方案编号
     */
    private String schemeNo;

    /**
     * 过关方式(多个用逗号分隔,如:single,2_1)
     */
    private String passTypes;

    /**
     * 倍数
     */
    private Integer multiple;

    /**
     * 总注数
     */
    private Integer totalBets;


    /**
     * 是否推荐
     */
    private Integer recommend;

    /**
     * 总金额(元)
     */
    private BigDecimal totalAmount;

    /**
     * 方案状态(0-待开奖,1-已中奖,2-未中奖,3-已取消)
     */
    private Integer status;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
