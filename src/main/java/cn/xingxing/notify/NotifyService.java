package cn.xingxing.notify;

import cn.xingxing.config.FootballApiConfig;
import cn.xingxing.dto.MatchAnalysis;
import cn.xingxing.service.MessageBuilderService;
import cn.xingxing.util.HttpClientUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yangyuanliang
 * @version 1.9
 * @date 2025/12/27 14:24
 */
@Slf4j
@Service
public class NotifyService {

	@Autowired
	private MessageBuilderService messageBuilder;
	@Autowired
	private FootballApiConfig apiConfig;

	public void sendMsg(List<MatchAnalysis> analyses) {
		if (!CollectionUtils.isEmpty(analyses)) {
			// 按每5条一组分批发送
			List<List<MatchAnalysis>> batches = splitIntoBatches(analyses, 3);

			log.info("共分析 {} 场比赛，将分为 {} 批发送",
					analyses.size(), batches.size());
			// 发送每个批次
			for (int i = 0; i < batches.size(); i++) {
				List<MatchAnalysis> batch = batches.get(i);
				log.info("正在发送第 {} 批，包含 {} 场比赛", i + 1, batch.size());

				// 构建消息
				String message = messageBuilder.buildFeishuMessage(batch, true);

				try {
					// 如果是最后一批，可以添加结束标记
					if (i == batches.size() - 1) {
						message = addEndMarker(message);
					}

					String response = HttpClientUtil.doPost(
							apiConfig.getFeishuWebhookUrl(),
							message,
							apiConfig.getHttpReadTimeout()
					);
					log.info("第 {} 批飞书消息发送成功: {}", i + 1, response);

					// 批次之间添加延迟，避免发送过快
					if (i < batches.size() - 1) {
						Thread.sleep(1000); // 1秒延迟
					}

				} catch (Exception e) {
					log.error("第 {} 批飞书消息发送失败", i + 1, e);
				}
			}
		} else {
			log.info("没有需要分析的比赛");
		}
	}

	/**
	 * 为最后一批消息添加结束标记
	 */
	private String addEndMarker(String message) {
		try {
			// 解析JSON消息
			JSONObject messageJson = JSONObject.parseObject(message);
			JSONObject card = messageJson.getJSONObject("card");
			JSONArray elements = card.getJSONArray("elements");

			// 添加结束标记
			JSONObject endMarker = new JSONObject();
			endMarker.put("tag", "div");

			JSONObject textContent = new JSONObject();
			textContent.put("tag", "lark_md");
			textContent.put("content", "--- 所有比赛分析发送完毕 ---");

			endMarker.put("text", textContent);
			elements.add(endMarker);

			// 重新构建消息
			return messageJson.toJSONString();
		} catch (Exception e) {
			log.warn("添加结束标记失败，返回原始消息", e);
			return message;
		}
	}

	private List<List<MatchAnalysis>> splitIntoBatches(List<MatchAnalysis> analyses, int batchSize) {
		List<List<MatchAnalysis>> batches = new ArrayList<>();

		for (int i = 0; i < analyses.size(); i += batchSize) {
			int end = Math.min(analyses.size(), i + batchSize);
			batches.add(new ArrayList<>(analyses.subList(i, end)));
		}

		return batches;
	}
}
