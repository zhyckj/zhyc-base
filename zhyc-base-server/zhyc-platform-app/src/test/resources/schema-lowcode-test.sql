-- Copyright (c) 2026 众汇云创科技（深圳）有限公司.
-- This file is part of ZHYC and is licensed for non-commercial use only.
-- Commercial use requires a separate written license from the copyright holder.
-- SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial

CREATE TABLE IF NOT EXISTS lowcode_data_source (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(128) NOT NULL,
    dialect VARCHAR(32) NOT NULL,
    jdbc_url VARCHAR(512) NOT NULL,
    username VARCHAR(128) NOT NULL,
    password_secret_ref VARCHAR(255),
    enabled TINYINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (tenant_id, code)
);

CREATE TABLE IF NOT EXISTS lowcode_table_model (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    data_source_id BIGINT,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(128) NOT NULL,
    table_name VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (tenant_id, code),
    UNIQUE (tenant_id, table_name)
);

CREATE TABLE IF NOT EXISTS lowcode_column_model (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_model_id BIGINT NOT NULL,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(128) NOT NULL,
    field_type VARCHAR(32) NOT NULL,
    length_value INT,
    scale_value INT,
    required TINYINT NOT NULL DEFAULT 0,
    primary_key_flag TINYINT NOT NULL DEFAULT 0,
    auto_increment_flag TINYINT NOT NULL DEFAULT 0,
    list_visible TINYINT NOT NULL DEFAULT 0,
    form_visible TINYINT NOT NULL DEFAULT 0,
    queryable TINYINT NOT NULL DEFAULT 0,
    dict_code VARCHAR(64),
    sort_order INT NOT NULL DEFAULT 0,
    comment VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (table_model_id, code)
);

CREATE TABLE IF NOT EXISTS lowcode_generation_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    table_model_code VARCHAR(64) NOT NULL,
    target VARCHAR(32) NOT NULL,
    module_name VARCHAR(128) NOT NULL,
    entity_name VARCHAR(128) NOT NULL,
    overwrite_strategy VARCHAR(32) NOT NULL,
    file_count INT NOT NULL DEFAULT 0,
    file_manifest_json CLOB,
    status VARCHAR(32) NOT NULL,
    error_message VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS lc_generation_file (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    record_id BIGINT NOT NULL,
    template_code VARCHAR(128) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_type VARCHAR(32) NOT NULL,
    overwrite_mode VARCHAR(32) NOT NULL,
    content_hash VARCHAR(128) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_tenant (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    tenant_name VARCHAR(128) NOT NULL,
    package_id BIGINT,
    isolation_mode VARCHAR(32) NOT NULL DEFAULT 'TENANT_COLUMN',
    status VARCHAR(32) NOT NULL DEFAULT 'enabled',
    contact_name VARCHAR(64),
    contact_phone VARCHAR(32),
    expire_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (tenant_id)
);

CREATE TABLE IF NOT EXISTS sys_tenant_package (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    package_code VARCHAR(64) NOT NULL,
    package_name VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'enabled',
    max_user_count INT NOT NULL DEFAULT 0,
    max_storage_mb INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (package_code)
);

CREATE TABLE IF NOT EXISTS sys_tenant_package_module (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    package_id BIGINT NOT NULL,
    module_code VARCHAR(64) NOT NULL,
    menu_code VARCHAR(64),
    permission VARCHAR(128),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (package_id, module_code, menu_code, permission)
);

CREATE TABLE IF NOT EXISTS sys_tenant_param (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    param_key VARCHAR(128) NOT NULL,
    param_value VARCHAR(1000),
    value_type VARCHAR(32) NOT NULL DEFAULT 'string',
    visible TINYINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (tenant_id, param_key)
);

CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    parent_id BIGINT,
    menu_code VARCHAR(64) NOT NULL,
    menu_name VARCHAR(128) NOT NULL,
    menu_type VARCHAR(32) NOT NULL,
    path VARCHAR(255),
    component VARCHAR(255),
    permission VARCHAR(128),
    sort_order INT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'enabled',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (tenant_id, menu_code)
);

