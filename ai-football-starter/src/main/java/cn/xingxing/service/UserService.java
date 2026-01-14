package cn.xingxing.service;

import cn.xingxing.dto.user.LoginUserResponse;

/**
 * @Author: yangyuanliang
 * @Date: 2026-01-09
 * @Version: 1.0
 */
public interface UserService {
    LoginUserResponse login(String phone, String code);
}
