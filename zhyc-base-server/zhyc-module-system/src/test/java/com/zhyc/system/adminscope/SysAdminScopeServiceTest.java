/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.adminscope;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.adminscope.domain.SysAdminScope;
import com.zhyc.system.adminscope.repository.SysAdminScopeRepository;
import com.zhyc.system.adminscope.service.AdminScopeBindCommand;
import com.zhyc.system.adminscope.service.AdminScopeBindItem;
import com.zhyc.system.adminscope.service.DefaultSysAdminScopeService;
import com.zhyc.system.adminscope.service.SysAdminScopeResponse;
import com.zhyc.system.adminscope.service.SysAdminScopeService;
import com.zhyc.system.module.domain.SysModule;
import com.zhyc.system.module.domain.SysModuleDependency;
import com.zhyc.system.module.domain.SysModuleResource;
import com.zhyc.system.module.repository.SysModuleRepository;
import com.zhyc.system.org.domain.SysOrg;
import com.zhyc.system.org.repository.SysOrgRepository;
import com.zhyc.system.user.domain.SysUser;
import com.zhyc.system.user.repository.SysUserRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 系统管理员管理范围业务服务测试。
 */
class SysAdminScopeServiceTest {

    /**
     * 验证管理员范围服务查询时修剪租户编码，并按范围类型和范围引用排序。
     */
    @Test
    void shouldListAdminScopesWithNormalizedTenant() {
        RecordingAdminScopeRepository repository = new RecordingAdminScopeRepository();
        SysAdminScopeService service = newService(repository);

        List<SysAdminScopeResponse> scopes = service.listAdminScopes(" tenant_a ", 1001L);

        assertEquals("tenant_a", repository.lastTenantId);
        assertEquals(1001L, repository.lastUserId);
        assertEquals(2, scopes.size());
        assertEquals("module", scopes.get(0).getScopeType());
        assertEquals("system", scopes.get(0).getScopeRefCode());
        assertEquals("org", scopes.get(1).getScopeType());
        assertEquals("10", scopes.get(1).getScopeRefCode());
    }

    /**
     * 验证管理员范围绑定会去重、裁剪编码并过滤无效范围。
     */
    @Test
    void shouldBindAdminScopesWithNormalizedScopes() {
        RecordingAdminScopeRepository repository = new RecordingAdminScopeRepository();
        SysAdminScopeService service = newService(repository);

        service.bindAdminScopes(new AdminScopeBindCommand(" tenant_a ", 1001L, List.of(
                new AdminScopeBindItem(" org ", " 10 "),
                new AdminScopeBindItem("org", "10"),
                new AdminScopeBindItem("tenant", " tenant_a "),
                new AdminScopeBindItem("", "invalid"),
                new AdminScopeBindItem("module", "")
        )));

        assertEquals("tenant_a", repository.lastTenantId);
        assertEquals(1001L, repository.lastUserId);
        assertEquals(2, repository.lastScopes.size());
        assertEquals("org", repository.lastScopes.get(0).getScopeType());
        assertEquals("10", repository.lastScopes.get(0).getScopeRefCode());
        assertEquals("tenant", repository.lastScopes.get(1).getScopeType());
        assertEquals("tenant_a", repository.lastScopes.get(1).getScopeRefCode());
    }

    /**
     * 验证管理员范围绑定会拒绝不受支持的范围类型。
     */
    @Test
    void shouldRejectUnsupportedScopeType() {
        RecordingAdminScopeRepository repository = new RecordingAdminScopeRepository();
        SysAdminScopeService service = newService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.bindAdminScopes(new AdminScopeBindCommand("tenant_a", 1001L,
                        List.of(new AdminScopeBindItem("region", "east")))));

