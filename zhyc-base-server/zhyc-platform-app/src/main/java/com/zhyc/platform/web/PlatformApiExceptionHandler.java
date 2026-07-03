/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.web;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.common.tenant.TenantContext;
import com.zhyc.platform.security.PlatformUserPrincipal;
import com.zhyc.system.exceptionlog.service.SysExceptionLogRecordCommand;
import com.zhyc.system.exceptionlog.service.SysExceptionLogService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 平台管理端 API 统一异常处理器。
 *
 * <p>负责把平台接口异常转换为统一响应，并将有效业务异常写入系统异常日志，供后台异常日志页面追踪。</p>
 */
@RestControllerAdvice
public class PlatformApiExceptionHandler {

    /** 无法从请求或登录主体识别租户时使用的平台默认租户，避免认证前异常丢失审计。 */
    private static final String DEFAULT_TENANT_ID = "zhyc-platform";

    /** 链路追踪请求头，优先承接前端、网关或反向代理传入的追踪编号。 */
    private static final String HEADER_TRACE_ID = "X-Trace-Id";

    /** 请求编号请求头，作为链路追踪编号的兼容来源。 */
    private static final String HEADER_REQUEST_ID = "X-Request-Id";

    /** 租户请求头，作为认证上下文缺失时的租户兜底来源。 */
    private static final String HEADER_TENANT_ID = "X-ZHYC-Tenant";

    /** 反向代理转发 IP 请求头。 */
    private static final String HEADER_FORWARDED_FOR = "X-Forwarded-For";

    /** 反向代理真实 IP 请求头。 */
    private static final String HEADER_REAL_IP = "X-Real-IP";

    /** 异常堆栈最大入库长度，避免单次异常占用过多存储。 */
    private static final int MAX_STACK_TRACE_LENGTH = 12000;

