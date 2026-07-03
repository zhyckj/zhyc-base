-- Copyright (c) 2026 众汇云创科技（深圳）有限公司.
-- This file is part of ZHYC and is licensed for non-commercial use only.
-- Commercial use requires a separate written license from the copyright holder.
-- SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial

CREATE TABLE IF NOT EXISTS lowcode_data_source (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    code VARCHAR(64) NOT NULL COMMENT '数据源编码',
    name VARCHAR(128) NOT NULL COMMENT '数据源名称',
    dialect VARCHAR(32) NOT NULL COMMENT '数据库类型',
    jdbc_url VARCHAR(512) NOT NULL COMMENT 'JDBC 连接地址',
    username VARCHAR(128) NOT NULL COMMENT '数据库用户名',
    password_secret_ref VARCHAR(255) DEFAULT NULL COMMENT '数据库口令密钥引用',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_lowcode_ds_tenant_code (tenant_id, code),
    KEY idx_lowcode_ds_tenant_enabled (tenant_id, enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='低代码数据源表';

CREATE TABLE IF NOT EXISTS lowcode_table_model (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    data_source_id BIGINT DEFAULT NULL COMMENT '数据源主键',
    code VARCHAR(64) NOT NULL COMMENT '模型编码',
    name VARCHAR(128) NOT NULL COMMENT '模型名称',
    table_name VARCHAR(128) NOT NULL COMMENT '物理表名',
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT' COMMENT '模型状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_lowcode_table_tenant_code (tenant_id, code),
    UNIQUE KEY uk_lowcode_table_tenant_table (tenant_id, table_name),
    KEY idx_lowcode_table_tenant_status (tenant_id, status),
    KEY idx_lowcode_table_data_source (data_source_id),
    CONSTRAINT fk_lowcode_table_data_source FOREIGN KEY (data_source_id) REFERENCES lowcode_data_source (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='低代码表模型表';

CREATE TABLE IF NOT EXISTS lowcode_column_model (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    table_model_id BIGINT NOT NULL COMMENT '表模型主键',
    code VARCHAR(64) NOT NULL COMMENT '字段编码',
    name VARCHAR(128) NOT NULL COMMENT '字段名称',
    field_type VARCHAR(32) NOT NULL COMMENT '平台统一字段类型',
    length_value INT DEFAULT NULL COMMENT '字段长度或数值精度',
    scale_value INT DEFAULT NULL COMMENT '小数位数',
    required TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否必填',
    primary_key_flag TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否主键',
    auto_increment_flag TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否自增',
    list_visible TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否列表展示',
    form_visible TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否表单展示',
    queryable TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否查询条件',
    dict_code VARCHAR(64) DEFAULT NULL COMMENT '绑定的系统字典编码',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号',
    comment VARCHAR(255) DEFAULT NULL COMMENT '字段备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_lowcode_column_table_code (table_model_id, code),
    KEY idx_lowcode_column_table_sort (table_model_id, sort_order),
    CONSTRAINT fk_lowcode_column_table FOREIGN KEY (table_model_id) REFERENCES lowcode_table_model (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='低代码字段模型表';

CREATE TABLE IF NOT EXISTS lowcode_table_relation (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    main_table_id BIGINT NOT NULL COMMENT '主表模型主键',
    sub_table_id BIGINT NOT NULL COMMENT '子表模型主键',
    relation_type VARCHAR(32) NOT NULL COMMENT '关系类型',
    join_column VARCHAR(64) NOT NULL COMMENT '主表关联字段编码',
    ref_column VARCHAR(64) NOT NULL COMMENT '子表引用字段编码',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_lowcode_relation_tenant_tables (tenant_id, main_table_id, sub_table_id, relation_type),
    KEY idx_lowcode_relation_tenant_main (tenant_id, main_table_id),
    KEY idx_lowcode_relation_tenant_sub (tenant_id, sub_table_id),
    CONSTRAINT fk_lowcode_relation_main_table FOREIGN KEY (main_table_id) REFERENCES lowcode_table_model (id),
    CONSTRAINT fk_lowcode_relation_sub_table FOREIGN KEY (sub_table_id) REFERENCES lowcode_table_model (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='低代码表关系模型表';

CREATE TABLE IF NOT EXISTS lowcode_page_model (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    table_model_id BIGINT NOT NULL COMMENT '表模型主键',
    page_type VARCHAR(32) NOT NULL COMMENT '页面类型',
    route_path VARCHAR(255) NOT NULL COMMENT '前端路由路径',
    component_path VARCHAR(255) NOT NULL COMMENT '组件路径',
    layout_type VARCHAR(64) NOT NULL COMMENT '页面布局类型',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_lowcode_page_tenant_table_type (tenant_id, table_model_id, page_type),
    KEY idx_lowcode_page_tenant_table (tenant_id, table_model_id),
    CONSTRAINT fk_lowcode_page_table FOREIGN KEY (table_model_id) REFERENCES lowcode_table_model (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='低代码页面模型表';

CREATE TABLE IF NOT EXISTS lowcode_generation_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    table_model_code VARCHAR(64) NOT NULL COMMENT '表模型编码',
    target VARCHAR(32) NOT NULL COMMENT '生成目标端',
    module_name VARCHAR(128) NOT NULL COMMENT '业务模块名称',
    entity_name VARCHAR(128) NOT NULL COMMENT '业务实体名称',
    overwrite_strategy VARCHAR(32) NOT NULL COMMENT '生成文件覆盖策略',
    file_count INT NOT NULL DEFAULT 0 COMMENT '生成文件数量',
    file_manifest_json TEXT DEFAULT NULL COMMENT '生成文件清单 JSON',
    status VARCHAR(32) NOT NULL COMMENT '生成状态',
    error_message VARCHAR(1000) DEFAULT NULL COMMENT '失败原因',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_lowcode_gen_tenant_model (tenant_id, table_model_code),
    KEY idx_lowcode_gen_tenant_status (tenant_id, status),
    KEY idx_lowcode_gen_target (target)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='低代码代码生成记录表';

CREATE TABLE IF NOT EXISTS lc_generation_file (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    record_id BIGINT NOT NULL COMMENT '生成记录主键',
    template_code VARCHAR(128) NOT NULL COMMENT '模板编码',
    file_path VARCHAR(500) NOT NULL COMMENT '生成文件路径',
    file_type VARCHAR(32) NOT NULL COMMENT '生成文件类型',
    overwrite_mode VARCHAR(32) NOT NULL COMMENT '覆盖模式',
    content_hash VARCHAR(128) NOT NULL COMMENT '文件内容哈希',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_lc_gen_file_tenant_record (tenant_id, record_id),
    KEY idx_lc_gen_file_hash (content_hash),
    CONSTRAINT fk_lc_gen_file_record FOREIGN KEY (record_id) REFERENCES lowcode_generation_record (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='低代码生成文件明细表';