        assertEquals("ZHYC_SYS_ADMIN_SCOPE_TYPE_UNSUPPORTED", exception.getCode());
        assertEquals("管理员管理范围类型不支持: region", exception.getMessage());
    }

    /**
     * 验证管理员范围绑定拒绝当前租户不存在的管理员用户，并且失败时不替换旧范围。
     */
    @Test
    void shouldRejectUnknownTenantAdminBeforeReplacingScopes() {
        RecordingAdminScopeRepository repository = new RecordingAdminScopeRepository();
        SysAdminScopeService service = newService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.bindAdminScopes(new AdminScopeBindCommand("tenant_a", 999L,
                        List.of(new AdminScopeBindItem("tenant", "tenant_a")))));

        assertEquals("ZHYC_SYS_ADMIN_SCOPE_REF_INVALID", exception.getCode());
        assertEquals("管理员用户主键不属于当前租户：999", exception.getMessage());
        assertEquals(0, repository.replaceCount);
    }

    /**
     * 验证管理员范围绑定拒绝当前租户不存在的组织，并且失败时不替换旧范围。
     */
    @Test
    void shouldRejectUnknownTenantOrgBeforeReplacingScopes() {
        RecordingAdminScopeRepository repository = new RecordingAdminScopeRepository();
        SysAdminScopeService service = newService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.bindAdminScopes(new AdminScopeBindCommand("tenant_a", 1001L,
                        List.of(new AdminScopeBindItem("org", "999")))));

        assertEquals("ZHYC_SYS_ADMIN_SCOPE_REF_INVALID", exception.getCode());
        assertEquals("组织范围不属于当前租户：999", exception.getMessage());
        assertEquals(0, repository.replaceCount);
    }

    /**
     * 验证管理员范围绑定拒绝不存在的模块编码，并且失败时不替换旧范围。
     */
    @Test
    void shouldRejectUnknownModuleBeforeReplacingScopes() {
        RecordingAdminScopeRepository repository = new RecordingAdminScopeRepository();
        SysAdminScopeService service = newService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.bindAdminScopes(new AdminScopeBindCommand("tenant_a", 1001L,
                        List.of(new AdminScopeBindItem("module", "ghost")))));

        assertEquals("ZHYC_SYS_ADMIN_SCOPE_REF_INVALID", exception.getCode());
        assertEquals("模块范围不存在或未启用：ghost", exception.getMessage());
        assertEquals(0, repository.replaceCount);
    }

    /**
     * 验证管理员范围绑定拒绝跨租户的租户范围编码，并且失败时不替换旧范围。
     */
    @Test
    void shouldRejectOtherTenantScopeBeforeReplacingScopes() {
        RecordingAdminScopeRepository repository = new RecordingAdminScopeRepository();
        SysAdminScopeService service = newService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.bindAdminScopes(new AdminScopeBindCommand("tenant_a", 1001L,
                        List.of(new AdminScopeBindItem("tenant", "tenant_b")))));

        assertEquals("ZHYC_SYS_ADMIN_SCOPE_REF_INVALID", exception.getCode());
        assertEquals("租户范围必须等于当前租户：tenant_b", exception.getMessage());
        assertEquals(0, repository.replaceCount);
    }

    /**
     * 创建测试用管理员范围业务服务。
     *
     * @param repository 管理员范围仓储
     * @return 管理员范围业务服务
     */
    private SysAdminScopeService newService(RecordingAdminScopeRepository repository) {
        return new DefaultSysAdminScopeService(repository, new RecordingUserRepository(),
                new RecordingOrgRepository(), new RecordingModuleRepository());
    }

    /**
     * 测试用系统用户仓储。
     */
    private static class RecordingUserRepository implements SysUserRepository {

        @Override
        public List<SysUser> findByTenantId(String tenantId) {
            return List.of(
                    new SysUser(1001L, tenantId, "admin", "平台管理员", "{noop}pwd",
                            "enabled", LocalDateTime.now(), LocalDateTime.now())
            );
        }

        @Override
        public Optional<SysUser> findByTenantIdAndUsername(String tenantId, String username) {
            return Optional.empty();
        }

        @Override
        public void insert(String tenantId, String username, String nickname, String passwordHash, String status) {
            throw new UnsupportedOperationException("管理员范围测试不应新增用户");
        }

        @Override
        public void update(String tenantId, Long userId, String username, String nickname, String status) {
            throw new UnsupportedOperationException("管理员范围测试不应更新用户");
        }

        @Override
        public void updateStatus(String tenantId, Long userId, String status) {
            throw new UnsupportedOperationException("管理员范围测试不应更新用户状态");
        }

        @Override
        public void updatePasswordHash(String tenantId, String username, String passwordHash) {
            throw new UnsupportedOperationException("管理员范围测试不应更新密码");
        }

        @Override
        public void updatePasswordHashById(String tenantId, Long userId, String passwordHash) {
            throw new UnsupportedOperationException("管理员范围测试不应重置密码");
        }

        @Override
        public void deleteByTenantIdAndId(String tenantId, Long userId) {
            throw new UnsupportedOperationException("管理员范围测试不应删除用户");
        }
    }

    /**
     * 测试用系统组织仓储。
     */
    private static class RecordingOrgRepository implements SysOrgRepository {

        @Override
        public List<SysOrg> findByTenantId(String tenantId) {
            return List.of(
                    new SysOrg(10L, tenantId, null, "0", "HQ", "总部", null,
                            1, "enabled", LocalDateTime.now(), LocalDateTime.now())
            );
        }

        @Override
        public void insert(SysOrg org) {
            throw new UnsupportedOperationException("管理员范围测试不应新增组织");
        }

        @Override
        public void update(SysOrg org) {
            throw new UnsupportedOperationException("管理员范围测试不应更新组织");
        }

        @Override
        public void updateStatus(String tenantId, Long orgId, String status) {
            throw new UnsupportedOperationException("管理员范围测试不应更新组织状态");
        }

        @Override
        public void deleteByTenantIdAndId(String tenantId, Long orgId) {
            throw new UnsupportedOperationException("管理员范围测试不应删除组织");
        }
    }

    /**
     * 测试用系统模块仓储。
     */
    private static class RecordingModuleRepository implements SysModuleRepository {

        @Override
        public List<SysModule> findAll() {
            return List.of(
                    new SysModule(1L, "system", "系统管理", "1.0.0", "core",
                            true, LocalDateTime.now(), LocalDateTime.now()),
                    new SysModule(2L, "disabled", "停用模块", "1.0.0", "plugin",
                            false, LocalDateTime.now(), LocalDateTime.now())
            );
        }

        @Override
        public List<SysModuleDependency> findDependenciesByModuleCode(String moduleCode) {
            return List.of();
        }

        @Override
        public List<SysModuleResource> findResourcesByModuleCode(String moduleCode) {
            return List.of();
        }

        @Override
        public void saveModule(SysModule module) {
            throw new UnsupportedOperationException("管理员范围测试不应保存模块");
        }

        @Override
        public void replaceDependencies(String moduleCode, List<SysModuleDependency> dependencies) {
            throw new UnsupportedOperationException("管理员范围测试不应替换模块依赖");
        }

        @Override
        public void replaceResources(String moduleCode, List<SysModuleResource> resources) {
            throw new UnsupportedOperationException("管理员范围测试不应替换模块资源");
        }

        @Override
        public void updateEnabled(String moduleCode, boolean enabled) {
            throw new UnsupportedOperationException("管理员范围测试不应更新模块状态");
        }
    }

    /**
     * 测试用管理员范围仓储。
     */
    private static class RecordingAdminScopeRepository implements SysAdminScopeRepository {

        /** 最近一次操作的租户业务编码。 */
        private String lastTenantId;
        /** 最近一次操作的管理员用户主键。 */
        private Long lastUserId;
        /** 最近一次写入的管理员范围列表。 */
        private List<SysAdminScope> lastScopes = new ArrayList<>();
        /** 替换管理员范围的调用次数。 */
        private int replaceCount;

        @Override
        public List<SysAdminScope> findByTenantIdAndUserId(String tenantId, Long userId) {
            lastTenantId = tenantId;
            lastUserId = userId;
            return List.of(
                    new SysAdminScope(1L, tenantId, userId, "org", "10", "华东大区",
                            LocalDateTime.now()),
                    new SysAdminScope(2L, tenantId, userId, "module", "system", "系统管理",
                            LocalDateTime.now())
            );
        }

        @Override
        public void replaceAdminScopes(String tenantId, Long userId, List<SysAdminScope> scopes) {
            lastTenantId = tenantId;
            lastUserId = userId;
            lastScopes = scopes;
            replaceCount++;
        }
    }
}
