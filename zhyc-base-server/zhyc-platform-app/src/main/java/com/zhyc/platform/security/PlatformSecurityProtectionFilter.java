/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.security;

import com.zhyc.system.securityprotection.service.SecurityEventRecordCommand;
import com.zhyc.system.securityprotection.service.SysSecurityProtectionService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * 平台运行时安全防护过滤器。
 *
 * <p>负责在请求进入业务 Controller 前判断来源 IP 是否被封禁，并在请求结束后记录轻量安全事件。</p>
 */
public class PlatformSecurityProtectionFilter implements Filter {

    /** 日志对象。 */
    private static final Logger log = LoggerFactory.getLogger(PlatformSecurityProtectionFilter.class);
    /** 当前租户请求头。 */
    private static final String HEADER_TENANT_ID = "X-ZHYC-Tenant-Id";
    /** 兼容旧版租户请求头。 */
    private static final String LEGACY_HEADER_TENANT = "X-ZHYC-Tenant";
    /** 当前用户请求头。 */
    private static final String HEADER_USER_ID = "X-ZHYC-User-Id";
    /** 代理转发来源请求头。 */
    private static final String HEADER_FORWARDED_FOR = "X-Forwarded-For";

    /** 系统安全防护中心业务服务。 */
    private final SysSecurityProtectionService securityProtectionService;

    /**
     * 创建平台运行时安全防护过滤器。
     *
     * @param securityProtectionService 系统安全防护中心业务服务
     */
    public PlatformSecurityProtectionFilter(SysSecurityProtectionService securityProtectionService) {
        this.securityProtectionService = Objects.requireNonNull(securityProtectionService,
                "系统安全防护中心业务服务不能为空");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest httpRequest)
                || !(response instanceof HttpServletResponse httpResponse)
                || shouldSkip(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }
        String tenantId = resolveTenantId(httpRequest);
        String sourceIp = resolveSourceIp(httpRequest);
        if (!StringUtils.hasText(tenantId) || !StringUtils.hasText(sourceIp)) {
            chain.doFilter(request, response);
            return;
        }
        if (isBlocked(tenantId, sourceIp)) {
            recordEvent(tenantId, sourceIp, httpRequest, "ip_block", "high", "block", "blocked",
                    "来源 IP 已被安全防护中心封禁");
            writeBlockedResponse(httpResponse);
            return;
        }
        try {
            chain.doFilter(request, response);
            recordEvent(tenantId, sourceIp, httpRequest, "request", resolveEventLevel(httpResponse),
                    "observe", resolveResult(httpResponse), "后台请求已记录");
        } catch (IOException | ServletException | RuntimeException ex) {
            recordEvent(tenantId, sourceIp, httpRequest, "request", "medium", "observe", "failed",
                    "后台请求处理异常");
            throw ex;
        }
    }

    private boolean shouldSkip(HttpServletRequest request) {
        String method = request.getMethod();
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }
        String path = request.getRequestURI();
        return path == null
                || path.startsWith("/assets/")
                || path.startsWith("/static/")
                || path.startsWith("/favicon")
                || path.startsWith("/error")
                || path.startsWith("/actuator/health")
                || path.startsWith("/system/security-protection");
    }

    private boolean isBlocked(String tenantId, String sourceIp) {
        try {
            return securityProtectionService.isIpBlocked(tenantId, sourceIp, LocalDateTime.now());
        } catch (RuntimeException ex) {
            log.warn("安全防护封禁判定失败，已放行请求，tenantId={}, sourceIp={}", tenantId, sourceIp, ex);
            return false;
        }
    }

    private void recordEvent(String tenantId, String sourceIp, HttpServletRequest request, String eventType,
                             String eventLevel, String action, String result, String message) {
        try {
            securityProtectionService.recordEvent(new SecurityEventRecordCommand(tenantId, eventType, eventLevel,
                    sourceIp, resolveUserId(request), null, request.getRequestURI(), request.getMethod(), action,
                    result, message, LocalDateTime.now()));
        } catch (RuntimeException ex) {
            log.warn("安全防护事件记录失败，tenantId={}, sourceIp={}, path={}", tenantId, sourceIp,
                    request.getRequestURI(), ex);
        }
    }

    private String resolveTenantId(HttpServletRequest request) {
        String tenantId = request.getHeader(HEADER_TENANT_ID);
        if (!StringUtils.hasText(tenantId)) {
            tenantId = request.getHeader(LEGACY_HEADER_TENANT);
        }
        if (!StringUtils.hasText(tenantId)) {
            tenantId = request.getParameter("tenantId");
        }
        return StringUtils.hasText(tenantId) ? tenantId.trim() : null;
    }

    private Long resolveUserId(HttpServletRequest request) {
        String userId = request.getHeader(HEADER_USER_ID);
        if (!StringUtils.hasText(userId)) {
            return null;
        }
        try {
            return Long.parseLong(userId.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String resolveSourceIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader(HEADER_FORWARDED_FOR);
        if (StringUtils.hasText(forwardedFor)) {
            String firstIp = forwardedFor.split(",", -1)[0].trim();
            if (StringUtils.hasText(firstIp)) {
                return firstIp;
            }
        }
        return request.getRemoteAddr();
    }

    private String resolveEventLevel(HttpServletResponse response) {
        int status = response.getStatus();
        if (status >= 500) {
            return "high";
        }
        if (status >= 400) {
            return "medium";
        }
        return "low";
    }

    private String resolveResult(HttpServletResponse response) {
        int status = response.getStatus();
        if (status >= 500) {
            return "failed";
        }
        if (status >= 400) {
            return "denied";
        }
        return "allowed";
    }

    private void writeBlockedResponse(HttpServletResponse response) throws IOException {
        byte[] body = """
            {"success":false,"code":"ZHYC_SECURITY_IP_BLOCKED","message":"来源 IP 已被安全防护中心封禁","data":null}
            """.trim().getBytes(StandardCharsets.UTF_8);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        response.getOutputStream().write(body);
    }
}
