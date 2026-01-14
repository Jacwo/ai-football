package cn.xingxing.common.util;

import java.util.Random;


public class RandomUtil {

    /**
     * 验证码类型为仅数字 0~9
     */
    public static final int TYPE_NUM_ONLY = 0;
    /**
     * 验证码类型为仅字母，即大写、小写字母混合
     */
    public static final int TYPE_LETTER_ONLY = 1;

    /**
     * 验证码类型为数字、大写字母、小写字母混合
     */
    public static final int TYPE_ALL_MIXED = 2;
    /**
     * 验证码类型为数字、大写字母混合
     */
    public static final int TYPE_NUM_UPPER = 3;

    /**
     * 验证码类型为数字、小写字母混合
     */
    public static final int TYPE_NUM_LOWER = 4;

    /**
     * 验证码类型为仅大写字母
     */
    public static final int TYPE_UPPER_ONLY = 5;

    /**
     * 验证码类型为仅小写字母
     */
    public static final int TYPE_LOWER_ONLY = 6;


    /**
     * 0-25 小写字母 26-51大写字母 52-61数字
     */
    private static final char[] STR_ARR = new char[]{'a', 'b', 'c', 'd', 'e',
            'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
            's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E',
            'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
            'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', '0'};

    private static final char[] CHAR_ARR = new char[]{'a', 'b', 'c', 'd', 'e',
            'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
            's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E',
            'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
            'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

    public static String generateTextCode(int type, int length) {

        if (length <= 0) {
            return "";
        }
        StringBuffer code = new StringBuffer();
        int i = 0;
        Random r = new Random();
        //大写字母长度
        int maxLength;
        //小写字母长度
        int smallLength;
        //数字长度
        int intLength;
        switch (type) {
            //仅数字
            case TYPE_NUM_ONLY:
                while (i < length) {
                    int t = r.nextInt(10) + 52;
                    char c = STR_ARR[t];
                    code.append(c);
                    i++;
                }
                break;
            //仅字母（即大写字母、小写字母混合）,必须同时包含大小写
            case TYPE_LETTER_ONLY:
                //大写长度
                maxLength = r.nextInt(length - 1) + 1;
                //小写长度
                smallLength = length - maxLength;
                int number = 52;
                while (i < length) {
                    int anInt = r.nextInt(number);
                    char c = CHAR_ARR[anInt];
                    if (anInt <= 25) {
                        if (smallLength != 0) {
                            smallLength--;
                            code.append(c);
                            i++;
                        }
                    } else if (maxLength != 0) {
                        maxLength--;
                        code.append(c);
                        i++;
                    } else {
                        number = 26;
                    }
                }
                break;

            //数字、大写字母、小写字母混合 必须同时存在
            case TYPE_ALL_MIXED:
                //大写长度
                maxLength = r.nextInt(length - 2) + 1;
                //小写长度
                smallLength = r.nextInt(length - maxLength - 1) + 1;
                //数字长度
                intLength = length - maxLength - smallLength;
                int num = 62;
                while (i < length) {
                    int anInt = r.nextInt(num);
                    char c = STR_ARR[anInt];
                    if (anInt <= 25) {
                        if (smallLength != 0) {
                            smallLength--;
                            code.append(c);
                            i++;
                        }
                    } else if (anInt <= 51) {
                        if (maxLength != 0) {
                            maxLength--;
                            code.append(c);
                            i++;
                        }
                    } else {
                        if (intLength != 0) {
                            intLength--;
                            code.append(c);
                            i++;
                        } else {
                            num = 52;
                        }
                    }
                }
                break;
            //数字、大写字母混合
            case TYPE_NUM_UPPER:
                while (i < length) {
                    int t = r.nextInt(91);
                    if ((t >= 65 || (t >= 48 && t <= 57))) {
                        code.append((char) t);
                        i++;
                    }
                }
                break;
            //数字、小写字母混合
            case TYPE_NUM_LOWER:
                while (i < length) {
                    int t = r.nextInt(123);
                    if ((t >= 97 || (t >= 48 && t <= 57))) {
                        code.append((char) t);
                        i++;
                    }
                }
                break;

            //仅大写字母
            case TYPE_UPPER_ONLY:
                while (i < length) {
                    int t = r.nextInt(91);
                    if ((t >= 65)) {
                        code.append((char) t);
                        i++;
                    }
                }
                break;
            //仅小写字母
            case TYPE_LOWER_ONLY:
                while (i < length) {
                    int t = r.nextInt(123);
                    if ((t >= 97)) {
                        code.append((char) t);
                        i++;
                    }
                }
                break;
            default:
                break;
        }
        return code.toString();
    }


    /**
     * 生成8为编码
     * @return 编码
     */
    public static String generateSmsCode() {
        return generateTextCode(TYPE_NUM_ONLY,4);
    }


    public static String generateUserName() {
        return generateTextCode(TYPE_UPPER_ONLY,8);
    }

    public static void main(String[] args) {
        System.out.println(generateSmsCode());
    }
}
