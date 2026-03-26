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
 * @Date: 2026-03-20
 * @Version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_information")
public class UserInformation extends BaseEntity{
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String userId;
    private String matchId;
}
