package cn.xingxing.service;



/**
 * @Author: yangyuanliang
 * @Date: 2026-01-09
 * @Version: 1.0
 */
public interface SmsService {
    boolean sendSms(String phone);

    boolean checkSmsCode(String phone,String code);

    void deleteSmsCode(String phone, String code);
}
