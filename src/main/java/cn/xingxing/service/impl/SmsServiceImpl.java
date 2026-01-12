package cn.xingxing.service.impl;


import cn.xingxing.domain.SmsInfo;
import cn.xingxing.exception.CommonException;
import cn.xingxing.mapper.SmsInfoMapper;
import cn.xingxing.service.SmsService;
import cn.xingxing.util.HttpClientUtil;
import cn.xingxing.util.RandomUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: yangyuanliang
 * @Date: 2026-01-09
 * @Version: 1.0
 */
@Slf4j
@Service
public class SmsServiceImpl extends ServiceImpl<SmsInfoMapper, SmsInfo> implements SmsService {

    @Value("${sms.white.list:17790065776}")
    private List<String> whiteList;
    public static final String SEND_SMS_URL = "https://push.spug.cc/send/v3aDVjB7DDrJeX9M?code=%s&targets=%s";

    @Override
    public boolean sendSms(String phone) {
        if (StringUtils.isEmpty(phone)) {
            throw new CommonException(100001, "手机号不能为空");
        }
        //{"code": 200, "msg": "请求成功", "request_id": "NAVEmpLvLbDm7g4b"}
        String code = RandomUtil.generateSmsCode();
        if(whiteList.contains(phone)){
            code ="9999";
            SmsInfo smsInfo = new SmsInfo();
            smsInfo.setCode(code);
            smsInfo.setPhone(phone);
            this.save(smsInfo);
            return true;
        }
        String url = String.format(SEND_SMS_URL, code, phone);

        String s = HttpClientUtil.doGet(url, 20000);
        try {
            JSONObject jsonObject = JSONObject.parseObject(s);
            if (jsonObject.getInteger("code") == 200) {
                SmsInfo smsInfo = new SmsInfo();
                smsInfo.setCode(code);
                smsInfo.setPhone(phone);
                this.save(smsInfo);
                return true;
            }
        } catch (Exception e) {
            log.error("send sms exception {}", s);
            return false;
        }

        return false;
    }

    @Override
    public boolean checkSmsCode(String phone, String code) {
        LambdaQueryWrapper<SmsInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SmsInfo::getPhone, phone);
        queryWrapper.eq(SmsInfo::getCode, code);
        List<SmsInfo> list = this.list(queryWrapper);
        return !list.isEmpty();
    }

    @Override
    public void deleteSmsCode(String phone, String code) {
        LambdaQueryWrapper<SmsInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SmsInfo::getPhone, phone);
        queryWrapper.eq(SmsInfo::getCode, code);
        this.getBaseMapper().deleteByPhoneAndCode(phone,code);
    }
}
