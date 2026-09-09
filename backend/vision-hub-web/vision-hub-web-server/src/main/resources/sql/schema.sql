
-- 创建数据库（如果需要）
-- CREATE DATABASE IF NOT EXISTS `vision_hub` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- USE `vision_hub`;

-- ========================================
-- 用户表
-- ========================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名(唯一标识)',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    `nickname` VARCHAR(100) DEFAULT NULL COMMENT '昵称',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像路径',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `last_login_at` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(128) DEFAULT NULL COMMENT '最后登录IP',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注信息',
    `created_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `updated_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_username` (`username`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 初始化默认管理员账号 (用户名: unicom  密码: ZJ_Unicom)
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `status`, `created_by`) VALUES
('unicom', '$2a$10$8gMMeRlSUIAYjD6jcI0AWu1No7VOEG.skIkd.oI3FcDJ0ao0jWOXa', '管理员', 1, 'unicom');

-- ========================================
-- 大模型信息表
-- ========================================
DROP TABLE IF EXISTS `llm_model`;
CREATE TABLE `llm_model` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `model_name` VARCHAR(100) NOT NULL COMMENT '模型名称',
    `model_id` VARCHAR(100) NOT NULL COMMENT '模型标识',
    `model_type` VARCHAR(50) NOT NULL COMMENT '模型功能类型',
    `provider` VARCHAR(50) NOT NULL COMMENT '供应商',
    `api_url` VARCHAR(500) NOT NULL COMMENT 'API地址',
    `api_key` VARCHAR(500) NOT NULL COMMENT 'API密钥(加密存储)',
    `model_version` VARCHAR(50) DEFAULT NULL COMMENT '模型版本号',
    `icon` VARCHAR(500) DEFAULT NULL COMMENT '图标路径',
    `context_length` INT DEFAULT NULL COMMENT '模型上下文长度(tokens)',
    `max_tokens` INT DEFAULT NULL COMMENT '最大输出tokens数',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '模型描述',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注信息',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',
    `image_transfer_mode` VARCHAR(20) NOT NULL DEFAULT 'base64' COMMENT '图片传输模式: url-URL链接, base64-Base64编码',
    `created_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `updated_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_model_type` (`model_type`),
    KEY `idx_provider` (`provider`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='大模型信息表';

-- ========================================
-- 提示词模板表
-- ========================================
DROP TABLE IF EXISTS `prompt_template`;
CREATE TABLE `prompt_template` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `template_name` VARCHAR(100) NOT NULL COMMENT '模板名称',
    `template_content` TEXT NOT NULL COMMENT '模板内容',
    `output_format` TEXT DEFAULT NULL COMMENT '输出格式模板(JSON格式)',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '模板描述',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注信息',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',
    `created_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `updated_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_template_name` (`template_name`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提示词模板表';

-- ========================================
-- 行业表
-- ========================================
DROP TABLE IF EXISTS `industry`;
CREATE TABLE `industry` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `industry_name` VARCHAR(100) NOT NULL COMMENT '行业名称',
    `industry_code` VARCHAR(50) NOT NULL COMMENT '行业编码',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '行业描述',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注信息',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',
    `created_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `updated_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_industry_code` (`industry_code`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='行业表';

-- ========================================
-- 提示词模板-行业关联表
-- ========================================
DROP TABLE IF EXISTS `prompt_template_industry`;
CREATE TABLE `prompt_template_industry` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `template_id` BIGINT UNSIGNED NOT NULL COMMENT '提示词模板ID',
    `industry_id` BIGINT UNSIGNED NOT NULL COMMENT '行业ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `created_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `updated_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_template_industry` (`template_id`, `industry_id`),
    KEY `idx_template_id` (`template_id`),
    KEY `idx_industry_id` (`industry_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提示词模板-行业关联表';

INSERT INTO `industry`(`id`, `industry_name`, `industry_code`, `description`, `status`, `remark`, `sort_order`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES (1, '安全生产', 'SAFE_PRODUCTION', '安全生产相关行业，包含消防、工业安全等场景', 1, NULL, 0, 'admin', NULL, '2026-05-21 08:54:21', '2026-05-21 08:54:21', 0);

INSERT INTO `prompt_template`(`id`, `template_name`, `template_content`, `output_format`, `description`, `status`, `remark`, `sort_order`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES (1, '明火检测', '检测图片中是否存在真实明火。', '{\"isAlarm\":{\"name\":\"是否告警\",\"type\":\"bool\",\"desc\":\"true表示检测到火焰，false表示未检测到\",\"required\":true,\"exampleValue\":true},\"count\":{\"name\":\"数量\",\"type\":\"int\",\"desc\":\"检测到的火焰区域数量（0表示未检测到）\",\"required\":true,\"exampleValue\":2},\"confidence\":{\"name\":\"置信度\",\"type\":\"int\",\"desc\":\"0-100之间的整数\",\"required\":true,\"exampleValue\":70},\"coordinates\":{\"name\":\"坐标\",\"type\":\"array\",\"desc\":\"每个火焰区域的归一化边界框坐标，格式为 [[x1,y1,x2,y2], ...]，取值范围 0-1，其中 (x1,y1) 是左上角坐标，(x2,y2) 是右下角坐标\",\"required\":true,\"items\":{\"type\":\"array\",\"items\":{\"type\":\"double\",\"desc\":\"归一化坐标值（0-1），格式为 [x1, y1, x2, y2]\"}},\"exampleValue\":[[0.12,0.34,0.56,0.78],[0.87,0.65,0.43,0.21]]},\"description\":{\"name\":\"说明\",\"type\":\"string\",\"desc\":\"描述每个火焰区域的位置、大小、颜色等特征\",\"required\":false,\"exampleValue\":\"检测到两处明火，分别位于图像的左上角区域和右上角区域\"}}', '用于检测图片中是否存在真实明火，包括点燃的蜡烛、打火机、火把、火炬等', 1, NULL, 0, 'admin', NULL, '2026-05-21 08:54:21', '2026-05-21 08:54:21', 0);
INSERT INTO `prompt_template`(`id`, `template_name`, `template_content`, `output_format`, `description`, `status`, `remark`, `sort_order`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES (2, '烟火检测', '检测图片中是否存在真实明火或烟雾。', '{\"isAlarm\":{\"name\":\"是否告警\",\"type\":\"bool\",\"desc\":\"true表示检测到烟火，false表示未检测到\",\"required\":true,\"exampleValue\":true},\"count\":{\"name\":\"数量\",\"type\":\"int\",\"desc\":\"检测到的烟火区域数量（0表示未检测到）\",\"required\":true,\"exampleValue\":2},\"confidence\":{\"name\":\"置信度\",\"type\":\"int\",\"desc\":\"0-100之间的整数\",\"required\":true,\"exampleValue\":70},\"coordinates\":{\"name\":\"坐标\",\"type\":\"array\",\"desc\":\"每个烟火区域的归一化边界框坐标，格式为 [[x1,y1,x2,y2], ...]，取值范围 0-1，其中 (x1,y1) 是左上角坐标，(x2,y2) 是右下角坐标\",\"required\":true,\"items\":{\"type\":\"array\",\"items\":{\"type\":\"double\",\"desc\":\"归一化坐标值（0-1），格式为 [x1, y1, x2, y2]\"}},\"exampleValue\":[[0.12,0.34,0.56,0.78],[0.87,0.65,0.43,0.21]]},\"description\":{\"name\":\"说明\",\"type\":\"string\",\"desc\":\"描述每个烟火区域的位置、大小、颜色等特征\",\"required\":false,\"exampleValue\":\"检测到两处烟火，分别位于图像的左上角区域和右上角区域\"}}', '用于检测图片中是否存在真实烟火，包括火焰或火焰产生的烟雾都会触发告警', 1, NULL, 0, 'admin', NULL, '2026-05-21 08:54:21', '2026-05-21 08:54:21', 0);
INSERT INTO `prompt_template_industry` (`id`, `template_id`, `industry_id`, `created_by`) VALUES
(1, 1, 1, 'system'),
(2, 2, 1, 'system');

-- ========================================
-- 算法表
-- ========================================
DROP TABLE IF EXISTS `algorithm`;
CREATE TABLE `algorithm` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `algorithm_name` VARCHAR(100) NOT NULL COMMENT '算法名称',
    `algorithm_code` VARCHAR(100) NOT NULL COMMENT '算法编码(唯一标识)',
    `algorithm_type` VARCHAR(50) NOT NULL COMMENT '算法类型: llm-大模型类, vision-视觉模型',
    `media_type` VARCHAR(20) NOT NULL COMMENT '介质类型: image-图片, stream-视频流',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '算法描述',

    `llm_model_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '大模型ID',
    `detection_content` TEXT DEFAULT NULL COMMENT '检测内容',
    `output_format` JSON DEFAULT NULL COMMENT '输出格式(JSON格式)',

    `model_format` VARCHAR(50) DEFAULT NULL COMMENT '模型格式: yolo, onnx, tensorrt',
    `model_path` VARCHAR(500) DEFAULT NULL COMMENT '模型路径',
    `inference_url` VARCHAR(500) DEFAULT NULL COMMENT '推理接口地址',
    `inference_config` JSON DEFAULT NULL COMMENT '推理配置(JSON格式)',

    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `version` VARCHAR(50) DEFAULT '1.0.0' COMMENT '算法版本',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注信息',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',
    `icon` VARCHAR(500) DEFAULT NULL COMMENT '图标路径',
    `created_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `updated_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记: 0-未删除, 1-已删除',

    PRIMARY KEY (`id`),
    KEY `idx_algorithm_code` (`algorithm_code`),
    KEY `idx_algorithm_type` (`algorithm_type`),
    KEY `idx_llm_model_id` (`llm_model_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='算法表';

-- ========================================
-- 算法调用记录表
-- ========================================
DROP TABLE IF EXISTS `algorithm_call_log`;
CREATE TABLE `algorithm_call_log` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `task_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '任务ID（任务调度执行时填充）',
    `device_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '设备ID（任务调度执行时填充）',
    `algorithm_id` BIGINT UNSIGNED NOT NULL COMMENT '算法ID',
    `algorithm_code` VARCHAR(100) NOT NULL COMMENT '算法编码',
    `algorithm_type` VARCHAR(50) NOT NULL COMMENT '算法类型',

    `request_id` VARCHAR(64) DEFAULT NULL COMMENT '请求ID',
    `input_data` JSON DEFAULT NULL COMMENT '输入数据(JSON格式)',

    `exec_status` VARCHAR(20) NOT NULL COMMENT '执行状态: success-成功, failed-失败, timeout-超时',
    `error_message` TEXT DEFAULT NULL COMMENT '执行错误信息',
    `execute_duration` BIGINT DEFAULT NULL COMMENT '执行时长(毫秒)',

    `recognize_code` INT DEFAULT NULL COMMENT '识别状态码: 0-成功, 50101-50199-大模型错误, 50201-50299-视觉模型错误',
    `recognize_message` VARCHAR(200) DEFAULT NULL COMMENT '识别状态消息',
    `raw_response` TEXT DEFAULT NULL COMMENT '原始响应内容',
    `output_data` JSON DEFAULT NULL COMMENT '业务数据(JSON格式)',
    `output_data_before` JSON DEFAULT NULL COMMENT '原始算法输出(JSON格式)',
    `is_alarm_before` TINYINT DEFAULT NULL COMMENT '算法执行前记录的告警状态: 0-否, 1-是',
    `is_alarm_after` TINYINT DEFAULT NULL COMMENT '算法执行后告警状态: 0-否, 1-是',
    `execute_source` VARCHAR(20) DEFAULT NULL COMMENT '执行来源: test-算法测试, task-任务调度',
    `result_image` VARCHAR(500) DEFAULT NULL COMMENT '结果图对象名称，检测到物体为画框图，未检测到为原图',

    `prompt_tokens` INT DEFAULT NULL COMMENT 'prompt消耗token数',
    `completion_tokens` INT DEFAULT NULL COMMENT 'completion消耗token数',
    `total_tokens` INT DEFAULT NULL COMMENT '总消耗token数',

    `started_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    `finished_at` DATETIME DEFAULT NULL COMMENT '结束时间',

    `created_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `updated_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记: 0-未删除, 1-已删除',

    PRIMARY KEY (`id`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_algorithm_id` (`algorithm_id`),
    KEY `idx_algorithm_code` (`algorithm_code`),
    KEY `idx_request_id` (`request_id`),
    KEY `idx_exec_status` (`exec_status`),
    KEY `idx_recognize_code` (`recognize_code`),
    KEY `idx_started_at` (`started_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='算法调用记录表';

-- ========================================
-- 文件上传日志表
-- ========================================
DROP TABLE IF EXISTS `file_upload_log`;
CREATE TABLE `file_upload_log` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `business_type` VARCHAR(50) NOT NULL COMMENT '业务类型(如llm-icon, algorithm-icon)',
    `file_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `file_size` BIGINT NOT NULL COMMENT '文件大小(字节)',
    `file_type` VARCHAR(50) DEFAULT NULL COMMENT '文件类型(MIME类型)',
    `file_extension` VARCHAR(20) DEFAULT NULL COMMENT '文件扩展名',
    `object_name` VARCHAR(500) NOT NULL COMMENT 'MinIO存储对象名(相对路径)',
    `minio_url` VARCHAR(500) NOT NULL COMMENT 'MinIO完整访问URL',
    `bucket_name` VARCHAR(100) DEFAULT NULL COMMENT 'MinIO桶名称',
    `upload_status` TINYINT NOT NULL DEFAULT 1 COMMENT '上传状态: 0-失败, 1-成功',
    `error_message` VARCHAR(500) DEFAULT NULL COMMENT '错误信息(失败时记录)',
    `ip_address` VARCHAR(64) DEFAULT NULL COMMENT '上传者IP地址',
    `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '用户代理',
    `created_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `updated_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_business_type` (`business_type`),
    KEY `idx_upload_status` (`upload_status`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_object_name` (`object_name`(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件上传日志表';


-- ========================================
-- 创建视图(可选)
-- ========================================

CREATE OR REPLACE VIEW `v_algorithm_detail` AS
SELECT
    a.id,
    a.algorithm_name,
    a.algorithm_code,
    a.algorithm_type,
    a.media_type,
    a.description,
    a.status,
    a.version,
    CASE WHEN a.algorithm_type = 'llm' THEN
        JSON_OBJECT(
            'model_id', a.llm_model_id,
            'model_name', m.model_name,
            'model_id', m.model_id
        )
    ELSE
        JSON_OBJECT(
            'model_format', a.model_format,
            'model_path', a.model_path,
            'inference_url', a.inference_url
        )
    END AS config,
    a.created_at,
    a.updated_at
FROM `algorithm` a
LEFT JOIN `llm_model` m ON a.llm_model_id = m.id AND m.deleted = 0
WHERE a.deleted = 0;

-- ========================================
-- 设备表
-- ========================================
DROP TABLE IF EXISTS `device`;
CREATE TABLE `device` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `device_name` VARCHAR(100) NOT NULL COMMENT '设备名称',
    `device_code` VARCHAR(100) NOT NULL COMMENT '设备编码',
    `device_type` VARCHAR(20) NOT NULL COMMENT '设备类型: ipc-网络摄像头, nvr-网络录像机',
    `access_protocol` VARCHAR(50) NOT NULL COMMENT '接入协议: stream-视频流地址, gb28181-GB28181协议',
    `stream_url` VARCHAR(500) DEFAULT NULL COMMENT '视频流地址（接入协议为stream时必填）',
    `manufacturer` VARCHAR(100) DEFAULT NULL COMMENT '厂商',
    `model` VARCHAR(100) DEFAULT NULL COMMENT '型号',
    `device_status` VARCHAR(20) NOT NULL DEFAULT 'unknown' COMMENT '设备状态: online-运行中, offline-异常, unknown-未知',
    `last_check_time` DATETIME DEFAULT NULL COMMENT '最后检测时间',
    `last_check_status` VARCHAR(500) DEFAULT NULL COMMENT '最后检测结果',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '启用状态: 0-禁用, 1-启用',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注信息',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',
    `created_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `updated_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_device_code` (`device_code`),
    KEY `idx_device_type` (`device_type`),
    KEY `idx_device_status` (`device_status`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备表';

-- ========================================
-- 任务表
-- ========================================
DROP TABLE IF EXISTS `task`;
CREATE TABLE `task` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `task_name` VARCHAR(100) NOT NULL COMMENT '任务名称',
    `task_status` VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT '任务状态: draft-编排中, running-运行中',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '启用状态: 0-禁用, 1-启用',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注信息',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',
    `created_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `updated_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_task_status` (`task_status`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务表';

-- ========================================
-- 事件定义表（用户自定义事件）
-- ========================================
DROP TABLE IF EXISTS `event`;
CREATE TABLE `event` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `task_id` BIGINT UNSIGNED NOT NULL COMMENT '任务ID',
    `algorithm_id` BIGINT UNSIGNED NOT NULL COMMENT '算法ID',
    `event_name` VARCHAR(100) NOT NULL COMMENT '事件名称',
    `event_type` VARCHAR(20) DEFAULT NULL COMMENT '事件类型: output-输出事件, intercept-拦截事件',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `created_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `updated_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_algorithm_id` (`algorithm_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='事件定义表';

-- ========================================
-- 任务算法配置表
-- ========================================
DROP TABLE IF EXISTS `task_algorithm`;
CREATE TABLE `task_algorithm` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `task_id` BIGINT UNSIGNED NOT NULL COMMENT '任务ID',
    `algorithm_id` BIGINT UNSIGNED NOT NULL COMMENT '算法ID',
    `sample_interval` INT NOT NULL DEFAULT 10000 COMMENT '采样间隔，单位：毫秒',
    `validity_end` DATETIME DEFAULT NULL COMMENT '有效期结束时间（null表示长期有效）',
    `event_id` BIGINT UNSIGNED NOT NULL COMMENT '事件ID',
    `schedule_rules` JSON DEFAULT NULL COMMENT '调度规则列表，格式: [{"days":[1,2,3],"periods":[{"start":"09:00","end":"17:00"}]}]',
    `created_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `updated_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_algorithm` (`task_id`, `algorithm_id`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_algorithm_id` (`algorithm_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务算法配置表';

-- ========================================
-- 任务点位关联表
-- ========================================
DROP TABLE IF EXISTS `task_device`;
CREATE TABLE `task_device` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `task_id` BIGINT UNSIGNED NOT NULL COMMENT '任务ID',
    `algorithm_id` BIGINT UNSIGNED NOT NULL COMMENT '算法ID',
    `device_id` BIGINT UNSIGNED NOT NULL COMMENT '设备ID',
    `enabled` TINYINT NOT NULL DEFAULT 0 COMMENT '是否启用: 0-禁用, 1-启用',
    `created_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `updated_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_algorithm_device` (`task_id`, `algorithm_id`, `device_id`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_algorithm_id` (`algorithm_id`),
    KEY `idx_device_id` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务点位关联表';

-- ========================================
-- 事件记录表
-- ========================================
DROP TABLE IF EXISTS `event_record`;
CREATE TABLE `event_record` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `task_id` BIGINT UNSIGNED NOT NULL COMMENT '任务ID',
    `device_id` BIGINT UNSIGNED NOT NULL COMMENT '设备ID',
    `algorithm_id` BIGINT UNSIGNED NOT NULL COMMENT '算法ID',
    `event_id` BIGINT UNSIGNED NOT NULL COMMENT '事件ID',
    `event_name` VARCHAR(100) NOT NULL COMMENT '事件名称（冗余）',
    `event_type` VARCHAR(20) DEFAULT NULL COMMENT '事件类型: output-输出事件, intercept-拦截事件',
    `alarm_image` VARCHAR(500) DEFAULT NULL COMMENT '报警图片对象名称，画框后的结果图',
    `analysis_result` TEXT COMMENT '分析结果JSON',
    `confidence` DECIMAL(5,2) DEFAULT NULL COMMENT '置信度',
    `execute_duration` BIGINT DEFAULT NULL COMMENT '执行时长(毫秒)',
    `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '事件状态: pending-待确认, confirmed-已确认, ignored-已忽略',
    `created_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `updated_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_algorithm_id` (`algorithm_id`),
    KEY `idx_event_id` (`event_id`),
    KEY `idx_event_name` (`event_name`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='事件记录表';
