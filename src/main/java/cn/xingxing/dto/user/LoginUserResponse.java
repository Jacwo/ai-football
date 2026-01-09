package cn.xingxing.dto.user;


import lombok.Data;

/**
 * @Author: yangyuanliang
 * @Date: 2026-01-09
 * @Version: 1.0
 */
@Data
public class LoginUserResponse {
    private String token;
    private UserInfoDto userInfo;
}
