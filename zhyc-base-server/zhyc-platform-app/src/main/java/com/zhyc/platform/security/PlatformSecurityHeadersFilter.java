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
import java.io.IOException;

/**
 * 平台管理端安全响应头过滤器。
 *
 * <p>统一补充浏览器安全基线头，覆盖认证失败、权限拒绝和正常业务响应，避免点击劫持、
 * MIME 嗅探、敏感接口缓存和过宽浏览器能力暴露。</p>
 */
public class PlatformSecurityHeadersFilter implements Filter {

    /** 禁止浏览器 MIME 嗅探。 */
    private static final String HEADER_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";
    /** 禁止页面被第三方框架嵌入。 */
    private static final String HEADER_FRAME_OPTIONS = "X-Frame-Options";
    /** 控制来源地址外泄。 */
    private static final String HEADER_REFERRER_POLICY = "Referrer-Policy";
    /** 限制浏览器敏感能力。 */
    private static final String HEADER_PERMISSIONS_POLICY = "Permissions-Policy";
    /** 限制内容嵌入边界。 */
    private static final String HEADER_CONTENT_SECURITY_POLICY = "Content-Security-Policy";
    /** 禁止接口响应被共享缓存或浏览器持久化缓存。 */
    private static final String HEADER_CACHE_CONTROL = "Cache-Control";
    /** 兼容旧代理缓存控制。 */
    private static final String HEADER_PRAGMA = "Pragma";
    /** 兼容旧浏览器过期时间。 */
    private static final String HEADER_EXPIRES = "Expires";
    /** HTTPS 强制传输安全头。 */
    private static final String HEADER_STRICT_TRANSPORT_SECURITY = "Strict-Transport-Security";
    /** 反向代理协议头。 */
    private static final String HEADER_FORWARDED_PROTO = "X-Forwarded-Proto";

    /**
     * 补充安全响应头并继续后续过滤器链。
     *
     * @param request Servlet 请求
     * @param response Servlet 响应
     * @param chain 后续过滤器链
     * @throws IOException IO 异常
     * @throws ServletException Servlet 异常
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (response instanceof HttpServletResponse httpResponse) {
            applySecurityHeaders(request, httpResponse);
        }
        chain.doFilter(request, response);
    }

    private void applySecurityHeaders(ServletRequest request, HttpServletResponse response) {
        setHeaderIfMissing(response, HEADER_CONTENT_TYPE_OPTIONS, "nosniff");
        setHeaderIfMissing(response, HEADER_FRAME_OPTIONS, "DENY");
        setHeaderIfMissing(response, HEADER_REFERRER_POLICY, "no-referrer");
        setHeaderIfMissing(response, HEADER_PERMISSIONS_POLICY, "camera=(), microphone=(), geolocation=(), payment=()");
        setHeaderIfMissing(response, HEADER_CONTENT_SECURITY_POLICY,
                "frame-ancestors 'none'; base-uri 'self'; object-src 'none'");
        setHeaderIfMissing(response, HEADER_CACHE_CONTROL, "no-store, no-cache, must-revalidate, max-age=0");
        setHeaderIfMissing(response, HEADER_PRAGMA, "no-cache");
        setHeaderIfMissing(response, HEADER_EXPIRES, "0");
        if (isHttpsRequest(request)) {
            setHeaderIfMissing(response, HEADER_STRICT_TRANSPORT_SECURITY, "max-age=31536000; includeSubDomains");
        }
    }

    private boolean isHttpsRequest(ServletRequest request) {
        if (!(request instanceof HttpServletRequest httpRequest)) {
            return false;
        }
        return httpRequest.isSecure() || "https".equalsIgnoreCase(httpRequest.getHeader(HEADER_FORWARDED_PROTO));
    }

    private void setHeaderIfMissing(HttpServletResponse response, String name, String value) {
        if (!response.containsHeader(name)) {
            response.setHeader(name, value);
        }
    }
}
