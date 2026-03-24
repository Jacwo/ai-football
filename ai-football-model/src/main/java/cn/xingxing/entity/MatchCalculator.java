package cn.xingxing.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 比赛计算器实体类
 * @Author: yangyuanliang
 * @Date: 2026-03-24
 * @Version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("match_calculator")
public class MatchCalculator extends BaseEntity {

    @TableId
    private Integer matchId;

    /**
     * 比赛编号
     */
    private Integer matchNum;

    /**
     * 比赛编号字符串
     */
    private String matchNumStr;

    /**
     * 比赛编号日期
     */
    private String matchNumDate;

    /**
     * 比赛日期
     */
    private String matchDate;

    /**
     * 比赛时间
     */
    private String matchTime;

    /**
     * 比赛星期
     */
    private String matchWeek;

    /**
     * 主队ID
     */
    private Integer homeTeamId;

    /**
     * 主队简称
     */
    private String homeTeamAbbName;

    /**
     * 主队英文简称
     */
    private String homeTeamAbbEnName;

    /**
     * 主队全称
     */
    private String homeTeamAllName;

    /**
     * 主队代码
     */
    private String homeTeamCode;

    /**
     * 主队排名
     */
    private String homeRank;

    /**
     * 客队ID
     */
    private Integer awayTeamId;

    /**
     * 客队简称
     */
    private String awayTeamAbbName;

    /**
     * 客队英文简称
     */
    private String awayTeamAbbEnName;

    /**
     * 客队全称
     */
    private String awayTeamAllName;

    /**
     * 客队代码
     */
    private String awayTeamCode;

    /**
     * 客队排名
     */
    private String awayRank;

    /**
     * 联赛ID
     */
    private Integer leagueId;

    /**
     * 联赛简称
     */
    private String leagueAbbName;

    /**
     * 联赛全称
     */
    private String leagueAllName;

    /**
     * 联赛代码
     */
    private String leagueCode;

    /**
     * 比赛状态
     */
    private String matchStatus;

    /**
     * 销售状态
     */
    private Integer sellStatus;

    /**
     * 背景颜色
     */
    private String backColor;

    /**
     * 业务日期
     */
    private String businessDate;

    /**
     * 是否隐藏
     */
    private Integer isHide;

    /**
     * 是否热门
     */
    private Integer isHot;

    /**
     * 税期编号
     */
    private String taxDateNo;

    /**
     * 让球过关单关
     */
    private Integer bettingSingle;

    /**
     * 让球过关串关
     */
    private Integer bettingAllUp;

    // ==================== HAD 胜平负数据 ====================
    /**
     * 胜平负-主胜赔率
     */
    private String hadH;

    /**
     * 胜平负-平局赔率
     */
    private String hadD;

    /**
     * 胜平负-客胜赔率
     */
    private String hadA;

    /**
     * 胜平负-主胜标识
     */
    private String hadHf;

    /**
     * 胜平负-平局标识
     */
    private String hadDf;

    /**
     * 胜平负-客胜标识
     */
    private String hadAf;

    /**
     * 胜平负-更新日期
     */
    private String hadUpdateDate;

    /**
     * 胜平负-更新时间
     */
    private String hadUpdateTime;

    // ==================== HHAD 让球胜平负数据 ====================
    /**
     * 让球胜平负-主胜赔率
     */
    private String hhadH;

    /**
     * 让球胜平负-平局赔率
     */
    private String hhadD;

    /**
     * 让球胜平负-客胜赔率
     */
    private String hhadA;

    /**
     * 让球胜平负-让球数
     */
    private String hhadGoalLine;

    /**
     * 让球胜平负-让球数值
     */
    private String hhadGoalLineValue;

    /**
     * 让球胜平负-主胜标识
     */
    private String hhadHf;

    /**
     * 让球胜平负-平局标识
     */
    private String hhadDf;

    /**
     * 让球胜平负-客胜标识
     */
    private String hhadAf;

    /**
     * 让球胜平负-更新日期
     */
    private String hhadUpdateDate;

    /**
     * 让球胜平负-更新时间
     */
    private String hhadUpdateTime;

    // ==================== TTG 总进球数据 ====================
    /**
     * 总进球-0球赔率
     */
    private String ttgS0;

    /**
     * 总进球-1球赔率
     */
    private String ttgS1;

    /**
     * 总进球-2球赔率
     */
    private String ttgS2;

    /**
     * 总进球-3球赔率
     */
    private String ttgS3;

    /**
     * 总进球-4球赔率
     */
    private String ttgS4;

    /**
     * 总进球-5球赔率
     */
    private String ttgS5;

