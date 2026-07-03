/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user.service;

import com.zhyc.system.user.domain.SysUser;
import com.zhyc.system.user.repository.SysUserRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

/**
 * 默认系统用户认证查询服务实现。
 */
@Service
public class DefaultSysUserAuthService implements SysUserAuthService {

    /** 系统用户仓储。 */
    private final SysUserRepository userRepository;

    /**
     * 创建默认系统用户认证查询服务。
     *
     * @param userRepository 系统用户仓储
     */
    public DefaultSysUserAuthService(SysUserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository, "系统用户仓储不能为空");
    }

    @Override
    public Optional<SysUserLoginAccount> findLoginAccount(String tenantId, String username) {
        String requiredTenantId = requireText(tenantId, "租户业务编码不能为空");
        String requiredUsername = requireText(username, "登录账号不能为空");
        return userRepository.findByTenantIdAndUsername(requiredTenantId, requiredUsername)
                .map(this::toLoginAccount);
    }

    private SysUserLoginAccount toLoginAccount(SysUser user) {
        return new SysUserLoginAccount(user.getId(), user.getTenantId(), user.getUsername(),
                user.getNickname(), user.getPasswordHash(), user.getStatus());
    }

    private String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw com.zhyc.system.support.SystemServiceValidation.businessFailure(message);
        }
        return value.trim();
    }
}
