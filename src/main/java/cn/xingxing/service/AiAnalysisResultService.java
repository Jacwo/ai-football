package cn.xingxing.service;


import cn.xingxing.domain.AiAnalysisResult;
import cn.xingxing.dto.AnalysisPageDTO;
import cn.xingxing.dto.PageVO;
import cn.xingxing.vo.AiAnalysisResultVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-24
 * @Version: 1.0
 */
public interface AiAnalysisResultService extends IService<AiAnalysisResult> {
    AiAnalysisResultVo findByMatchId(String matchId);

    PageVO<AiAnalysisResult> matchInfoHistoryList(AnalysisPageDTO analysisPageDTO);
}
