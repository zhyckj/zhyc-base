/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.menu;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统菜单按钮权限种子测试。
 */
class SysMenuSeedPermissionTest {

    /** 菜单种子脚本路径。 */
    private static final Path SEED_SQL = Path.of("src/main/resources/db/V2__system_seed.sql");

    /**
     * 验证系统管理核心页面的按钮权限种子完整，避免前端按钮因缺少授权节点而全部隐藏。
     *
     * @throws IOException 读取种子脚本失败时抛出
     */
    @Test
    void shouldContainCoreSystemButtonPermissions() throws IOException {
        String sql = Files.readString(SEED_SQL, StandardCharsets.UTF_8);

        assertContains(sql, "system:tenant:update");
        assertContains(sql, "system:tenant-package:update");
        assertContains(sql, "system:tenant-param:save");
        assertContains(sql, "system:admin:edit");
        assertContains(sql, "system:role:edit");
        assertContains(sql, "system:access-restriction:save");
        assertContains(sql, "system:access-restriction:evaluate");
        assertContains(sql, "system:password-policy:save");
        assertContains(sql, "system:password-policy:validate");
        assertContains(sql, "system:module:update");
        assertContains(sql, "system:code-rule:save");
        assertContains(sql, "system:code-rule:generate");
        assertContains(sql, "system:param:save");
    }

    /**
     * 断言种子脚本包含指定权限编码。
     *
     * @param sql 种子脚本文本
     * @param permission 权限编码
     */
    private void assertContains(String sql, String permission) {
        assertTrue(sql.contains("'" + permission + "'"), "缺少按钮权限种子: " + permission);
    }
}
