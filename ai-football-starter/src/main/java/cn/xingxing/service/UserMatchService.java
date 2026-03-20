package cn.xingxing.service;

import cn.xingxing.dto.user.BatchCheckDto;
import cn.xingxing.dto.user.BatchCheckResponseDto;
import cn.xingxing.dto.user.UserMatchDto;

/**
 * @Author: yangyuanliang
 * @Date: 2026-01-09
 * @Version: 1.0
 */
public interface UserMatchService {

    void saveUserMatch(UserMatchDto userMatchDto);

    BatchCheckResponseDto batchCheckUnlock(BatchCheckDto batchCheckDto);
}
