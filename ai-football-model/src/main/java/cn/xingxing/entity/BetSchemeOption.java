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

/**
 * 投注方案选项表实体类
 * @Author: yangyuanliang
 * @Date: 2026-03-24
 * @Version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("bet_scheme_option")
public class BetSchemeOption extends BaseEntity {

    /**
     * 选项ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 明细ID
     */
    private Long detailId;

    /**
     * 方案ID
     */
    private Long schemeId;

    /**
     * 比赛ID
     */
    private Long matchId;

    /**
     * 选项类型(had-胜平负,hhad-让球胜平负,ttg-总进球,hafu-半全场,crs-比分)
     */
    private String optionType;

    /**
     * 选项值(如:3,0:0,1:1等)
     */
    private String optionValue;

    /**
     * 赔率
     */
    private BigDecimal odds;

    /**
     * 是否命中(0-未命中,1-命中,NULL-未开奖)
     */
    private Integer isHit;
}
