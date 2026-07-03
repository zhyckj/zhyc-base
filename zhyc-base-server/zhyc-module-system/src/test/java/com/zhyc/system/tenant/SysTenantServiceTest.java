/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenant;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.common.tenant.TenantIsolationMode;
import com.zhyc.system.tenant.domain.Tenant;
import com.zhyc.system.tenant.repository.SysTenantRepository;
import com.zhyc.system.tenant.service.DefaultSysTenantService;
import com.zhyc.system.tenant.service.SysTenantCreateCommand;
import com.zhyc.system.tenant.service.SysTenantResponse;
import com.zhyc.system.tenant.service.SysTenantService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 系统租户管理业务服务测试。
 */
class SysTenantServiceTest {

    /**
     * 验证租户服务会按状态查询租户清单并转换套餐、联系人和到期时间字段。
     */
    @Test
    void shouldListTenantsByStatus() {
        RecordingTenantRepository repository = new RecordingTenantRepository();
        SysTenantService service = new DefaultSysTenantService(repository);

        List<SysTenantResponse> tenants = service.listTenants(" enabled ");

        assertEquals("enabled", repository.lastStatus);
        assertEquals(1, tenants.size());
        assertEquals("tenant_a", tenants.get(0).getTenantId());
        assertEquals(10L, tenants.get(0).getPackageId());
        assertEquals("张三", tenants.get(0).getContactName());
    }

    /**
     * 验证授权租户查询会按登录账号读取可访问租户，并裁剪账号空白。
     */
    @Test
    void shouldListAuthorizedTenantsByUsername() {
        RecordingTenantRepository repository = new RecordingTenantRepository();
        SysTenantService service = new DefaultSysTenantService(repository);

        List<SysTenantResponse> tenants = service.listAuthorizedTenants(" admin ");

        assertEquals("admin", repository.lastAuthorizedUsername);
        assertEquals(1, tenants.size());
        assertEquals("tenant_a", tenants.get(0).getTenantId());
    }

    /**
     * 验证租户启停会裁剪租户编码和状态。
     */
    @Test
    void shouldChangeTenantStatus() {
        RecordingTenantRepository repository = new RecordingTenantRepository();
        SysTenantService service = new DefaultSysTenantService(repository);

        service.changeStatus(" tenant_a ", " disabled ");

        assertEquals("tenant_a", repository.lastTenantId);
        assertEquals("disabled", repository.lastChangedStatus);
    }

    /**
     * 验证创建租户时会裁剪业务编码、名称、联系人和状态，并保留套餐与到期时间。
     */
    @Test
    void shouldCreateTenantWithNormalizedFields() {
        RecordingTenantRepository repository = new RecordingTenantRepository();
        SysTenantService service = new DefaultSysTenantService(repository);
        LocalDateTime expireAt = LocalDateTime.now().plusDays(90);

        service.createTenant(new SysTenantCreateCommand(" tenant_b ", " 租户B ", 20L,
                TenantIsolationMode.TENANT_COLUMN, " enabled ", " 李四 ", " 13900000000 ", expireAt));

        assertEquals("tenant_b", repository.savedTenant.getTenantId());
        assertEquals("租户B", repository.savedTenant.getName());
        assertEquals(20L, repository.savedTenant.getPackageId());
        assertEquals(TenantIsolationMode.TENANT_COLUMN, repository.savedTenant.getIsolationMode());
        assertEquals("enabled", repository.savedTenant.getStatus());
        assertEquals("李四", repository.savedTenant.getContactName());
        assertEquals("13900000000", repository.savedTenant.getContactPhone());
        assertEquals(expireAt, repository.savedTenant.getExpireAt());
    }

    /**
     * 验证更新租户时会先确认租户存在，再裁剪租户编码、名称、联系人和状态。
     */
    @Test
    void shouldUpdateTenantWithNormalizedFields() {
        RecordingTenantRepository repository = new RecordingTenantRepository();
        SysTenantService service = new DefaultSysTenantService(repository);
        LocalDateTime expireAt = LocalDateTime.now().plusDays(60);

        service.updateTenant(" tenant_b ", new SysTenantCreateCommand(" tenant_b ", " 租户B2 ", 30L,
                TenantIsolationMode.SCHEMA, " disabled ", " 王五 ", " 13911112222 ", expireAt));

        assertEquals("tenant_b", repository.lastLookupTenantId);
        assertEquals("tenant_b", repository.updatedTenant.getTenantId());
        assertEquals("租户B2", repository.updatedTenant.getName());
        assertEquals(30L, repository.updatedTenant.getPackageId());
        assertEquals(TenantIsolationMode.SCHEMA, repository.updatedTenant.getIsolationMode());
        assertEquals("disabled", repository.updatedTenant.getStatus());
        assertEquals("王五", repository.updatedTenant.getContactName());
        assertEquals("13911112222", repository.updatedTenant.getContactPhone());
        assertEquals(expireAt, repository.updatedTenant.getExpireAt());
    }

