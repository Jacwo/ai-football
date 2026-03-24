package cn.xingxing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 投注方案明细表实体类
 * @Author: yangyuanliang
 * @Date: 2026-03-24
 * @Version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("bet_scheme_detail")
public class BetSchemeDetail extends BaseEntity {

    /**
     * 明细ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 方案ID
     */
    private Long schemeId;

    /**
     * 比赛ID
     */
    private Long matchId;

    /**
     * 比赛编号
     */
    private String matchNumStr;

    /**
     * 主队名称
     */
    private String homeTeamName;

    /**
     * 客队名称
     */
    private String awayTeamName;

    /**
     * 比赛时间
     */
    private String matchTime;
}