CREATE TABLE IF NOT EXISTS sys_org (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    parent_id BIGINT,
    ancestors VARCHAR(500) NOT NULL DEFAULT '0',
    org_code VARCHAR(64) NOT NULL,
    org_name VARCHAR(128) NOT NULL,
    leader_user_id BIGINT,
    sort_order INT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'enabled',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (tenant_id, org_code)
);

CREATE TABLE IF NOT EXISTS sys_post (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    org_id BIGINT,
    post_code VARCHAR(64) NOT NULL,
    post_name VARCHAR(128) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'enabled',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (tenant_id, post_code)
);

CREATE TABLE IF NOT EXISTS sys_user_post (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    primary_flag TINYINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (tenant_id, user_id, post_id)
);

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    username VARCHAR(64) NOT NULL,
    nickname VARCHAR(128),
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'enabled',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (tenant_id, username)
);

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    role_code VARCHAR(64) NOT NULL,
    name VARCHAR(128) NOT NULL,
    data_scope VARCHAR(32) NOT NULL DEFAULT 'SELF',
    status VARCHAR(32) NOT NULL DEFAULT 'enabled',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (tenant_id, role_code)
);

CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (tenant_id, user_id, role_id)
);

CREATE TABLE IF NOT EXISTS sys_admin_scope (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    scope_type VARCHAR(32) NOT NULL,
    scope_ref_code VARCHAR(128) NOT NULL,
    scope_name VARCHAR(128),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (tenant_id, user_id, scope_type, scope_ref_code)
);

CREATE TABLE IF NOT EXISTS sys_role_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (tenant_id, role_id, menu_id)
);

CREATE TABLE IF NOT EXISTS sys_role_data_scope (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    role_id BIGINT NOT NULL,
    org_id BIGINT NOT NULL,
    scope_type VARCHAR(32) NOT NULL DEFAULT 'org',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (tenant_id, role_id, org_id, scope_type)
);

CREATE TABLE IF NOT EXISTS sys_login_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    user_id BIGINT,
    username VARCHAR(64),
    login_type VARCHAR(32) NOT NULL,
    result VARCHAR(32) NOT NULL,
    client_ip VARCHAR(64),
    user_agent VARCHAR(512),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_exception_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    trace_id VARCHAR(128),
    user_id BIGINT,
    username VARCHAR(64),
    request_uri VARCHAR(255) NOT NULL,
    request_method VARCHAR(16) NOT NULL,
    exception_name VARCHAR(255) NOT NULL,
    message CLOB,
    stack_trace CLOB,
    client_ip VARCHAR(64),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_permission_audit (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    operator_id BIGINT,
    target_type VARCHAR(64) NOT NULL,
    target_id VARCHAR(128) NOT NULL,
    before_value CLOB,
    after_value CLOB,
    change_type VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    user_id BIGINT,
    username VARCHAR(64),
    action VARCHAR(128) NOT NULL,
    target_type VARCHAR(64),
    target_id VARCHAR(128),
    result VARCHAR(32) NOT NULL,
    client_ip VARCHAR(64),
    detail CLOB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_param (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    param_key VARCHAR(128) NOT NULL,
    param_value CLOB,
    value_type VARCHAR(32) NOT NULL DEFAULT 'string',
    system_flag TINYINT NOT NULL DEFAULT 0,
    editable TINYINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (tenant_id, param_key)
);

CREATE TABLE IF NOT EXISTS sys_access_restriction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    restriction_type VARCHAR(32) NOT NULL,
    rule_value VARCHAR(255) NOT NULL,
    effect VARCHAR(32) NOT NULL,
    start_at TIMESTAMP,
    end_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (tenant_id, restriction_type, rule_value)
);

