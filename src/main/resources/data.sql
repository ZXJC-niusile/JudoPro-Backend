-- JudoPro 数据库初始化数据
-- 创建时间: 2025-01-20
-- 说明: 为系统提供基础测试数据
-- 重要说明: 除用户数据外，其他测试数据仅为设计参考，实际系统中运动员数据存储在Lucene索引中

-- 插入测试用户数据
INSERT INTO `users` (`username`, `email`, `password`, `avatar`, `enabled`) VALUES
('admin', 'admin@judopro.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdxIcnvtcflQjXaC', 'https://example.com/avatars/admin.jpg', TRUE),
('judofan01', 'fan01@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdxIcnvtcflQjXaC', 'https://example.com/avatars/fan01.jpg', TRUE),
('coach_zhang', 'zhang@judoclub.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdxIcnvtcflQjXaC', 'https://example.com/avatars/coach.jpg', TRUE)
ON DUPLICATE KEY UPDATE `username` = VALUES(`username`);

-- 插入测试运动员数据 (仅设计参考，实际未在代码中使用)
INSERT INTO `players` (`name`, `age`, `age_group`, `weight`, `weight_class`, `country`, `continent`, `image`, `location`, `location_icon`) VALUES
('张伟', 25, 'SENIOR', 73.50, '-73kg', '中国', 'ASIA', 'https://example.com/players/zhang_wei.jpg', '北京', 'https://example.com/icons/beijing.png'),
('田中太郎', 22, 'JUNIOR', 81.20, '-81kg', '日本', 'ASIA', 'https://example.com/players/tanaka.jpg', '东京', 'https://example.com/icons/tokyo.png'),
('李明浩', 19, 'JUNIOR', 66.80, '-66kg', '韩国', 'ASIA', 'https://example.com/players/lee.jpg', '首尔', 'https://example.com/icons/seoul.png'),
('Pierre Dubois', 28, 'SENIOR', 90.30, '-90kg', '法国', 'EUROPE', 'https://example.com/players/pierre.jpg', '巴黎', 'https://example.com/icons/paris.png'),
('Maria Silva', 24, 'SENIOR', 57.40, '-57kg', '巴西', 'SOUTH_AMERICA', 'https://example.com/players/maria.jpg', '里约热内卢', 'https://example.com/icons/rio.png'),
('John Smith', 26, 'SENIOR', 100.50, '-100kg', '美国', 'NORTH_AMERICA', 'https://example.com/players/john.jpg', '纽约', 'https://example.com/icons/newyork.png'),
('Ahmed Hassan', 23, 'SENIOR', 78.90, '-81kg', '埃及', 'AFRICA', 'https://example.com/players/ahmed.jpg', '开罗', 'https://example.com/icons/cairo.png'),
('Emma Johnson', 20, 'JUNIOR', 52.30, '-52kg', '澳大利亚', 'OCEANIA', 'https://example.com/players/emma.jpg', '悉尼', 'https://example.com/icons/sydney.png'),
('Ivan Petrov', 27, 'SENIOR', 110.20, '+100kg', '俄罗斯', 'EUROPE', 'https://example.com/players/ivan.jpg', '莫斯科', 'https://example.com/icons/moscow.png'),
('Yuki Sato', 17, 'CADET', 60.70, '-60kg', '日本', 'ASIA', 'https://example.com/players/yuki.jpg', '大阪', 'https://example.com/icons/osaka.png')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- 插入测试赛事数据 (仅设计参考，实际未在代码中使用)
INSERT INTO `events` (`name`, `description`, `start_date`, `end_date`, `location`, `country`, `continent`, `event_type`, `age_groups`, `weight_classes`, `max_participants`, `registration_deadline`, `entry_fee`, `prize_money`, `status`, `organizer`, `contact_email`, `website`) VALUES
('2024年世界柔道锦标赛', '国际柔道联合会主办的世界最高级别柔道赛事', '2024-05-15', '2024-05-22', '东京', '日本', 'ASIA', 'WORLD_CHAMPIONSHIP', 'SENIOR', '-48kg,-52kg,-57kg,-63kg,-70kg,-78kg,+78kg,-60kg,-66kg,-73kg,-81kg,-90kg,-100kg,+100kg', 500, '2024-04-15', 200.00, 1000000.00, 'COMPLETED', '国际柔道联合会', 'info@ijf.org', 'https://www.ijf.org'),
('2024年亚洲柔道锦标赛', '亚洲地区最高级别的柔道赛事', '2024-04-10', '2024-04-14', '北京', '中国', 'ASIA', 'CONTINENTAL_CHAMPIONSHIP', 'SENIOR,JUNIOR', '-48kg,-52kg,-57kg,-63kg,-70kg,-78kg,+78kg,-60kg,-66kg,-73kg,-81kg,-90kg,-100kg,+100kg', 300, '2024-03-10', 150.00, 500000.00, 'COMPLETED', '亚洲柔道联合会', 'info@jua.sport', 'https://www.jua.sport'),
('2024年全国青少年柔道锦标赛', '中国全国青少年柔道最高级别赛事', '2024-07-20', '2024-07-25', '上海', '中国', 'ASIA', 'NATIONAL_CHAMPIONSHIP', 'CADET,JUNIOR', '-40kg,-44kg,-48kg,-52kg,-57kg,-63kg,-70kg,+70kg,-50kg,-55kg,-60kg,-66kg,-73kg,-81kg,-90kg,+90kg', 200, '2024-06-20', 100.00, 200000.00, 'UPCOMING', '中国柔道协会', 'info@judo.org.cn', 'https://www.judo.org.cn'),
('巴黎国际柔道公开赛', '欧洲地区重要的国际柔道赛事', '2024-09-05', '2024-09-08', '巴黎', '法国', 'EUROPE', 'INTERNATIONAL_TOURNAMENT', 'SENIOR', '-48kg,-52kg,-57kg,-63kg,-70kg,-78kg,+78kg,-60kg,-66kg,-73kg,-81kg,-90kg,-100kg,+100kg', 400, '2024-08-05', 180.00, 300000.00, 'UPCOMING', '法国柔道联合会', 'contact@ffjudo.com', 'https://www.ffjudo.com'),
('东京青少年柔道友谊赛', '促进国际青少年柔道交流的友谊赛事', '2024-11-15', '2024-11-17', '东京', '日本', 'ASIA', 'LOCAL_TOURNAMENT', 'CADET,JUNIOR', '-40kg,-44kg,-48kg,-52kg,-57kg,-63kg,-70kg,+70kg,-50kg,-55kg,-60kg,-66kg,-73kg,-81kg,-90kg,+90kg', 150, '2024-10-15', 80.00, 50000.00, 'UPCOMING', '东京柔道协会', 'info@tokyo-judo.jp', 'https://www.tokyo-judo.jp')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- 插入测试照片数据 (仅设计参考，实际未在代码中使用)
INSERT INTO `photos` (`title`, `url`, `thumbnail_url`, `description`, `category`, `player_id`, `event_name`, `event_date`, `location`, `photographer`, `tags`, `view_count`, `like_count`, `is_public`) VALUES
('张伟夺冠瞬间', 'https://example.com/photos/zhang_wei_victory.jpg', 'https://example.com/thumbnails/zhang_wei_victory_thumb.jpg', '张伟在2024年亚洲柔道锦标赛-73kg级别决赛中夺冠的精彩瞬间', 'COMPETITION', 1, '2024年亚洲柔道锦标赛', '2024-04-14', '北京', '李摄影师', '夺冠,一本,张伟,亚锦赛', 1250, 89, TRUE),
('田中太郎训练照', 'https://example.com/photos/tanaka_training.jpg', 'https://example.com/thumbnails/tanaka_training_thumb.jpg', '田中太郎在道场进行日常训练的照片', 'TRAINING', 2, NULL, '2024-03-20', '东京柔道馆', '山田摄影', '训练,田中太郎,道场,技术', 856, 45, TRUE),
('世锦赛开幕式', 'https://example.com/photos/world_championship_opening.jpg', 'https://example.com/thumbnails/world_championship_opening_thumb.jpg', '2024年世界柔道锦标赛盛大开幕式现场', 'EVENT', NULL, '2024年世界柔道锦标赛', '2024-05-15', '东京', '官方摄影团队', '开幕式,世锦赛,东京,仪式', 3420, 156, TRUE),
('李明浩精彩技术', 'https://example.com/photos/lee_technique.jpg', 'https://example.com/thumbnails/lee_technique_thumb.jpg', '李明浩展示完美的内股技术', 'SPOTLIGHT', 3, '2024年亚洲柔道锦标赛', '2024-04-12', '北京', '体育摄影师', '技术,内股,李明浩,精彩', 967, 72, TRUE),
('Pierre训练集锦', 'https://example.com/photos/pierre_training_session.jpg', 'https://example.com/thumbnails/pierre_training_session_thumb.jpg', 'Pierre Dubois在法国国家队训练营的训练照片', 'TRAINING', 4, NULL, '2024-02-28', '巴黎国家训练中心', 'Jean Photographer', '训练,Pierre,法国,国家队', 634, 38, TRUE),
('Maria Silva比赛瞬间', 'https://example.com/photos/maria_competition.jpg', 'https://example.com/thumbnails/maria_competition_thumb.jpg', 'Maria Silva在比赛中展现出色技术的瞬间', 'COMPETITION', 5, '泛美柔道锦标赛', '2024-03-25', '里约热内卢', 'Carlos Fotógrafo', '比赛,Maria,技术,泛美', 1123, 67, TRUE),
('青少年训练营', 'https://example.com/photos/youth_training_camp.jpg', 'https://example.com/thumbnails/youth_training_camp_thumb.jpg', '国际青少年柔道训练营的集体训练照片', 'TRAINING', NULL, NULL, '2024-06-10', '北京体育大学', '训练营摄影师', '青少年,训练营,集体,教学', 789, 54, TRUE),
('John Smith力量训练', 'https://example.com/photos/john_strength_training.jpg', 'https://example.com/thumbnails/john_strength_training_thumb.jpg', 'John Smith进行专项力量训练的照片', 'TRAINING', 6, NULL, '2024-04-05', '纽约训练中心', 'Mike Photographer', '力量训练,John,专项,美国', 445, 29, TRUE),
('赛事颁奖典礼', 'https://example.com/photos/award_ceremony.jpg', 'https://example.com/thumbnails/award_ceremony_thumb.jpg', '2024年世界柔道锦标赛颁奖典礼现场', 'EVENT', NULL, '2024年世界柔道锦标赛', '2024-05-22', '东京', '官方摄影', '颁奖,典礼,世锦赛,冠军', 2156, 98, TRUE),
('Emma技术展示', 'https://example.com/photos/emma_technique_demo.jpg', 'https://example.com/thumbnails/emma_technique_demo_thumb.jpg', 'Emma Johnson在技术展示活动中的精彩表现', 'SPOTLIGHT', 8, NULL, '2024-05-30', '悉尼柔道中心', 'Australian Sports Photo', '技术展示,Emma,澳大利亚,青少年', 678, 41, TRUE)
ON DUPLICATE KEY UPDATE `title` = VALUES(`title`);

