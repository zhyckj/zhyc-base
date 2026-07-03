/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenant;

import com.zhyc.common.tenant.TenantIsolationMode;
import com.zhyc.system.tenant.domain.Tenant;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 租户基础模型测试。
 */
class TenantModelTest {

    /**
     * 验证租户模型在未显式指定隔离模式时使用租户字段隔离。
     */
    @Test
    void shouldDefaultIsolationModeToTenantColumn() {
        Tenant tenant = new Tenant();

        assertEquals(TenantIsolationMode.TENANT_COLUMN, tenant.getIsolationMode());
    }

    /**
     * 验证全参构造器传入空隔离模式时仍回退到默认租户字段隔离。
     */
    @Test
    void shouldDefaultIsolationModeWhenConstructorReceivesNull() {
        Tenant tenant = new Tenant(1L, "tenant_a", "租户A", null, "enabled", null, null);

        assertEquals(TenantIsolationMode.TENANT_COLUMN, tenant.getIsolationMode());
    }

    /**
     * 验证设置空隔离模式时仍回退到默认租户字段隔离。
     */
    @Test
    void shouldDefaultIsolationModeWhenSetterReceivesNull() {
        Tenant tenant = new Tenant();
        tenant.setIsolationMode(TenantIsolationMode.DATABASE);

        tenant.setIsolationMode(null);

        assertEquals(TenantIsolationMode.TENANT_COLUMN, tenant.getIsolationMode());
    }

    /**
     * 验证角色表默认数据权限范围，避免初始化脚本漂移。
     *
     * @throws IOException 读取 SQL 资源失败时抛出
     */
    @Test
    void shouldDeclareDefaultRoleDataScopeInSchema() throws IOException {
        String sql = normalizedSystemCoreSql();

        assertTrue(sql.contains("data_scope varchar(32) not null default 'self'"),
                "sys_role.data_scope should default to SELF");
    }

    /**
     * 验证租户主表字段与首期 SaaS 租户设计保持一致。
     *
     * @throws IOException 读取 SQL 资源失败时抛出
     */
    @Test
    void shouldDeclareTenantManagementColumnsInSchema() throws IOException {
        String sql = normalizedSystemCoreSql();

        assertTrue(sql.contains("tenant_name varchar(128) not null comment '租户名称'"),
                "sys_tenant should use tenant_name column");
        assertTrue(sql.contains("package_id bigint default null comment '当前租户套餐 id'"),
                "sys_tenant should include package_id");
        assertTrue(sql.contains("contact_name varchar(64) default null comment '租户联系人'"),
                "sys_tenant should include contact_name");
        assertTrue(sql.contains("expire_at datetime default null comment '到期时间'"),
                "sys_tenant should include expire_at");
    }

    /**
     * 验证用户角色关联表使用租户复合外键，避免跨租户错绑。
     *
     * @throws IOException 读取 SQL 资源失败时抛出
     */
    @Test
    void shouldDeclareTenantCompositeConstraintsForUserRoleBindings() throws IOException {
        String sql = normalizedSystemCoreSql();

        assertTrue(sql.contains("unique key uk_sys_user_tenant_id (tenant_id, id)"),
                "sys_user should expose (tenant_id, id) for composite foreign keys");
        assertTrue(sql.contains("unique key uk_sys_role_tenant_id (tenant_id, id)"),
                "sys_role should expose (tenant_id, id) for composite foreign keys");
        assertTrue(sql.contains("constraint fk_sys_user_role_user foreign key (tenant_id, user_id) references sys_user (tenant_id, id)"),
                "sys_user_role should constrain user_id within the same tenant");
        assertTrue(sql.contains("constraint fk_sys_user_role_role foreign key (tenant_id, role_id) references sys_role (tenant_id, id)"),
                "sys_user_role should constrain role_id within the same tenant");
    }

    /**
     * 验证角色菜单关联表使用租户复合外键，避免跨租户错绑。
     *
     * @throws IOException 读取 SQL 资源失败时抛出
     */
    @Test
    void shouldDeclareTenantCompositeConstraintsForRoleMenuBindings() throws IOException {
        String sql = normalizedSystemCoreSql();

        assertTrue(sql.contains("unique key uk_sys_menu_tenant_id (tenant_id, id)"),
                "sys_menu should expose (tenant_id, id) for composite foreign keys");
        assertTrue(sql.contains("constraint fk_sys_role_menu_role foreign key (tenant_id, role_id) references sys_role (tenant_id, id)"),
                "sys_role_menu should constrain role_id within the same tenant");
        assertTrue(sql.contains("constraint fk_sys_role_menu_menu foreign key (tenant_id, menu_id) references sys_menu (tenant_id, id)"),
                "sys_role_menu should constrain menu_id within the same tenant");
    }

    /**
     * 验证菜单表字段与首期菜单设计保持一致，避免后端模型和前端路由元数据漂移。
     *
     * @throws IOException 读取 SQL 资源失败时抛出
     */
    @Test
    void shouldDeclareMenuNameAndMenuTypeColumnsInSchema() throws IOException {
        String sql = normalizedSystemCoreSql();

        assertTrue(sql.contains("menu_name varchar(128) not null comment '菜单名称'"),
                "sys_menu should use menu_name column");
        assertTrue(sql.contains("menu_type varchar(32) not null comment '菜单类型'"),
                "sys_menu should use menu_type column");
    }

    /**
     * 读取并规范化系统核心 SQL，便于断言关键约束片段。
     *
     * @return 规范化后的 SQL 内容
     * @throws IOException 读取 SQL 资源失败时抛出
     */
    private String normalizedSystemCoreSql() throws IOException {
        try (InputStream inputStream = TenantModelTest.class.getClassLoader()
                .getResourceAsStream("db/V1__system_core.sql")) {
            assertNotNull(inputStream, "db/V1__system_core.sql should be available on the test classpath");
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8)
                    .toLowerCase()
                    .replaceAll("\\s+", " ");
        }
    }
}
