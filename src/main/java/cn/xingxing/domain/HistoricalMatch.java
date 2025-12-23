package cn.xingxing.domain;


import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-20
 * @Version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("historical_match")
public class HistoricalMatch extends BaseEntity {
    @TableId
    private Integer  id;
    private String matchId;
    private String homeTeam;
    private String awayTeam;
    private String score;
    private String league;
    private String matchDate;
}