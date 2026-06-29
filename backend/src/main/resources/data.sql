-- Initial service types
INSERT INTO service_type (name, code, description, base_duration_min, base_price, extra_pet_price, applicable_species, checklist_template, sort_order) VALUES
('上门喂养', 'FEEDING', '上门喂食、换水、铲屎、基础陪伴', 30, 49.00, 20.00, '["CAT","DOG","BIRD","FISH","REPTILE","SMALL_ANIMAL"]', '["检查宠物状态","喂食换水","清理猫砂/排泄物","简单陪伴","拍照记录"]', 1),
('遛狗服务', 'DOG_WALKING', '专业遛狗，GPS轨迹实时可查', 45, 59.00, 25.00, '["DOG"]', '["佩戴牵引绳","检查狗狗状态","户外遛行","清理排泄物","安全送回"]', 2),
('陪玩互动', 'PLAY_SESSION', '深度陪伴、互动玩耍、情绪安抚', 60, 79.00, 30.00, '["CAT","DOG","SMALL_ANIMAL"]', '["检查宠物状态","互动玩耍","情绪观察","拍照录像","记录行为"]', 3),
('基础洗护', 'GROOMING', '基础清洁、梳毛、指甲修剪', 45, 89.00, 35.00, '["CAT","DOG"]', '["检查皮肤状态","梳理毛发","清洁耳朵","修剪指甲","拍照对比"]', 4),
('喂药护理', 'MEDICATION', '按医嘱喂药、伤口护理、术后看护', 30, 69.00, 25.00, '["CAT","DOG","BIRD","REPTILE","SMALL_ANIMAL"]', '["确认用药清单","按时喂药","观察反应","记录症状","异常及时上报"]', 5),
('长期寄养', 'BOARDING', '在喂养师家中寄养照顾(按天)', 1440, 129.00, 50.00, '["CAT","DOG","SMALL_ANIMAL"]', '["接收宠物","安排生活区域","按时喂食","日常陪伴","每日报告"]', 6);

-- Demo owner
INSERT INTO owner (nickname, phone, address, member_level) VALUES
('张小花', '13800001111', '北京市朝阳区建国路88号', 'VIP'),
('李明', '13800002222', '北京市海淀区中关村大街1号', 'NORMAL');

-- Demo pets
INSERT INTO pet (owner_id, name, species, breed, gender, weight, personality, diet_info) VALUES
(1, '小橘', 'CAT', '橘猫', 'NEUTERED_MALE', 5.5, '亲人、贪吃、不怕生', '每天两顿，早晚各一次，皇家K36成猫粮50g/顿'),
(1, '豆豆', 'DOG', '柯基', 'MALE', 12.0, '活泼好动、喜欢追球', '冠能中型犬成犬粮，每天两顿各100g，不能吃巧克力'),
(2, '咪咪', 'CAT', '英短蓝猫', 'SPAYED_FEMALE', 4.2, '胆小怕生、喜欢躲沙发下', '渴望无谷猫粮40g/顿，不吃湿粮');

-- Demo sitter
INSERT INTO sitter (name, phone, bio, experience_years, service_area, home_latitude, home_longitude, accepted_species, base_price, status, rating) VALUES
('王大勇', '13900001111', '5年宠物护理经验，持有宠物护理师证书，特别擅长照顾猫咪', 5, '北京市朝阳区', 39.9087, 116.4716, '["CAT","DOG"]', 49.00, 'ACTIVE', 4.90),
('赵小美', '13900002222', '3年猫咪寄养经验，家中有独立猫房', 3, '北京市海淀区', 39.9842, 116.3074, '["CAT","SMALL_ANIMAL"]', 55.00, 'ACTIVE', 4.85);
