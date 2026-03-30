package cn.xingxing.service.impl;

import cn.xingxing.common.exception.CommonException;
import cn.xingxing.dto.groupbuy.*;
import cn.xingxing.entity.GroupBuy;
import cn.xingxing.entity.GroupBuyMember;
import cn.xingxing.entity.User;
import cn.xingxing.enums.PointChangeType;
import cn.xingxing.mapper.GroupBuyMapper;
import cn.xingxing.mapper.GroupBuyMemberMapper;
import cn.xingxing.mapper.UserMapper;
import cn.xingxing.service.GroupBuyService;
import cn.xingxing.service.UserPointDetailService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 拼团服务实现类
 * @Author: yangyuanliang
 * @Date: 2026-03-30
 * @Version: 1.0
 */
@Slf4j
@Service
public class GroupBuyServiceImpl extends ServiceImpl<GroupBuyMapper, GroupBuy> implements GroupBuyService {

    @Autowired
    private GroupBuyMapper groupBuyMapper;

    @Autowired
    private GroupBuyMemberMapper groupBuyMemberMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserPointDetailService userPointDetailService;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GroupBuyVo createGroupBuy(CreateGroupBuyDto createDto) {
        String userId = createDto.getUserId();
        Integer groupSize = createDto.getGroupSize() == null || createDto.getGroupSize() < 2 ? 2 : createDto.getGroupSize();

        log.info("用户 {} 发起拼团，团员数量: {}", userId, groupSize);

        // 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.error("用户不存在: {}", userId);
            throw new CommonException(10001, "用户不存在");
        }

        // 检查用户三天内是否已发起过拼团
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        LambdaQueryWrapper<GroupBuy> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(GroupBuy::getLeaderId, userId)
                .ge(GroupBuy::getCreateTime, threeDaysAgo);
        Long count = groupBuyMapper.selectCount(checkWrapper);

        log.info("-- SQL: SELECT COUNT(*) FROM group_buy WHERE leader_id = '{}' AND create_time >= '{}' AND deleted = 0",
                userId, threeDaysAgo.format(DATE_TIME_FORMATTER));

        if (count > 0) {
            log.error("用户 {} 三天内已发起过拼团", userId);
            throw new CommonException(10003, "三天内已发起过拼团，请稍后再试");
        }

        // 创建拼团
        GroupBuy groupBuy = GroupBuy.builder()
                .leaderId(userId)
                .groupSize(groupSize)
                .currentSize(1)
                .status(0)  // 0-进行中
                .expireTime(LocalDateTime.now().plusHours(12))  // 12小时后过期
                .rewardDistributed(0)
                .build();

        groupBuyMapper.insert(groupBuy);
        log.info("-- SQL: INSERT INTO group_buy (id, leader_id, group_size, current_size, status, expire_time, reward_distributed, create_time, deleted) " +
                "VALUES ('{}', '{}', {}, 1, 0, '{}', 0, NOW(), 0)",
                groupBuy.getId(), userId, groupSize, groupBuy.getExpireTime().format(DATE_TIME_FORMATTER));

        // 创建团长成员记录
        GroupBuyMember leaderMember = GroupBuyMember.builder()
                .groupId(groupBuy.getId())
                .userId(userId)
                .isLeader(1)  // 1-团长
                .joinTime(LocalDateTime.now())
                .build();

        groupBuyMemberMapper.insert(leaderMember);
        log.info("-- SQL: INSERT INTO group_buy_member (id, group_id, user_id, is_leader, join_time, create_time, deleted) " +
                "VALUES ('{}', '{}', '{}', 1, NOW(), NOW(), 0)",
                leaderMember.getId(), groupBuy.getId(), userId);

        log.info("拼团创建成功，拼团ID: {}", groupBuy.getId());

