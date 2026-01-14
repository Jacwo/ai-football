package cn.xingxing.web;


import cn.xingxing.ai.Assistant;
import cn.xingxing.ai.StreamingAssistant;
import cn.xingxing.entity.Guide;
import cn.xingxing.entity.SubMatchInfo;
import cn.xingxing.dto.ApiResponse;
import cn.xingxing.dto.ai.AiMessageRequest;
import cn.xingxing.dto.ai.GuestAskDto;
import cn.xingxing.dto.url5.Match;
import cn.xingxing.service.FootballAnalysisService;
import cn.xingxing.service.GuideService;
import cn.xingxing.service.MatchInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-09
 * @Version: 1.0
 */
@RestController
@RequestMapping("/api")
public class AiController {
    @Autowired
    Assistant assistant;

    @Autowired
    private StreamingAssistant streamingAssistant;

    @Autowired
    private GuideService guideService;

    @Autowired
    private FootballAnalysisService analysisService;

    @GetMapping("/chat")
    public String chat(String message) {
        return assistant.chat(message);
    }

    @PostMapping(value = "/stream/chat", produces = "text/event-stream; charset=utf-8")
    public Flux<String> streamingAssistant(@RequestBody AiMessageRequest message) {
        List<AiMessageRequest.Message> messages = message.getMessages();
        AiMessageRequest.Message first = messages.getLast();
        String userMessage = first.getContent();
        Guide guide = guideService.findByQuestionName(userMessage);
        if(guide != null){
          return analysisService.analysisByMatchIdStream(guide.getMatchId());
        }
        return streamingAssistant.chat(message.getMessages().toString());
    }
    @PostMapping("/ai/guest/ask")
    public ApiResponse<GuestAskDto> getGuestAsk() {
        guideService.initEngine();
        GuestAskDto guestAskDto = guideService.loadGuestAsk();
        return ApiResponse.success(guestAskDto);
    }
}
