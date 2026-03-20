package cn.xingxing.dto.user;


import lombok.Data;

import java.util.List;

/**
 * @Author: yangyuanliang
 * @Date: 2026-03-20
 * @Version: 1.0
 */
@Data
public class BatchCheckDto {
    private String userId;
    private List<String> matchIds;
}
