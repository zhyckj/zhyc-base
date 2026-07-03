/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user.repository;

import com.zhyc.system.user.domain.SysUser;
import com.zhyc.system.user.mapper.SysUserMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 基于 MyBatis 的系统用户仓储实现。
 */
@Repository
public class MyBatisSysUserRepository implements SysUserRepository {

    /** 系统用户 Mapper。 */
    private final SysUserMapper userMapper;

    /**
     * 创建系统用户仓储实现。
     *
     * @param userMapper 系统用户 Mapper
     */
    public MyBatisSysUserRepository(SysUserMapper userMapper) {
        this.userMapper = Objects.requireNonNull(userMapper, "系统用户 Mapper 不能为空");
    }

    @Override
    public List<SysUser> findByTenantId(String tenantId) {
        return userMapper.selectByTenantId(tenantId);
    }

    @Override
    public Optional<SysUser> findByTenantIdAndUsername(String tenantId, String username) {
        return Optional.ofNullable(userMapper.selectByTenantIdAndUsername(tenantId, username));
    }

    @Override
    public void insert(String tenantId, String username, String nickname, String passwordHash, String status) {
        userMapper.insert(tenantId, username, nickname, passwordHash, status);
    }

    @Override
    public void update(String tenantId, Long userId, String username, String nickname, String status) {
        userMapper.update(tenantId, userId, username, nickname, status);
    }

    @Override
    public void updateStatus(String tenantId, Long userId, String status) {
        userMapper.updateStatus(tenantId, userId, status);
    }

    /**
     * 按租户和登录账号更新用户密码哈希。
     *
     * @param tenantId 租户业务编码
     * @param username 登录账号
     * @param passwordHash 新密码哈希值
     */
    @Override
    public void updatePasswordHash(String tenantId, String username, String passwordHash) {
        userMapper.updatePasswordHash(tenantId, username, passwordHash);
    }

    @Override
    public void updatePasswordHashById(String tenantId, Long userId, String passwordHash) {
        userMapper.updatePasswordHashById(tenantId, userId, passwordHash);
    }

    @Override
    public void deleteByTenantIdAndId(String tenantId, Long userId) {
        userMapper.deleteUserPostsByTenantIdAndUserId(tenantId, userId);
        userMapper.deleteUserRolesByTenantIdAndUserId(tenantId, userId);
        userMapper.deleteAdminScopesByTenantIdAndUserId(tenantId, userId);
        userMapper.deleteByTenantIdAndId(tenantId, userId);
    }
}
