/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.userrole;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.permission.DataScope;
import com.zhyc.system.role.domain.SysRole;
import com.zhyc.system.role.repository.SysRoleRepository;
import com.zhyc.system.user.domain.SysUserRole;
import com.zhyc.system.user.repository.SysUserRoleRepository;
import com.zhyc.system.user.service.DefaultSysUserRoleService;
import com.zhyc.system.user.service.SysUserRoleBindCommand;
import com.zhyc.system.user.service.SysUserRoleResponse;
import com.zhyc.system.user.service.SysUserRoleService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 系统用户角色绑定业务服务测试。
 */
class SysUserRoleServiceTest {

    /**
     * 验证用户角色服务查询时修剪租户编码，并按角色主键稳定排序。
     */
    @Test
    void shouldListUserRolesWithNormalizedTenant() {
        RecordingUserRoleRepository repository = new RecordingUserRoleRepository();
        SysUserRoleService service = new DefaultSysUserRoleService(repository, new RecordingRoleRepository());

        List<SysUserRoleResponse> roles = service.listUserRoles(" tenant_a ", 1001L);

        assertEquals("tenant_a", repository.lastTenantId);
        assertEquals(1001L, repository.lastUserId);
        assertEquals(2, roles.size());
        assertEquals(1L, roles.get(0).getRoleId());
        assertEquals("管理员", roles.get(0).getRoleName());
        assertEquals(2L, roles.get(1).getRoleId());
    }

    /**
     * 验证用户角色绑定会过滤非法角色主键，并保留首次出现的角色顺序。
     */
    @Test
    void shouldReplaceUserRolesWithNormalizedRoleIds() {
        RecordingUserRoleRepository repository = new RecordingUserRoleRepository();
        SysUserRoleService service = new DefaultSysUserRoleService(repository, new RecordingRoleRepository());

        service.bindUserRoles(new SysUserRoleBindCommand(" tenant_a ", 1001L,
                List.of(3L, 2L, 2L, 0L, -1L)));

        assertEquals("tenant_a", repository.lastTenantId);
        assertEquals(1001L, repository.lastUserId);
        assertEquals(2, repository.lastBindings.size());
        assertEquals(3L, repository.lastBindings.get(0).getRoleId());
        assertEquals(2L, repository.lastBindings.get(1).getRoleId());
    }

    /**
     * 验证用户角色绑定拒绝当前租户不存在的角色，并且失败时不替换旧绑定。
     */
    @Test
    void shouldRejectUnknownTenantRoleBeforeReplacingBindings() {
        RecordingUserRoleRepository repository = new RecordingUserRoleRepository();
        SysUserRoleService service = new DefaultSysUserRoleService(repository, new RecordingRoleRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.bindUserRoles(new SysUserRoleBindCommand(" tenant_a ", 1001L, List.of(2L, 999L))));

        assertEquals("ZHYC_SYSTEM_ARGUMENT_INVALID", exception.getCode());
        assertEquals("角色主键不属于当前租户：999", exception.getMessage());
        assertEquals(0, repository.replaceCount);
    }

    /**
     * 测试用系统角色仓储。
     */
    private static class RecordingRoleRepository implements SysRoleRepository {

        /** 最近一次查询角色的租户业务编码。 */
        private String lastTenantId;

        @Override
        public List<SysRole> findByTenantId(String tenantId) {
            lastTenantId = tenantId;
            return List.of(
                    new SysRole(2L, tenantId, "AUDITOR", "审计员", DataScope.CURRENT_DEPT,
                            "enabled", LocalDateTime.now(), LocalDateTime.now()),
                    new SysRole(3L, tenantId, "USER", "普通用户", DataScope.SELF,
                            "enabled", LocalDateTime.now(), LocalDateTime.now())
            );
        }

        @Override
        public void insert(SysRole role) {
            throw new AssertionError("用户角色绑定测试不应新增角色");
        }

        @Override
        public void update(SysRole role) {
            throw new AssertionError("用户角色绑定测试不应更新角色");
        }

        @Override
        public void updateStatus(String tenantId, Long roleId, String status) {
            throw new AssertionError("用户角色绑定测试不应更新角色状态");
        }

        @Override
        public void deleteByTenantIdAndId(String tenantId, Long roleId) {
            throw new AssertionError("用户角色绑定测试不应删除角色");
        }

        @Override
        public void replaceRoleMenus(String tenantId, Long roleId, List<Long> menuIds) {
            throw new UnsupportedOperationException("用户角色绑定测试不应替换角色菜单");
        }

        @Override
        public List<Long> findRoleMenuIds(String tenantId, Long roleId) {
            throw new UnsupportedOperationException("用户角色绑定测试不应查询角色菜单");
        }
    }

    /**
     * 测试用系统用户角色仓储。
     */
    private static class RecordingUserRoleRepository implements SysUserRoleRepository {

        /** 最近一次操作的租户业务编码。 */
        private String lastTenantId;
        /** 最近一次操作的用户主键。 */
        private Long lastUserId;
        /** 最近一次写入的角色绑定列表。 */
        private List<SysUserRole> lastBindings = new ArrayList<>();
        /** 替换用户角色绑定的调用次数。 */
        private int replaceCount;

        @Override
        public List<SysUserRole> findByTenantIdAndUserId(String tenantId, Long userId) {
            lastTenantId = tenantId;
            lastUserId = userId;
            return List.of(
                    new SysUserRole(2L, tenantId, userId, 2L, "AUDITOR", "审计员",
                            "DEPT", "enabled", LocalDateTime.now()),
                    new SysUserRole(1L, tenantId, userId, 1L, "ADMIN", "管理员",
                            "ALL", "enabled", LocalDateTime.now())
            );
        }

        @Override
        public void replaceUserRoles(String tenantId, Long userId, List<SysUserRole> bindings) {
            lastTenantId = tenantId;
            lastUserId = userId;
            lastBindings = bindings;
            replaceCount++;
        }
    }
}
