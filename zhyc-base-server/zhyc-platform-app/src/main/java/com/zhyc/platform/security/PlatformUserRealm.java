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
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import java.util.Objects;

/**
 * 平台后台用户 Shiro Realm。
 *
 * <p>该 Realm 负责把系统模块中的用户、角色菜单权限接入 Shiro 认证授权上下文。</p>
 */
public class PlatformUserRealm extends AuthorizingRealm {

    /** 启用状态编码。 */
    private static final String STATUS_ENABLED = "enabled";

    /** 系统用户认证查询服务。 */
    private final SysUserAuthService userAuthService;
    /** 系统权限业务服务。 */
    private final SysPermissionService permissionService;
    /** 认证中心令牌主体映射器。 */
    private final PlatformTokenPrincipalMapper tokenPrincipalMapper;

    /**
     * 创建平台后台用户 Realm。
     *
     * @param userAuthService 系统用户认证查询服务
     * @param permissionService 系统权限业务服务
     * @param credentialsMatcher Shiro 凭证匹配器
     */
    public PlatformUserRealm(SysUserAuthService userAuthService, SysPermissionService permissionService,
                             CredentialsMatcher credentialsMatcher) {
        this(userAuthService, permissionService, credentialsMatcher, new PlatformTokenPrincipalMapper());
    }

    /**
     * 创建平台后台用户 Realm。
     *
     * @param userAuthService 系统用户认证查询服务
     * @param permissionService 系统权限业务服务
     * @param credentialsMatcher Shiro 凭证匹配器
     * @param tokenPrincipalMapper 认证中心令牌主体映射器
     */
    public PlatformUserRealm(SysUserAuthService userAuthService, SysPermissionService permissionService,
                             CredentialsMatcher credentialsMatcher, PlatformTokenPrincipalMapper tokenPrincipalMapper) {
        super(Objects.requireNonNull(credentialsMatcher, "Shiro 凭证匹配器不能为空"));
        this.userAuthService = Objects.requireNonNull(userAuthService, "系统用户认证查询服务不能为空");
        this.permissionService = Objects.requireNonNull(permissionService, "系统权限业务服务不能为空");
        this.tokenPrincipalMapper = Objects.requireNonNull(tokenPrincipalMapper, "认证中心令牌主体映射器不能为空");
        setName("platformUserRealm");
    }

    /**
     * 判断当前 Realm 是否支持指定认证令牌。
     *
     * @param token Shiro 认证令牌
     * @return 支持平台用户名密码令牌或认证中心 Claims 令牌时返回 {@code true}
     */
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof PlatformUsernamePasswordToken
                || token instanceof PlatformAuthCenterAuthenticationToken;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        PlatformUserPrincipal principal = (PlatformUserPrincipal) principals.getPrimaryPrincipal();
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        authorizationInfo.addStringPermissions(permissionService.listUserPermissions(
                principal.getTenantId(), principal.getUserId()));
        return authorizationInfo;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        if (token instanceof PlatformAuthCenterAuthenticationToken authCenterToken) {
            return authenticateAuthCenterToken(authCenterToken);
        }
        PlatformUsernamePasswordToken platformToken = (PlatformUsernamePasswordToken) token;
        SysUserLoginAccount account = userAuthService.findLoginAccount(
                        platformToken.getTenantId(), platformToken.getUsername())
                .orElseThrow(() -> new UnknownAccountException("租户内登录账号不存在"));
        if (!STATUS_ENABLED.equals(account.getStatus())) {
            throw new DisabledAccountException("登录账号已被禁用");
        }
        PlatformUserPrincipal principal = new PlatformUserPrincipal(account.getUserId(), account.getTenantId(),
                account.getUsername(), account.getNickname());
        return new SimpleAuthenticationInfo(principal, account.getPasswordHash(), getName());
    }

    /**
     * 对认证中心 Claims 令牌执行平台本地用户绑定校验。
     *
     * <p>Token 的签名和有效期应在进入 Realm 前完成校验；Realm 负责确认本地用户仍存在、启用且用户主键一致。</p>
     *
     * @param token 认证中心 Claims 认证令牌
     * @return Shiro 认证信息
     */
    private AuthenticationInfo authenticateAuthCenterToken(PlatformAuthCenterAuthenticationToken token) {
        PlatformUserPrincipal claimsPrincipal = tokenPrincipalMapper.mapClaims(token.getClaims());
        SysUserLoginAccount account = userAuthService.findLoginAccount(
                        claimsPrincipal.getTenantId(), claimsPrincipal.getUsername())
                .orElseThrow(() -> new UnknownAccountException("租户内登录账号不存在"));
        if (!STATUS_ENABLED.equals(account.getStatus())) {
            throw new DisabledAccountException("登录账号已被禁用");
        }
        if (!account.getUserId().equals(claimsPrincipal.getUserId())) {
            throw new AuthenticationException("认证中心用户主键与本地用户不一致");
        }
        PlatformUserPrincipal principal = new PlatformUserPrincipal(account.getUserId(), account.getTenantId(),
                account.getUsername(), account.getNickname());
        return new SimpleAuthenticationInfo(principal, token.getCredentials(), getName());
    }

    /**
     * 校验认证凭据。
     *
     * <p>认证中心 Claims 令牌在进入 Realm 前已经完成 Token 校验，这里只跳过密码匹配；
     * 用户名密码令牌仍使用 Shiro 凭证匹配器校验密码哈希。</p>
     *
     * @param token Shiro 认证令牌
     * @param info Shiro 认证信息
     * @throws AuthenticationException 凭据不匹配时抛出
     */
    @Override
    protected void assertCredentialsMatch(AuthenticationToken token, AuthenticationInfo info)
            throws AuthenticationException {
        if (token instanceof PlatformAuthCenterAuthenticationToken) {
            return;
        }
        super.assertCredentialsMatch(token, info);
    }
}
