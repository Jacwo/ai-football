package cn.xingxing.service;

import cn.xingxing.vo.DragonAnalysisVO;

/**
 * 长龙分析Service
 *
 * @Author: yangyuanliang
 * @Date: 2025-01-15
 * @Version: 1.0
 */
public interface DragonAnalysisService {

    /**
     * 分析近N场单关比赛的长龙情况
     * @param sampleSize 样本数量（默认30场）
     * @return 长龙分析结果
     */
    DragonAnalysisVO analyzeDragon(Integer sampleSize);
}
