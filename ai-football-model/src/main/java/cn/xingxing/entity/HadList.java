package cn.xingxing.entity;

import cn.xingxing.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@TableName("had_list")
public class HadList extends BaseEntity {
    @TableId
    private String id;
    //ke
    private String a;
    //ping
    private String d;
    //zhu
    private String h;
    private String hf;
    private String goalLine;

    private String matchId;

    private String updateDate;

    private String updateTime;
   // "hf": "1",
     //       "goalLine": "+3"
    // Define fields corresponding to each key-value pair in the "hhadList" array
    // You can add these fields similar to the other classes above

    @Override
    public String toString() {
        return "HadList{" +
                "客队赔率='" + a + '\'' +
                ", 平局赔率='" + d + '\'' +
                ", 主队赔率='" + h + '\'' +
                ", hf='" + hf + '\'' +
                ", goalLine='" + goalLine + '\'' +
                ", matchId='" + matchId + '\'' +
                ", updateDate='" + updateDate + '\'' +
                ", updateTime='" + updateTime + '\'' +
                '}';
    }
}
