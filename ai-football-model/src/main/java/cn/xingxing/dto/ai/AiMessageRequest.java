package cn.xingxing.dto.ai;


import lombok.Data;

import java.util.List;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-31
 * @Version: 1.0
 */
@Data
public class AiMessageRequest {
    private List<Message> messages;
    private Boolean deepThinking;

    @Data
    public static class Message {
        private String content;
        private String role;
    }
}
