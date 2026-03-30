package cn.xingxing.web;

import cn.xingxing.dto.ApiResponse;
import cn.xingxing.dto.groupbuy.ClaimRewardDto;
import cn.xingxing.dto.groupbuy.CreateGroupBuyDto;
import cn.xingxing.dto.groupbuy.GroupBuyVo;
import cn.xingxing.dto.groupbuy.JoinGroupBuyDto;
import cn.xingxing.dto.groupbuy.MyGroupBuyQueryDto;
import cn.xingxing.service.GroupBuyService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 拼团控制器
 * @Author: yangyuanliang
 * @Date: 2026-03-30
 * @Version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/groupbuy")
public class GroupBuyController {

    @Autowired
    private GroupBuyService groupBuyService;

    /**
     * 发起拼团
     * @param createDto 创建拼团请求
     * @return 拼团信息
     */
    @PostMapping("/create")
    public ApiResponse<GroupBuyVo> createGroupBuy(@RequestBody CreateGroupBuyDto createDto) {
        log.info("发起拼团请求: userId={}, groupSize={}", createDto.getUserId(), createDto.getGroupSize());
        GroupBuyVo result = groupBuyService.createGroupBuy(createDto);
        return ApiResponse.success(result);
    }

    /**
     * 加入拼团
     * @param joinDto 加入拼团请求
     * @return 拼团信息
     */
    @PostMapping("/join")
    public ApiResponse<GroupBuyVo> joinGroupBuy(@RequestBody JoinGroupBuyDto joinDto) {
        log.info("加入拼团请求: groupId={}, userId={}", joinDto.getGroupId(), joinDto.getUserId());
        GroupBuyVo result = groupBuyService.joinGroupBuy(joinDto);
        return ApiResponse.success(result);
    }

    /**
     * 查询拼团详情
     * @param groupId 拼团ID
     * @return 拼团信息
     */
    @GetMapping("/detail/{groupId}")
    public ApiResponse<GroupBuyVo> getGroupBuyDetail(@PathVariable String groupId) {
        log.info("查询拼团详情: groupId={}", groupId);
        GroupBuyVo result = groupBuyService.getGroupBuyDetail(groupId);
        return ApiResponse.success(result);
    }

    /**
     * 查询我的拼团列表（分页）
     * @param queryDto 查询条件
     * @return 分页结果
     */
    @PostMapping("/my/list")
    public ApiResponse<Page<GroupBuyVo>> getMyGroupBuyList(@RequestBody MyGroupBuyQueryDto queryDto) {
        log.info("查询我的拼团列表: userId={}, status={}, pageNum={}, pageSize={}",
                queryDto.getUserId(), queryDto.getStatus(), queryDto.getPageNum(), queryDto.getPageSize());
        Page<GroupBuyVo> result = groupBuyService.getMyGroupBuyList(queryDto);
        return ApiResponse.success(result);
    }

    /**
     * 团长领取积分奖励
     * @param claimDto 领取请求
     * @return 是否成功
     */
    @PostMapping("/claim/reward")
    public ApiResponse<Boolean> claimReward(@RequestBody ClaimRewardDto claimDto) {
        log.info("团长领取积分: groupId={}, userId={}", claimDto.getGroupId(), claimDto.getUserId());
        Boolean result = groupBuyService.claimReward(claimDto);
        return ApiResponse.success(result);
    }
}
