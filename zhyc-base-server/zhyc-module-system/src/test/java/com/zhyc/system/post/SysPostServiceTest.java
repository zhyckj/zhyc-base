/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.post;

import com.zhyc.system.post.domain.SysPost;
import com.zhyc.system.post.repository.SysPostRepository;
import com.zhyc.system.post.service.DefaultSysPostService;
import com.zhyc.system.post.service.SysPostResponse;
import com.zhyc.system.post.service.SysPostService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 系统岗位业务服务测试。
 */
class SysPostServiceTest {

    /**
     * 验证岗位服务按租户和组织查询岗位，并按排序号排列。
     */
    @Test
    void shouldListTenantPostsByOrgAndSortOrder() {
        RecordingPostRepository repository = new RecordingPostRepository();
        SysPostService service = new DefaultSysPostService(repository);

        List<SysPostResponse> posts = service.listPosts(" tenant_a ", 10L);

        assertEquals("tenant_a", repository.lastTenantId);
        assertEquals(10L, repository.lastOrgId);
        assertEquals(2, posts.size());
        assertEquals("开发工程师", posts.get(0).getPostName());
        assertEquals("项目经理", posts.get(1).getPostName());
    }

    /**
     * 测试用系统岗位仓储。
     */
    private static class RecordingPostRepository implements SysPostRepository {

        /** 最近一次查询的租户业务编码。 */
        private String lastTenantId;
        /** 最近一次查询的组织主键。 */
        private Long lastOrgId;

        @Override
        public List<SysPost> findByTenantIdAndOrgId(String tenantId, Long orgId) {
            lastTenantId = tenantId;
            lastOrgId = orgId;
            return List.of(
                    new SysPost(2L, tenantId, orgId, "PM", "项目经理", 2, "enabled",
                            LocalDateTime.now(), LocalDateTime.now()),
                    new SysPost(1L, tenantId, orgId, "DEV", "开发工程师", 1, "enabled",
                            LocalDateTime.now(), LocalDateTime.now()));
        }

        @Override
        public void insert(SysPost post) {
            throw new AssertionError("岗位查询测试不应新增岗位");
        }

        @Override
        public void update(SysPost post) {
            throw new AssertionError("岗位查询测试不应更新岗位");
        }

        @Override
        public void updateStatus(String tenantId, Long postId, String status) {
            throw new AssertionError("岗位查询测试不应更新岗位状态");
        }

        @Override
        public void deleteByTenantIdAndId(String tenantId, Long postId) {
            throw new AssertionError("岗位查询测试不应删除岗位");
        }
    }
}
