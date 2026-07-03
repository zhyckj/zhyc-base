/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.role;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.menu.domain.SysMenu;
import com.zhyc.system.menu.repository.SysMenuRepository;
import com.zhyc.system.permission.DataScope;
import com.zhyc.system.role.domain.SysRole;
import com.zhyc.system.role.repository.SysRoleRepository;
import com.zhyc.system.role.service.DefaultSysRoleService;
import com.zhyc.system.role.service.RoleMenuBindCommand;
import com.zhyc.system.role.service.SysRoleResponse;
import com.zhyc.system.role.service.SysRoleService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 系统角色业务服务测试。
 */
class SysRoleServiceTest {

    /**
     * 验证角色服务按租户查询角色列表，并转换为后台管理端响应对象。
     */
    @Test
    void shouldListTenantRoles() {
        RecordingRoleRepository repository = new RecordingRoleRepository();
        SysRoleService service = new DefaultSysRoleService(repository, new RecordingMenuRepository());

        List<SysRoleResponse> roles = service.listRoles(" tenant_a ");

        assertEquals("tenant_a", repository.lastTenantId);
        assertEquals(2, roles.size());
        assertEquals("admin", roles.get(0).getRoleCode());
        assertEquals("管理员", roles.get(0).getName());
        assertEquals(DataScope.ALL, roles.get(0).getDataScope());
    }

    /**
     * 验证角色菜单绑定会按租户和角色重置菜单 ID，并过滤空值和重复值。
     */
    @Test
    void shouldBindRoleMenusByTenantAndRole() {
        RecordingRoleRepository repository = new RecordingRoleRepository();
        SysRoleService service = new DefaultSysRoleService(repository, new RecordingMenuRepository());

        service.bindRoleMenus(new RoleMenuBindCommand(" tenant_a ", 10L,
                List.of(100L, 101L, 100L, 0L, -1L)));

        assertEquals("tenant_a", repository.lastTenantId);
        assertEquals(10L, repository.lastRoleId);
        assertEquals(List.of(100L, 101L), repository.lastMenuIds);
    }

    /**
     * 验证角色授权回显按租户和角色查询已绑定菜单 ID。
     */
    @Test
    void shouldListRoleMenuIdsByTenantAndRole() {
        RecordingRoleRepository repository = new RecordingRoleRepository();
        SysRoleService service = new DefaultSysRoleService(repository, new RecordingMenuRepository());

        List<Long> menuIds = service.listRoleMenuIds(" tenant_a ", 10L);

        assertEquals("tenant_a", repository.lastTenantId);
        assertEquals(10L, repository.lastRoleId);
        assertEquals(List.of(100L, 101L), menuIds);
    }

    /**
     * 验证角色菜单绑定拒绝当前租户不存在的角色，并且失败时不替换旧授权。
     */
    @Test
    void shouldRejectUnknownTenantRoleBeforeReplacingRoleMenus() {
        RecordingRoleRepository repository = new RecordingRoleRepository();
        SysRoleService service = new DefaultSysRoleService(repository, new RecordingMenuRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.bindRoleMenus(new RoleMenuBindCommand(" tenant_a ", 999L, List.of(100L))));

        assertEquals("ZHYC_SYSTEM_ARGUMENT_INVALID", exception.getCode());
        assertEquals("角色主键不属于当前租户：999", exception.getMessage());
        assertEquals(0, repository.replaceCount);
    }

    /**
     * 验证角色菜单绑定拒绝当前租户不存在的菜单，并且失败时不替换旧授权。
     */
    @Test
    void shouldRejectUnknownTenantMenuBeforeReplacingRoleMenus() {
        RecordingRoleRepository repository = new RecordingRoleRepository();
        SysRoleService service = new DefaultSysRoleService(repository, new RecordingMenuRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.bindRoleMenus(new RoleMenuBindCommand(" tenant_a ", 10L, List.of(100L, 999L))));

        assertEquals("ZHYC_SYSTEM_ARGUMENT_INVALID", exception.getCode());
        assertEquals("菜单主键不属于当前租户：999", exception.getMessage());
        assertEquals(0, repository.replaceCount);
    }

    /**
     * 测试用菜单仓储。
     */
    private static class RecordingMenuRepository implements SysMenuRepository {

        /** 最近一次查询菜单的租户业务编码。 */
        private String lastTenantId;

        @Override
        public List<SysMenu> findEnabledByTenantId(String tenantId) {
            lastTenantId = tenantId;
            return List.of(
                    new SysMenu(100L, tenantId, null, "system", "系统管理", "directory",
                            "/system", null, null, 1, "enabled", LocalDateTime.now(), LocalDateTime.now()),
                    new SysMenu(101L, tenantId, 100L, "system:user", "用户管理", "menu",
                            "/system/user", "system/user/index", "system:user:query", 2,
                            "enabled", LocalDateTime.now(), LocalDateTime.now())
            );
        }

        @Override
        public List<SysMenu> findByTenantId(String tenantId) {
            return findEnabledByTenantId(tenantId);
        }

        @Override
        public void insert(SysMenu menu) {
            throw new UnsupportedOperationException("测试不需要新增菜单");
        }

        @Override
        public void update(SysMenu menu) {
            throw new UnsupportedOperationException("测试不需要更新菜单");
        }

        @Override
        public void updateStatus(String tenantId, Long menuId, String status) {
            throw new UnsupportedOperationException("测试不需要更新菜单状态");
        }

        @Override
        public void deleteByTenantIdAndId(String tenantId, Long menuId) {
            throw new UnsupportedOperationException("测试不需要删除菜单");
        }
    }

    /**
     * 测试用角色仓储。
     */
    private static class RecordingRoleRepository implements SysRoleRepository {

        /** 最近一次操作的租户业务编码。 */
        private String lastTenantId;
        /** 最近一次操作的角色主键。 */
        private Long lastRoleId;
        /** 最近一次绑定的菜单主键列表。 */
        private List<Long> lastMenuIds;
        /** 替换角色菜单绑定的调用次数。 */
        private int replaceCount;

        @Override
        public List<SysRole> findByTenantId(String tenantId) {
            lastTenantId = tenantId;
            return List.of(
                    new SysRole(10L, tenantId, "admin", "管理员", DataScope.ALL,
                            "enabled", LocalDateTime.now(), LocalDateTime.now()),
                    new SysRole(11L, tenantId, "user", "普通用户", DataScope.SELF,
                            "enabled", LocalDateTime.now(), LocalDateTime.now()));
        }

        @Override
        public void insert(SysRole role) {
            throw new UnsupportedOperationException("测试不需要新增角色");
        }

        @Override
        public void update(SysRole role) {
            throw new UnsupportedOperationException("测试不需要更新角色");
        }

        @Override
        public void updateStatus(String tenantId, Long roleId, String status) {
            throw new UnsupportedOperationException("测试不需要更新角色状态");
        }

        @Override
        public void deleteByTenantIdAndId(String tenantId, Long roleId) {
            throw new UnsupportedOperationException("测试不需要删除角色");
        }

        @Override
        public List<Long> findRoleMenuIds(String tenantId, Long roleId) {
            lastTenantId = tenantId;
            lastRoleId = roleId;
            return List.of(100L, 101L);
        }

        @Override
        public void replaceRoleMenus(String tenantId, Long roleId, List<Long> menuIds) {
            lastTenantId = tenantId;
            lastRoleId = roleId;
            lastMenuIds = menuIds;
            replaceCount++;
        }
    }
}
