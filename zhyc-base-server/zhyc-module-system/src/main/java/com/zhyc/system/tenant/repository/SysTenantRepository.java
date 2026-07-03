/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenant.repository;

import com.zhyc.system.tenant.domain.Tenant;

import java.util.List;

/**
 * 系统租户仓储。
 */
public interface SysTenantRepository {

    /**
     * 按租户状态查询租户列表。
     *
     * @param status 租户状态
     * @return 租户列表
     */
    List<Tenant> findByStatus(String status);

    /**
     * 按登录账号查询可访问的启用租户列表。
     *
     * @param username 登录账号
     * @return 授权租户列表
     */
    List<Tenant> findAuthorizedByUsername(String username);

    /**
     * 按租户业务编码查询租户主记录。
     *
     * @param tenantId 租户业务编码
     * @return 租户主记录；不存在时返回 {@code null}
     */
    Tenant findByTenantId(String tenantId);

    /**
     * 保存租户基础信息。
     *
     * @param tenant 租户基础信息
     */
    void save(Tenant tenant);

    /**
     * 更新租户基础信息。
     *
     * @param tenant 租户基础信息
     */
    void update(Tenant tenant);

    /**
     * 修改租户状态。
     *
     * @param tenantId 租户业务编码
     * @param status 目标状态
     */
    void updateStatus(String tenantId, String status);

    /**
     * 删除系统租户主记录。
     *
     * @param tenantId 租户业务编码
     */
    void deleteByTenantId(String tenantId);
}
