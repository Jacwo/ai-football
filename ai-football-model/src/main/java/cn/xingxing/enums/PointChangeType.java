package cn.xingxing.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 积分变化类型枚举
 * @Author: yangyuanliang
 * @Date: 2026-03-30
 * @Version: 1.0
 */
@Getter
@AllArgsConstructor
public enum PointChangeType {

    DEDUCT_MATCH("DEDUCT_MATCH", "赛事扣除"),
    DEDUCT_INFO("DEDUCT_INFO", "情报扣除"),
    SIGN("SIGN", "签到奖励"),
    REGISTER("REGISTER", "注册赠送"),
    BIND_PHONE("BIND_PHONE", "绑定手机奖励");

    private final String code;
    private final String desc;
}
