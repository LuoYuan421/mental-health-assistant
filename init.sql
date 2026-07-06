-- 心理健康AI助手 数据库初始化脚本
-- 根据entity类反推

CREATE DATABASE IF NOT EXISTS mental_health_assistant DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
USE mental_health_assistant;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `password` VARCHAR(255) NOT NULL COMMENT '密码',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像路径',
  `gender` INT DEFAULT NULL COMMENT '性别 0:未知 1:男 2:女',
  `birthday` DATE DEFAULT NULL COMMENT '生日',
  `user_type` INT DEFAULT 1 COMMENT '用户类型 1:普通用户 2:管理员',
  `status` INT DEFAULT 1 COMMENT '状态 0:禁用 1:正常',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 咨询会话表
CREATE TABLE IF NOT EXISTS `consultation_session` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `session_title` VARCHAR(200) DEFAULT NULL COMMENT '会话标题',
  `started_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
  `last_emotion_analysis` TEXT DEFAULT NULL COMMENT '最后一次情绪分析结果(JSON)',
  `last_emotion_updated_at` DATETIME DEFAULT NULL COMMENT '情绪分析更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='咨询会话表';

-- 咨询消息表
CREATE TABLE IF NOT EXISTS `consultation_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `session_id` BIGINT NOT NULL COMMENT '会话ID',
  `sender_type` INT NOT NULL COMMENT '发送者类型 1:用户 2:AI助手',
  `message_type` INT NOT NULL DEFAULT 1 COMMENT '消息类型 1:文本',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `emotion_tag` VARCHAR(50) DEFAULT NULL COMMENT '情绪标签',
  `ai_model` VARCHAR(50) DEFAULT NULL COMMENT '使用的AI模型',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='咨询消息表';

-- 情绪日记表
CREATE TABLE IF NOT EXISTS `emotion_diary` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `diary_date` DATE NOT NULL COMMENT '日记日期',
  `mood_score` INT NOT NULL COMMENT '情绪评分(1-10)',
  `dominant_emotion` VARCHAR(50) NOT NULL COMMENT '主要情绪',
  `emotion_triggers` TEXT DEFAULT NULL COMMENT '情绪触发因素',
  `diary_content` TEXT DEFAULT NULL COMMENT '今日感想/日记内容',
  `sleep_quality` INT DEFAULT NULL COMMENT '睡眠质量(1-5)',
  `stress_level` INT DEFAULT NULL COMMENT '压力水平(1-5)',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_emotion_user_id` (`user_id`),
  KEY `idx_emotion_diary_date` (`diary_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='情绪日记表';

-- 知识分类表
CREATE TABLE IF NOT EXISTS `knowledge_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `category_name` VARCHAR(100) NOT NULL COMMENT '分类名称',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `status` INT DEFAULT 1 COMMENT '状态 0:禁用 1:正常',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识分类表';

-- 知识文章表
CREATE TABLE IF NOT EXISTS `knowledge_article` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(500) NOT NULL COMMENT '文章标题',
  `summary` VARCHAR(1000) DEFAULT NULL COMMENT '文章摘要',
  `content` TEXT DEFAULT NULL COMMENT '文章内容',
  `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
  `author_name` VARCHAR(50) DEFAULT NULL COMMENT '作者名称',
  `cover_image` VARCHAR(255) DEFAULT NULL COMMENT '封面图片路径',
  `read_count` INT DEFAULT 0 COMMENT '阅读量',
  `status` INT DEFAULT 0 COMMENT '状态 0:草稿 1:已发布 2:已下线',
  `tags` VARCHAR(500) DEFAULT NULL COMMENT '标签，逗号分隔',
  `published_at` DATETIME DEFAULT NULL COMMENT '发布时间',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_article_category_id` (`category_id`),
  KEY `idx_article_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识文章表';

-- 插入一些初始分类数据
INSERT INTO `knowledge_category` (`category_name`, `sort_order`, `status`) VALUES
('情绪管理', 1, 1),
('压力调节', 2, 1),
('人际关系', 3, 1),
('自我认知', 4, 1),
('心理健康常识', 5, 1);

