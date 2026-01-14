package cn.xingxing.service;

import cn.xingxing.entity.Guide;
import cn.xingxing.dto.ai.AiMessageRequest;
import cn.xingxing.dto.ai.GuestAskDto;

/**
 * @Author: yangyuanliang
 * @Date: 2026-01-06
 * @Version: 1.0
 */
public interface GuideService {
    void initEngine();

    GuestAskDto loadGuestAsk();

    Guide findByQuestionName(String usermessage);
}
