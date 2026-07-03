/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantpackage;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.tenantpackage.domain.SysTenantPackage;
import com.zhyc.system.tenantpackage.repository.SysTenantPackageRepository;
import com.zhyc.system.tenantpackage.service.DefaultSysTenantPackageService;
import com.zhyc.system.tenantpackage.service.SysTenantPackageResponse;
import com.zhyc.system.tenantpackage.service.SysTenantPackageService;
import com.zhyc.system.tenantpackage.service.TenantPackageCreateCommand;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 系统租户套餐业务服务测试。
 */
class SysTenantPackageServiceTest {

    /**
     * 验证套餐服务会按状态查询套餐列表并转换用户数、存储容量字段。
     */
    @Test
    void shouldListPackagesByStatus() {
        RecordingTenantPackageRepository repository = new RecordingTenantPackageRepository();
        SysTenantPackageService service = new DefaultSysTenantPackageService(repository);

        List<SysTenantPackageResponse> packages = service.listPackages(" enabled ");

        assertEquals("enabled", repository.lastStatus);
        assertEquals(1, packages.size());
        assertEquals("standard", packages.get(0).getPackageCode());
        assertEquals(100, packages.get(0).getMaxUserCount());
        assertEquals(10240, packages.get(0).getMaxStorageMb());
    }

    /**
     * 验证套餐启停会裁剪套餐编码和状态。
     */
    @Test
    void shouldChangePackageStatus() {
        RecordingTenantPackageRepository repository = new RecordingTenantPackageRepository();
        SysTenantPackageService service = new DefaultSysTenantPackageService(repository);

        service.changeStatus(" standard ", " disabled ");

        assertEquals("standard", repository.lastPackageCode);
        assertEquals("disabled", repository.lastChangedStatus);
    }

    /**
     * 验证创建租户套餐会裁剪文本字段、校验状态并保留容量限制。
     */
    @Test
    void shouldCreateTenantPackageWithNormalizedFields() {
        RecordingTenantPackageRepository repository = new RecordingTenantPackageRepository();
        SysTenantPackageService service = new DefaultSysTenantPackageService(repository);

        SysTenantPackageResponse response = service.createPackage(new TenantPackageCreateCommand(
                " premium ", " 旗舰版 ", " enabled ", 300, 20480));

        assertEquals("premium", repository.createdPackage.getPackageCode());
        assertEquals("旗舰版", repository.createdPackage.getPackageName());
        assertEquals("enabled", repository.createdPackage.getStatus());
        assertEquals(300, repository.createdPackage.getMaxUserCount());
        assertEquals(20480, repository.createdPackage.getMaxStorageMb());
        assertEquals("premium", response.getPackageCode());
        assertEquals("旗舰版", response.getPackageName());
    }

    /**
     * 验证创建租户套餐会拒绝负数容量限制。
     */
    @Test
    void shouldRejectNegativePackageLimitWhenCreating() {
        RecordingTenantPackageRepository repository = new RecordingTenantPackageRepository();
        SysTenantPackageService service = new DefaultSysTenantPackageService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.createPackage(new TenantPackageCreateCommand(
                        "premium", "旗舰版", "enabled", -1, 20480)));

        assertEquals("ZHYC_SYS_TENANT_PACKAGE_LIMIT_INVALID", exception.getCode());
        assertEquals("套餐容量限制不能小于 0", exception.getMessage());
    }

    /**
     * 验证套餐列表查询会拒绝不受支持的套餐状态。
     */
    @Test
    void shouldRejectUnsupportedPackageStatusWhenListing() {
        RecordingTenantPackageRepository repository = new RecordingTenantPackageRepository();
        SysTenantPackageService service = new DefaultSysTenantPackageService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.listPackages(" archived "));

        assertEquals("ZHYC_SYS_TENANT_PACKAGE_STATUS_UNSUPPORTED", exception.getCode());
        assertEquals("套餐状态不支持: archived", exception.getMessage());
    }

    /**
     * 验证套餐启停会拒绝不受支持的套餐状态。
     */
    @Test
    void shouldRejectUnsupportedPackageStatusWhenChanging() {
        RecordingTenantPackageRepository repository = new RecordingTenantPackageRepository();
        SysTenantPackageService service = new DefaultSysTenantPackageService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.changeStatus("standard", " archived "));

        assertEquals("ZHYC_SYS_TENANT_PACKAGE_STATUS_UNSUPPORTED", exception.getCode());
        assertEquals("套餐状态不支持: archived", exception.getMessage());
    }

    /**
     * 测试用租户套餐仓储。
     */
    private static class RecordingTenantPackageRepository implements SysTenantPackageRepository {

        /** 最近一次查询状态。 */
        private String lastStatus;
        /** 最近一次变更套餐编码。 */
        private String lastPackageCode;
        /** 最近一次变更状态。 */
        private String lastChangedStatus;
        /** 最近一次创建的租户套餐。 */
        private SysTenantPackage createdPackage;

        @Override
        public List<SysTenantPackage> findByStatus(String status) {
            lastStatus = status;
            return List.of(new SysTenantPackage(1L, "standard", "标准版", "enabled",
                    100, 10240, LocalDateTime.now(), LocalDateTime.now()));
        }

        @Override
        public Optional<SysTenantPackage> findByCode(String packageCode) {
            return Optional.empty();
        }

        @Override
        public void updateStatus(String packageCode, String status) {
            lastPackageCode = packageCode;
            lastChangedStatus = status;
        }

        @Override
        public SysTenantPackage save(SysTenantPackage tenantPackage) {
            createdPackage = tenantPackage;
            return new SysTenantPackage(2L, tenantPackage.getPackageCode(), tenantPackage.getPackageName(),
                    tenantPackage.getStatus(), tenantPackage.getMaxUserCount(), tenantPackage.getMaxStorageMb(),
                    LocalDateTime.now(), LocalDateTime.now());
        }
    }
}