    /**
     * 总进球-6球赔率
     */
    private String ttgS6;

    /**
     * 总进球-7+球赔率
     */
    private String ttgS7;

    /**
     * 总进球-更新日期
     */
    private String ttgUpdateDate;

    /**
     * 总进球-更新时间
     */
    private String ttgUpdateTime;

    // ==================== HAFU 半全场数据 ====================
    /**
     * 半全场-主主赔率
     */
    private String hafuHh;

    /**
     * 半全场-主平赔率
     */
    private String hafuHd;

    /**
     * 半全场-主客赔率
     */
    private String hafuHa;

    /**
     * 半全场-平主赔率
     */
    private String hafuDh;

    /**
     * 半全场-平平赔率
     */
    private String hafuDd;

    /**
     * 半全场-平客赔率
     */
    private String hafuDa;

    /**
     * 半全场-客主赔率
     */
    private String hafuAh;

    /**
     * 半全场-客平赔率
     */
    private String hafuAd;

    /**
     * 半全场-客客赔率
     */
    private String hafuAa;

    /**
     * 半全场-主主标识
     */
    private String hafuHhf;

    /**
     * 半全场-主平标识
     */
    private String hafuHdf;

    /**
     * 半全场-主客标识
     */
    private String hafuHaf;

    /**
     * 半全场-平主标识
     */
    private String hafuDhf;

    /**
     * 半全场-平平标识
     */
    private String hafuDdf;

    /**
     * 半全场-平客标识
     */
    private String hafuDaf;

    /**
     * 半全场-客主标识
     */
    private String hafuAhf;

    /**
     * 半全场-客平标识
     */
    private String hafuAdf;

    /**
     * 半全场-客客标识
     */
    private String hafuAaf;

    /**
     * 半全场-更新日期
     */
    private String hafuUpdateDate;

    /**
     * 半全场-更新时间
     */
    private String hafuUpdateTime;

    // ==================== CRS 比分数据 ====================
    /**
     * 比分-0:0赔率
     */
    private String crsS00s00;

    /**
     * 比分-0:1赔率
     */
    private String crsS00s01;

    /**
     * 比分-0:2赔率
     */
    private String crsS00s02;

    /**
     * 比分-0:3赔率
     */
    private String crsS00s03;

    /**
     * 比分-0:4赔率
     */
    private String crsS00s04;

    /**
     * 比分-0:5+赔率
     */
    private String crsS00s05;

    /**
     * 比分-1:0赔率
     */
    private String crsS01s00;

    /**
     * 比分-1:1赔率
     */
    private String crsS01s01;

    /**
     * 比分-1:2赔率
     */
    private String crsS01s02;

    /**
     * 比分-1:3赔率
     */
    private String crsS01s03;

    /**
     * 比分-1:4赔率
     */
    private String crsS01s04;

    /**
     * 比分-1:5+赔率
     */
    private String crsS01s05;

    /**
     * 比分-2:0赔率
     */
    private String crsS02s00;

    /**
     * 比分-2:1赔率
     */
    private String crsS02s01;

    /**
     * 比分-2:2赔率
     */
    private String crsS02s02;

    /**
     * 比分-2:3赔率
     */
    private String crsS02s03;

    /**
     * 比分-2:4赔率
     */
    private String crsS02s04;

    /**
     * 比分-2:5+赔率
     */
    private String crsS02s05;

    /**
     * 比分-3:0赔率
     */
    private String crsS03s00;

    /**
     * 比分-3:1赔率
     */
    private String crsS03s01;

    /**
     * 比分-3:2赔率
     */
    private String crsS03s02;

    /**
     * 比分-3:3赔率
     */
    private String crsS03s03;

    /**
     * 比分-4:0赔率
     */
    private String crsS04s00;

    /**
     * 比分-4:1赔率
     */
    private String crsS04s01;

    /**
     * 比分-4:2赔率
     */
    private String crsS04s02;

    /**
     * 比分-5+:0赔率
     */
    private String crsS05s00;

    /**
     * 比分-5+:1赔率
     */
    private String crsS05s01;

    /**
     * 比分-5+:2赔率
     */
    private String crsS05s02;

    /**
     * 比分-半场主胜赔率
     */
    private String crsS1sh;

    /**
     * 比分-半场平局赔率
     */
    private String crsS1sd;

    /**
     * 比分-半场客胜赔率
     */
    private String crsS1sa;

    /**
     * 比分-更新日期
     */
    private String crsUpdateDate;

    /**
     * 比分-更新时间
     */
    private String crsUpdateTime;
}
