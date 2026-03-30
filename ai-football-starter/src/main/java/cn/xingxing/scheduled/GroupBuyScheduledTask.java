package cn.xingxing.scheduled;

import cn.xingxing.entity.GroupBuy;
import cn.xingxing.mapper.GroupBuyMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 拼团定时任务
 * @Author: yangyuanliang
 * @Date: 2026-03-30
 * @Version: 1.0
 */
@Slf4j
@Component
public class GroupBuyScheduledTask {

    @Autowired
    private GroupBuyMapper groupBuyMapper;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 扫描过期的拼团，每5分钟执行一次
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void scanExpiredGroupBuy() {
        log.info("========== 开始扫描过期拼团任务 ==========");

        LocalDateTime now = LocalDateTime.now();
        log.info("当前时间: {}", now.format(DATE_TIME_FORMATTER));

        // 查询所有进行中且已过期的拼团
        LambdaQueryWrapper<GroupBuy> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupBuy::getStatus, 0)  // 状态为进行中
                .lt(GroupBuy::getExpireTime, now);  // 过期时间小于当前时间

        List<GroupBuy> expiredGroups = groupBuyMapper.selectList(queryWrapper);

        log.info("-- SQL: SELECT * FROM group_buy WHERE status = 0 AND expire_time < '{}' AND deleted = 0",
                now.format(DATE_TIME_FORMATTER));

        if (expiredGroups.isEmpty()) {
            log.info("未发现过期的拼团");
            log.info("========== 扫描过期拼团任务完成 ==========");
            return;
        }

        log.info("发现 {} 个过期的拼团", expiredGroups.size());

        int updateCount = 0;
        for (GroupBuy groupBuy : expiredGroups) {
            try {
                // 更新状态为失败
                groupBuy.setStatus(2);  // 2-失败
                int rows = groupBuyMapper.updateById(groupBuy);

                if (rows > 0) {
                    log.info("-- SQL: UPDATE group_buy SET status = 2 WHERE id = '{}' AND deleted = 0", groupBuy.getId());
                    log.info("拼团 {} 已标记为失败，团长: {}, 过期时间: {}, 当前人数: {}/{}",
                            groupBuy.getId(),
                            groupBuy.getLeaderId(),
                            groupBuy.getExpireTime().format(DATE_TIME_FORMATTER),
                            groupBuy.getCurrentSize(),
                            groupBuy.getGroupSize());
                    updateCount++;
                }
            } catch (Exception e) {
                log.error("更新拼团 {} 状态失败", groupBuy.getId(), e);
            }
        }

        log.info("成功更新 {} 个过期拼团的状态", updateCount);
        log.info("========== 扫描过期拼团任务完成 ==========");
    }

    /**
     * 扫描超过7天的失败拼团进行清理（可选）
     * 每天凌晨3点执行
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanOldFailedGroupBuy() {
        log.info("========== 开始清理旧的失败拼团任务 ==========");

        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        log.info("清理时间阈值: {}", sevenDaysAgo.format(DATE_TIME_FORMATTER));

        // 查询7天前失败的拼团
        LambdaQueryWrapper<GroupBuy> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupBuy::getStatus, 2)  // 状态为失败
                .lt(GroupBuy::getCreateTime, sevenDaysAgo);  // 创建时间在7天前

        List<GroupBuy> oldFailedGroups = groupBuyMapper.selectList(queryWrapper);

        log.info("-- SQL: SELECT * FROM group_buy WHERE status = 2 AND create_time < '{}' AND deleted = 0",
                sevenDaysAgo.format(DATE_TIME_FORMATTER));

        if (oldFailedGroups.isEmpty()) {
            log.info("未发现需要清理的旧拼团");
            log.info("========== 清理旧的失败拼团任务完成 ==========");
            return;
        }

        log.info("发现 {} 个需要清理的旧失败拼团", oldFailedGroups.size());

        int deleteCount = 0;
        for (GroupBuy groupBuy : oldFailedGroups) {
            try {
                // 逻辑删除
                int rows = groupBuyMapper.deleteById(groupBuy.getId());

                if (rows > 0) {
                    log.info("-- SQL: UPDATE group_buy SET deleted = 1 WHERE id = '{}' AND deleted = 0", groupBuy.getId());
                    log.info("已清理拼团 {}, 创建时间: {}",
                            groupBuy.getId(),
                            groupBuy.getCreateTime().format(DATE_TIME_FORMATTER));
                    deleteCount++;
                }
            } catch (Exception e) {
                log.error("清理拼团 {} 失败", groupBuy.getId(), e);
            }
        }

        log.info("成功清理 {} 个旧失败拼团", deleteCount);
        log.info("========== 清理旧的失败拼团任务完成 ==========");
    }
}
