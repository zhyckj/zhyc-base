/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.util.ThreadContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.io.IOException;
import java.util.Objects;

/**
 * 认证中心 Bearer Token 过滤器。
 *
 * <p>负责解析后台管理端请求中的 OAuth2/OIDC Bearer Token，并把认证中心 Claims 登录到 Shiro Subject。
 * 业务接口继续使用 Shiro 权限注解和本地用户权限模型。</p>
 */
public class PlatformAuthCenterBearerFilter implements Filter {

    /** Authorization 请求头名称。 */
    public static final String HEADER_AUTHORIZATION = "Authorization";
    /** 认证失败原因响应头名称。 */
    public static final String HEADER_AUTH_ERROR = "X-ZHYC-Auth-Error";
    /** 认证中心令牌无效错误码。 */
    public static final String ERROR_TOKEN_INVALID = "AUTH_CENTER_TOKEN_INVALID";
    /** Bearer Token 前缀。 */
    private static final String BEARER_PREFIX = "Bearer ";

    /** JWT 解码器，负责校验签名、有效期并解析 Claims。 */
    private final JwtDecoder jwtDecoder;
    /** Shiro Subject 登录适配器。 */
    private final PlatformShiroSubjectAuthenticator subjectAuthenticator;
    /** Shiro 安全管理器，用于为当前请求建立线程上下文。 */
    private final SecurityManager securityManager;

    /**
     * 创建认证中心 Bearer Token 过滤器。
     *
     * @param jwtDecoder JWT 解码器
     * @param subjectAuthenticator Shiro Subject 登录适配器
     * @param securityManager Shiro 安全管理器
     */
    public PlatformAuthCenterBearerFilter(JwtDecoder jwtDecoder,
            PlatformShiroSubjectAuthenticator subjectAuthenticator,
            SecurityManager securityManager) {
        this.jwtDecoder = Objects.requireNonNull(jwtDecoder, "JWT 解码器不能为空");
        this.subjectAuthenticator = Objects.requireNonNull(subjectAuthenticator, "Shiro Subject 登录适配器不能为空");
        this.securityManager = Objects.requireNonNull(securityManager, "Shiro 安全管理器不能为空");
    }

    /**
     * 执行 Bearer Token 认证。
     *
     * <p>未携带 Bearer Token 时透传；携带 Bearer Token 时必须先由 JWT 解码器校验，再登录 Shiro Subject。
     * Token 无效或本地用户绑定失败时返回 401，不进入后续业务链路。</p>
     *
     * @param request servlet 请求
     * @param response servlet 响应
     * @param chain 过滤器链
     * @throws IOException IO 异常
     * @throws ServletException servlet 异常
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        ThreadContext.bind(securityManager);
        try {
            if (!(request instanceof HttpServletRequest httpRequest)
                    || !(response instanceof HttpServletResponse httpResponse)) {
                chain.doFilter(request, response);
                return;
            }
            String bearerToken = resolveBearerToken(httpRequest);
            if (bearerToken == null) {
                chain.doFilter(request, response);
                return;
            }
            try {
                Jwt jwt = jwtDecoder.decode(bearerToken);
                subjectAuthenticator.authenticate(new PlatformAuthCenterAuthenticationToken(jwt.getClaims()));
                chain.doFilter(request, response);
            } catch (JwtException | AuthenticationException | IllegalArgumentException exception) {
                writeUnauthorized(httpResponse);
            }
        } finally {
            ThreadContext.unbindSubject();
            ThreadContext.unbindSecurityManager();
        }
    }

    /**
     * 从请求头中解析 Bearer Token。
     *
     * @param request HTTP 请求
     * @return Bearer Token；未携带时返回 null
     */
    private String resolveBearerToken(HttpServletRequest request) {
        String authorization = request.getHeader(HEADER_AUTHORIZATION);
        if (authorization == null || authorization.length() < BEARER_PREFIX.length()
                || !authorization.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            return null;
        }
        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        return token.isEmpty() ? null : token;
    }

    /**
     * 写入认证失败响应。
     *
     * @param response HTTP 响应
     * @throws IOException 响应写入失败
     */
    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader(HEADER_AUTH_ERROR, ERROR_TOKEN_INVALID);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().write("{\"code\":\"" + ERROR_TOKEN_INVALID
                + "\",\"message\":\"认证中心令牌无效\"}");
    }
}
