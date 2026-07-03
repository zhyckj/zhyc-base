/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.org.repository;

import com.zhyc.system.org.domain.SysOrg;

import java.util.List;

/**
 * 系统组织机构仓储接口。
 */
public interface SysOrgRepository {

    /**
     * 查询租户内启用状态的组织机构。
     *
     * @param tenantId 租户业务编码
     * @return 租户内组织机构列表
     */
    List<SysOrg> findByTenantId(String tenantId);

    void insert(SysOrg org);

    void update(SysOrg org);

    void updateStatus(String tenantId, Long orgId, String status);

    void deleteByTenantIdAndId(String tenantId, Long orgId);
}
