/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user.service;

import java.util.Optional;

/**
 * 系统用户认证查询服务。
 */
public interface SysUserAuthService {

    /**
     * 按租户和登录账号查询认证所需账号信息。
     *
     * @param tenantId 租户业务编码
     * @param username 登录账号
     * @return 登录账号信息，不存在时返回空
     */
    Optional<SysUserLoginAccount> findLoginAccount(String tenantId, String username);
}
