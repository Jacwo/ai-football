package cn.xingxing.service.impl;

import cn.xingxing.common.exception.CommonException;
import cn.xingxing.service.WeChatService;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * 微信服务实现类
 * @Author: yangyuanliang
 * @Date: 2026-03-25
 * @Version: 1.0
 */
@Service
public class WeChatServiceImpl implements WeChatService {

    @Value("${wechat.miniapp.app-id}")
    private String appId;

    @Value("${wechat.miniapp.app-secret}")
    private String appSecret;

    @Value("${wechat.miniapp.code2session-url}")
    private String code2sessionUrl;

    @Override
    public JSONObject code2Session(String code) {
        // 构建请求 URL
        String url = String.format("%s?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                code2sessionUrl, appId, appSecret, code);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
                JSONObject result = JSONObject.parseObject(responseBody);

                // 检查是否有错误码
                if (result.containsKey("errcode") && result.getInteger("errcode") != 0) {
                    throw new CommonException(10006,
                        "微信登录失败: " + result.getString("errmsg"));
                }

                return result;
            }
        } catch (IOException e) {
            throw new CommonException(10007, "调用微信接口异常: " + e.getMessage());
        }
    }
}
