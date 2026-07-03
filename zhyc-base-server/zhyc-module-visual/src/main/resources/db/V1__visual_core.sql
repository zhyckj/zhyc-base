-- Copyright (c) 2026 众汇云创科技（深圳）有限公司.
-- This file is part of ZHYC and is licensed for non-commercial use only.
-- Commercial use requires a separate written license from the copyright holder.
-- SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial

CREATE TABLE IF NOT EXISTS visual_dataset (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码，用于共享表模式数据隔离',
    dataset_code VARCHAR(64) NOT NULL COMMENT '数据集编码，租户内唯一',
    dataset_name VARCHAR(128) NOT NULL COMMENT '数据集名称',
    datasource_code VARCHAR(64) NOT NULL COMMENT '数据源编码，对应低代码数据源或默认数据源',
    sql_text TEXT NOT NULL COMMENT '查询 SQL，由数据集执行器统一校验后执行',
    dataset_status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '数据集状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识',
    PRIMARY KEY (id),
    UNIQUE KEY uk_visual_dataset_tenant_code (tenant_id, dataset_code),
    KEY idx_visual_dataset_tenant_status (tenant_id, dataset_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='可视化数据集表';

CREATE TABLE IF NOT EXISTS visual_report (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码，用于共享表模式数据隔离',
    report_code VARCHAR(64) NOT NULL COMMENT '报表编码，租户内唯一',
    report_name VARCHAR(128) NOT NULL COMMENT '报表名称',
    dataset_code VARCHAR(64) NOT NULL COMMENT '数据集编码，指向同租户数据集',
    chart_type VARCHAR(32) NOT NULL DEFAULT 'table' COMMENT '图表类型',
    config_json TEXT NOT NULL COMMENT '图表配置 JSON',
    report_status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '报表状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识',
    PRIMARY KEY (id),
    UNIQUE KEY uk_visual_report_tenant_code (tenant_id, report_code),
    KEY idx_visual_report_tenant_status (tenant_id, report_status),
    KEY idx_visual_report_tenant_dataset (tenant_id, dataset_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='可视化报表表';

CREATE TABLE IF NOT EXISTS visual_screen (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码，用于共享表模式数据隔离',
    screen_code VARCHAR(64) NOT NULL COMMENT '大屏编码，租户内唯一',
    screen_name VARCHAR(128) NOT NULL COMMENT '大屏名称',
    layout_json TEXT NOT NULL COMMENT '大屏布局 JSON，保存组件位置、尺寸和报表编码',
    screen_status VARCHAR(32) NOT NULL DEFAULT 'draft' COMMENT '大屏状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识',
    PRIMARY KEY (id),
    UNIQUE KEY uk_visual_screen_tenant_code (tenant_id, screen_code),
    KEY idx_visual_screen_tenant_status (tenant_id, screen_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='可视化大屏表';
