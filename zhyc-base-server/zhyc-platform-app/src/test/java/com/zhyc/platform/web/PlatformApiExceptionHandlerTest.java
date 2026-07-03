/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.web;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.exceptionlog.service.SysExceptionLogRecordCommand;
import com.zhyc.system.exceptionlog.service.SysExceptionLogResponse;
import com.zhyc.system.exceptionlog.service.SysExceptionLogService;
import java.util.List;
import org.apache.shiro.authz.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 平台 API 异常处理器测试。
 */
class PlatformApiExceptionHandlerTest {

    /**
     * 验证业务异常会转换为统一失败响应，避免接口直接暴露 Java 异常类型。
     */
    @Test
    void shouldConvertBusinessExceptionToApiResult() {
        RecordingExceptionLogService exceptionLogService = new RecordingExceptionLogService();
        PlatformApiExceptionHandler handler = new PlatformApiExceptionHandler(exceptionLogService);
        MockHttpServletRequest request = buildRequest();

        ApiResult<Void> result = handler.handleBusinessException(
                new BusinessException("MODULE_DEPENDENCY_CONFLICT", "模块 common 仍被启用模块依赖：[system]"),
                request);

        assertFalse(result.isSuccess());
        assertEquals("MODULE_DEPENDENCY_CONFLICT", result.getCode());
        assertEquals("模块 common 仍被启用模块依赖：[system]", result.getMessage());
        assertEquals(BusinessException.class.getName(), exceptionLogService.saved.getExceptionName());
        assertEquals("/system/modules", exceptionLogService.saved.getRequestUri());
    }

    /**
     * 验证参数异常会转换为统一失败响应，避免调用方看到框架或 Java 异常类型。
     */
    @Test
    void shouldConvertIllegalArgumentExceptionToApiResult() {
        RecordingExceptionLogService exceptionLogService = new RecordingExceptionLogService();
        PlatformApiExceptionHandler handler = new PlatformApiExceptionHandler(exceptionLogService);

        ApiResult<Void> result = handler.handleIllegalArgumentException(
                new IllegalArgumentException("租户编码不能为空"), buildRequest());

        assertFalse(result.isSuccess());
        assertEquals("REQUEST_INVALID", result.getCode());
        assertEquals("租户编码不能为空", result.getMessage());
        assertEquals(IllegalArgumentException.class.getName(), exceptionLogService.saved.getExceptionName());
    }

    /**
     * 验证 Shiro 授权异常会转换为统一权限拒绝响应，避免暴露安全框架异常类型。
     */
    @Test
    void shouldConvertShiroAuthorizationExceptionToApiResult() {
        RecordingExceptionLogService exceptionLogService = new RecordingExceptionLogService();
        PlatformApiExceptionHandler handler = new PlatformApiExceptionHandler(exceptionLogService);

        ApiResult<Void> result = handler.handleAuthorizationException(
                new UnauthorizedException("Subject does not have permission [system:user:delete]"), buildRequest());

        assertFalse(result.isSuccess());
        assertEquals("PERMISSION_DENIED", result.getCode());
        assertEquals("没有权限访问该资源", result.getMessage());
        assertEquals(UnauthorizedException.class.getName(), exceptionLogService.saved.getExceptionName());
    }

    /**
     * 验证未知运行时异常会转换为统一失败响应，避免接口直接暴露框架异常或堆栈信息。
     */
    @Test
    void shouldConvertUnexpectedExceptionToApiResult() {
        RecordingExceptionLogService exceptionLogService = new RecordingExceptionLogService();
        PlatformApiExceptionHandler handler = new PlatformApiExceptionHandler(exceptionLogService);

        ApiResult<Void> result = handler.handleUnexpectedException(
                new IllegalStateException("数据库连接池不可用"), buildRequest());

        assertFalse(result.isSuccess());
        assertEquals("SYSTEM_ERROR", result.getCode());
        assertEquals("系统繁忙，请稍后重试", result.getMessage());
        assertEquals(IllegalStateException.class.getName(), exceptionLogService.saved.getExceptionName());
        assertEquals("数据库连接池不可用", exceptionLogService.saved.getMessage());
        assertNotNull(exceptionLogService.saved.getStackTrace());
        assertEquals("127.0.0.1", exceptionLogService.saved.getClientIp());
    }

    /**
     * 验证异常日志服务自身失败时不影响原错误响应。
     */
    @Test
    void shouldKeepApiResponseWhenExceptionLogRecordFails() {
        PlatformApiExceptionHandler handler = new PlatformApiExceptionHandler(new FailingExceptionLogService());

        ApiResult<Void> result = handler.handleUnexpectedException(
                new IllegalStateException("数据库连接池不可用"), buildRequest());

        assertFalse(result.isSuccess());
        assertEquals("SYSTEM_ERROR", result.getCode());
        assertEquals("系统繁忙，请稍后重试", result.getMessage());
    }

    /**
     * 创建测试用 HTTP 请求。
     *
     * @return 带租户、链路和客户端信息的请求
     */
    private MockHttpServletRequest buildRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/system/modules");
        request.setParameter("tenantId", "tenant_a");
        request.addHeader("X-Trace-Id", "trace-1");
        request.addHeader("X-Forwarded-For", "127.0.0.1, 10.0.0.1");
        return request;
    }

    /**
     * 测试用异常日志服务，记录最近一次写入命令。
     */
    private static class RecordingExceptionLogService implements SysExceptionLogService {

        /** 最近一次保存的异常日志命令。 */
        private SysExceptionLogRecordCommand saved;

        @Override
        public void record(SysExceptionLogRecordCommand command) {
            this.saved = command;
        }

        @Override
        public List<SysExceptionLogResponse> listRecent(String tenantId, int limit) {
            assertNull(tenantId);
            return List.of();
        }
    }

    /**
     * 测试用异常日志服务，模拟审计落库失败。
     */
    private static class FailingExceptionLogService implements SysExceptionLogService {

        @Override
        public void record(SysExceptionLogRecordCommand command) {
            throw new IllegalStateException("审计库不可用");
        }

        @Override
        public List<SysExceptionLogResponse> listRecent(String tenantId, int limit) {
            return List.of();
        }
    }
}
