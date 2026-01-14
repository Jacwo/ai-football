package cn.xingxing.web;


import cn.xingxing.dto.ApiResponse;
import cn.xingxing.dto.sms.UserLoginDto;
import cn.xingxing.dto.user.LoginUserResponse;
import cn.xingxing.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: yangyuanliang
 * @Date: 2026-01-09
 * @Version: 1.0
 */
@RestController
@RequestMapping("/api")
public class UserController {
    @Autowired
    private UserService userService;
    @PostMapping("/user/login")
    public ApiResponse<LoginUserResponse> sendSms(@RequestBody UserLoginDto userLoginDto) {
        return ApiResponse.success(userService.login(userLoginDto.getPhone(), userLoginDto.getCode()));
    }

}
