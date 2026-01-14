package cn.xingxing.service.impl;


import cn.xingxing.entity.Information;
import cn.xingxing.mapper.InformationMapper;
import cn.xingxing.service.InformationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-26
 * @Version: 1.0
 */
@Service
public class InformationServiceImpl extends ServiceImpl<InformationMapper, Information> implements InformationService {
}
