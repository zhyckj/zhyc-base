/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.security;

import com.zhyc.system.permission.service.SysPermissionService;
import com.zhyc.system.user.service.SysUserAuthService;
import com.zhyc.system.user.service.SysUserLoginAccount;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.PasswordMatcher;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 平台 Shiro 用户 Realm 测试。
 */
class PlatformUserRealmTest {

    /**
     * 验证启用用户可以通过租户、账号和密码哈希完成认证，并形成平台用户主体。
     */
    @Test
    void shouldAuthenticateEnabledTenantUserWithPasswordHash() {
        DefaultPasswordService passwordService = new DefaultPasswordService();
        RecordingUserAuthService userAuthService = new RecordingUserAuthService(
                Optional.of(account(passwordService.encryptPassword("secret"), "enabled")));
        PlatformUserRealm realm = new PlatformUserRealm(userAuthService, new RecordingPermissionService(List.of()),
                passwordMatcher(passwordService));

        AuthenticationInfo info = realm.getAuthenticationInfo(
                new PlatformUsernamePasswordToken("tenant_a", "admin", "secret".toCharArray()));
        PlatformUserPrincipal principal = (PlatformUserPrincipal) info.getPrincipals().getPrimaryPrincipal();

        assertEquals("tenant_a", userAuthService.lastTenantId);
        assertEquals("admin", userAuthService.lastUsername);
        assertEquals(1001L, principal.getUserId());
        assertEquals("tenant_a", principal.getTenantId());
        assertEquals("admin", principal.getUsername());
    }

    /**
     * 验证认证中心已校验的 Claims Token 可以回查本地启用用户并形成 Shiro 主体。
     */
    @Test
    void shouldAuthenticateAuthCenterClaimsTokenWithLocalUser() {
        DefaultPasswordService passwordService = new DefaultPasswordService();
        RecordingUserAuthService userAuthService = new RecordingUserAuthService(
                Optional.of(account(passwordService.encryptPassword("secret"), "enabled")));
        PlatformUserRealm realm = new PlatformUserRealm(userAuthService, new RecordingPermissionService(List.of()),
                passwordMatcher(passwordService));

        AuthenticationInfo info = realm.getAuthenticationInfo(new PlatformAuthCenterAuthenticationToken(Map.of(
                "tenant_id", "tenant_a",
                "user_id", 1001L,
                "preferred_username", "admin",
                "name", "管理员")));
        PlatformUserPrincipal principal = (PlatformUserPrincipal) info.getPrincipals().getPrimaryPrincipal();

        assertEquals("tenant_a", userAuthService.lastTenantId);
        assertEquals("admin", userAuthService.lastUsername);
        assertEquals(1001L, principal.getUserId());
        assertEquals("tenant_a", principal.getTenantId());
        assertEquals("admin", principal.getUsername());
    }

    /**
     * 验证认证中心 Claims 中的用户主键与本地用户不一致时拒绝认证，避免 Token 错绑本地账号。
     */
    @Test
    void shouldRejectAuthCenterClaimsWhenLocalUserIdMismatch() {
        DefaultPasswordService passwordService = new DefaultPasswordService();
        PlatformUserRealm realm = new PlatformUserRealm(new RecordingUserAuthService(
                Optional.of(account(passwordService.encryptPassword("secret"), "enabled"))),
                new RecordingPermissionService(List.of()), passwordMatcher(passwordService));

        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> realm.getAuthenticationInfo(new PlatformAuthCenterAuthenticationToken(Map.of(
                        "tenant_id", "tenant_a",
                        "user_id", 2002L,
                        "preferred_username", "admin"))));

