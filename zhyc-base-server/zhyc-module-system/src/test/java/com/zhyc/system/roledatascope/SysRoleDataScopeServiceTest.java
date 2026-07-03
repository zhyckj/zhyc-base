/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.roledatascope;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.org.domain.SysOrg;
import com.zhyc.system.org.repository.SysOrgRepository;
import com.zhyc.system.permission.DataScope;
import com.zhyc.system.role.domain.SysRole;
import com.zhyc.system.role.domain.SysRoleDataScope;
import com.zhyc.system.role.repository.SysRoleRepository;
import com.zhyc.system.role.repository.SysRoleDataScopeRepository;
import com.zhyc.system.role.service.DefaultSysRoleDataScopeService;
import com.zhyc.system.role.service.RoleDataScopeBindCommand;
import com.zhyc.system.role.service.SysRoleDataScopeResponse;
import com.zhyc.system.role.service.SysRoleDataScopeService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 系统角色自定义数据权限业务服务测试。
 */
class SysRoleDataScopeServiceTest {

    /**
     * 验证角色数据权限服务按租户和角色查询组织范围，并按组织主键排序。
     */
    @Test
    void shouldListCustomOrgScopesByTenantAndRole() {
        RecordingRoleDataScopeRepository repository = new RecordingRoleDataScopeRepository();
        SysRoleDataScopeService service = new DefaultSysRoleDataScopeService(repository,
                new RecordingRoleRepository(), new RecordingOrgRepository());

        List<SysRoleDataScopeResponse> scopes = service.listRoleDataScopes(" tenant_a ", 10L);

        assertEquals("tenant_a", repository.lastTenantId);
        assertEquals(10L, repository.lastRoleId);
        assertEquals(2, scopes.size());
        assertEquals(1L, scopes.get(0).getOrgId());
        assertEquals("总部", scopes.get(0).getOrgName());
        assertEquals(3L, scopes.get(1).getOrgId());
    }

    /**
     * 验证角色数据权限绑定会过滤非法组织主键并去重。
     */
    @Test
    void shouldBindCustomOrgScopesWithNormalizedOrgIds() {
        RecordingRoleDataScopeRepository repository = new RecordingRoleDataScopeRepository();
        SysRoleDataScopeService service = new DefaultSysRoleDataScopeService(repository,
                new RecordingRoleRepository(), new RecordingOrgRepository());

        service.bindRoleDataScopes(new RoleDataScopeBindCommand(" tenant_a ", 10L,
                List.of(3L, 1L, 3L, 0L, -1L)));

        assertEquals("tenant_a", repository.lastTenantId);
        assertEquals(10L, repository.lastRoleId);
        assertEquals(List.of(3L, 1L), repository.lastOrgIds);
    }

    /**
     * 验证角色数据权限绑定拒绝当前租户不存在的角色，并且失败时不替换旧范围。
     */
    @Test
    void shouldRejectUnknownTenantRoleBeforeReplacingRoleDataScopes() {
        RecordingRoleDataScopeRepository repository = new RecordingRoleDataScopeRepository();
        SysRoleDataScopeService service = new DefaultSysRoleDataScopeService(repository,
                new RecordingRoleRepository(), new RecordingOrgRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.bindRoleDataScopes(new RoleDataScopeBindCommand(" tenant_a ", 999L, List.of(1L))));

        assertEquals("ZHYC_SYSTEM_ARGUMENT_INVALID", exception.getCode());
        assertEquals("角色主键不属于当前租户：999", exception.getMessage());
        assertEquals(0, repository.replaceCount);
    }

    /**
     * 验证角色数据权限绑定拒绝当前租户不存在的组织，并且失败时不替换旧范围。
     */
    @Test
    void shouldRejectUnknownTenantOrgBeforeReplacingRoleDataScopes() {
        RecordingRoleDataScopeRepository repository = new RecordingRoleDataScopeRepository();
        SysRoleDataScopeService service = new DefaultSysRoleDataScopeService(repository,
                new RecordingRoleRepository(), new RecordingOrgRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.bindRoleDataScopes(new RoleDataScopeBindCommand(" tenant_a ", 10L,
                        List.of(1L, 999L))));

        assertEquals("ZHYC_SYSTEM_ARGUMENT_INVALID", exception.getCode());
        assertEquals("组织主键不属于当前租户：999", exception.getMessage());
        assertEquals(0, repository.replaceCount);
    }