CREATE TABLE IF NOT EXISTS sys_password_policy (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    policy_code VARCHAR(64) NOT NULL,
    policy_name VARCHAR(128) NOT NULL,
    min_length INT NOT NULL DEFAULT 8,
    require_uppercase TINYINT NOT NULL DEFAULT 0,
    require_lowercase TINYINT NOT NULL DEFAULT 1,
    require_digit TINYINT NOT NULL DEFAULT 1,
    require_special TINYINT NOT NULL DEFAULT 0,
    expire_days INT NOT NULL DEFAULT 90,
    history_count INT NOT NULL DEFAULT 3,
    max_retry_count INT NOT NULL DEFAULT 5,
    lock_minutes INT NOT NULL DEFAULT 30,
    enabled TINYINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (tenant_id, policy_code)
);

CREATE TABLE IF NOT EXISTS sys_code_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    rule_code VARCHAR(64) NOT NULL,
    rule_name VARCHAR(128) NOT NULL,
    prefix VARCHAR(32),
    date_pattern VARCHAR(32),
    sequence_length INT NOT NULL DEFAULT 5,
    current_value INT NOT NULL DEFAULT 0,
    enabled TINYINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (tenant_id, rule_code)
);

CREATE TABLE IF NOT EXISTS openapi_app (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    app_code VARCHAR(64) NOT NULL,
    app_name VARCHAR(128) NOT NULL,
    owner_user_id BIGINT NOT NULL,
    auth_mode VARCHAR(32) NOT NULL,
    ip_whitelist CLOB,
    status VARCHAR(32) NOT NULL DEFAULT 'enabled',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (tenant_id, app_code)
);

CREATE TABLE IF NOT EXISTS openapi_api_key (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    app_code VARCHAR(64) NOT NULL,
    access_key VARCHAR(128) NOT NULL,
    secret_cipher VARCHAR(512) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'enabled',
    expire_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (access_key)
);

CREATE TABLE IF NOT EXISTS openapi_api_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    app_code VARCHAR(64) NOT NULL,
    api_code VARCHAR(128) NOT NULL,
    api_name VARCHAR(128) NOT NULL,
    http_method VARCHAR(16) NOT NULL,
    path_pattern VARCHAR(256) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'enabled',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (tenant_id, app_code, api_code)
);

CREATE TABLE IF NOT EXISTS openapi_call_audit (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    app_code VARCHAR(64) NOT NULL,
    access_key VARCHAR(128) NOT NULL,
    api_code VARCHAR(128) NOT NULL,
    http_method VARCHAR(16) NOT NULL,
    request_path VARCHAR(512) NOT NULL,
    response_status INT NOT NULL,
    duration_ms BIGINT NOT NULL,
    success TINYINT NOT NULL,
    error_code VARCHAR(64),
    client_ip VARCHAR(64) NOT NULL,
    request_id VARCHAR(128) NOT NULL,
    called_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS openapi_rate_limit_counter (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    app_code VARCHAR(64) NOT NULL,
    api_code VARCHAR(128) NOT NULL,
    window_seconds BIGINT NOT NULL,
    window_index BIGINT NOT NULL,
    request_count INT NOT NULL DEFAULT 0,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (tenant_id, app_code, api_code, window_seconds, window_index)
);

CREATE TABLE IF NOT EXISTS openapi_replay_nonce (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    app_key VARCHAR(128) NOT NULL,
    nonce_value VARCHAR(128) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (app_key, nonce_value)
);

CREATE TABLE IF NOT EXISTS sys_dict_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    dict_code VARCHAR(64) NOT NULL,
    dict_name VARCHAR(128) NOT NULL,
    system_flag TINYINT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'enabled',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (tenant_id, dict_code)
);

