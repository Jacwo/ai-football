package com.example.demo.ai;


import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-09
 * @Version: 1.0
 */
@AiService
public interface Assistant {
    @SystemMessage("你是一位专业的足球比赛分析师，擅长基于赔率数据、历史交锋记录结果进行综合分析。请用专业、客观的态度进行分析，给出有理有据的比分预测。")
    String chat(String userMessage);


    @SystemMessage("你是一位专业的足球数据分析师，擅长基于比赛结果和上轮ai分析结果，不断优化分析模型，给出优化后的模型建议。比如提问方式？赛前数据等等")
    String chatV2(String userMessage);
}
