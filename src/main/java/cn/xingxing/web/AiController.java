package cn.xingxing.web;


import cn.xingxing.ai.Assistant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-09
 * @Version: 1.0
 */
@RestController
public class AiController {
    @Autowired
    Assistant assistant;

    @GetMapping("/chat")
    public String chat(String message) {
        return assistant.chat(message);
    }
}
