/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user.repository;

import com.zhyc.system.user.domain.SysUser;

import java.util.List;
import java.util.Optional;

/**
 * 系统用户仓储。
 */
public interface SysUserRepository {

    /**
     * 查询租户内系统用户列表。
     *
     * @param tenantId 租户业务编码
     * @return 系统用户列表
     */
    List<SysUser> findByTenantId(String tenantId);

    /**
     * 按租户和登录账号查询系统用户。
     *
     * @param tenantId 租户业务编码
     * @param username 登录账号
     * @return 系统用户，不存在时返回空
     */
    Optional<SysUser> findByTenantIdAndUsername(String tenantId, String username);

    /**
     * 新增系统用户。
     *
     * @param tenantId 租户业务编码
     * @param username 登录账号
     * @param nickname 用户显示名称
     * @param passwordHash 密码哈希
     * @param status 用户状态
     */
    void insert(String tenantId, String username, String nickname, String passwordHash, String status);

    /**
     * 更新系统用户基础信息。
     *
     * @param tenantId 租户业务编码
     * @param userId 用户主键
     * @param username 登录账号
     * @param nickname 用户显示名称
     * @param status 用户状态
     */
    void update(String tenantId, Long userId, String username, String nickname, String status);

    /**
     * 更新系统用户状态。
     *
     * @param tenantId 租户业务编码
     * @param userId 用户主键
     * @param status 用户状态
     */
    void updateStatus(String tenantId, Long userId, String status);

    /**
     * 按租户和登录账号更新用户密码哈希。
     *
     * @param tenantId 租户业务编码
     * @param username 登录账号
     * @param passwordHash 新密码哈希值
     */
    void updatePasswordHash(String tenantId, String username, String passwordHash);

    /**
     * 按主键更新用户密码哈希。
     *
     * @param tenantId 租户业务编码
     * @param userId 用户主键
     * @param passwordHash 新密码哈希值
     */
    void updatePasswordHashById(String tenantId, Long userId, String passwordHash);

    /**
     * 删除租户内系统用户及其关联数据。
     *
     * @param tenantId 租户业务编码
     * @param userId 用户主键
     */
    void deleteByTenantIdAndId(String tenantId, Long userId);
}
