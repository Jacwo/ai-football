package cn.xingxing.dto.sms;


import lombok.Data;

/**
 * @Author: yangyuanliang
 * @Date: 2026-01-09
 * @Version: 1.0
 */
@Data
public class UserLoginDto {
    private String phone;
    private String code;
}
