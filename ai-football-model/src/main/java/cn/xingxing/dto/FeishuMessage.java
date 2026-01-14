package cn.xingxing.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeishuMessage {
    @JSONField(name = "msg_type")
    private String msgType;
    private Card card;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Card {
        private List<Object> elements;
    }
}