        return buildGroupBuyVo(groupBuy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GroupBuyVo joinGroupBuy(JoinGroupBuyDto joinDto) {
        String groupId = joinDto.getGroupId();
        String userId = joinDto.getUserId();

        log.info("用户 {} 加入拼团: {}", userId, groupId);

        // 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.error("用户不存在: {}", userId);
            throw new CommonException(10001, "用户不存在");
        }

        // 查询拼团信息
        GroupBuy groupBuy = groupBuyMapper.selectById(groupId);
        log.info("-- SQL: SELECT * FROM group_buy WHERE id = '{}' AND deleted = 0", groupId);

        if (groupBuy == null) {
            log.error("拼团不存在: {}", groupId);
            throw new CommonException(10004, "拼团不存在");
        }

        // 检查拼团状态
        if (groupBuy.getStatus() != 0) {
            log.error("拼团状态异常: {}, status: {}", groupId, groupBuy.getStatus());
            throw new CommonException(10005, "拼团已结束");
        }

        // 检查是否过期
        if (LocalDateTime.now().isAfter(groupBuy.getExpireTime())) {
            log.error("拼团已过期: {}", groupId);
            // 更新拼团状态为失败
            groupBuy.setStatus(2);
            groupBuyMapper.updateById(groupBuy);
            log.info("-- SQL: UPDATE group_buy SET status = 2 WHERE id = '{}' AND deleted = 0", groupId);
            throw new CommonException(10006, "拼团已过期");
        }

        // 检查是否已满员
        if (groupBuy.getCurrentSize() >= groupBuy.getGroupSize()) {
            log.error("拼团已满员: {}", groupId);
            throw new CommonException(10007, "拼团已满员");
        }

        // 检查用户是否已加入该团
        LambdaQueryWrapper<GroupBuyMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(GroupBuyMember::getGroupId, groupId)
                .eq(GroupBuyMember::getUserId, userId);
        Long memberCount = groupBuyMemberMapper.selectCount(memberWrapper);
        log.info("-- SQL: SELECT COUNT(*) FROM group_buy_member WHERE group_id = '{}' AND user_id = '{}' AND deleted = 0",
                groupId, userId);

        if (memberCount > 0) {
            log.error("用户已加入该拼团: userId={}, groupId={}", userId, groupId);
            throw new CommonException(10008, "您已加入该拼团");
        }

        // 添加团员记录
        GroupBuyMember member = GroupBuyMember.builder()
                .groupId(groupId)
                .userId(userId)
                .isLeader(0)  // 0-团员
                .joinTime(LocalDateTime.now())
                .build();

        groupBuyMemberMapper.insert(member);
        log.info("-- SQL: INSERT INTO group_buy_member (id, group_id, user_id, is_leader, join_time, create_time, deleted) " +
                "VALUES ('{}', '{}', '{}', 0, NOW(), NOW(), 0)",
                member.getId(), groupId, userId);

        // 更新拼团当前人数
        groupBuy.setCurrentSize(groupBuy.getCurrentSize() + 1);

        // 检查是否达到目标人数
        if (groupBuy.getCurrentSize() >= groupBuy.getGroupSize()) {
            groupBuy.setStatus(1);  // 1-成功
            groupBuy.setSuccessTime(LocalDateTime.now());
            log.info("拼团成功，等待团长领取积分奖励");
        }

        groupBuyMapper.updateById(groupBuy);
        log.info("-- SQL: UPDATE group_buy SET current_size = {}, status = {}, success_time = {}, reward_distributed = {} WHERE id = '{}' AND deleted = 0",
                groupBuy.getCurrentSize(), groupBuy.getStatus(),
                groupBuy.getSuccessTime() != null ? "'" + groupBuy.getSuccessTime().format(DATE_TIME_FORMATTER) + "'" : "NULL",
                groupBuy.getRewardDistributed(), groupId);

        log.info("用户 {} 成功加入拼团: {}", userId, groupId);

        return buildGroupBuyVo(groupBuy);
    }

    @Override
    public GroupBuyVo getGroupBuyDetail(String groupId) {
        log.info("查询拼团详情: {}", groupId);

        GroupBuy groupBuy = groupBuyMapper.selectById(groupId);
        log.info("-- SQL: SELECT * FROM group_buy WHERE id = '{}' AND deleted = 0", groupId);

        if (groupBuy == null) {
            log.error("拼团不存在: {}", groupId);
            throw new CommonException(10004, "拼团不存在");
        }

        return buildGroupBuyVo(groupBuy);
    }

    /**
     * 发放积分奖励
     */
    private void distributeRewards(GroupBuy groupBuy) {
        if (groupBuy.getRewardDistributed() == 1) {
            log.warn("积分已发放，跳过: {}", groupBuy.getId());
            return;
        }

        String groupId = groupBuy.getId();

        // 查询所有成员
        LambdaQueryWrapper<GroupBuyMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupBuyMember::getGroupId, groupId);
        List<GroupBuyMember> members = groupBuyMemberMapper.selectList(wrapper);
        log.info("-- SQL: SELECT * FROM group_buy_member WHERE group_id = '{}' AND deleted = 0", groupId);

