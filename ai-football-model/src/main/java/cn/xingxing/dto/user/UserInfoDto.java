package cn.xingxing.dto.user;


import lombok.Data;

/**
 * @Author: yangyuanliang
 * @Date: 2026-01-09
 * @Version: 1.0
 */
@Data
public class UserInfoDto {
    private String id;
    private String phone;
    private String userName;
    private Integer gender;
    private String createTime;
    private String status;
}