    /**
     * 验证更新不存在的租户会返回明确业务错误。
     */
    @Test
    void shouldRejectMissingTenantWhenUpdating() {
        RecordingTenantRepository repository = new RecordingTenantRepository();
        SysTenantService service = new DefaultSysTenantService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.updateTenant("tenant_missing", new SysTenantCreateCommand("tenant_missing", "租户", 1L,
                        TenantIsolationMode.TENANT_COLUMN, "enabled", null, null, null)));

        assertEquals("ZHYC_SYS_TENANT_NOT_FOUND", exception.getCode());
        assertEquals("租户不存在: tenant_missing", exception.getMessage());
    }

    /**
     * 验证租户状态只能使用设计表结构约定的启用、禁用和已过期状态。
     */
    @Test
    void shouldRejectUnsupportedTenantStatus() {
        RecordingTenantRepository repository = new RecordingTenantRepository();
        SysTenantService service = new DefaultSysTenantService(repository);

        BusinessException createException = assertThrows(BusinessException.class,
                () -> service.createTenant(new SysTenantCreateCommand("tenant_c", "租户C", 30L,
                        TenantIsolationMode.TENANT_COLUMN, " archived ", null, null, null)));
        BusinessException changeException = assertThrows(BusinessException.class,
                () -> service.changeStatus("tenant_c", "archived"));
        BusinessException listException = assertThrows(BusinessException.class,
                () -> service.listTenants("archived"));

        assertEquals("ZHYC_SYS_TENANT_STATUS_UNSUPPORTED", createException.getCode());
        assertEquals("ZHYC_SYS_TENANT_STATUS_UNSUPPORTED", changeException.getCode());
        assertEquals("ZHYC_SYS_TENANT_STATUS_UNSUPPORTED", listException.getCode());
        assertEquals("租户状态不支持: archived", createException.getMessage());
        assertEquals("租户状态不支持: archived", changeException.getMessage());
        assertEquals("租户状态不支持: archived", listException.getMessage());
    }

    /**
     * 测试用租户仓储。
     */
    private static class RecordingTenantRepository implements SysTenantRepository {

        /** 最近一次查询状态。 */
        private String lastStatus;
        /** 最近一次变更租户编码。 */
        private String lastTenantId;
        /** 最近一次变更状态。 */
        private String lastChangedStatus;
        /** 最近一次保存的租户。 */
        private Tenant savedTenant;
        /** 最近一次更新的租户。 */
        private Tenant updatedTenant;
        /** 最近一次授权租户查询账号。 */
        private String lastAuthorizedUsername;
        /** 最近一次按租户编码查询的租户编码。 */
        private String lastLookupTenantId;

        @Override
        public List<Tenant> findByStatus(String status) {
            lastStatus = status;
            return List.of(new Tenant(1L, "tenant_a", "租户A", 10L, TenantIsolationMode.TENANT_COLUMN,
                    "enabled", "张三", "13800000000", LocalDateTime.now().plusDays(30),
                    LocalDateTime.now(), LocalDateTime.now()));
        }

        @Override
        public List<Tenant> findAuthorizedByUsername(String username) {
            lastAuthorizedUsername = username;
            return List.of(new Tenant(1L, "tenant_a", "租户A", 10L, TenantIsolationMode.TENANT_COLUMN,
                    "enabled", "张三", "13800000000", LocalDateTime.now().plusDays(30),
                    LocalDateTime.now(), LocalDateTime.now()));
        }

        @Override
        public Tenant findByTenantId(String tenantId) {
            lastLookupTenantId = tenantId;
            if ("tenant_missing".equals(tenantId)) {
                return null;
            }
            return new Tenant(1L, tenantId, "租户A", 10L, TenantIsolationMode.TENANT_COLUMN,
                    "enabled", "张三", "13800000000", LocalDateTime.now().plusDays(30),
                    LocalDateTime.now(), LocalDateTime.now());
        }

        @Override
        public void updateStatus(String tenantId, String status) {
            lastTenantId = tenantId;
            lastChangedStatus = status;
        }

        @Override
        public void save(Tenant tenant) {
            savedTenant = tenant;
        }

        @Override
        public void update(Tenant tenant) {
            updatedTenant = tenant;
        }

        @Override
        public void deleteByTenantId(String tenantId) {
            throw new AssertionError("租户服务测试不应删除租户");
        }
    }
}