        for (GroupBuyMember member : members) {
            // 查询用户当前积分
            User user = userMapper.selectById(member.getUserId());
            log.info("-- SQL: SELECT * FROM user WHERE id = '{}' AND deleted = 0", member.getUserId());

            Long pointBefore = user.getPoint();
            Long pointReward = member.getIsLeader() == 1 ? groupBuy.getGroupSize() : 2L;  // 团长5分，团员2分
            Long pointAfter = pointBefore + pointReward;

            // 更新用户积分
            user.setPoint(pointAfter);
            userMapper.updateById(user);
            log.info("-- SQL: UPDATE user SET point = {} WHERE id = '{}' AND deleted = 0", pointAfter, member.getUserId());

            // 保存积分明细
            String changeType = member.getIsLeader() == 1
                    ? PointChangeType.GROUP_BUY_LEADER.getCode()
                    : PointChangeType.GROUP_BUY_MEMBER.getCode();
            String remark = member.getIsLeader() == 1
                    ? "拼团成功团长奖励"
                    : "拼团成功团员奖励";

            userPointDetailService.savePointDetail(
                    member.getUserId(),
                    pointReward,
                    pointBefore,
                    pointAfter,
                    changeType,
                    null,
                    remark
            );
            log.info("-- SQL: INSERT INTO user_point_detail (id, user_id, point_change, point_before, point_after, change_type, match_id, remark, create_time, deleted) " +
                    "VALUES ('生成ID', '{}', {}, {}, {}, '{}', NULL, '{}', NOW(), 0)",
                    member.getUserId(), pointReward, pointBefore, pointAfter, changeType, remark);

            log.info("用户 {} 获得拼团奖励: {} 积分", member.getUserId(), pointReward);
        }

