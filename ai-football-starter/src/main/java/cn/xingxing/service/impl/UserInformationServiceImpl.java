package cn.xingxing.service.impl;

import cn.xingxing.dto.user.UserInformationDto;
import cn.xingxing.entity.UserInformation;
import cn.xingxing.mapper.UserInformationMapper;
import cn.xingxing.service.UserInformationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
/**
 * @Author: yangyuanliang
 * @Date: 2026-01-09
 * @Version: 1.0
 */
@Service
public class UserInformationServiceImpl extends ServiceImpl<UserInformationMapper, UserInformation> implements UserInformationService {


    @Override
    public Boolean checkInformationUnlock(UserInformationDto userInformationDto) {
        LambdaQueryWrapper<UserInformation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInformation::getUserId, userInformationDto.getUserId())
                .eq(UserInformation::getMatchId, userInformationDto.getMatchId());
        UserInformation one = this.getOne(queryWrapper);
        return one != null;

    }

    @Override
    public void saveUserInformation(UserInformation userInformation) {
        this.save(userInformation);
    }
}
