-- 创建消息表
CREATE TABLE IF NOT EXISTS `message` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
  `title` VARCHAR(200) NOT NULL COMMENT '消息标题',
  `content` LONGTEXT NOT NULL COMMENT '消息内容',
  `type` VARCHAR(50) COMMENT '消息类型：announcement(公告)、notice(通知)、event(活动)',
  `publisher_id` BIGINT COMMENT '发布者ID',
  `publisher_name` VARCHAR(100) COMMENT '发布者名称',
  `views` INT DEFAULT 0 COMMENT '浏览次数',
  `likes` INT DEFAULT 0 COMMENT '点赞数',
  `status` VARCHAR(20) DEFAULT 'published' COMMENT '消息状态：published(已发布)、draft(草稿)、archived(归档)',
  `publish_time` DATETIME COMMENT '发布时间',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_status` (`status`),
  INDEX `idx_type` (`type`),
  INDEX `idx_publish_time` (`publish_time`),
  INDEX `idx_views` (`views`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- 插入示例数据
INSERT INTO `message` (`title`, `content`, `type`, `publisher_id`, `publisher_name`, `status`, `publish_time`) VALUES
('欢迎使用智能排课系统', '这是一个基于OptaPlanner的智能排课系统，能够自动生成最优的课程安排方案。', 'announcement', 1, 'admin', 'published', NOW()),
('系统更新通知', '系统已更新美化了用户界面和优化了排课算法，欢迎大家使用新版本！', 'notice', 1, 'admin', 'published', NOW()),
('春季学期排课开始', '请各位教师和班级管理员在系统中确认课程信息，准备开始春季学期排课。', 'notice', 1, 'admin', 'published', NOW()),
('学院学术讲座', '今年4月将举办多场学术讲座，邀请业界专家为大家分享最新技术动态，欢迎参加。', 'event', 1, 'admin', 'published', NOW()),
('教室临时变更', '由于教室维修，B栋203教室本周停用，使用该教室的课程已自动调整，请查看详细信息。', 'notice', 1, 'admin', 'published', NOW());
