/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.permissionaudit;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.permissionaudit.domain.SysPermissionAudit;
import com.zhyc.system.permissionaudit.repository.SysPermissionAuditRepository;
import com.zhyc.system.permissionaudit.service.DefaultSysPermissionAuditService;
import com.zhyc.system.permissionaudit.service.SysPermissionAuditRecordCommand;
import com.zhyc.system.permissionaudit.service.SysPermissionAuditResponse;
import com.zhyc.system.permissionaudit.service.SysPermissionAuditService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 系统权限变更审计业务服务测试。
 */
class SysPermissionAuditServiceTest {

    /**
     * 验证权限变更审计记录会修剪租户、目标和变更前后内容。
     */
    @Test
    void shouldRecordNormalizedPermissionAudit() {
        RecordingPermissionAuditRepository repository = new RecordingPermissionAuditRepository();
        SysPermissionAuditService service = new DefaultSysPermissionAuditService(repository);

        service.record(new SysPermissionAuditRecordCommand(" tenant_a ", 1001L, " role ",
                " 10 ", " old ", " new ", " BIND_MENU "));

        assertEquals("tenant_a", repository.saved.getTenantId());
        assertEquals(1001L, repository.saved.getOperatorId());
        assertEquals("role", repository.saved.getTargetType());
        assertEquals("10", repository.saved.getTargetId());
        assertEquals("old", repository.saved.getBeforeValue());
        assertEquals("new", repository.saved.getAfterValue());
        assertEquals("BIND_MENU", repository.saved.getChangeType());
    }

    /**
     * 验证最近权限审计查询会限制查询条数并转换为响应对象。
     */
    @Test
    void shouldListRecentPermissionAuditsWithBoundedLimit() {
        RecordingPermissionAuditRepository repository = new RecordingPermissionAuditRepository();
        SysPermissionAuditService service = new DefaultSysPermissionAuditService(repository);

        List<SysPermissionAuditResponse> audits = service.listRecent(" tenant_a ", 1000);

        assertEquals("tenant_a", repository.lastTenantId);
        assertEquals(200, repository.lastLimit);
        assertEquals(1, audits.size());
        assertEquals("role", audits.get(0).getTargetType());
    }

    /**
     * 验证权限变更审计记录会拒绝不受支持的目标类型。
     */
    @Test
    void shouldRejectUnsupportedTargetTypeWhenRecording() {
        RecordingPermissionAuditRepository repository = new RecordingPermissionAuditRepository();
        SysPermissionAuditService service = new DefaultSysPermissionAuditService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.record(new SysPermissionAuditRecordCommand("tenant_a", 1001L, "department",
                        "10", "old", "new", "BIND_MENU")));

        assertEquals("ZHYC_SYS_PERMISSION_AUDIT_TARGET_TYPE_UNSUPPORTED", exception.getCode());
        assertEquals("权限审计目标类型不支持: department", exception.getMessage());
    }

    /**
     * 验证权限变更审计记录会拒绝不受支持的变更类型。
     */
    @Test
    void shouldRejectUnsupportedChangeTypeWhenRecording() {
        RecordingPermissionAuditRepository repository = new RecordingPermissionAuditRepository();
        SysPermissionAuditService service = new DefaultSysPermissionAuditService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.record(new SysPermissionAuditRecordCommand("tenant_a", 1001L, "role",
                        "10", "old", "new", "DELETE_ALL")));

        assertEquals("ZHYC_SYS_PERMISSION_AUDIT_CHANGE_TYPE_UNSUPPORTED", exception.getCode());
        assertEquals("权限审计变更类型不支持: DELETE_ALL", exception.getMessage());
    }

    /**
     * 测试用权限变更审计仓储。
     */
    private static class RecordingPermissionAuditRepository implements SysPermissionAuditRepository {

        /** 最近保存的权限变更审计。 */
        private SysPermissionAudit saved;
        /** 最近查询的租户业务编码。 */
        private String lastTenantId;
        /** 最近查询的条数上限。 */
        private int lastLimit;

        @Override
        public void save(SysPermissionAudit permissionAudit) {
            saved = permissionAudit;
        }

        @Override
        public List<SysPermissionAudit> findRecentByTenantId(String tenantId, int limit) {
            lastTenantId = tenantId;
            lastLimit = limit;
            return List.of(new SysPermissionAudit(1L, tenantId, 1001L, "role",
                    "10", "old", "new", "BIND_MENU", LocalDateTime.now()));
        }
    }
}
