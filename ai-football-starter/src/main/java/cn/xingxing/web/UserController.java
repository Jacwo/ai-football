package cn.xingxing.web;


import cn.xingxing.dto.ApiResponse;
import cn.xingxing.dto.sms.UserLoginDto;
import cn.xingxing.dto.sms.UserUpdateDto;
import cn.xingxing.dto.user.*;
import cn.xingxing.service.UserPointDetailService;
import cn.xingxing.service.UserService;

import java.util.List;
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
    @Autowired
    private UserPointDetailService userPointDetailService;
    @PostMapping("/user/login")
    public ApiResponse<LoginUserResponse> sendSms(@RequestBody UserLoginDto userLoginDto) {
        return ApiResponse.success(userService.login(userLoginDto.getPhone(), userLoginDto.getCode()));
    }

    @PostMapping("/user/point/deduct")
    public ApiResponse<Boolean> deductPoint(@RequestBody UserPointDto userPointDto) {
        return ApiResponse.success(userService.deductPoint(userPointDto));
    }


    @PostMapping("/user/point/information/deduct")
    public ApiResponse<Boolean> deductPointForInformation(@RequestBody UserPointDto userPointDto) {
        return ApiResponse.success(userService.deductPointForInformation(userPointDto));
    }

    @PostMapping("/user/info")
    public ApiResponse<UserInfoDto> getUserInfo(@RequestBody UserPointDto userPointDto) {
        return ApiResponse.success(userService.getUserInfo(userPointDto));
    }


    @PostMapping("/user/info/update")
    public ApiResponse<Boolean> updateUserInfo(@RequestBody UserUpdateDto userUpdateDto) {
        return ApiResponse.success(userService.updateUserInfo(userUpdateDto));
    }


    @PostMapping("/user/info/update/name")
    public ApiResponse<Boolean> updateUserName(@RequestBody UserUpdateDto userUpdateDto) {
        return ApiResponse.success(userService.updateUserName(userUpdateDto));
    }



    @PostMapping("/user/sign/{userId}")
    public ApiResponse<Boolean> userSign(@PathVariable String userId) {
        return ApiResponse.success(userService.userSign(userId));
    }


    @PostMapping("/user/wx/login")
    public ApiResponse<LoginUserResponse> wxLogin(@RequestBody WxLoginDto wxLoginDto) {
        return ApiResponse.success(userService.wxLogin(wxLoginDto.getCode()));
    }

    /**
     * 查询用户积分明细列表
     * @param queryDto 查询条件
     * @return 积分明细列表
     */
    @PostMapping("/user/point/detail/list")
    public ApiResponse<List<UserPointDetailDto>> getUserPointDetailList(@RequestBody UserPointDetailQueryDto queryDto) {
        return ApiResponse.success(userPointDetailService.getUserPointDetailList(queryDto));
    }


    @PostMapping("/user/logout")
    public ApiResponse<Boolean> logout() {
        return ApiResponse.success(true);
    }

}
