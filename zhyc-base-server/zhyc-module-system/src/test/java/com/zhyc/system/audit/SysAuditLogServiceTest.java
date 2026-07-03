/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.audit;

import com.zhyc.common.audit.AuditEvent;
import com.zhyc.system.audit.domain.SysAuditLog;
import com.zhyc.system.audit.repository.SysAuditLogRepository;
import com.zhyc.system.audit.service.DefaultSysAuditLogService;
import com.zhyc.system.audit.service.SysAuditLogResponse;
import com.zhyc.system.audit.service.SysAuditLogService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 系统审计日志业务服务测试。
 */
class SysAuditLogServiceTest {

    /**
     * 验证审计服务会把公共审计事件转换为系统审计日志并写入租户审计表。
     */
    @Test
    void shouldRecordAuditEvent() {
        RecordingAuditLogRepository repository = new RecordingAuditLogRepository();
        SysAuditLogService service = new DefaultSysAuditLogService(repository);

        service.record(new AuditEvent(" tenant_a ", "1001", "ROLE_BIND_MENU",
                "sys_role:10", true, 1710000000000L, "绑定角色菜单"));

        assertEquals("tenant_a", repository.lastSaved.getTenantId());
        assertEquals(1001L, repository.lastSaved.getUserId());
        assertEquals("ROLE_BIND_MENU", repository.lastSaved.getAction());
        assertEquals("sys_role", repository.lastSaved.getTargetType());
        assertEquals("10", repository.lastSaved.getTargetId());
        assertEquals("success", repository.lastSaved.getResult());
        assertEquals("绑定角色菜单", repository.lastSaved.getDetail());
    }

    /**
     * 验证审计查询会按租户隔离并限制最近记录数量。
     */
    @Test
    void shouldListRecentAuditLogsByTenant() {
        RecordingAuditLogRepository repository = new RecordingAuditLogRepository();
        SysAuditLogService service = new DefaultSysAuditLogService(repository);

        List<SysAuditLogResponse> logs = service.listRecent(" tenant_a ", 50);

        assertEquals("tenant_a", repository.lastTenantId);
        assertEquals(50, repository.lastLimit);
        assertEquals(1, logs.size());
        assertEquals("ROLE_BIND_MENU", logs.get(0).getAction());
    }

    /**
     * 测试用系统审计日志仓储。
     */
    private static class RecordingAuditLogRepository implements SysAuditLogRepository {

        /** 最近一次保存的审计日志。 */
        private SysAuditLog lastSaved;
        /** 最近一次查询的租户业务编码。 */
        private String lastTenantId;
        /** 最近一次查询条数。 */
        private int lastLimit;

        @Override
        public void save(SysAuditLog auditLog) {
            lastSaved = auditLog;
        }

        @Override
        public List<SysAuditLog> findRecentByTenantId(String tenantId, int limit) {
            lastTenantId = tenantId;
            lastLimit = limit;
            return List.of(new SysAuditLog(1L, tenantId, 1001L, "admin",
                    "ROLE_BIND_MENU", "sys_role", "10", "success", "127.0.0.1",
                    "绑定角色菜单", LocalDateTime.now()));
        }
    }
}