-- 心理测评问卷表
CREATE TABLE IF NOT EXISTS `questionnaire` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL COMMENT '量表名称',
  `code` VARCHAR(20) NOT NULL COMMENT '量表编码',
  `description` TEXT DEFAULT NULL COMMENT '量表描述',
  `questions` TEXT NOT NULL COMMENT '题目列表(JSON)',
  `question_count` INT NOT NULL COMMENT '题目数量',
  `total_score` INT NOT NULL COMMENT '满分',
  `scoring_rules` TEXT DEFAULT NULL COMMENT '评分规则(JSON)',
  `status` INT DEFAULT 1 COMMENT '状态 0:禁用 1:正常',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='心理测评问卷表';

-- 测评记录表
CREATE TABLE IF NOT EXISTS `questionnaire_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `questionnaire_id` BIGINT NOT NULL COMMENT '问卷ID',
  `answers` TEXT NOT NULL COMMENT '答案(JSON数组)',
  `total_score` INT NOT NULL COMMENT '总分',
  `level` VARCHAR(20) NOT NULL COMMENT '评估等级',
  `suggestion` TEXT DEFAULT NULL COMMENT '评估建议',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_record_user_id` (`user_id`),
  KEY `idx_record_questionnaire_id` (`questionnaire_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测评记录表';

-- PHQ-9 抑郁量表
INSERT INTO `questionnaire` (`name`, `code`, `description`, `questions`, `question_count`, `total_score`, `scoring_rules`, `status`) VALUES
('PHQ-9 抑郁症筛查量表', 'PHQ-9', 'PHQ-9是国际通用的抑郁症筛查量表，用于评估过去两周内抑郁症状的严重程度。',
'[{"id":1,"text":"做事时提不起劲或没有兴趣","options":["完全不会","好几天","一半以上的天数","几乎每天"]},{"id":2,"text":"感到心情低落、沮丧或绝望","options":["完全不会","好几天","一半以上的天数","几乎每天"]},{"id":3,"text":"入睡困难、睡不安稳或睡眠过多","options":["完全不会","好几天","一半以上的天数","几乎每天"]},{"id":4,"text":"感觉疲倦或没有活力","options":["完全不会","好几天","一半以上的天数","几乎每天"]},{"id":5,"text":"食欲不振或吃太多","options":["完全不会","好几天","一半以上的天数","几乎每天"]},{"id":6,"text":"觉得自己很糟或觉得自己很失败，让自己或家人失望","options":["完全不会","好几天","一半以上的天数","几乎每天"]},{"id":7,"text":"对事物专注有困难，例如阅读报纸或看电视","options":["完全不会","好几天","一半以上的天数","几乎每天"]},{"id":8,"text":"动作或说话速度缓慢到旁人可察觉，或正好相反——坐立不安、动来动去","options":["完全不会","好几天","一半以上的天数","几乎每天"]},{"id":9,"text":"有不如死掉或用某种方式伤害自己的念头","options":["完全不会","好几天","一半以上的天数","几乎每天"]}]',
9, 27, '{"0":"正常(0-4分)","1":"轻度(5-9分)","2":"中度(10-14分)","3":"中重度(15-19分)","4":"重度(20-27分)"}', 1),

('GAD-7 广泛性焦虑量表', 'GAD-7', 'GAD-7是用于筛查广泛性焦虑障碍的自评量表，评估过去两周内的焦虑水平。',
'[{"id":1,"text":"感到紧张、焦虑或急切","options":["完全不会","好几天","一半以上的天数","几乎每天"]},{"id":2,"text":"不能够停止或控制担忧","options":["完全不会","好几天","一半以上的天数","几乎每天"]},{"id":3,"text":"对各种各样的事情担忧过多","options":["完全不会","好几天","一半以上的天数","几乎每天"]},{"id":4,"text":"很难放松下来","options":["完全不会","好几天","一半以上的天数","几乎每天"]},{"id":5,"text":"由于不安而无法静坐","options":["完全不会","好几天","一半以上的天数","几乎每天"]},{"id":6,"text":"变得容易烦恼或急躁","options":["完全不会","好几天","一半以上的天数","几乎每天"]},{"id":7,"text":"感到似乎将有可怕的事情发生","options":["完全不会","好几天","一半以上的天数","几乎每天"]}]',
7, 21, '{"0":"正常(0-4分)","1":"轻度(5-9分)","2":"中度(10-14分)","3":"重度(15-21分)"}', 1);

-- 插入一些初始文章数据
INSERT INTO `knowledge_article` (`title`, `summary`, `content`, `category_id`, `author_name`, `status`, `tags`, `read_count`, `published_at`) VALUES
('认识焦虑：它不是你的敌人', '焦虑是人类进化中保留下来的生存机制，适度焦虑能帮助我们应对挑战。', '## 什么是焦虑？\n\n焦虑是人类面对潜在威胁时产生的一种自然情绪反应。从进化角度看，焦虑帮助我们的祖先避开危险，是一种保护机制。\n\n## 适度焦虑 vs 过度焦虑\n\n适度的焦虑可以提高注意力和工作效率，但当焦虑过度或持续时间过长，就可能影响日常生活。\n\n## 如何与焦虑共处\n\n1. **觉察它**：当焦虑出现时，先承认它的存在\n2. **接纳它**：不要试图压制焦虑，这反而会加强它\n3. **理解它**：问问自己，焦虑在试图保护你什么？\n4. **行动起来**：做几次深呼吸，或者起身走动', 1, '小药AI', 1, '焦虑,情绪管理,心理健康', 128, NOW()),
('五个实用的压力管理技巧', '学会管理压力，让生活重回正轨。这些简单易行的方法可以帮助你缓解日常压力。', '## 1. 4-7-8 呼吸法\n\n吸气4秒，屏住呼吸7秒，呼气8秒。重复3-4次，可以快速平复心情。\n\n## 2. 写下你的烦恼\n\n把困扰你的事情写在纸上，这个简单的动作能帮助你理清思路，减少大脑的负担。\n\n## 3. 运动释压\n\n哪怕只是10分钟的散步，也能促进内啡肽分泌，让你感觉好很多。\n\n## 4. 限制信息输入\n\n适当减少刷手机的时间，尤其是睡前。过多的信息会加重心理负担。\n\n## 5. 和信任的人聊聊\n\n倾诉本身就是一种疗愈。不需要对方给建议，有人倾听就够了。', 2, '小药AI', 1, '压力,技巧,自我调节', 89, NOW()),
('如何建立健康的人际边界', '设立边界不是自私，而是对自己和他人都负责的表现。', '## 什么是人际边界？\n\n人际边界是指你在与他人互动时，为自己设定的合理 limits。它定义了什么是可以接受的，什么是不可以接受的。\n\n## 为什么边界很重要？\n\n- 没有边界容易导致疲惫和怨恨\n- 健康的边界让关系更持久\n- 边界帮助你维护自尊\n\n## 如何设立边界？\n\n1. 明确自己的需求和底线\n2. 用"我"开头表达，比如"我需要一些独处时间"\n3. 说"不"不代表你不关心对方\n4. 边界需要反复沟通和维护', 3, '小药AI', 1, '人际关系,边界,沟通', 67, NOW()),
('认识自我：你真的了解自己吗？', '自我认知是心理健康的基石。了解自己的情绪模式、价值观和需求，是成长的第一步。', '## 自我认知的重要性\n\n心理学家认为，自我认知是情商的核心组成部分。了解自己的人更能做出符合内心的决策。\n\n## 如何提升自我认知？\n\n### 1. 写情绪日记\n每天记录自己的情绪变化，慢慢你会发现自己的情绪模式。\n\n### 2. 接受他人反馈\n有时候别人眼中的你，能补充你的盲区。\n\n### 3. 反思关键时刻\n回顾人生中的重要决定，思考当时的动机和感受。\n\n### 4. 尝试心理咨询\n专业的心理咨询师可以帮助你更深入地了解自己。', 4, '小药AI', 1, '自我认知,成长,心理', 45, NOW()),
('睡眠与心理健康的密切关系', '良好的睡眠是心理健康的支柱。睡眠不足会影响情绪调节、认知功能和整体幸福感。', '## 睡眠不足的后果\n\n长期睡眠不足会导致：\n- 情绪波动加大\n- 注意力下降\n- 免疫力降低\n- 焦虑和抑郁风险增加\n\n## 改善睡眠的建议\n\n1. **固定作息时间**：每天同一时间睡觉和起床\n2. **睡前1小时远离屏幕**：蓝光会抑制褪黑素分泌\n3. **营造舒适环境**：保持卧室凉爽、安静、黑暗\n4. **避免睡前咖啡因**：下午2点后不喝咖啡或浓茶\n5. **建立睡前仪式**：如泡脚、听轻音乐、做拉伸', 5, '小药AI', 1, '睡眠,健康,生活习惯', 156, NOW());
