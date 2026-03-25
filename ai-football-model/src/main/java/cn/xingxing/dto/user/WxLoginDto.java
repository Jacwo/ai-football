package cn.xingxing.dto.user;

import lombok.Data;

/**
 * 微信登录请求 DTO
 * @Author: yangyuanliang
 * @Date: 2026-03-25
 * @Version: 1.0
 */
@Data
public class WxLoginDto {
    /**
     * 微信小程序登录凭证 code
     */
    private String code;
}
