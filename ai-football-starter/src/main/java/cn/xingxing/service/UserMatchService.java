package cn.xingxing.service;

import cn.xingxing.dto.user.BatchCheckDto;
import cn.xingxing.dto.user.BatchCheckResponseDto;
import cn.xingxing.dto.user.UserMatchDto;

import java.util.List;

/**
 * @Author: yangyuanliang
 * @Date: 2026-01-09
 * @Version: 1.0
 */
public interface UserMatchService {

    void saveUserMatch(UserMatchDto userMatchDto);

    List<BatchCheckResponseDto> batchCheckUnlock(BatchCheckDto batchCheckDto);
}
