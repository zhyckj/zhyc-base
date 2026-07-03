/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.org.repository;

import com.zhyc.system.org.domain.SysOrg;
import com.zhyc.system.org.mapper.SysOrgMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * 基于 MyBatis 的系统组织机构仓储实现。
 */
@Repository
public class MyBatisSysOrgRepository implements SysOrgRepository {

    /** 系统组织机构 Mapper。 */
    private final SysOrgMapper orgMapper;

    /**
     * 创建系统组织机构仓储实现。
     *
     * @param orgMapper 系统组织机构 Mapper
     */
    public MyBatisSysOrgRepository(SysOrgMapper orgMapper) {
        this.orgMapper = Objects.requireNonNull(orgMapper, "系统组织机构 Mapper 不能为空");
    }

    @Override
    public List<SysOrg> findByTenantId(String tenantId) {
        return orgMapper.selectByTenantId(tenantId);
    }

    @Override
    public void insert(SysOrg org) {
        orgMapper.insert(org);
    }

    @Override
    public void update(SysOrg org) {
        orgMapper.update(org);
    }

    @Override
    public void updateStatus(String tenantId, Long orgId, String status) {
        orgMapper.updateStatus(tenantId, orgId, status);
    }

    @Override
    public void deleteByTenantIdAndId(String tenantId, Long orgId) {
        orgMapper.deleteByTenantIdAndId(tenantId, orgId);
    }
}
