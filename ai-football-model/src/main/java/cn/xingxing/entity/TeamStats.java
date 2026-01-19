package cn.xingxing.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-25
 * @Version: 1.0
 *
 * https://understat.com/league/EPL/2025
 */
@Data
@TableName("team_stats")
public class TeamStats {
    @TableId
    private Long id; // 数据库自增主键

    private String flag; //all home away
    private Integer number; // 排名

    private String team; // 球队名称
    @TableField("team_name")
    private String teamName; // 球队名称

    private Integer matches; // 比赛场次
    private Integer wins; // 胜
    private Integer draws; // 平
    private Integer loses; // 负
    private Integer goals; // 进球
    private Integer ga; // 失球 (Goals Against)

    private Integer points; // 积分

    private Integer rankNum;

    // --- 以下为高级数据字段 ---
    @TableField("xG")
    private Double xG; // 预期进球
    @TableField("NPxG")
    private Double NPxG; // 非点球预期进球
    @TableField("xGA")
    private Double xGA; // 预期失球
    @TableField("NPxGA")
    private Double NPxGA; // 非点球预期失球
    @TableField("NPxGD")
    private Double NPxGD; // 非点球预期净胜球

    private Double ppda; // 对方每防守动作允许的传球次数
    @TableField("ppda_allowed")
    private Double ppdaAllowed; // 己方每防守动作允许的传球次数

    private Integer deep; // 进攻三区传球/触球次数
    @TableField("deep_allowed")
    private Integer deepAllowed; // 被对手在进攻三区传球/触球次数
    @TableField("xPTS")
    private Double xPTS; // 预期积分

    // 可以方便地计算净胜球
    public Integer getGoalDifference() {
        return goals - ga;
    }
}
