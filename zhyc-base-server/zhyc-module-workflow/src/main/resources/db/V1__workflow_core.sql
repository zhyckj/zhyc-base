-- Copyright (c) 2026 众汇云创科技（深圳）有限公司.
-- This file is part of ZHYC and is licensed for non-commercial use only.
-- Commercial use requires a separate written license from the copyright holder.
-- SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial

CREATE TABLE IF NOT EXISTS wf_category (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    category_code VARCHAR(64) NOT NULL COMMENT '流程分类编码',
    category_name VARCHAR(128) NOT NULL COMMENT '流程分类名称',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号',
    status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '分类状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0 未删除，1 已删除',
    version BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark VARCHAR(500) NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_wf_category_tenant_code (tenant_id, category_code),
    KEY idx_wf_category_tenant_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流分类表';

CREATE TABLE IF NOT EXISTS wf_process_model (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    model_code VARCHAR(128) NOT NULL COMMENT '流程模型编码',
    model_name VARCHAR(128) NOT NULL COMMENT '流程模型名称',
    category_id BIGINT NULL COMMENT '流程分类 ID',
    flowable_model_id VARCHAR(128) NOT NULL COMMENT 'Flowable 模型 ID',
    bpmn_xml MEDIUMTEXT NULL COMMENT 'BPMN XML 设计稿，用于保存在线流程编排草稿',
    status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '流程模型状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0 未删除，1 已删除',
    version BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark VARCHAR(500) NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_wf_model_tenant_code (tenant_id, model_code),
    KEY idx_wf_model_tenant_category (tenant_id, category_id),
    KEY idx_wf_model_tenant_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流流程模型表';

CREATE TABLE IF NOT EXISTS wf_form_binding (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    process_key VARCHAR(128) NOT NULL COMMENT '流程定义 key',
    business_module VARCHAR(64) NOT NULL COMMENT '业务模块编码',
    business_table VARCHAR(128) NOT NULL COMMENT '业务表名',
    form_route VARCHAR(255) NOT NULL COMMENT '后台表单路由',
    mobile_route VARCHAR(255) NULL COMMENT '移动端表单路由',
    status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '绑定状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0 未删除，1 已删除',
    version BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark VARCHAR(500) NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_wf_form_binding_tenant_process (tenant_id, process_key),
    KEY idx_wf_form_binding_tenant_module (tenant_id, business_module),
    KEY idx_wf_form_binding_tenant_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流表单绑定表';

CREATE TABLE IF NOT EXISTS wf_process_definition (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    process_key VARCHAR(128) NOT NULL COMMENT '流程定义 key',
    process_name VARCHAR(128) NOT NULL COMMENT '流程定义名称',
    version INT NOT NULL COMMENT '流程定义版本号',
    deployment_id VARCHAR(128) NOT NULL COMMENT 'Flowable 部署 ID',
    status VARCHAR(32) NOT NULL DEFAULT 'active' COMMENT '流程定义状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0 未删除，1 已删除',
    version_no BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark VARCHAR(500) NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_wf_definition_tenant_key_version (tenant_id, process_key, version),
    KEY idx_wf_definition_tenant_key (tenant_id, process_key),
    KEY idx_wf_definition_tenant_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流流程定义表';

CREATE TABLE IF NOT EXISTS wf_process_instance (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    process_instance_id VARCHAR(128) NOT NULL COMMENT '流程实例 ID',
    process_key VARCHAR(128) NOT NULL COMMENT '流程定义 key',
    business_key VARCHAR(128) NOT NULL COMMENT '业务对象唯一标识',
    starter_user_id BIGINT NULL COMMENT '流程发起人用户 ID',
    status VARCHAR(32) NOT NULL COMMENT '流程实例状态',
    started_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '流程启动时间',
    ended_at DATETIME NULL COMMENT '流程结束时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0 未删除，1 已删除',
    version BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark VARCHAR(500) NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_wf_pi_tenant_instance (tenant_id, process_instance_id),
    KEY idx_wf_pi_tenant_business (tenant_id, business_key),
    KEY idx_wf_pi_tenant_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流流程实例表';

CREATE TABLE IF NOT EXISTS wf_task (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    task_id VARCHAR(128) NOT NULL COMMENT '任务 ID',
    process_instance_id VARCHAR(128) NOT NULL COMMENT '流程实例 ID',
    task_name VARCHAR(128) NOT NULL COMMENT '任务名称',
    business_key VARCHAR(128) NOT NULL COMMENT '业务对象唯一标识',
    assignee_user_id BIGINT NOT NULL COMMENT '任务处理人用户 ID',
    status VARCHAR(32) NOT NULL COMMENT '任务状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '任务创建时间',
    completed_at DATETIME NULL COMMENT '任务完成时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0 未删除，1 已删除',
    version BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark VARCHAR(500) NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_wf_task_tenant_task (tenant_id, task_id),
    KEY idx_wf_task_tenant_assignee (tenant_id, assignee_user_id, status),
    KEY idx_wf_task_tenant_instance (tenant_id, process_instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流任务表';

CREATE TABLE IF NOT EXISTS wf_approval_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    task_id VARCHAR(128) NOT NULL COMMENT '任务 ID',
    process_instance_id VARCHAR(128) NULL COMMENT '流程实例 ID',
    operator_user_id BIGINT NOT NULL COMMENT '操作用户 ID',
    action VARCHAR(32) NOT NULL COMMENT '审批动作',
    approval_comment VARCHAR(1000) NULL COMMENT '审批意见',
    operated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_wf_record_tenant_task (tenant_id, task_id),
    KEY idx_wf_record_tenant_instance (tenant_id, process_instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流审批记录表';

CREATE TABLE IF NOT EXISTS wf_cc_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    process_instance_id VARCHAR(128) NOT NULL COMMENT '流程实例 ID',
    receiver_id BIGINT NOT NULL COMMENT '抄送接收人用户 ID',
    read_flag TINYINT NOT NULL DEFAULT 0 COMMENT '阅读标识，0 未读，1 已读',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0 未删除，1 已删除',
    version BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark VARCHAR(500) NULL COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_wf_cc_tenant_receiver (tenant_id, receiver_id, read_flag),
    KEY idx_wf_cc_tenant_instance (tenant_id, process_instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流抄送记录表';
