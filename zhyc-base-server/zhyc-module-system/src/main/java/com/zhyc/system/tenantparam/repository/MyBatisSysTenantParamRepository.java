/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantparam.repository;

import com.zhyc.system.tenantparam.domain.SysTenantParam;
import com.zhyc.system.tenantparam.mapper.SysTenantParamMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 基于 MyBatis 的租户参数仓储实现。
 */
@Repository
public class MyBatisSysTenantParamRepository implements SysTenantParamRepository {

    /** 租户参数 Mapper。 */
    private final SysTenantParamMapper tenantParamMapper;

    /**
     * 创建租户参数仓储实现。
     *
     * @param tenantParamMapper 租户参数 Mapper
     */
    public MyBatisSysTenantParamRepository(SysTenantParamMapper tenantParamMapper) {
        this.tenantParamMapper = Objects.requireNonNull(tenantParamMapper, "租户参数 Mapper 不能为空");
    }

    @Override
    public List<SysTenantParam> findByTenantId(String tenantId) {
        return tenantParamMapper.selectByTenantId(tenantId);
    }

    @Override
    public Optional<SysTenantParam> findByTenantIdAndParamKey(String tenantId, String paramKey) {
        return Optional.ofNullable(tenantParamMapper.selectByTenantIdAndParamKey(tenantId, paramKey));
    }

    @Override
    public void save(SysTenantParam param) {
        tenantParamMapper.upsert(param);
    }
}
