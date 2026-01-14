package cn.xingxing.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

@AiService
public interface StreamingAssistant {

    @SystemMessage("你是一位专业的足球比赛分析师，擅长基于赔率数据、历史交锋记录结果进行综合分析。请用专业、客观的态度进行分析，给出有理有据的比分预测。")
    Flux<String> chat(String userMessage);
}