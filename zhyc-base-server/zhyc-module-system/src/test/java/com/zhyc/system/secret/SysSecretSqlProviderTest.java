/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.secret;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统密钥 SQL Provider 测试。
 */
class SysSecretSqlProviderTest {

    /**
     * 验证密钥列表查询 SQL 始终带租户过滤和显式列。
     *
     * @throws Exception 反射调用失败时抛出
     */
    @Test
    void shouldGenerateTenantScopedListSqlWithExplicitColumns() throws Exception {
        Object provider = newProvider();

        String sql = invokeSql(provider, "selectByTenantId");

        assertTrue(sql.contains("FROM sys_secret"));
        assertTrue(sql.contains("WHERE tenant_id = #{tenantId}"));
        assertTrue(sql.contains("secret_code AS secretCode"));
        assertTrue(sql.contains("secret_mask AS secretMask"));
        assertTrue(sql.contains("last_rotated_at AS lastRotatedAt"));
        assertFalse(sql.contains("SELECT *"));
    }

    /**
     * 验证按密钥编码查询时仍保留租户边界。
     *
     * @throws Exception 反射调用失败时抛出
     */
    @Test
    void shouldGenerateTenantScopedSecretCodeSql() throws Exception {
        Object provider = newProvider();

        String sql = invokeSql(provider, "selectByTenantIdAndSecretCode");

        assertTrue(sql.contains("WHERE tenant_id = #{tenantId}"));
        assertTrue(sql.contains("AND secret_code = #{secretCode}"));
        assertFalse(sql.contains("SELECT *"));
    }

    /**
     * 验证选项查询返回当前租户已启用且可被业务配置引用的密钥。
     *
     * @throws Exception 反射调用失败时抛出
     */
    @Test
    void shouldGenerateEnabledSelectableSecretOptionsSql() throws Exception {
        Object provider = newProvider();

        String sql = invokeSql(provider, "selectSelectableSecrets", Map.of("secretKind", ""));

        assertTrue(sql.contains("WHERE tenant_id = #{tenantId}"));
        assertTrue(sql.contains("AND status = #{status}"));
        assertTrue(sql.contains("AND secret_kind IN ('db_password', 'generic')"));
        assertTrue(sql.contains("ORDER BY secret_code"));
        assertFalse(sql.contains("SELECT *"));
    }

    /**
     * 验证指定 API 密钥类型时，选项查询可以返回 AI 供应商可引用的密钥。
     *
     * @throws Exception 反射调用失败时抛出
     */
    @Test
    void shouldGenerateApiSecretOptionsSqlWhenSecretKindSpecified() throws Exception {
        Object provider = newProvider();

        String sql = invokeSql(provider, "selectSelectableSecrets", Map.of("secretKind", "api_secret"));

        assertTrue(sql.contains("WHERE tenant_id = #{tenantId}"));
        assertTrue(sql.contains("AND status = #{status}"));
        assertTrue(sql.contains("AND secret_kind = #{secretKind}"));
        assertFalse(sql.contains("secret_kind IN ('db_password', 'generic')"));
        assertFalse(sql.contains("SELECT *"));
    }

    /**
     * 验证更新 SQL 仍通过租户和主键定位记录。
     *
     * @throws Exception 反射调用失败时抛出
     */
    @Test
    void shouldGenerateUpdateSqlWithTenantAndId() throws Exception {
        Object provider = newProvider();

        String sql = invokeSql(provider, "update");

        assertTrue(sql.contains("WHERE tenant_id = #{tenantId}"));
        assertTrue(sql.contains("AND id = #{id}"));
        assertTrue(sql.contains("secret_cipher = #{secretCipher}"));
        assertTrue(sql.contains("last_rotated_at = #{lastRotatedAt}"));
    }

    /**
     * 创建 SQL Provider 实例。
     *
     * @return SQL Provider
     * @throws Exception 创建失败时抛出
     */
    private static Object newProvider() throws Exception {
        Class<?> providerClass = Class.forName("com.zhyc.system.secret.mapper.SysSecretSqlProvider");
        return providerClass.getConstructor().newInstance();
    }

    /**
     * 调用指定 SQL 生成方法。
     *
     * @param provider SQL Provider
     * @param methodName 方法名
     * @return SQL 文本
     * @throws Exception 调用失败时抛出
     */
    private static String invokeSql(Object provider, String methodName) throws Exception {
        Method method = Arrays.stream(provider.getClass().getMethods())
                .filter(candidate -> candidate.getName().equals(methodName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("缺少 SQL 生成方法: " + methodName));
        return (String) method.invoke(provider);
    }

    /**
     * 调用带参数的 SQL 生成方法。
     *
     * @param provider SQL Provider
     * @param methodName 方法名
     * @param params 查询参数
     * @return SQL 文本
     * @throws Exception 调用失败时抛出
     */
    private static String invokeSql(Object provider, String methodName, Map<String, Object> params) throws Exception {
        Method method = Arrays.stream(provider.getClass().getMethods())
                .filter(candidate -> candidate.getName().equals(methodName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("缺少 SQL 生成方法: " + methodName));
        return (String) method.invoke(provider, params);
    }
}
