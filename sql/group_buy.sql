-- 拼团功能相关表

-- 拼团表
CREATE TABLE `group_buy` (
  `id` VARCHAR(32) NOT NULL COMMENT '拼团ID',
  `leader_id` VARCHAR(32) NOT NULL COMMENT '团长ID(发起人)',
  `group_size` INT NOT NULL DEFAULT 2 COMMENT '需要的团员数量',
  `current_size` INT NOT NULL DEFAULT 1 COMMENT '当前团员数量(含团长)',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '拼团状态: 0-进行中, 1-成功, 2-失败',
  `expire_time` DATETIME NOT NULL COMMENT '过期时间(2小时后)',
  `success_time` DATETIME NULL COMMENT '成功时间',
  `reward_distributed` TINYINT NOT NULL DEFAULT 0 COMMENT '积分是否已发放: 0-未发放, 1-已发放',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_leader_id` (`leader_id`),
  KEY `idx_status_expire` (`status`, `expire_time`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拼团表';

-- 拼团成员表
CREATE TABLE `group_buy_member` (
  `id` VARCHAR(32) NOT NULL COMMENT '成员记录ID',
  `group_id` VARCHAR(32) NOT NULL COMMENT '拼团ID',
  `user_id` VARCHAR(32) NOT NULL COMMENT '用户ID',
  `is_leader` TINYINT NOT NULL DEFAULT 0 COMMENT '是否团长: 0-团员, 1-团长',
  `join_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_join_time` (`join_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拼团成员表';
