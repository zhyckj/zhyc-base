/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenant.repository;

import com.zhyc.system.tenant.domain.Tenant;
import com.zhyc.system.tenant.mapper.SysTenantMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * 基于 MyBatis 的系统租户仓储实现。
 */
@Repository
public class MyBatisSysTenantRepository implements SysTenantRepository {

    /** 系统租户 Mapper。 */
    private final SysTenantMapper tenantMapper;

    /**
     * 创建系统租户仓储实现。
     *
     * @param tenantMapper 系统租户 Mapper
     */
    public MyBatisSysTenantRepository(SysTenantMapper tenantMapper) {
        this.tenantMapper = Objects.requireNonNull(tenantMapper, "系统租户 Mapper 不能为空");
    }

    @Override
    public List<Tenant> findByStatus(String status) {
        return tenantMapper.selectByStatus(status);
    }

    /**
     * 按登录账号查询可访问的启用租户列表。
     *
     * @param username 登录账号
     * @return 授权租户列表
     */
    @Override
    public List<Tenant> findAuthorizedByUsername(String username) {
        return tenantMapper.selectAuthorizedByUsername(username);
    }

    @Override
    public Tenant findByTenantId(String tenantId) {
        return tenantMapper.selectByTenantId(tenantId);
    }

    @Override
    public void save(Tenant tenant) {
        tenantMapper.upsertTenant(tenant);
    }

    @Override
    public void update(Tenant tenant) {
        tenantMapper.updateTenant(tenant);
    }

    @Override
    public void updateStatus(String tenantId, String status) {
        tenantMapper.updateStatus(tenantId, status);
    }

    @Override
    public void deleteByTenantId(String tenantId) {
        tenantMapper.deleteByTenantId(tenantId);
    }
}
