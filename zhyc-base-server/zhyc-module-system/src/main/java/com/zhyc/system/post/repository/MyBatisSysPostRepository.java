/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.post.repository;

import com.zhyc.system.post.domain.SysPost;
import com.zhyc.system.post.mapper.SysPostMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * 基于 MyBatis 的系统岗位仓储实现。
 */
@Repository
public class MyBatisSysPostRepository implements SysPostRepository {

    /** 系统岗位 Mapper。 */
    private final SysPostMapper postMapper;

    /**
     * 创建系统岗位仓储实现。
     *
     * @param postMapper 系统岗位 Mapper
     */
    public MyBatisSysPostRepository(SysPostMapper postMapper) {
        this.postMapper = Objects.requireNonNull(postMapper, "系统岗位 Mapper 不能为空");
    }

    @Override
    public List<SysPost> findByTenantIdAndOrgId(String tenantId, Long orgId) {
        return postMapper.selectByTenantIdAndOrgId(tenantId, orgId);
    }

    @Override
    public void insert(SysPost post) {
        postMapper.insert(post);
    }

    @Override
    public void update(SysPost post) {
        postMapper.update(post);
    }

    @Override
    public void updateStatus(String tenantId, Long postId, String status) {
        postMapper.updateStatus(tenantId, postId, status);
    }

    @Override
    public void deleteByTenantIdAndId(String tenantId, Long postId) {
        postMapper.deleteUserPostsByTenantAndPost(tenantId, postId);
        postMapper.deleteByTenantIdAndId(tenantId, postId);
    }
}