        // 标记积分已发放
        groupBuy.setRewardDistributed(1);
        log.info("拼团 {} 积分发放完成", groupId);
    }

    /**
     * 构建拼团VO
     */
    private GroupBuyVo buildGroupBuyVo(GroupBuy groupBuy) {
        // 查询团长信息
        User leader = userMapper.selectById(groupBuy.getLeaderId());
        log.info("-- SQL: SELECT * FROM user WHERE id = '{}' AND deleted = 0", groupBuy.getLeaderId());

        // 查询成员列表
        LambdaQueryWrapper<GroupBuyMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupBuyMember::getGroupId, groupBuy.getId());
        List<GroupBuyMember> members = groupBuyMemberMapper.selectList(wrapper);
        log.info("-- SQL: SELECT * FROM group_buy_member WHERE group_id = '{}' AND deleted = 0", groupBuy.getId());

        List<GroupBuyMemberVo> memberVos = members.stream().map(member -> {
            User user = userMapper.selectById(member.getUserId());
            log.info("-- SQL: SELECT * FROM user WHERE id = '{}' AND deleted = 0", member.getUserId());

            return GroupBuyMemberVo.builder()
                    .id(member.getId())
                    .userId(member.getUserId())
                    .userName(user != null ? user.getUserName() : "未知用户")
                    .isLeader(member.getIsLeader())
                    .joinTime(member.getJoinTime().format(DATE_TIME_FORMATTER))
                    .build();
        }).collect(Collectors.toList());

        String statusDesc;
        switch (groupBuy.getStatus()) {
            case 0:
                statusDesc = "进行中";
                break;
            case 1:
                statusDesc = "拼团成功";
                break;
            case 2:
                statusDesc = "拼团失败";
                break;
            default:
                statusDesc = "未知状态";
        }

        return GroupBuyVo.builder()
                .id(groupBuy.getId())
                .leaderId(groupBuy.getLeaderId())
                .leaderName(leader != null ? leader.getUserName() : "未知用户")
                .groupSize(groupBuy.getGroupSize())
                .currentSize(groupBuy.getCurrentSize())
                .status(groupBuy.getStatus())
                .statusDesc(statusDesc)
                .expireTime(groupBuy.getExpireTime().format(DATE_TIME_FORMATTER))
                .successTime(groupBuy.getSuccessTime() != null ? groupBuy.getSuccessTime().format(DATE_TIME_FORMATTER) : null)
                .createTime(groupBuy.getCreateTime().format(DATE_TIME_FORMATTER))
                .members(memberVos)
                .build();
    }

    @Override
    public Page<GroupBuyVo> getMyGroupBuyList(MyGroupBuyQueryDto queryDto) {
        String userId = queryDto.getUserId();
        log.info("查询用户 {} 的拼团列表, 状态: {}, 页码: {}, 每页: {}",
                userId, queryDto.getStatus(), queryDto.getPageNum(), queryDto.getPageSize());

        // 先查询用户参与的所有拼团ID
        LambdaQueryWrapper<GroupBuyMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(GroupBuyMember::getUserId, userId);
        List<GroupBuyMember> memberList = groupBuyMemberMapper.selectList(memberWrapper);
        log.info("-- SQL: SELECT * FROM group_buy_member WHERE user_id = '{}' AND deleted = 0", userId);

        if (memberList.isEmpty()) {
            log.info("用户 {} 未参与任何拼团", userId);
            return new Page<>(queryDto.getPageNum(), queryDto.getPageSize());
        }

        // 提取拼团ID列表
        List<String> groupIds = memberList.stream()
                .map(GroupBuyMember::getGroupId)
                .collect(Collectors.toList());

        // 构建拼团查询条件
        LambdaQueryWrapper<GroupBuy> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(GroupBuy::getId, groupIds);

        // 如果指定了状态，添加状态过滤
        if (queryDto.getStatus() != null) {
            queryWrapper.eq(GroupBuy::getStatus, queryDto.getStatus());
        }

        // 按创建时间倒序
        queryWrapper.orderByDesc(GroupBuy::getCreateTime);

        // 构建SQL日志
        StringBuilder sqlLog = new StringBuilder("-- SQL: SELECT * FROM group_buy WHERE id IN (");
        sqlLog.append(groupIds.stream().map(id -> "'" + id + "'").collect(Collectors.joining(", ")));
        sqlLog.append(")");
        if (queryDto.getStatus() != null) {
            sqlLog.append(" AND status = ").append(queryDto.getStatus());
        }
        sqlLog.append(" AND deleted = 0 ORDER BY create_time DESC LIMIT ")
                .append((queryDto.getPageNum() - 1) * queryDto.getPageSize())
                .append(", ")
                .append(queryDto.getPageSize());
        log.info(sqlLog.toString());

        // 分页查询
        Page<GroupBuy> page = new Page<>(queryDto.getPageNum(), queryDto.getPageSize());
        Page<GroupBuy> resultPage = this.page(page, queryWrapper);

        // 转换为VO
        Page<GroupBuyVo> voPage = new Page<>();
        voPage.setCurrent(resultPage.getCurrent());
        voPage.setSize(resultPage.getSize());
        voPage.setTotal(resultPage.getTotal());
        voPage.setPages(resultPage.getPages());

        List<GroupBuyVo> voList = resultPage.getRecords().stream()
                .map(this::buildGroupBuyVo)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        log.info("查询到 {} 条拼团记录", resultPage.getTotal());

        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean claimReward(ClaimRewardDto claimDto) {
        String groupId = claimDto.getGroupId();
        String userId = claimDto.getUserId();

        log.info("团长 {} 领取拼团 {} 的积分奖励", userId, groupId);

        // 查询拼团信息
        GroupBuy groupBuy = groupBuyMapper.selectById(groupId);
        log.info("-- SQL: SELECT * FROM group_buy WHERE id = '{}' AND deleted = 0", groupId);

        if (groupBuy == null) {
            log.error("拼团不存在: {}", groupId);
            throw new CommonException(10004, "拼团不存在");
        }

        // 验证是否为团长
        if (!groupBuy.getLeaderId().equals(userId)) {
            log.error("用户 {} 不是拼团 {} 的团长", userId, groupId);
            throw new CommonException(10009, "只有团长才能领取积分奖励");
        }

        // 检查拼团状态是否为成功
        if (groupBuy.getStatus() != 1) {
            log.error("拼团状态不是成功: groupId={}, status={}", groupId, groupBuy.getStatus());
            throw new CommonException(10010, "拼团未成功，无法领取奖励");
        }

        // 检查积分是否已发放
        if (groupBuy.getRewardDistributed() == 1) {
            log.error("拼团 {} 积分已领取", groupId);
            throw new CommonException(10011, "积分已领取，请勿重复领取");
        }

        // 发放积分奖励
        distributeRewards(groupBuy);

        // 标记积分已发放
        groupBuy.setRewardDistributed(1);
        groupBuyMapper.updateById(groupBuy);
        log.info("-- SQL: UPDATE group_buy SET reward_distributed = 1 WHERE id = '{}' AND deleted = 0", groupId);

        log.info("团长 {} 成功领取拼团 {} 的积分奖励", userId, groupId);

        return true;
    }
}
