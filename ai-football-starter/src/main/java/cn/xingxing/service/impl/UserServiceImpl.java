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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
                user.setPoint(2L);
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
            userInfo.setIsAdmin(dbUser.getIsAdmin());
            userInfo.setCreateTime(dbUser.getCreateTime().toString());
            userInfo.setSignToday(checkIfSignedToday(dbUser.getSignDateTime()));
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
            userInfo.setIsAdmin(one.getIsAdmin());

            // 判断今日是否签到（以早上5点为分界点）
            userInfo.setSignToday(checkIfSignedToday(one.getSignDateTime()));

            return userInfo;
        }

        throw new CommonException(10004, "用户不存在");
    }

    /**
     * 判断是否在今天签到（以早上5点为分界点）
     * @param signDateTime 签到时间
     * @return true表示今天已签到，false表示未签到
     */
    private Boolean checkIfSignedToday(LocalDateTime signDateTime) {
        if (signDateTime == null) {
            return false;
        }

        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();

        // 计算今天凌晨5点的时间
        LocalDateTime todayFiveAM = LocalDate.now().atTime(5, 0, 0);

        // 如果当前时间早于今天5点，则今天的开始时间应该是昨天5点
        LocalDateTime signStartTime;
        if (now.isBefore(todayFiveAM)) {
            signStartTime = todayFiveAM.minusDays(1);
        } else {
            signStartTime = todayFiveAM;
        }

        // 判断签到时间是否在今天的签到有效期内（从早上5点开始到第二天早上5点）
        return signDateTime.isAfter(signStartTime) || signDateTime.isEqual(signStartTime);
    }

    @Override
    public Boolean userSign(String userId) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getId, userId);
        User one = this.getOne(queryWrapper);
        if(one == null){
            throw new CommonException(10004, "用户不存在");
        }

        // 检查今天是否已经签到
        if(checkIfSignedToday(one.getSignDateTime())){
            throw new CommonException(10005, "今日已签到，请勿重复签到");
        }

        // 签到并增加积分
        one.setSignDateTime(LocalDateTime.now());
        one.setPoint(one.getPoint() + 2);  // 签到奖励1积分
        this.updateById(one);
        return true;
    }
}
