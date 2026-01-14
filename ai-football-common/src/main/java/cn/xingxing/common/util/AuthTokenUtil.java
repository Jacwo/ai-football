package cn.xingxing.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AuthTokenUtil {

    static final String BEARER = "Bearer ";
    static final String SECRET = "V1d0b1YyRnRSWHBoTTFKcFZqQmFkVmt3WkhOaVJYaFlXa2hTYUdKWVRqQlphMlEwWTNjOVBRPT1HbWprMTIzNA==";

    /**
     * 创建token
     */
    public static String createAuthToken(Map<String, Object> map) {
        String jwt = Jwts.builder().claims(map)
//				.setExpiration(new Date(System.currentTimeMillis() + 3600_000_000L))// 1000hour
                .expiration(new Date(System.currentTimeMillis() + + 480_000_000L))  // saas,plat 0.5小时  app 30天
                .signWith(SignatureAlgorithm.HS512, SECRET).compact();
        return BEARER + jwt;
    }

    /**
     * 解析Token，同时也能验证Token，当验证失败返回null
     */
    public static Claims parserJavaWebToken(String token) {
        try {
            return Jwts.parser().setSigningKey(SECRET).build().parseClaimsJws(token.replace(BEARER, "")).getBody();
        } catch (Exception e) {
            return null;
        }
    }

    public static void main(String[] args) {
        String upm = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJwaG9uZSI6IjEzNjMxNDQ4NDY5IiwicmVnaXN0ZXJUaW1lIjoxNzU3NDcyMDY3MTEzLCJleHBUaW1lIjoyNTkyMDAwMDAwLCJhY2NvdW50Tm8iOiJHTTIwMjUwOTEwMTA0MTA3MDAwMDAwMDU2MyIsInBsYXRUeXBlIjoiMyIsInVzZXJOYW1lIjoiZ21fODQ2OSIsImV4cCI6MTc2MDA2NDA2NywidXNlcklkIjoxMzE2MzJ9.A4giJhGgFFfIPovSAhdMiXgIIIAZagLCQgh_CRtKdggvPFd6tqiDNZW71Tc2enG_960M1dam_vNOxVlYW9XYJw";
        String str = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJwbGF0VHlwZSI6Im1hZ3BpZS1hZG1pbi1wbGF0Zm9ybSIsImV4cCI6MTc1MDQwMDk0MCwidXNlcklkIjoxLCJleHBUaW1lIjozNjAwLCJ1c2VybmFtZSI6InN1cGVyQWRtaW4ifQ.vodtQsdyqPccJy_RJAoDwGzUQo0CAn3GS7rWGH8TdmsQoHfpyVm7MWGt5ZMSe6SldQ3hzjnzLWhN7A9PFPioZg";
        Claims c1 = AuthTokenUtil.parserJavaWebToken(upm);
        Claims c2 = AuthTokenUtil.parserJavaWebToken(str);
        System.out.println(c1);
        System.out.println(c2);
        Map<String, Object> params = new HashMap<>(8);
        params.put("platType", "7");
        params.put("phone", "15618924238");
        params.put("userId", 11L);
        params.put("accountNo", "GM20181130201240000095");
        params.put("userName", "刘亚俊");
        params.put("expTime", 2592_000_000L);
        String authToken = createAuthToken(params);
        System.out.println("authToken = " + authToken);
    }
}
