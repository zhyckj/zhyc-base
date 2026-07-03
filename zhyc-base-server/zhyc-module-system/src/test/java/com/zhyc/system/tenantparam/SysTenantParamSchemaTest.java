/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantparam;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 租户参数表结构测试。
 */
class SysTenantParamSchemaTest {

    /**
     * 验证租户参数表字段与首期 SaaS 租户参数设计保持一致。
     *
     * @throws IOException 读取建表脚本失败时抛出
     */
    @Test
    void shouldDeclareTenantParamTableInSchema() throws IOException {
        String sql = Files.readString(Path.of("src/main/resources/db/V1__system_core.sql"), StandardCharsets.UTF_8)
                .toLowerCase();

        assertTrue(sql.contains("create table if not exists sys_tenant_param"),
                "should create sys_tenant_param table");
        assertTrue(sql.contains("tenant_id varchar(64) not null comment '租户业务编码'"),
                "sys_tenant_param should include tenant_id");
        assertTrue(sql.contains("param_key varchar(128) not null comment '参数键'"),
                "sys_tenant_param should include param_key");
        assertTrue(sql.contains("param_value varchar(1000) default null comment '参数值'"),
                "sys_tenant_param should include param_value");
        assertTrue(sql.contains("value_type varchar(32) not null default 'string' comment '参数值类型'"),
                "sys_tenant_param should include value_type");
        assertTrue(sql.contains("visible tinyint not null default 1 comment '是否显示'"),
                "sys_tenant_param should include visible");
        assertTrue(sql.contains("unique key uk_sys_tenant_param_key (tenant_id, param_key)"),
                "sys_tenant_param should include unique tenant param key");
    }
}
