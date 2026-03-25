package cn.xingxing.common.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AES 加解密工具类
 * 用于微信小程序数据解密
 * @Author: yangyuanliang
 * @Date: 2026-03-25
 * @Version: 1.0
 */
public class AESUtil {

    private static final String ALGORITHM = "AES";
    private static final String AES_CBC_PKCS7 = "AES/CBC/PKCS7Padding";
    private static final String AES_CBC_PKCS5 = "AES/CBC/PKCS5Padding";

    /**
     * 微信小程序数据解密
     * @param encryptedData 加密数据
     * @param sessionKey 会话密钥
     * @param iv 初始向量
     * @return 解密后的 JSON 字符串
     */
    public static String decryptWeChatData(String encryptedData, String sessionKey, String iv) {
        try {
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
            byte[] keyBytes = Base64.getDecoder().decode(sessionKey);
            byte[] ivBytes = Base64.getDecoder().decode(iv);

            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

            Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("微信数据解密失败: " + e.getMessage(), e);
        }
    }
}
