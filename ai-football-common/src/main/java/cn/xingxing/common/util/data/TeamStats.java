package cn.xingxing.common.util.data;


import lombok.Data;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-25
 * @Version: 1.0
 */
@Data
public class TeamStats {
    private String teamName;
    private int rank;
    private int matches;
    private int wins;
    private int draws;
    private int losses;
    private int goalsFor;
    private int goalsAgainst;
    private int points;
    private double xg;
    private double xga;
    private double xpts;
    private double ppda;
    private double oppda;
    private int deep;
    private int deepAllowed;
}
