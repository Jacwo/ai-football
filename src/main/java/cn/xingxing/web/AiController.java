package cn.xingxing.web;


import cn.xingxing.ai.Assistant;
import cn.xingxing.ai.StreamingAssistant;
import cn.xingxing.dto.ai.AiMessageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

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

    @GetMapping("/chat")
    public String chat(String message) {
        return assistant.chat(message);
    }

    @PostMapping(value = "/stream/chat", produces = "text/event-stream; charset=utf-8")
    public Flux<String> streamingAssistant(@RequestBody AiMessageRequest message) {
        return streamingAssistant.chat(message.getMessages().toString());
    }
}
