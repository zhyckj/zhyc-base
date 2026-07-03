/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.userpost;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.post.domain.SysPost;
import com.zhyc.system.post.repository.SysPostRepository;
import com.zhyc.system.user.domain.SysUserPost;
import com.zhyc.system.user.repository.SysUserPostRepository;
import com.zhyc.system.user.service.DefaultSysUserPostService;
import com.zhyc.system.user.service.SysUserPostBindCommand;
import com.zhyc.system.user.service.SysUserPostBindItem;
import com.zhyc.system.user.service.SysUserPostResponse;
import com.zhyc.system.user.service.SysUserPostService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统用户岗位绑定业务服务测试。
 */
class SysUserPostServiceTest {

    /**
     * 验证用户岗位服务查询时修剪租户编码，并将主岗位排在前面。
     */
    @Test
    void shouldListUserPostsWithPrimaryPostFirst() {
        RecordingUserPostRepository repository = new RecordingUserPostRepository();
        SysUserPostService service = new DefaultSysUserPostService(repository, new RecordingPostRepository());

        List<SysUserPostResponse> posts = service.listUserPosts(" tenant_a ", 1001L);

        assertEquals("tenant_a", repository.lastTenantId);
        assertEquals(1001L, repository.lastUserId);
        assertEquals(2, posts.size());
        assertEquals(2L, posts.get(0).getPostId());
        assertTrue(posts.get(0).isPrimaryFlag());
        assertEquals(1L, posts.get(1).getPostId());
        assertFalse(posts.get(1).isPrimaryFlag());
    }

    /**
     * 验证用户岗位绑定会去重、过滤非法岗位主键，并只保留第一个主岗位。
     */
    @Test
    void shouldReplaceUserPostsWithNormalizedBindings() {
        RecordingUserPostRepository repository = new RecordingUserPostRepository();
        SysUserPostService service = new DefaultSysUserPostService(repository, new RecordingPostRepository());

        service.bindUserPosts(new SysUserPostBindCommand(" tenant_a ", 1001L, List.of(
                new SysUserPostBindItem(3L, false),
                new SysUserPostBindItem(2L, true),
                new SysUserPostBindItem(2L, true),
                new SysUserPostBindItem(0L, true)
        )));

        assertEquals("tenant_a", repository.lastTenantId);
        assertEquals(1001L, repository.lastUserId);
        assertEquals(2, repository.lastBindings.size());
        assertEquals(3L, repository.lastBindings.get(0).getPostId());
        assertFalse(repository.lastBindings.get(0).isPrimaryFlag());
        assertEquals(2L, repository.lastBindings.get(1).getPostId());
        assertTrue(repository.lastBindings.get(1).isPrimaryFlag());
    }

    /**
     * 验证用户岗位绑定拒绝当前租户不存在的岗位，并且失败时不替换旧绑定。
     */
    @Test
    void shouldRejectUnknownTenantPostBeforeReplacingBindings() {
        RecordingUserPostRepository repository = new RecordingUserPostRepository();
        SysUserPostService service = new DefaultSysUserPostService(repository, new RecordingPostRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.bindUserPosts(new SysUserPostBindCommand(" tenant_a ", 1001L, List.of(
                        new SysUserPostBindItem(2L, true),
                        new SysUserPostBindItem(999L, false)
                ))));

        assertEquals("ZHYC_SYSTEM_ARGUMENT_INVALID", exception.getCode());
        assertEquals("岗位主键不属于当前租户：999", exception.getMessage());
        assertEquals(0, repository.replaceCount);
    }

    /**
     * 测试用系统岗位仓储。
     */
    private static class RecordingPostRepository implements SysPostRepository {

        /** 最近一次查询岗位的租户业务编码。 */
        private String lastTenantId;
        /** 最近一次查询岗位的组织主键。 */
        private Long lastOrgId;

        @Override
        public List<SysPost> findByTenantIdAndOrgId(String tenantId, Long orgId) {
            lastTenantId = tenantId;
            lastOrgId = orgId;
            return List.of(
                    new SysPost(2L, tenantId, 10L, "PM", "项目经理", 1,
                            "enabled", LocalDateTime.now(), LocalDateTime.now()),
                    new SysPost(3L, tenantId, 10L, "DEV", "开发工程师", 2,
                            "enabled", LocalDateTime.now(), LocalDateTime.now())
            );
        }

        @Override
        public void insert(SysPost post) {
            throw new AssertionError("用户岗位绑定测试不应新增岗位");
        }

        @Override
        public void update(SysPost post) {
            throw new AssertionError("用户岗位绑定测试不应更新岗位");
        }

        @Override
        public void updateStatus(String tenantId, Long postId, String status) {
            throw new AssertionError("用户岗位绑定测试不应更新岗位状态");
        }

        @Override
        public void deleteByTenantIdAndId(String tenantId, Long postId) {
            throw new AssertionError("用户岗位绑定测试不应删除岗位");
        }
    }

    /**
     * 测试用系统用户岗位仓储。
     */
    private static class RecordingUserPostRepository implements SysUserPostRepository {

        /** 最近一次操作的租户业务编码。 */
        private String lastTenantId;
        /** 最近一次操作的用户主键。 */
        private Long lastUserId;
        /** 最近一次写入的岗位绑定列表。 */
        private List<SysUserPost> lastBindings = new ArrayList<>();
        /** 替换用户岗位绑定的调用次数。 */
        private int replaceCount;

        @Override
        public List<SysUserPost> findByTenantIdAndUserId(String tenantId, Long userId) {
            lastTenantId = tenantId;
            lastUserId = userId;
            return List.of(
                    new SysUserPost(1L, tenantId, userId, 1L, "DEV", "开发工程师", false,
                            LocalDateTime.now()),
                    new SysUserPost(2L, tenantId, userId, 2L, "PM", "项目经理", true,
                            LocalDateTime.now())
            );
        }

        @Override
        public void replaceUserPosts(String tenantId, Long userId, List<SysUserPost> bindings) {
            lastTenantId = tenantId;
            lastUserId = userId;
            lastBindings = bindings;
            replaceCount++;
        }
    }
}