-- 插入测试参赛记录数据 (仅设计参考，实际未在代码中使用)
INSERT INTO `participations` (`player_id`, `event_id`, `weight_class`, `result`, `final_rank`, `points_earned`, `notes`) VALUES
(1, 2, '-73kg', 'GOLD', 1, 1000, '决赛中以一本获胜，表现出色'),
(2, 1, '-81kg', 'SILVER', 2, 700, '决赛惜败，但整体表现优异'),
(3, 2, '-66kg', 'BRONZE', 3, 500, '铜牌争夺战中获胜'),
(4, 1, '-90kg', 'PARTICIPATED', 7, 100, '止步八强，经验有待提升'),
(5, 1, '-57kg', 'GOLD', 1, 1000, '完美的技术展示，实至名归的冠军'),
(6, 1, '-100kg', 'BRONZE', 3, 500, '在铜牌争夺战中展现出强大实力'),
(7, 2, '-81kg', 'PARTICIPATED', 5, 200, '首次参加洲际赛事，表现可圈可点'),
(8, 1, '-52kg', 'SILVER', 2, 700, '年轻选手的出色表现，未来可期'),
(9, 1, '+100kg', 'PARTICIPATED', 9, 50, '经验丰富但状态有所下滑'),
(10, 2, '-60kg', 'GOLD', 1, 1000, '青少年组别的绝对实力派')
ON DUPLICATE KEY UPDATE `player_id` = VALUES(`player_id`);

-- 插入测试用户收藏数据 (仅设计参考，实际未在代码中使用)
INSERT INTO `user_favorites` (`user_id`, `target_type`, `target_id`) VALUES
(2, 'PLAYER', 1),
(2, 'PLAYER', 5),
(2, 'EVENT', 1),
(2, 'PHOTO', 1),
(2, 'PHOTO', 3),
(3, 'PLAYER', 2),
(3, 'PLAYER', 4),
(3, 'EVENT', 2),
(3, 'PHOTO', 2),
(3, 'PHOTO', 7),
(1, 'PLAYER', 1),
(1, 'PLAYER', 2),
(1, 'PLAYER', 3),
(1, 'EVENT', 1),
(1, 'EVENT', 2)
ON DUPLICATE KEY UPDATE `user_id` = VALUES(`user_id`);