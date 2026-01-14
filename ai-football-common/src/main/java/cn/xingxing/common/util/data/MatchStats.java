package cn.xingxing.common.util.data;


import lombok.Data;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-25
 * @Version: 1.0
 */
@Data
public class MatchStats {
    private String date;
    private String homeTeam;
    private String awayTeam;
    private int homeGoals;
    private int awayGoals;
    private double homeXg;
    private double awayXg;
    private double homePpda;
    private double awayPpda;
}
