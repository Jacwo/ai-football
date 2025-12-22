package com.example.demo.dto;

import com.baomidou.mybatisplus.annotation.TableId;
import com.example.demo.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
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
}