CREATE TABLE IF NOT EXISTS sys_dict_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    dict_code VARCHAR(64) NOT NULL,
    item_label VARCHAR(128) NOT NULL,
    item_value VARCHAR(128) NOT NULL,
    item_color VARCHAR(32),
    sort_order INT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'enabled',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (tenant_id, dict_code, item_value)
);

CREATE TABLE IF NOT EXISTS sys_module (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    module_code VARCHAR(64) NOT NULL,
    module_name VARCHAR(128) NOT NULL,
    version VARCHAR(32) NOT NULL,
    module_type VARCHAR(32) NOT NULL,
    enabled TINYINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (module_code)
);

CREATE TABLE IF NOT EXISTS sys_module_dependency (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    module_code VARCHAR(64) NOT NULL,
    depends_on_code VARCHAR(64) NOT NULL,
    required_version VARCHAR(32),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (module_code, depends_on_code)
);

CREATE TABLE IF NOT EXISTS sys_module_resource (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    module_code VARCHAR(64) NOT NULL,
    resource_type VARCHAR(32) NOT NULL,
    resource_code VARCHAR(128) NOT NULL,
    resource_path VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (module_code, resource_type, resource_code)
);

CREATE TABLE IF NOT EXISTS pur_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    request_no VARCHAR(64) NOT NULL,
    request_title VARCHAR(128) NOT NULL,
    applicant_id BIGINT NOT NULL,
    org_id BIGINT NOT NULL,
    total_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    request_reason VARCHAR(1000),
    process_status VARCHAR(32) NOT NULL,
    process_instance_id VARCHAR(128),
    submitted_at TIMESTAMP,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    remark VARCHAR(500),
    UNIQUE (tenant_id, request_no)
);

CREATE TABLE IF NOT EXISTS wf_process_instance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    process_instance_id VARCHAR(128) NOT NULL,
    process_key VARCHAR(128) NOT NULL,
    business_key VARCHAR(128) NOT NULL,
    starter_user_id BIGINT,
    status VARCHAR(32) NOT NULL,
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    remark VARCHAR(500),
    UNIQUE (tenant_id, process_instance_id)
);

CREATE TABLE IF NOT EXISTS wf_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    task_id VARCHAR(128) NOT NULL,
    process_instance_id VARCHAR(128) NOT NULL,
    task_name VARCHAR(128) NOT NULL,
    business_key VARCHAR(128) NOT NULL,
    assignee_user_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    remark VARCHAR(500),
    UNIQUE (tenant_id, task_id)
);

CREATE TABLE IF NOT EXISTS wf_approval_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    task_id VARCHAR(128) NOT NULL,
    process_instance_id VARCHAR(128),
    operator_user_id BIGINT NOT NULL,
    action VARCHAR(32) NOT NULL,
    approval_comment VARCHAR(1000),
    operated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS job_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    job_code VARCHAR(64) NOT NULL,
    job_name VARCHAR(128) NOT NULL,
    cron_expression VARCHAR(128) NOT NULL,
    handler_name VARCHAR(128) NOT NULL,
    job_description VARCHAR(500),
    job_status VARCHAR(32) NOT NULL DEFAULT 'disabled',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE (tenant_id, job_code)
);

CREATE TABLE IF NOT EXISTS job_task_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    job_id BIGINT NOT NULL,
    trigger_type VARCHAR(32) NOT NULL,
    start_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_at TIMESTAMP,
    result VARCHAR(32) NOT NULL,
    error_message VARCHAR(1000),
    operator_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cms_channel (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    parent_id BIGINT,
    channel_code VARCHAR(64) NOT NULL,
    channel_name VARCHAR(128) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    channel_status VARCHAR(32) NOT NULL DEFAULT 'enabled',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE (tenant_id, channel_code)
);

