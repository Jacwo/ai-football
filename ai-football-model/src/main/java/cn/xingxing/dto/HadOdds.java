package cn.xingxing.dto;

import lombok.Data;

/**
 * 胜平负赔率
 * @Author: yangyuanliang
 * @Date: 2026-03-24
 * @Version: 1.0
 */
@Data
public class HadOdds {
    private String h;
    private String d;
    private String a;
    private String hf;
    private String df;
    private String af;
    private String goalLine;
    private String goalLineValue;
    private String updateDate;
    private String updateTime;
}
