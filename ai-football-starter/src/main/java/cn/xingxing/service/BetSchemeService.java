package cn.xingxing.service;

import cn.xingxing.dto.BetSchemeSaveDto;
import cn.xingxing.dto.BetSchemeVo;

import java.util.List;

/**
 * 投注方案服务接口
 * @Author: yangyuanliang
 * @Date: 2026-03-24
 * @Version: 1.0
 */
public interface BetSchemeService {

    /**
     * 保存投注方案
     * @param saveDto 保存参数
     * @return 方案编号
     */
    String saveBetScheme(BetSchemeSaveDto saveDto);

    /**
     * 查询用户方案列表
     * @param userId 用户ID
     * @return 方案列表
     */
    List<BetSchemeVo> getUserSchemes(String userId);

    Boolean deleteScheme(String id);

    Boolean recommendScheme(String id);

    List<BetSchemeVo> listSchemes();
}
