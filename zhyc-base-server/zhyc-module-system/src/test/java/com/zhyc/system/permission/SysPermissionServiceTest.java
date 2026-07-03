/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.permission;

import com.zhyc.system.permission.repository.SysPermissionRepository;
import com.zhyc.system.permission.service.DefaultSysPermissionService;
import com.zhyc.system.permission.service.SysPermissionService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 系统权限业务服务测试。
 */
class SysPermissionServiceTest {

    /**
     * 验证用户权限查询会按租户和用户读取权限，并去重、过滤空权限标识。
     */
    @Test
    void shouldListDistinctUserPermissionsByTenantAndUser() {
        RecordingPermissionRepository repository = new RecordingPermissionRepository();
        SysPermissionService service = new DefaultSysPermissionService(repository);

        List<String> permissions = service.listUserPermissions("tenant_a", 1001L);

        assertEquals("tenant_a", repository.lastTenantId);
        assertEquals(1001L, repository.lastUserId);
        assertEquals(List.of("system:user:query", "system:user:create"), permissions);
    }

    /**
     * 测试用权限仓储。
     */
    private static class RecordingPermissionRepository implements SysPermissionRepository {

        /** 最近一次查询的租户业务编码。 */
        private String lastTenantId;
        /** 最近一次查询的用户 ID。 */
        private Long lastUserId;

        @Override
        public List<String> findGrantedPermissions(String tenantId, Long userId) {
            lastTenantId = tenantId;
            lastUserId = userId;
            return List.of("system:user:query", "", "system:user:create", "system:user:query", " ");
        }
    }
}
