-- Copyright (c) 2026 众汇云创科技（深圳）有限公司.
-- This file is part of ZHYC and is licensed for non-commercial use only.
-- Commercial use requires a separate written license from the copyright holder.
-- SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial

CREATE TABLE IF NOT EXISTS pur_request (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    request_no VARCHAR(64) NOT NULL COMMENT '采购申请单号',
    request_title VARCHAR(128) NOT NULL COMMENT '采购申请标题',
    applicant_id BIGINT NOT NULL COMMENT '申请人用户 ID',
    org_id BIGINT NOT NULL COMMENT '申请部门 ID',
    total_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '采购申请总金额',
    request_reason VARCHAR(1000) NULL COMMENT '采购申请原因',
    process_status VARCHAR(32) NOT NULL COMMENT '流程状态',
    process_instance_id VARCHAR(128) NULL COMMENT '流程实例 ID',
    submitted_at DATETIME NULL COMMENT '提交审批时间',
    created_by BIGINT NULL COMMENT '创建人用户 ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT NULL COMMENT '更新人用户 ID',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0 未删除，1 已删除',
    version BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark VARCHAR(500) NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_pur_request_tenant_no (tenant_id, request_no),
    KEY idx_pur_request_tenant_applicant (tenant_id, applicant_id),
    KEY idx_pur_request_tenant_status (tenant_id, process_status),
    KEY idx_pur_request_tenant_process (tenant_id, process_instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购申请主表';

CREATE TABLE IF NOT EXISTS pur_order (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    order_no VARCHAR(64) NOT NULL COMMENT '采购订单号',
    request_no VARCHAR(64) NOT NULL COMMENT '采购申请单号',
    supplier_id BIGINT NOT NULL COMMENT '供应商 ID',
    buyer_id BIGINT NOT NULL COMMENT '采购员用户 ID',
    total_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '采购订单总金额',
    order_status VARCHAR(32) NOT NULL COMMENT '订单状态',
    created_by BIGINT NULL COMMENT '创建人用户 ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT NULL COMMENT '更新人用户 ID',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0 未删除，1 已删除',
    version BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark VARCHAR(500) NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_pur_order_tenant_no (tenant_id, order_no),
    KEY idx_pur_order_tenant_request (tenant_id, request_no),
    KEY idx_pur_order_tenant_status (tenant_id, order_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单主表';

CREATE TABLE IF NOT EXISTS pur_order_item (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    order_no VARCHAR(64) NOT NULL COMMENT '采购订单号',
    item_name VARCHAR(128) NOT NULL COMMENT '物品名称',
    quantity DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '采购数量',
    unit_price DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '采购单价',
    amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '明细金额',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0 未删除，1 已删除',
    PRIMARY KEY (id),
    KEY idx_pur_order_item_tenant_order (tenant_id, order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单明细表';

-- 采购申请状态开放 API 目录注册，用于开放 API 网关运行态路由发现。
INSERT INTO openapi_catalog (
    api_code,
    api_name,
    group_code,
    http_method,
    path_pattern,
    status
) VALUES (
    'purchase-request-status',
    '采购申请状态查询',
    'purchase',
    'GET',
    '/openapi/v1/purchase/requests/{requestNo}/status',
    'enabled'
) ON DUPLICATE KEY UPDATE
    api_name = VALUES(api_name),
    group_code = VALUES(group_code),
    http_method = VALUES(http_method),
    path_pattern = VALUES(path_pattern),
    status = VALUES(status),
    updated_at = CURRENT_TIMESTAMP;

-- 采购申请状态开放 API 版本注册，用于开放 API 网关定位后端服务入口。
INSERT INTO openapi_version (
    api_code,
    version,
    backend_route,
    request_schema,
    response_schema,
    status
) VALUES (
    'purchase-request-status',
    'v1',
    'http://zhyc-platform-app/openapi/v1/purchase/requests/{requestNo}/status',
    JSON_OBJECT('method', 'GET', 'tenantHeader', 'X-ZHYC-Tenant-Id', 'pathVariable', 'requestNo'),
    JSON_OBJECT('apiCode', 'purchase-request-status', 'fields', JSON_ARRAY('requestNo', 'requestTitle', 'processStatus', 'totalAmount', 'submittedAt')),
    'published'
) ON DUPLICATE KEY UPDATE
    backend_route = VALUES(backend_route),
    request_schema = VALUES(request_schema),
    response_schema = VALUES(response_schema),
    status = VALUES(status),
    updated_at = CURRENT_TIMESTAMP;

-- 采购订单详情开放 API 目录注册，用于开放 API 网关运行态路由发现。
INSERT INTO openapi_catalog (
    api_code,
    api_name,
    group_code,
    http_method,
    path_pattern,
    status
) VALUES (
    'purchase-order-detail',
    '采购订单详情查询',
    'purchase',
    'GET',
    '/openapi/v1/purchase/orders/{orderNo}',
    'enabled'
) ON DUPLICATE KEY UPDATE
    api_name = VALUES(api_name),
    group_code = VALUES(group_code),
    http_method = VALUES(http_method),
    path_pattern = VALUES(path_pattern),
    status = VALUES(status),
    updated_at = CURRENT_TIMESTAMP;

-- 采购订单详情开放 API 版本注册，用于开放 API 网关定位后端服务入口。
INSERT INTO openapi_version (
    api_code,
    version,
    backend_route,
    request_schema,
    response_schema,
    status
) VALUES (
    'purchase-order-detail',
    'v1',
    'http://zhyc-platform-app/openapi/v1/purchase/orders/{orderNo}',
    JSON_OBJECT('method', 'GET', 'tenantHeader', 'X-ZHYC-Tenant-Id', 'pathVariable', 'orderNo'),
    JSON_OBJECT('apiCode', 'purchase-order-detail', 'fields', JSON_ARRAY('orderNo', 'requestNo', 'supplierId', 'buyerId', 'totalAmount', 'orderStatus', 'items')),
    'published'
) ON DUPLICATE KEY UPDATE
    backend_route = VALUES(backend_route),
    request_schema = VALUES(request_schema),
    response_schema = VALUES(response_schema),
    status = VALUES(status),
    updated_at = CURRENT_TIMESTAMP;
