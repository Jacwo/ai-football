package cn.xingxing.web;


import cn.xingxing.dto.ApiResponse;
import cn.xingxing.dto.sms.UserLoginDto;
import cn.xingxing.dto.user.LoginUserResponse;
import cn.xingxing.dto.user.UserInfoDto;
import cn.xingxing.dto.user.UserPointDto;
import cn.xingxing.dto.user.WxLoginDto;
import cn.xingxing.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/user/point/deduct")
    public ApiResponse<Boolean> deductPoint(@RequestBody UserPointDto userPointDto) {
        return ApiResponse.success(userService.deductPoint(userPointDto));
    }

    @PostMapping("/user/info")
    public ApiResponse<UserInfoDto> getUserInfo(@RequestBody UserPointDto userPointDto) {
        return ApiResponse.success(userService.getUserInfo(userPointDto));
    }


    @PostMapping("/user/sign/{userId}")
    public ApiResponse<Boolean> userSign(@PathVariable String userId) {
        return ApiResponse.success(userService.userSign(userId));
    }


    @PostMapping("/user/wx/login")
    public ApiResponse<LoginUserResponse> wxLogin(@RequestBody WxLoginDto wxLoginDto) {
        return ApiResponse.success(userService.wxLogin(wxLoginDto.getCode()));
    }


}