        assertEquals("认证中心用户主键与本地用户不一致", exception.getMessage());
    }

    /**
     * 验证租户内账号不存在时拒绝认证，避免跨租户回退查询。
     */
    @Test
    void shouldRejectUnknownTenantUser() {
        PlatformUserRealm realm = new PlatformUserRealm(new RecordingUserAuthService(Optional.empty()),
                new RecordingPermissionService(List.of()), passwordMatcher(new DefaultPasswordService()));

        assertThrows(UnknownAccountException.class,
                () -> realm.getAuthenticationInfo(new PlatformUsernamePasswordToken("tenant_a", "missing", "secret")));
    }

    /**
     * 验证禁用账号不能登录后台管理端。
     */
    @Test
    void shouldRejectDisabledUser() {
        DefaultPasswordService passwordService = new DefaultPasswordService();
        PlatformUserRealm realm = new PlatformUserRealm(new RecordingUserAuthService(
                Optional.of(account(passwordService.encryptPassword("secret"), "disabled"))),
                new RecordingPermissionService(List.of()), passwordMatcher(passwordService));

        assertThrows(DisabledAccountException.class,
                () -> realm.getAuthenticationInfo(new PlatformUsernamePasswordToken("tenant_a", "admin", "secret")));
    }

    /**
     * 验证 Realm 会按平台用户主体的租户和用户 ID 加载菜单按钮权限。
     */
    @Test
    void shouldAuthorizePermissionsByTenantAndUser() {
        RecordingPermissionService permissionService = new RecordingPermissionService(
                List.of("system:user:query", "system:user:create"));
        PlatformUserRealm realm = new PlatformUserRealm(new RecordingUserAuthService(Optional.empty()),
                permissionService, passwordMatcher(new DefaultPasswordService()));
        PlatformUserPrincipal principal = new PlatformUserPrincipal(1001L, "tenant_a", "admin", "管理员");
        SimplePrincipalCollection principals = new SimplePrincipalCollection(principal, realm.getName());

        assertTrue(realm.isPermitted(principals, "system:user:query"));
        assertFalse(realm.isPermitted(principals, "system:user:delete"));
        assertThrows(UnauthorizedException.class, () -> realm.checkPermission(principals, "system:user:delete"));
        assertEquals("tenant_a", permissionService.lastTenantId);
        assertEquals(1001L, permissionService.lastUserId);
    }

    /**
     * 验证内置管理员返回 Shiro 通配权限后，可以访问后台任意受保护资源。
     */
    @Test
    void shouldAuthorizeAnyPermissionWhenWildcardPermissionExists() {
        RecordingPermissionService permissionService = new RecordingPermissionService(List.of("*"));
        PlatformUserRealm realm = new PlatformUserRealm(new RecordingUserAuthService(Optional.empty()),
                permissionService, passwordMatcher(new DefaultPasswordService()));
        PlatformUserPrincipal principal = new PlatformUserPrincipal(1001L, "tenant_a", "admin", "管理员");
        SimplePrincipalCollection principals = new SimplePrincipalCollection(principal, realm.getName());

        assertTrue(realm.isPermitted(principals, "system:user:query"));
        assertTrue(realm.isPermitted(principals, "workflow:task:approve"));
        assertEquals("tenant_a", permissionService.lastTenantId);
        assertEquals(1001L, permissionService.lastUserId);
    }

    private static SysUserLoginAccount account(String passwordHash, String status) {
        return new SysUserLoginAccount(1001L, "tenant_a", "admin", "管理员", passwordHash, status);
    }

    private static PasswordMatcher passwordMatcher(DefaultPasswordService passwordService) {
        PasswordMatcher matcher = new PasswordMatcher();
        matcher.setPasswordService(passwordService);
        return matcher;
    }

    /**
     * 测试用系统用户认证服务。
     */
    private static class RecordingUserAuthService implements SysUserAuthService {

        /** 预设查询结果。 */
        private final Optional<SysUserLoginAccount> account;
        /** 最近一次查询的租户业务编码。 */
        private String lastTenantId;
        /** 最近一次查询的登录账号。 */
        private String lastUsername;

        private RecordingUserAuthService(Optional<SysUserLoginAccount> account) {
            this.account = account;
        }

        @Override
        public Optional<SysUserLoginAccount> findLoginAccount(String tenantId, String username) {
            lastTenantId = tenantId;
            lastUsername = username;
            return account;
        }
    }

    /**
     * 测试用系统权限服务。
     */
    private static class RecordingPermissionService implements SysPermissionService {

        /** 预设权限标识列表。 */
        private final List<String> permissions;
        /** 最近一次查询的租户业务编码。 */
        private String lastTenantId;
        /** 最近一次查询的用户主键。 */
        private Long lastUserId;

        private RecordingPermissionService(List<String> permissions) {
            this.permissions = permissions;
        }

        @Override
        public List<String> listUserPermissions(String tenantId, Long userId) {
            lastTenantId = tenantId;
            lastUserId = userId;
            return permissions;
        }
    }
}
