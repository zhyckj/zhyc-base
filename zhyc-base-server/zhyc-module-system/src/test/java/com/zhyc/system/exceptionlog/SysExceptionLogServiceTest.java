/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.exceptionlog;

import com.zhyc.system.exceptionlog.domain.SysExceptionLog;
import com.zhyc.system.exceptionlog.repository.SysExceptionLogRepository;
import com.zhyc.system.exceptionlog.service.DefaultSysExceptionLogService;
import com.zhyc.system.exceptionlog.service.SysExceptionLogRecordCommand;
import com.zhyc.system.exceptionlog.service.SysExceptionLogResponse;
import com.zhyc.system.exceptionlog.service.SysExceptionLogService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 系统异常日志业务服务测试。
 */
class SysExceptionLogServiceTest {

    /**
     * 验证异常日志记录会修剪租户、链路、请求和异常信息。
     */
    @Test
    void shouldRecordNormalizedExceptionLog() {
        RecordingExceptionLogRepository repository = new RecordingExceptionLogRepository();
        SysExceptionLogService service = new DefaultSysExceptionLogService(repository);

        service.record(new SysExceptionLogRecordCommand(" tenant_a ", " trace-1 ", 1001L, " admin ",
                " /system/users ", " POST ", " java.lang.IllegalStateException ",
                " failed ", " stack ", " 127.0.0.1 "));

        assertEquals("tenant_a", repository.saved.getTenantId());
        assertEquals("trace-1", repository.saved.getTraceId());
        assertEquals(1001L, repository.saved.getUserId());
        assertEquals("admin", repository.saved.getUsername());
        assertEquals("/system/users", repository.saved.getRequestUri());
        assertEquals("POST", repository.saved.getRequestMethod());
        assertEquals("java.lang.IllegalStateException", repository.saved.getExceptionName());
        assertEquals("failed", repository.saved.getMessage());
        assertEquals("stack", repository.saved.getStackTrace());
        assertEquals("127.0.0.1", repository.saved.getClientIp());
    }

    /**
     * 验证最近异常日志查询会限制查询条数并转换为响应对象。
     */
    @Test
    void shouldListRecentExceptionLogsWithBoundedLimit() {
        RecordingExceptionLogRepository repository = new RecordingExceptionLogRepository();
        SysExceptionLogService service = new DefaultSysExceptionLogService(repository);

        List<SysExceptionLogResponse> logs = service.listRecent(" tenant_a ", 1000);

        assertEquals("tenant_a", repository.lastTenantId);
        assertEquals(200, repository.lastLimit);
        assertEquals(1, logs.size());
        assertEquals("java.lang.IllegalStateException", logs.get(0).getExceptionName());
    }

    /**
     * 测试用异常日志仓储。
     */
    private static class RecordingExceptionLogRepository implements SysExceptionLogRepository {

        /** 最近保存的异常日志。 */
        private SysExceptionLog saved;
        /** 最近查询的租户业务编码。 */
        private String lastTenantId;
        /** 最近查询的条数上限。 */
        private int lastLimit;

        @Override
        public void save(SysExceptionLog exceptionLog) {
            saved = exceptionLog;
        }

        @Override
        public List<SysExceptionLog> findRecentByTenantId(String tenantId, int limit) {
            lastTenantId = tenantId;
            lastLimit = limit;
            return List.of(new SysExceptionLog(1L, tenantId, "trace-1", 1001L, "admin",
                    "/system/users", "POST", "java.lang.IllegalStateException", "failed",
                    "stack", "127.0.0.1", LocalDateTime.now()));
        }
    }
}
