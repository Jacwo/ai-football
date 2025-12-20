package com.example.demo.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @Author:yangyuanliang
 * @Date:2020/3/30 10:42
 * @Description:由于restTemplate可能存在bug，使用原生httpClient
 */
@Slf4j
public class HttpClientUtil {

    public static final String METHOD_POST = "POST";
    public static final String METHOD_GET = "GET";

    public static String getHttpContent(String url, String method, String postData, Map<String, String> header, int timeout) {
        return getHttpContent(url, method, postData, header, timeout, timeout);
    }

    public static String getHttpContent(
            String url, String method,
            String postData, Map<String, String> header,
            int connectTimeout, int readTimeout) {

        BufferedReader reader = null;
        InputStreamReader inputStreamReader=null;
        try {
            if (StringUtils.isBlank(url)) {
                throw new IllegalArgumentException("url cannot be null");
            }
            if (!METHOD_POST.equalsIgnoreCase(method) && !METHOD_GET.equalsIgnoreCase(method)) {
                throw new IllegalArgumentException("method must be one of Post or Get");
            }
            URL address = new URL(url);

           /* Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 10809));
            HttpURLConnection conn = (HttpURLConnection)address.openConnection(proxy);*/

            HttpURLConnection conn = (HttpURLConnection) address.openConnection();
            conn.setAllowUserInteraction(false);
            conn.setDoOutput(true);
            conn.setConnectTimeout(connectTimeout);
            conn.setReadTimeout(readTimeout);
            conn.setRequestMethod(method);
            Map<String, String> defaultHeader = new HashMap<>(6);

            if (header != null && !header.isEmpty()) {
                Set<String> key = header.keySet();
                for (Iterator<String> it = key.iterator(); it.hasNext(); ) {
                    String s = it.next();
                    defaultHeader.put(s, header.get(s));
                }
            }
            Set<String> key = defaultHeader.keySet();
            for (Iterator<String> it = key.iterator(); it.hasNext(); ) {
                String s = it.next();
                conn.addRequestProperty(s, defaultHeader.get(s));
            }
            if (METHOD_POST.equalsIgnoreCase(method)) {
                postData = StringUtils.trimToEmpty(postData);
                conn.getOutputStream().write(postData.getBytes("UTF-8"));
                conn.getOutputStream().flush();
                conn.getOutputStream().close();
            }
            inputStreamReader = new InputStreamReader(conn.getInputStream(), "UTF-8");
            reader = new BufferedReader(inputStreamReader);
            StringBuffer sb = new StringBuffer();
            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                sb.append(inputLine).append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
             log.error("http IOE Exception",e);
            throw new RuntimeException(e.getMessage(), e);
        } catch (RuntimeException e) {
            log.error("http RuntimeException",e);
            throw e;
        } finally {
            if(inputStreamReader!=null){
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    log.error("http IOE Exception",e);
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error("http IOE Exception",e);
                }
            }
        }
    }

    public static String doGet(String url, int timeout) {
        Map<String,String> header=new HashMap<>();
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

    public static String doPost(String url, String postData, int timeout) {
        Map<String,String> header=new HashMap<>();
        header.put("Content-Type","application\\/json");
        return getHttpContent(url, METHOD_POST, postData, header, timeout);
    }


}