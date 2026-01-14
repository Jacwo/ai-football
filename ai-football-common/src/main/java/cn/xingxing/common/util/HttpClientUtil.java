package cn.xingxing.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class HttpClientUtil {

    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_DELETE = "DELETE";

    private static final CloseableHttpClient httpClient;

    static {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(30000)
                .setSocketTimeout(30000)
                .setConnectionRequestTimeout(30000)
                .build();

        httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    public static String doGet(String url, int timeout) {
        Map<String, String> header = new HashMap<>();
        header.put("origin", "https://m.sporttery.cn");
        header.put("accept", "application/json, text/plain, */*");
        header.put("accept-language", "zh-CN,zh;q=0.9");
        header.put("cache-control", "no-cache");
        header.put("dnt", "1");
        header.put("pragma", "no-cache");
        header.put("priority", "u=1, i");
        header.put("referer", "https://m.sporttery.cn/");
        header.put("sec-fetch-dest", "empty");
        header.put("sec-fetch-mode", "cors");
        header.put("sec-fetch-site", "same-site");
        header.put("user-agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 18_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.5 Mobile/15E148 Safari/604.1");

        return getHttpContent(url, METHOD_GET, null, header, timeout);
    }

    public static String doPost(String url, String body, int timeout) throws IOException {
        return getHttpContent(url, METHOD_POST, body, null, timeout);
    }

    public static String getHttpContent(String url, String method, String body,
                                        Map<String, String> headers, int timeout) {

        HttpRequestBase request = createRequest(url, method, body);

        // 设置超时
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setSocketTimeout(timeout)
                .build();
        request.setConfig(requestConfig);

        // 设置请求头
        if (headers != null) {
            headers.forEach(request::addHeader);
        }

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, "UTF-8");
            EntityUtils.consume(entity);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode >= 200 && statusCode < 300) {
                return result;
            } else {
                throw new IOException("HTTP请求失败，状态码: " + statusCode + ", 响应: " + result);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    private static HttpRequestBase createRequest(String url, String method, String body) {
        switch (method.toUpperCase()) {
            case METHOD_POST:
                HttpPost post = new HttpPost(url);
                if (body != null) {
                    post.setEntity(new StringEntity(body, "UTF-8"));
                    post.setHeader("Content-Type", "application/json");
                }
                return post;

            case METHOD_PUT:
                HttpPut put = new HttpPut(url);
                if (body != null) {
                    put.setEntity(new StringEntity(body, "UTF-8"));
                    put.setHeader("Content-Type", "application/json");
                }
                return put;

            case METHOD_DELETE:
                return new HttpDelete(url);

            case METHOD_GET:
            default:
                return new HttpGet(url);
        }
    }
}