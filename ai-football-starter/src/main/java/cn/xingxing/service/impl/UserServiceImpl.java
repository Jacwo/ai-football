package cn.xingxing.service.impl;


import cn.xingxing.dto.user.UserMatchDto;
import cn.xingxing.dto.user.UserPointDto;
import cn.xingxing.entity.User;
import cn.xingxing.dto.user.LoginUserResponse;
import cn.xingxing.dto.user.UserInfoDto;
import cn.xingxing.common.exception.CommonException;
import cn.xingxing.mapper.UserMapper;
import cn.xingxing.service.SmsService;
import cn.xingxing.service.UserMatchService;
import cn.xingxing.service.UserService;
import cn.xingxing.common.util.AuthTokenUtil;
import cn.xingxing.common.util.RandomUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @Author: yangyuanliang
 * @Date: 2026-01-09
 * @Version: 1.0
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private SmsService smsService;
    @Autowired
    private UserMatchService userMatchService;
    @Override
    public LoginUserResponse login(String phone, String code) {
        boolean result = smsService.checkSmsCode(phone, code);
        if (result) {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);
            User one = this.getOne(queryWrapper);
            if (one == null) {
                User user = new User();
                user.setPhone(phone);
                user.setUserName("用户" + RandomUtil.generateUserName());
                user.setStatus("1");
                user.setPoint(3L);
                this.save(user);
            }
            User dbUser = this.getOne(queryWrapper);
            smsService.deleteSmsCode(phone, code);
            LoginUserResponse loginUserResponse = new LoginUserResponse();

            UserInfoDto userInfo = new UserInfoDto();
            userInfo.setPhone(phone);
            userInfo.setId(dbUser.getId());
            userInfo.setUserName(dbUser.getUserName());
            userInfo.setStatus(dbUser.getStatus());
            userInfo.setGender(1);
            userInfo.setPoint(dbUser.getPoint());
            userInfo.setCreateTime(dbUser.getCreateTime().toString());
            String authToken = AuthTokenUtil.createAuthToken(JSONObject.parseObject(JSONObject.toJSONString(userInfo), Map.class));
            loginUserResponse.setToken(authToken);
            loginUserResponse.setUserInfo(userInfo);
            return loginUserResponse;
        }
        throw new CommonException(10002, "验证码错误");
    }

    @Override
    public Boolean deductPoint(UserPointDto userPointDto) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getId, userPointDto.getId());
        User one = this.getOne(queryWrapper);
        if(one.getPoint()<userPointDto.getDeductPoint()){
            throw new CommonException(10003,"积分余额不足");
        }
        one.setPoint(one.getPoint()- userPointDto.getDeductPoint());
        this.updateById(one);
        UserMatchDto userMatchDto =new UserMatchDto();
        userMatchDto.setUserId(userPointDto.getId());
        userMatchDto.setMatchId(userPointDto.getMatchId());
        userMatchService.saveUserMatch(userMatchDto);
        return true;
    }

    @Override
    public UserInfoDto getUserInfo(UserPointDto userPointDto) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getId, userPointDto.getId());
        User one = this.getOne(queryWrapper);
        if(one!=null){
            UserInfoDto userInfo = new UserInfoDto();
            userInfo.setPhone(one.getPhone());
            userInfo.setId(one.getId());
            userInfo.setUserName(one.getUserName());
            userInfo.setStatus(one.getStatus());
            userInfo.setGender(1);
            userInfo.setPoint(one.getPoint());
            userInfo.setCreateTime(one.getCreateTime().toString());
            return userInfo;
        }

        throw new CommonException(10004, "用户不存在");
    }
}