CREATE TABLE IF NOT EXISTS cms_content (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    channel_code VARCHAR(64) NOT NULL,
    title VARCHAR(200) NOT NULL,
    summary VARCHAR(500),
    body_content CLOB,
    content_status VARCHAR(32) NOT NULL DEFAULT 'draft',
    author_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS visual_dataset (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    dataset_code VARCHAR(64) NOT NULL,
    dataset_name VARCHAR(128) NOT NULL,
    datasource_code VARCHAR(64) NOT NULL,
    sql_text CLOB NOT NULL,
    dataset_status VARCHAR(32) NOT NULL DEFAULT 'enabled',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE (tenant_id, dataset_code)
);

CREATE TABLE IF NOT EXISTS visual_report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    report_code VARCHAR(64) NOT NULL,
    report_name VARCHAR(128) NOT NULL,
    dataset_code VARCHAR(64) NOT NULL,
    chart_type VARCHAR(32) NOT NULL DEFAULT 'table',
    config_json CLOB NOT NULL,
    report_status VARCHAR(32) NOT NULL DEFAULT 'enabled',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE (tenant_id, report_code)
);

CREATE TABLE IF NOT EXISTS visual_screen (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    screen_code VARCHAR(64) NOT NULL,
    screen_name VARCHAR(128) NOT NULL,
    layout_json CLOB NOT NULL,
    screen_status VARCHAR(32) NOT NULL DEFAULT 'draft',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE (tenant_id, screen_code)
);

CREATE TABLE IF NOT EXISTS i18n_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    locale VARCHAR(32) NOT NULL,
    message_key VARCHAR(190) NOT NULL,
    message_value VARCHAR(1000) NOT NULL,
    message_status VARCHAR(32) NOT NULL DEFAULT 'enabled',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE (tenant_id, locale, message_key)
);

CREATE TABLE IF NOT EXISTS search_index_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    index_code VARCHAR(128) NOT NULL,
    index_name VARCHAR(128) NOT NULL,
    source_table VARCHAR(128) NOT NULL,
    search_fields VARCHAR(512) NOT NULL,
    filter_fields VARCHAR(512),
    index_status VARCHAR(32) NOT NULL DEFAULT 'enabled',
    remark VARCHAR(512),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE (tenant_id, index_code)
);

CREATE TABLE IF NOT EXISTS search_rebuild_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    index_code VARCHAR(128) NOT NULL,
    task_status VARCHAR(32) NOT NULL DEFAULT 'pending',
    trigger_type VARCHAR(32) NOT NULL DEFAULT 'manual',
    started_at TIMESTAMP,
    finished_at TIMESTAMP,
    error_message VARCHAR(1024),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS search_query_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    index_code VARCHAR(128) NOT NULL,
    keyword VARCHAR(256) NOT NULL,
    result_count INT NOT NULL DEFAULT 0,
    cost_ms BIGINT NOT NULL DEFAULT 0,
    query_status VARCHAR(32) NOT NULL DEFAULT 'success',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS file_storage_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    storage_code VARCHAR(64) NOT NULL,
    storage_name VARCHAR(128) NOT NULL,
    storage_type VARCHAR(32) NOT NULL,
    endpoint VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'enabled',
    default_flag TINYINT NOT NULL DEFAULT 0,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    remark VARCHAR(500),
    UNIQUE (tenant_id, storage_code)
);

CREATE TABLE IF NOT EXISTS file_object (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    file_code VARCHAR(64) NOT NULL,
    storage_code VARCHAR(64) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(128) NOT NULL,
    file_size BIGINT NOT NULL DEFAULT 0,
    object_key VARCHAR(500) NOT NULL,
    file_status VARCHAR(32) NOT NULL DEFAULT 'stored',
    uploader_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE (tenant_id, file_code)
);

CREATE TABLE IF NOT EXISTS file_preview_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    file_code VARCHAR(64) NOT NULL,
    preview_type VARCHAR(32) NOT NULL,
    preview_url VARCHAR(512) NOT NULL,
    result VARCHAR(32) NOT NULL,
    cost_ms BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
