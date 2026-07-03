/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user.service;

import java.util.List;

/**
 * 系统用户业务服务。
 */
public interface SysUserService {

    /**
     * 查询租户内系统用户列表。
     *
     * @param tenantId 租户业务编码
     * @return 系统用户列表
     */
    List<SysUserResponse> listUsers(String tenantId);

    /**
     * 新增或编辑系统用户。
     *
     * @param command 用户保存命令
     */
    void saveUser(SysUserSaveCommand command);

    /**
     * 修改系统用户状态。
     *
     * @param tenantId 租户业务编码
     * @param userId 用户主键
     * @param status 目标状态
     */
    void updateStatus(String tenantId, Long userId, String status);

    /**
     * 重置系统用户密码。
     *
     * @param tenantId 租户业务编码
     * @param userId 用户主键
     * @param password 新密码明文
     */
    void resetPassword(String tenantId, Long userId, String password);

    /**
     * 删除系统用户。
     *
     * @param tenantId 租户业务编码
     * @param userId 用户主键
     */
    void deleteUser(String tenantId, Long userId);

    /**
     * 修改系统用户密码。
     *
     * <p>调用方必须传入租户、账号、当前密码和新密码；服务层负责旧密码校验、租户密码策略校验和密码哈希更新。</p>
     *
     * @param command 修改密码命令
     */
    void changePassword(SysUserPasswordChangeCommand command);
}