    /** 异常处理日志记录器，用于保留内部排障信息。 */
    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformApiExceptionHandler.class);

    /** 系统异常日志服务，用于持久化接口异常。 */
    private final SysExceptionLogService exceptionLogService;

    /**
     * 创建平台 API 统一异常处理器。
     *
     * @param exceptionLogService 系统异常日志服务
     */
    public PlatformApiExceptionHandler(SysExceptionLogService exceptionLogService) {
        this.exceptionLogService = Objects.requireNonNull(exceptionLogService, "系统异常日志服务不能为空");
    }

    /**
     * 处理业务异常并转换为统一失败响应。
     *
     * @param exception 业务异常
     * @param request 当前 HTTP 请求
     * @return 统一 API 失败响应
     */
    @ExceptionHandler(BusinessException.class)
    public ApiResult<Void> handleBusinessException(BusinessException exception, HttpServletRequest request) {
        recordException(exception, request);
        return ApiResult.fail(exception.getCode(), exception.getMessage());
    }

    /**
     * 处理参数异常并转换为统一失败响应。
     *
     * @param exception 参数异常
     * @param request 当前 HTTP 请求
     * @return 统一 API 失败响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResult<Void> handleIllegalArgumentException(IllegalArgumentException exception, HttpServletRequest request) {
        recordException(exception, request);
        return ApiResult.fail("REQUEST_INVALID", exception.getMessage());
    }

    /**
     * 处理缺少必填请求参数异常并转换为统一失败响应。
     *
     * @param exception 缺少请求参数异常
     * @param request 当前 HTTP 请求
     * @return 统一 API 失败响应
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ApiResult<Void> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException exception, HttpServletRequest request) {
        recordException(exception, request);
        return ApiResult.fail("REQUEST_INVALID", "缺少必要请求参数：" + exception.getParameterName());
    }

    /**
     * 处理不存在的静态资源或路由请求。
     *
     * <p>浏览器或前端代理可能持续请求旧 websocket、静态资源路径，该类请求不应按系统异常写入错误日志。</p>
     *
     * @param exception 资源不存在异常
     * @return 统一 API 失败响应
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ApiResult<Void> handleNoResourceFoundException(NoResourceFoundException exception) {
        return ApiResult.fail("RESOURCE_NOT_FOUND", "请求资源不存在");
    }

    /**
     * 处理 Shiro 授权异常并转换为统一权限拒绝响应。
     *
     * @param exception Shiro 授权异常
     * @param request 当前 HTTP 请求
     * @return 统一 API 失败响应
     */
    @ExceptionHandler(AuthorizationException.class)
    public ApiResult<Void> handleAuthorizationException(AuthorizationException exception, HttpServletRequest request) {
        recordException(exception, request);
        return ApiResult.fail("PERMISSION_DENIED", "没有权限访问该资源");
    }

    /**
     * 处理未知异常并转换为统一失败响应。
     *
     * @param exception 未知异常
     * @param request 当前 HTTP 请求
     * @return 统一 API 失败响应
     */
    @ExceptionHandler(Exception.class)
    public ApiResult<Void> handleUnexpectedException(Exception exception, HttpServletRequest request) {
        recordException(exception, request);
        LOGGER.error("平台 API 出现未处理异常", exception);
        return ApiResult.fail("SYSTEM_ERROR", "系统繁忙，请稍后重试");
    }

    /**
     * 记录接口异常日志。
     *
     * <p>异常日志是旁路审计能力，写入失败不得影响本次异常响应，避免异常处理器再次抛错。</p>
     *
     * @param exception 待记录异常
     * @param request 当前 HTTP 请求
     */
    private void recordException(Exception exception, HttpServletRequest request) {
        try {
            PlatformUserPrincipal principal = resolvePrincipal();
            exceptionLogService.record(new SysExceptionLogRecordCommand(
                    resolveTenantId(request, principal),
                    resolveTraceId(request),
                    principal == null ? null : principal.getUserId(),
                    principal == null ? null : principal.getUsername(),
                    resolveRequestUri(request),
                    resolveRequestMethod(request),
                    exception.getClass().getName(),
                    exception.getMessage(),
                    buildStackTrace(exception),
                    resolveClientIp(request)));
        } catch (RuntimeException logException) {
            LOGGER.warn("系统异常日志写入失败", logException);
        }
    }

    /**
     * 解析当前 Shiro 用户主体。
     *
     * @return 平台用户主体；未登录或上下文不可用时返回空
     */
    private PlatformUserPrincipal resolvePrincipal() {
        try {
            Object principal = SecurityUtils.getSubject().getPrincipal();
            return principal instanceof PlatformUserPrincipal platformPrincipal ? platformPrincipal : null;
        } catch (RuntimeException exception) {
            return null;
        }
    }

    /**
     * 解析异常所属租户。
     *
     * @param request 当前 HTTP 请求
     * @param principal 当前登录主体
     * @return 租户业务编码
     */
    private String resolveTenantId(HttpServletRequest request, PlatformUserPrincipal principal) {
        String tenantId = trimToNull(TenantContext.getTenantId());
        if (tenantId != null) {
            return tenantId;
        }
        if (principal != null && trimToNull(principal.getTenantId()) != null) {
            return principal.getTenantId().trim();
        }
        if (request != null) {
            tenantId = trimToNull(request.getParameter("tenantId"));
            if (tenantId != null) {
                return tenantId;
            }
            tenantId = trimToNull(request.getHeader(HEADER_TENANT_ID));
            if (tenantId != null) {
                return tenantId;
            }
        }
        return DEFAULT_TENANT_ID;
    }

    /**
     * 解析链路追踪编号。
     *
     * @param request 当前 HTTP 请求
     * @return 链路追踪编号；未携带时返回空
     */
    private String resolveTraceId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String traceId = trimToNull(request.getHeader(HEADER_TRACE_ID));
        return traceId == null ? trimToNull(request.getHeader(HEADER_REQUEST_ID)) : traceId;
    }

    /**
     * 解析请求地址。
     *
     * @param request 当前 HTTP 请求
     * @return 请求 URI
     */
    private String resolveRequestUri(HttpServletRequest request) {
        return request == null ? "unknown" : trimToDefault(request.getRequestURI(), "unknown");
    }

    /**
     * 解析请求方法。
     *
     * @param request 当前 HTTP 请求
     * @return HTTP 方法
     */
    private String resolveRequestMethod(HttpServletRequest request) {
        return request == null ? "UNKNOWN" : trimToDefault(request.getMethod(), "UNKNOWN");
    }

    /**
     * 解析客户端 IP。
     *
     * @param request 当前 HTTP 请求
     * @return 客户端 IP；无法识别时返回空
     */
    private String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwardedFor = trimToNull(request.getHeader(HEADER_FORWARDED_FOR));
        if (forwardedFor != null) {
            int commaIndex = forwardedFor.indexOf(',');
            return commaIndex >= 0 ? forwardedFor.substring(0, commaIndex).trim() : forwardedFor;
        }
        String realIp = trimToNull(request.getHeader(HEADER_REAL_IP));
        return realIp == null ? trimToNull(request.getRemoteAddr()) : realIp;
    }

    /**
     * 构建异常堆栈文本。
     *
     * @param exception 待记录异常
     * @return 截断后的异常堆栈
     */
    private String buildStackTrace(Exception exception) {
        StringWriter writer = new StringWriter();
        exception.printStackTrace(new PrintWriter(writer));
        String stackTrace = writer.toString();
        return stackTrace.length() <= MAX_STACK_TRACE_LENGTH
                ? stackTrace
                : stackTrace.substring(0, MAX_STACK_TRACE_LENGTH);
    }

    /**
     * 去除文本首尾空白，空文本统一返回空。
     *
     * @param value 原始文本
     * @return 标准化文本
     */
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * 去除文本首尾空白，并在空值时返回默认值。
     *
     * @param value 原始文本
     * @param defaultValue 默认值
     * @return 标准化文本
     */
    private String trimToDefault(String value, String defaultValue) {
        String trimmed = trimToNull(value);
        return trimmed == null ? defaultValue : trimmed;
    }
}
