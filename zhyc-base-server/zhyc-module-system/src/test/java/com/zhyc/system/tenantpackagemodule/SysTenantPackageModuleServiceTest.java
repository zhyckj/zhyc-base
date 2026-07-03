/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantpackagemodule;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.module.domain.SysModule;
import com.zhyc.system.module.domain.SysModuleDependency;
import com.zhyc.system.module.domain.SysModuleResource;
import com.zhyc.system.module.repository.SysModuleRepository;
import com.zhyc.system.tenantpackagemodule.domain.SysTenantPackageModule;
import com.zhyc.system.tenantpackagemodule.repository.SysTenantPackageModuleRepository;
import com.zhyc.system.tenantpackagemodule.service.DefaultSysTenantPackageModuleService;
import com.zhyc.system.tenantpackagemodule.service.TenantPackageModuleBindCommand;
import com.zhyc.system.tenantpackagemodule.service.TenantPackageModuleGrantCommand;
import com.zhyc.system.tenantpackagemodule.service.TenantPackageModuleResponse;
import com.zhyc.system.tenantpackagemodule.service.SysTenantPackageModuleService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 租户套餐模块授权业务服务测试。
 */
class SysTenantPackageModuleServiceTest {

    /**
     * 验证服务会按套餐查询授权资源并转换模块、菜单和权限字段。
     */
    @Test
    void shouldListGrantsByPackageId() {
        RecordingTenantPackageModuleRepository repository = new RecordingTenantPackageModuleRepository();
        SysTenantPackageModuleService service = new DefaultSysTenantPackageModuleService(repository,
                new RecordingSysModuleRepository());

        List<TenantPackageModuleResponse> grants = service.listGrants(10L);

        assertEquals(10L, repository.lastPackageId);
        assertEquals(1, grants.size());
        assertEquals("system", grants.get(0).getModuleCode());
        assertEquals("system:user", grants.get(0).getMenuCode());
        assertEquals("system:user:query", grants.get(0).getPermission());
    }

    /**
     * 验证套餐授权绑定会先删除旧授权，再写入裁剪后的新授权。
     */
    @Test
    void shouldBindPackageGrants() {
        RecordingTenantPackageModuleRepository repository = new RecordingTenantPackageModuleRepository();
        SysTenantPackageModuleService service = new DefaultSysTenantPackageModuleService(repository,
                new RecordingSysModuleRepository());

        service.bindGrants(new TenantPackageModuleBindCommand(10L, List.of(
                new TenantPackageModuleGrantCommand(" system ", " system:user ", " system:user:query "))));

        assertEquals(10L, repository.deletedPackageId);
        assertEquals(1, repository.savedGrants.size());
        assertEquals("system", repository.savedGrants.get(0).getModuleCode());
        assertEquals("system:user", repository.savedGrants.get(0).getMenuCode());
        assertEquals("system:user:query", repository.savedGrants.get(0).getPermission());
    }

    /**
     * 验证套餐授权绑定会拒绝模块元数据中不存在的菜单编码，避免套餐授权写入脏资源。
     */
    @Test
    void shouldRejectGrantWhenMenuIsNotDeclaredByModule() {
        SysTenantPackageModuleService service = new DefaultSysTenantPackageModuleService(
                new RecordingTenantPackageModuleRepository(), new RecordingSysModuleRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.bindGrants(new TenantPackageModuleBindCommand(10L, List.of(
                        new TenantPackageModuleGrantCommand("system", "system:missing", "system:user:query")))));

        assertEquals("套餐授权菜单不属于模块: system:missing", exception.getMessage());
    }

    /**
     * 验证套餐授权绑定会拒绝模块元数据中不存在的权限编码，避免越过模块权限注册表授权。
     */
    @Test
    void shouldRejectGrantWhenPermissionIsNotDeclaredByModule() {
        SysTenantPackageModuleService service = new DefaultSysTenantPackageModuleService(
                new RecordingTenantPackageModuleRepository(), new RecordingSysModuleRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.bindGrants(new TenantPackageModuleBindCommand(10L, List.of(
                        new TenantPackageModuleGrantCommand("system", "system:user", "system:user:delete")))));

        assertEquals("套餐授权权限不属于模块: system:user:delete", exception.getMessage());
    }

    /**
     * 验证套餐授权绑定会在删除旧授权前拒绝重复资源，避免数据库唯一键异常导致授权回滚不清晰。
     */
    @Test
    void shouldRejectDuplicateGrantBeforeDeletingOldGrants() {
        RecordingTenantPackageModuleRepository repository = new RecordingTenantPackageModuleRepository();
        SysTenantPackageModuleService service = new DefaultSysTenantPackageModuleService(repository,
                new RecordingSysModuleRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.bindGrants(new TenantPackageModuleBindCommand(10L, List.of(
                        new TenantPackageModuleGrantCommand("system", "system:user", "system:user:query"),
                        new TenantPackageModuleGrantCommand(" system ", " system:user ", " system:user:query ")))));

        assertEquals("套餐授权资源重复: system|system:user|system:user:query", exception.getMessage());
        assertEquals(null, repository.deletedPackageId);
    }

    /**
     * 测试用租户套餐模块授权仓储。
     */
    private static class RecordingTenantPackageModuleRepository implements SysTenantPackageModuleRepository {

        /** 最近一次查询套餐主键。 */
        private Long lastPackageId;
        /** 最近一次删除套餐主键。 */
        private Long deletedPackageId;
        /** 最近一次保存授权。 */
        private List<SysTenantPackageModule> savedGrants = List.of();

        @Override
        public List<SysTenantPackageModule> findByPackageId(Long packageId) {
            lastPackageId = packageId;
            return List.of(new SysTenantPackageModule(1L, packageId, "system", "system:user",
                    "system:user:query", LocalDateTime.now()));
        }

        @Override
        public void deleteByPackageId(Long packageId) {
            deletedPackageId = packageId;
        }

        @Override
        public void batchInsert(List<SysTenantPackageModule> grants) {
            savedGrants = grants;
        }
    }

    /**
     * 测试用系统模块仓储。
     */
    private static class RecordingSysModuleRepository implements SysModuleRepository {

        @Override
        public List<SysModule> findAll() {
            return List.of();
        }

        @Override
        public List<SysModuleDependency> findDependenciesByModuleCode(String moduleCode) {
            return List.of();
        }

        @Override
        public List<SysModuleResource> findResourcesByModuleCode(String moduleCode) {
            if (!"system".equals(moduleCode)) {
                return List.of();
            }
            return List.of(
                    new SysModuleResource(1L, "system", "menu", "system:user", "system:user", LocalDateTime.now()),
                    new SysModuleResource(2L, "system", "permission", "system:user:query",
                            "system:user:query", LocalDateTime.now()));
        }

        @Override
        public void saveModule(SysModule module) {
        }

        @Override
        public void replaceDependencies(String moduleCode, List<SysModuleDependency> dependencies) {
        }

        @Override
        public void replaceResources(String moduleCode, List<SysModuleResource> resources) {
        }

        @Override
        public void updateEnabled(String moduleCode, boolean enabled) {
        }
    }
}
