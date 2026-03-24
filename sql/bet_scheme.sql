-- 投注方案主表
CREATE TABLE `bet_scheme` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '方案ID',
    `user_id` VARCHAR(50) NOT NULL COMMENT '用户ID',
    `scheme_no` VARCHAR(50) NOT NULL COMMENT '方案编号',
    `pass_types` VARCHAR(200) DEFAULT NULL COMMENT '过关方式(多个用逗号分隔,如:single,2_1)',
    `multiple` INT DEFAULT 1 COMMENT '倍数',
    `total_bets` INT DEFAULT 1 COMMENT '总注数',
    `total_amount` DECIMAL(10,2) DEFAULT 2.00 COMMENT '总金额(元)',
    `status` TINYINT DEFAULT 0 COMMENT '方案状态(0-待开奖,1-已中奖,2-未中奖,3-已取消)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT DEFAULT 0 COMMENT '逻辑删除符',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_scheme_no` (`scheme_no`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投注方案主表';

-- 投注方案明细表
CREATE TABLE `bet_scheme_detail` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细ID',
    `scheme_id` BIGINT NOT NULL COMMENT '方案ID',
    `match_id` BIGINT NOT NULL COMMENT '比赛ID',
    `match_num_str` VARCHAR(50) DEFAULT NULL COMMENT '比赛编号',
    `home_team_name` VARCHAR(100) DEFAULT NULL COMMENT '主队名称',
    `away_team_name` VARCHAR(100) DEFAULT NULL COMMENT '客队名称',
    `match_time` VARCHAR(20) DEFAULT NULL COMMENT '比赛时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` INT DEFAULT 0 COMMENT '逻辑删除符',

    PRIMARY KEY (`id`),
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投注方案明细表';

-- 投注方案选项表
CREATE TABLE `bet_scheme_option` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '选项ID',
    `detail_id` BIGINT NOT NULL COMMENT '明细ID',
    `scheme_id` BIGINT NOT NULL COMMENT '方案ID',
    `match_id` BIGINT NOT NULL COMMENT '比赛ID',
    `option_type` VARCHAR(20) NOT NULL COMMENT '选项类型(had-胜平负,hhad-让球胜平负,ttg-总进球,hafu-半全场,crs-比分)',
    `option_value` VARCHAR(50) NOT NULL COMMENT '选项值(如:3,0:0,1:1等)',
    `odds` DECIMAL(10,2) NOT NULL COMMENT '赔率',
    `is_hit` TINYINT DEFAULT NULL COMMENT '是否命中(0-未命中,1-命中,NULL-未开奖)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` INT DEFAULT 0 COMMENT '逻辑删除符',

    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投注方案选项表';
