package cn.xingxing.service;

import cn.xingxing.dto.user.UserInformationDto;
import cn.xingxing.entity.UserInformation;

/**
 * @Author: yangyuanliang
 * @Date: 2026-01-09
 * @Version: 1.0
 */
public interface UserInformationService {

    Boolean checkInformationUnlock(UserInformationDto userInformationDto);

    void saveUserInformation(UserInformation userInformation);
}
