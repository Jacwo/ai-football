package cn.xingxing.rag.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * RAG模块配置
 */
@Configuration
@ComponentScan(basePackages = "cn.xingxing.rag")
@EnableAsync
public class RagConfig {

}
