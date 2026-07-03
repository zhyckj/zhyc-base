/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantpackage.repository;

import com.zhyc.system.tenantpackage.domain.SysTenantPackage;
import com.zhyc.system.tenantpackage.mapper.SysTenantPackageMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 基于 MyBatis 的系统租户套餐仓储实现。
 */
@Repository
public class MyBatisSysTenantPackageRepository implements SysTenantPackageRepository {

    /** 系统租户套餐 Mapper。 */
    private final SysTenantPackageMapper tenantPackageMapper;

    /**
     * 创建系统租户套餐仓储实现。
     *
     * @param tenantPackageMapper 系统租户套餐 Mapper
     */
    public MyBatisSysTenantPackageRepository(SysTenantPackageMapper tenantPackageMapper) {
        this.tenantPackageMapper = Objects.requireNonNull(tenantPackageMapper, "系统租户套餐 Mapper 不能为空");
    }

    @Override
    public List<SysTenantPackage> findByStatus(String status) {
        return tenantPackageMapper.selectByStatus(status);
    }

    @Override
    public Optional<SysTenantPackage> findByCode(String packageCode) {
        return Optional.ofNullable(tenantPackageMapper.selectByCode(packageCode));
    }

    @Override
    public SysTenantPackage save(SysTenantPackage tenantPackage) {
        tenantPackageMapper.insert(tenantPackage);
        return findByCode(tenantPackage.getPackageCode()).orElse(tenantPackage);
    }

    @Override
    public void updateStatus(String packageCode, String status) {
        tenantPackageMapper.updateStatus(packageCode, status);
    }
}
