package cn.xingxing.entity;


import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author: yangyuanliang
 * @Date: 2026-01-06
 * @Version: 1.0
 */
@Setter
@Getter
@TableName("guide")
public class Guide {
    @TableId
    private String matchId;
    private String questionName;
}
