/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.org;

import com.zhyc.system.org.domain.SysOrg;
import com.zhyc.system.org.repository.SysOrgRepository;
import com.zhyc.system.org.service.DefaultSysOrgService;
import com.zhyc.system.org.service.SysOrgService;
import com.zhyc.system.org.service.SysOrgTreeNode;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 系统组织机构业务服务测试。
 */
class SysOrgServiceTest {

    /**
     * 验证组织服务按租户构建组织树，并按排序号排列同级组织。
     */
    @Test
    void shouldBuildTenantOrgTreeByParentAndSortOrder() {
        RecordingOrgRepository repository = new RecordingOrgRepository();
        SysOrgService service = new DefaultSysOrgService(repository);

        List<SysOrgTreeNode> tree = service.listOrgTree(" tenant_a ");

        assertEquals("tenant_a", repository.lastTenantId);
        assertEquals(1, tree.size());
        assertEquals("总部", tree.get(0).getOrgName());
        assertEquals(2, tree.get(0).getChildren().size());
        assertEquals("研发部", tree.get(0).getChildren().get(0).getOrgName());
        assertEquals("销售部", tree.get(0).getChildren().get(1).getOrgName());
    }

    /**
     * 测试用组织机构仓储。
     */
    private static class RecordingOrgRepository implements SysOrgRepository {

        /** 最近一次查询的租户业务编码。 */
        private String lastTenantId;

        @Override
        public List<SysOrg> findByTenantId(String tenantId) {
            lastTenantId = tenantId;
            return List.of(
                    new SysOrg(1L, tenantId, null, "0", "HQ", "总部",
                            null, 1, "enabled", LocalDateTime.now(), LocalDateTime.now()),
                    new SysOrg(3L, tenantId, 1L, "0,1", "SALES", "销售部",
                            null, 2, "enabled", LocalDateTime.now(), LocalDateTime.now()),
                    new SysOrg(2L, tenantId, 1L, "0,1", "RD", "研发部",
                            null, 1, "enabled", LocalDateTime.now(), LocalDateTime.now()));
        }

        @Override
        public void insert(SysOrg org) {
            throw new AssertionError("组织树查询测试不应新增组织");
        }

        @Override
        public void update(SysOrg org) {
            throw new AssertionError("组织树查询测试不应更新组织");
        }

        @Override
        public void updateStatus(String tenantId, Long orgId, String status) {
            throw new AssertionError("组织树查询测试不应更新组织状态");
        }

        @Override
        public void deleteByTenantIdAndId(String tenantId, Long orgId) {
            throw new AssertionError("组织树查询测试不应删除组织");
        }
    }
}
