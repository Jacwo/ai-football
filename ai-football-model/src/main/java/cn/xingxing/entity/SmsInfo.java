package cn.xingxing.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: yangyuanliang
 * @Date: 2026-01-09
 * @Version: 1.0
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sms_info")
public class SmsInfo extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String phone;
    private String code;
}
