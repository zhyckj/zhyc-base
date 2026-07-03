/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.loginlog;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.loginlog.domain.SysLoginLog;
import com.zhyc.system.loginlog.repository.SysLoginLogRepository;
import com.zhyc.system.loginlog.service.DefaultSysLoginLogService;
import com.zhyc.system.loginlog.service.SysLoginLogRecordCommand;
import com.zhyc.system.loginlog.service.SysLoginLogResponse;
import com.zhyc.system.loginlog.service.SysLoginLogService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 系统登录日志业务服务测试。
 */
class SysLoginLogServiceTest {

    /**
     * 验证登录日志记录会修剪租户、账号、登录类型和客户端信息。
     */
    @Test
    void shouldRecordNormalizedLoginLog() {
        RecordingLoginLogRepository repository = new RecordingLoginLogRepository();
        SysLoginLogService service = new DefaultSysLoginLogService(repository);

        service.record(new SysLoginLogRecordCommand(" tenant_a ", 1001L, " admin ",
                " password ", " success ", " 127.0.0.1 ", " Browser "));

        assertEquals("tenant_a", repository.saved.getTenantId());
        assertEquals(1001L, repository.saved.getUserId());
        assertEquals("admin", repository.saved.getUsername());
        assertEquals("password", repository.saved.getLoginType());
        assertEquals("success", repository.saved.getResult());
        assertEquals("127.0.0.1", repository.saved.getClientIp());
        assertEquals("Browser", repository.saved.getUserAgent());
    }

    /**
     * 验证最近登录日志查询会限制查询条数并转换为响应对象。
     */
    @Test
    void shouldListRecentLoginLogsWithBoundedLimit() {
        RecordingLoginLogRepository repository = new RecordingLoginLogRepository();
        SysLoginLogService service = new DefaultSysLoginLogService(repository);

        List<SysLoginLogResponse> logs = service.listRecent(" tenant_a ", 1000);

        assertEquals("tenant_a", repository.lastTenantId);
        assertEquals(200, repository.lastLimit);
        assertEquals(1, logs.size());
        assertEquals("admin", logs.get(0).getUsername());
    }

    /**
     * 验证登录日志记录会拒绝不受支持的登录结果。
     */
    @Test
    void shouldRejectUnsupportedLoginResultWhenRecording() {
        RecordingLoginLogRepository repository = new RecordingLoginLogRepository();
        SysLoginLogService service = new DefaultSysLoginLogService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.record(new SysLoginLogRecordCommand("tenant_a", 1001L, "admin",
                        "password", "unknown", "127.0.0.1", "Browser")));

        assertEquals("ZHYC_SYS_LOGIN_LOG_RESULT_UNSUPPORTED", exception.getCode());
        assertEquals("登录结果不支持: unknown", exception.getMessage());
    }

    /**
     * 测试用登录日志仓储。
     */
    private static class RecordingLoginLogRepository implements SysLoginLogRepository {

        /** 最近保存的登录日志。 */
        private SysLoginLog saved;
        /** 最近查询的租户业务编码。 */
        private String lastTenantId;
        /** 最近查询的条数上限。 */
        private int lastLimit;

        @Override
        public void save(SysLoginLog loginLog) {
            saved = loginLog;
        }

        @Override
        public List<SysLoginLog> findRecentByTenantId(String tenantId, int limit) {
            lastTenantId = tenantId;
            lastLimit = limit;
            return List.of(new SysLoginLog(1L, tenantId, 1001L, "admin", "password",
                    "success", "127.0.0.1", "Browser", LocalDateTime.now()));
        }
    }
}
