package cn.xingxing.common.util.data;


import lombok.Data;

import java.util.Map;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-25
 * @Version: 1.0
 */
@Data
public class PlayerStats {
    private String playerName;
    private String team;
    private int games;
    private int goals;
    private int assists;
    private double xg;
    private double xa;
    private double npxg;
    private double xg90;
    private double xa90;
    private double xgChain;
    private double xgBuildup;
    private double xgPer90;
    private double xaPer90;
    private Map<String, Object> additionalStats;
}
