/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.menu;

import com.zhyc.system.menu.domain.SysMenu;
import com.zhyc.system.menu.repository.SysMenuRepository;
import com.zhyc.system.menu.service.DefaultSysMenuService;
import com.zhyc.system.menu.service.SysMenuService;
import com.zhyc.system.menu.service.SysMenuTreeNode;
import org.springframework.cache.annotation.Cacheable;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统菜单业务服务测试。
 */
class SysMenuServiceTest {

    /**
     * 验证菜单服务只读取指定租户菜单，并按父子关系和排序号组装菜单树。
     */
    @Test
    void shouldBuildTenantMenuTreeByParentAndSortOrder() {
        RecordingSysMenuRepository repository = new RecordingSysMenuRepository();
        SysMenuService service = new DefaultSysMenuService(repository);

        List<SysMenuTreeNode> tree = service.listMenuTree("tenant_a", false);

        assertEquals("tenant_a", repository.lastTenantId);
        assertEquals(2, tree.size());
        assertEquals("system", tree.get(0).getMenuCode());
        assertEquals("enabled", tree.get(0).getStatus());
        assertEquals("lowcode", tree.get(1).getMenuCode());
        assertEquals("enabled", tree.get(1).getStatus());
        assertEquals(1, tree.get(0).getChildren().size());
        assertEquals("system.user", tree.get(0).getChildren().get(0).getMenuCode());
        assertEquals("enabled", tree.get(0).getChildren().get(0).getStatus());
    }

    /**
     * 验证菜单树缓存 key 带版本前缀，避免新增字段后命中旧序列化缓存导致状态为空。
     *
     * @throws Exception 反射读取方法失败时抛出
     */
    @Test
    void shouldVersionMenuTreeCacheKey() throws Exception {
        Method method = DefaultSysMenuService.class.getMethod("listMenuTree", String.class, boolean.class);
        Cacheable cacheable = method.getAnnotation(Cacheable.class);

        assertNotNull(cacheable, "菜单树查询必须声明缓存");
        assertTrue(cacheable.key().contains("'v2:'"), "菜单树缓存 key 必须包含版本前缀");
    }

    /**
     * 测试用菜单仓储，记录服务传入的租户编码。
     */
    private static class RecordingSysMenuRepository implements SysMenuRepository {

        /** 最近一次查询的租户业务编码。 */
        private String lastTenantId;

        @Override
        public List<SysMenu> findEnabledByTenantId(String tenantId) {
            lastTenantId = tenantId;
            return Arrays.asList(
                    new SysMenu(2L, tenantId, null, "lowcode", "低代码中心", "directory",
                            "/lowcode", null, "lowcode:view", 20, "enabled", null, null),
                    new SysMenu(3L, tenantId, 1L, "system.user", "用户管理", "menu",
                            "/system/user", "system/user/index", "system:user:view", 10, "enabled", null, null),
                    new SysMenu(1L, tenantId, null, "system", "系统管理", "directory",
                            "/system", null, "system:view", 10, "enabled", null, null));
        }

        @Override
        public List<SysMenu> findByTenantId(String tenantId) {
            return findEnabledByTenantId(tenantId);
        }

        @Override
        public void insert(SysMenu menu) {
            throw new AssertionError("菜单树查询测试不应新增菜单");
        }

        @Override
        public void update(SysMenu menu) {
            throw new AssertionError("菜单树查询测试不应更新菜单");
        }

        @Override
        public void updateStatus(String tenantId, Long menuId, String status) {
            throw new AssertionError("菜单树查询测试不应更新菜单状态");
        }

        @Override
        public void deleteByTenantIdAndId(String tenantId, Long menuId) {
            throw new AssertionError("菜单树查询测试不应删除菜单");
        }
    }
}
