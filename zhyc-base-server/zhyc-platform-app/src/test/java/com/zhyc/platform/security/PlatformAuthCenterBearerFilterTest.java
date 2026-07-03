/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.io.IOException;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 认证中心 Bearer Token 过滤器测试。
 */
class PlatformAuthCenterBearerFilterTest {

    /**
     * 验证未携带 Bearer Token 的请求直接透传，不创建 Shiro 登录上下文。
     *
     * @throws Exception 过滤器执行异常
     */
    @Test
    void shouldPassThroughWhenBearerTokenMissing() throws Exception {
        RecordingSubjectAuthenticator authenticator = new RecordingSubjectAuthenticator();
        SecurityManager securityManager = new DefaultSecurityManager();
        PlatformAuthCenterBearerFilter filter = new PlatformAuthCenterBearerFilter(validJwtDecoder(), authenticator,
                securityManager);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/system/users");
        MockHttpServletResponse response = new MockHttpServletResponse();
        RecordingFilterChain chain = new RecordingFilterChain(securityManager);

        filter.doFilter(request, response, chain);

        assertTrue(chain.invoked);
        assertTrue(chain.shiroContextBound);
        assertFalse(authenticator.authenticated);
        assertEquals(200, response.getStatus());
        assertFalse(ThreadContext.getResources().containsKey(ThreadContext.SECURITY_MANAGER_KEY));
    }

    /**
     * 验证有效 Bearer Token 会解析 Claims，并交给 Shiro 登录适配器建立平台授权上下文。
     *
     * @throws Exception 过滤器执行异常
     */
    @Test
    void shouldAuthenticateSubjectWhenBearerTokenValid() throws Exception {
        RecordingSubjectAuthenticator authenticator = new RecordingSubjectAuthenticator();
        SecurityManager securityManager = new DefaultSecurityManager();
        PlatformAuthCenterBearerFilter filter = new PlatformAuthCenterBearerFilter(validJwtDecoder(), authenticator,
                securityManager);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/system/users");
        request.addHeader("Authorization", "Bearer token-1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        RecordingFilterChain chain = new RecordingFilterChain(securityManager);

        filter.doFilter(request, response, chain);

        assertTrue(chain.invoked);
        assertTrue(chain.shiroContextBound);
        assertTrue(authenticator.authenticated);
        assertEquals("tenant_a", authenticator.token.getClaims().get("tenant_id"));
        assertEquals(1001L, authenticator.token.getClaims().get("user_id"));
        assertFalse(ThreadContext.getResources().containsKey(ThreadContext.SECURITY_MANAGER_KEY));
    }

    /**
     * 验证无效 Bearer Token 会返回 401，避免请求进入后续业务链路。
     *
     * @throws Exception 过滤器执行异常
     */
    @Test
    void shouldRejectInvalidBearerToken() throws Exception {
        RecordingSubjectAuthenticator authenticator = new RecordingSubjectAuthenticator();
        PlatformAuthCenterBearerFilter filter = new PlatformAuthCenterBearerFilter(token -> {
            throw new JwtException("bad token");
        }, authenticator, new DefaultSecurityManager());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/system/users");
        request.addHeader("Authorization", "Bearer invalid");
        MockHttpServletResponse response = new MockHttpServletResponse();
        RecordingFilterChain chain = new RecordingFilterChain(null);

        filter.doFilter(request, response, chain);

        assertFalse(chain.invoked);
        assertFalse(authenticator.authenticated);
        assertEquals(401, response.getStatus());
        assertEquals("AUTH_CENTER_TOKEN_INVALID", response.getHeader("X-ZHYC-Auth-Error"));
        assertTrue(response.getContentAsString().contains("认证中心令牌无效"));
        assertFalse(ThreadContext.getResources().containsKey(ThreadContext.SECURITY_MANAGER_KEY));
    }

    /**
     * 创建测试用 JWT 解码器。
     *
     * @return 可返回固定 Claims 的 JWT 解码器
     */
    private JwtDecoder validJwtDecoder() {
        return token -> Jwt.withTokenValue(token)
                .header("alg", "RS256")
                .issuedAt(Instant.parse("2026-06-26T00:00:00Z"))
                .expiresAt(Instant.parse("2026-06-26T01:00:00Z"))
                .claim("tenant_id", "tenant_a")
                .claim("user_id", 1001L)
                .claim("preferred_username", "admin")
                .claim("name", "管理员")
                .build();
    }

    /**
     * 记录 Shiro 登录适配器入参。
     */
    private static class RecordingSubjectAuthenticator extends PlatformShiroSubjectAuthenticator {

        /** 是否已执行认证。 */
        private boolean authenticated;
        /** 最近一次认证令牌。 */
        private PlatformAuthCenterAuthenticationToken token;

        @Override
        public void authenticate(PlatformAuthCenterAuthenticationToken token) {
            this.authenticated = true;
            this.token = token;
        }
    }

    /**
     * 记录过滤器链是否被继续调用。
     */
    private static class RecordingFilterChain implements FilterChain {

        /** 是否已进入后续过滤器链。 */
        private boolean invoked;
        /** 后续过滤器执行时是否已经绑定 Shiro 安全上下文。 */
        private boolean shiroContextBound;
        /** 期望绑定到当前请求线程的 Shiro 安全管理器。 */
        private final SecurityManager expectedSecurityManager;

        /**
         * 创建记录过滤器链。
         *
         * @param expectedSecurityManager 期望绑定的 Shiro 安全管理器
         */
        private RecordingFilterChain(SecurityManager expectedSecurityManager) {
            this.expectedSecurityManager = expectedSecurityManager;
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
            invoked = true;
            if (expectedSecurityManager != null) {
                assertSame(expectedSecurityManager, ThreadContext.getSecurityManager());
                shiroContextBound = true;
            }
        }
    }
}