    /**
     * 测试用系统角色仓储。
     */
    private static class RecordingRoleRepository implements SysRoleRepository {

        @Override
        public List<SysRole> findByTenantId(String tenantId) {
            return List.of(
                    new SysRole(10L, tenantId, "admin", "管理员", DataScope.CUSTOM,
                            "enabled", LocalDateTime.now(), LocalDateTime.now()),
                    new SysRole(11L, tenantId, "auditor", "审计员", DataScope.SELF,
                            "enabled", LocalDateTime.now(), LocalDateTime.now())
            );
        }

        @Override
        public void insert(SysRole role) {
            throw new UnsupportedOperationException("角色数据权限测试不应新增角色");
        }

        @Override
        public void update(SysRole role) {
            throw new UnsupportedOperationException("角色数据权限测试不应更新角色");
        }

        @Override
        public void updateStatus(String tenantId, Long roleId, String status) {
            throw new UnsupportedOperationException("角色数据权限测试不应更新角色状态");
        }

        @Override
        public void deleteByTenantIdAndId(String tenantId, Long roleId) {
            throw new UnsupportedOperationException("角色数据权限测试不应删除角色");
        }

        @Override
        public void replaceRoleMenus(String tenantId, Long roleId, List<Long> menuIds) {
            throw new UnsupportedOperationException("角色数据权限测试不应替换角色菜单");
        }

        @Override
        public List<Long> findRoleMenuIds(String tenantId, Long roleId) {
            throw new UnsupportedOperationException("角色数据权限测试不应查询角色菜单");
        }
    }

    /**
     * 测试用系统组织仓储。
     */
    private static class RecordingOrgRepository implements SysOrgRepository {

        @Override
        public List<SysOrg> findByTenantId(String tenantId) {
            return List.of(
                    new SysOrg(1L, tenantId, null, "0", "HQ", "总部", null,
                            1, "enabled", LocalDateTime.now(), LocalDateTime.now()),
                    new SysOrg(3L, tenantId, 1L, "0,1", "SALE", "销售部", null,
                            2, "enabled", LocalDateTime.now(), LocalDateTime.now())
            );
        }

        @Override
        public void insert(SysOrg org) {
            throw new UnsupportedOperationException("角色数据权限测试不应新增组织");
        }

        @Override
        public void update(SysOrg org) {
            throw new UnsupportedOperationException("角色数据权限测试不应更新组织");
        }

        @Override
        public void updateStatus(String tenantId, Long orgId, String status) {
            throw new UnsupportedOperationException("角色数据权限测试不应更新组织状态");
        }

        @Override
        public void deleteByTenantIdAndId(String tenantId, Long orgId) {
            throw new UnsupportedOperationException("角色数据权限测试不应删除组织");
        }
    }

    /**
     * 测试用角色数据权限仓储。
     */
    private static class RecordingRoleDataScopeRepository implements SysRoleDataScopeRepository {

        /** 最近一次操作的租户业务编码。 */
        private String lastTenantId;
        /** 最近一次操作的角色主键。 */
        private Long lastRoleId;
        /** 最近一次绑定的组织主键列表。 */
        private List<Long> lastOrgIds;
        /** 替换角色数据权限绑定的调用次数。 */
        private int replaceCount;

        @Override
        public List<SysRoleDataScope> findByTenantIdAndRoleId(String tenantId, Long roleId) {
            lastTenantId = tenantId;
            lastRoleId = roleId;
            return List.of(
                    new SysRoleDataScope(2L, tenantId, roleId, 3L, "销售部", "org",
                            LocalDateTime.now()),
                    new SysRoleDataScope(1L, tenantId, roleId, 1L, "总部", "org",
                            LocalDateTime.now()));
        }

        @Override
        public void replaceRoleDataScopes(String tenantId, Long roleId, List<Long> orgIds) {
            lastTenantId = tenantId;
            lastRoleId = roleId;
            lastOrgIds = orgIds;
            replaceCount++;
        }
    }
}
