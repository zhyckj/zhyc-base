/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user;

import com.zhyc.system.user.domain.SysUser;
import com.zhyc.system.user.repository.SysUserRepository;
import com.zhyc.system.user.service.DefaultSysUserAuthService;
import com.zhyc.system.user.service.SysUserAuthService;
import com.zhyc.system.user.service.SysUserLoginAccount;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统用户认证查询服务测试。
 */
class SysUserAuthServiceTest {

    /**
     * 验证认证查询会按租户和登录账号读取用户凭证，并保留用户状态供 Shiro 判断。
     */
    @Test
    void shouldFindLoginAccountByTenantAndUsername() {
        RecordingUserRepository repository = new RecordingUserRepository();
        SysUserAuthService service = new DefaultSysUserAuthService(repository);

        Optional<SysUserLoginAccount> account = service.findLoginAccount(" tenant_a ", " admin ");

        assertTrue(account.isPresent());
        assertEquals("tenant_a", repository.lastTenantId);
        assertEquals("admin", repository.lastUsername);
        assertEquals(1001L, account.get().getUserId());
        assertEquals("tenant_a", account.get().getTenantId());
        assertEquals("admin", account.get().getUsername());
        assertEquals("管理员", account.get().getNickname());
        assertEquals("hash-value", account.get().getPasswordHash());
        assertEquals("enabled", account.get().getStatus());
    }

    /**
     * 测试用系统用户仓储。
     */
    private static class RecordingUserRepository implements SysUserRepository {

        /** 最近一次查询的租户业务编码。 */
        private String lastTenantId;
        /** 最近一次查询的登录账号。 */
        private String lastUsername;

        @Override
        public List<SysUser> findByTenantId(String tenantId) {
            return List.of();
        }

        @Override
        public Optional<SysUser> findByTenantIdAndUsername(String tenantId, String username) {
            lastTenantId = tenantId;
            lastUsername = username;
            return Optional.of(new SysUser(1001L, tenantId, username, "管理员", "hash-value",
                    "enabled", LocalDateTime.now(), LocalDateTime.now()));
        }

        @Override
        public void insert(String tenantId, String username, String nickname, String passwordHash, String status) {
            throw new AssertionError("认证查询服务不应新增用户");
        }

        @Override
        public void update(String tenantId, Long userId, String username, String nickname, String status) {
            throw new AssertionError("认证查询服务不应更新用户");
        }

        @Override
        public void updateStatus(String tenantId, Long userId, String status) {
            throw new AssertionError("认证查询服务不应更新用户状态");
        }

        @Override
        public void updatePasswordHash(String tenantId, String username, String passwordHash) {
            throw new AssertionError("认证查询服务不应更新密码哈希");
        }

        @Override
        public void updatePasswordHashById(String tenantId, Long userId, String passwordHash) {
            throw new AssertionError("认证查询服务不应重置密码");
        }

        @Override
        public void deleteByTenantIdAndId(String tenantId, Long userId) {
            throw new AssertionError("认证查询服务不应删除用户");
        }
    }
}
