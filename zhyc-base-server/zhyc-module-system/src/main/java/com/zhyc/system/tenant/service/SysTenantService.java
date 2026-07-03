/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenant.service;

import java.util.List;

/**
 * 系统租户业务服务。
 */
public interface SysTenantService {

    /**
     * 按租户状态查询租户列表。
     *
     * @param status 租户状态
     * @return 租户列表
     */
    List<SysTenantResponse> listTenants(String status);

    /**
     * 查询登录账号可访问的启用租户列表。
     *
     * @param username 登录账号
     * @return 授权租户列表
     */
    List<SysTenantResponse> listAuthorizedTenants(String username);

    /**
     * 创建或更新租户基础信息。
     *
     * @param command 租户创建命令
     */
    void createTenant(SysTenantCreateCommand command);

    /**
     * 更新租户基础信息。
     *
     * @param tenantId 租户业务编码
     * @param command 租户基础信息命令
     */
    void updateTenant(String tenantId, SysTenantCreateCommand command);

    /**
     * 修改租户状态。
     *
     * @param tenantId 租户业务编码
     * @param status 目标状态
     */
    void changeStatus(String tenantId, String status);

    /**
     * 删除系统租户主记录。
     *
     * @param tenantId 租户业务编码
     */
    void deleteTenant(String tenantId);
}
