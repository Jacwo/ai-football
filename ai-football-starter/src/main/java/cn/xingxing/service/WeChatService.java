package cn.xingxing.service;

import com.alibaba.fastjson.JSONObject;

/**
 * 微信服务接口
 * @Author: yangyuanliang
 * @Date: 2026-03-25
 * @Version: 1.0
 */
public interface WeChatService {

    /**
     * 通过微信小程序 code 获取 session 信息(包含 openId 和 session_key)
     * @param code 微信小程序登录凭证
     * @return 包含 openId 和 session_key 的 JSON 对象
     */
    JSONObject code2Session(String code);
}
