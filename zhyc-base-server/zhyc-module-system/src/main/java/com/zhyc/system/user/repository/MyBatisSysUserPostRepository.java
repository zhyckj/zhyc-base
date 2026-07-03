/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user.repository;

import com.zhyc.system.user.domain.SysUserPost;
import com.zhyc.system.user.mapper.SysUserPostMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * 基于 MyBatis 的系统用户岗位仓储实现。
 */
@Repository
public class MyBatisSysUserPostRepository implements SysUserPostRepository {

    /** 系统用户岗位 Mapper。 */
    private final SysUserPostMapper userPostMapper;

    /**
     * 创建系统用户岗位仓储实现。
     *
     * @param userPostMapper 系统用户岗位 Mapper
     */
    public MyBatisSysUserPostRepository(SysUserPostMapper userPostMapper) {
        this.userPostMapper = Objects.requireNonNull(userPostMapper, "系统用户岗位 Mapper 不能为空");
    }

    @Override
    public List<SysUserPost> findByTenantIdAndUserId(String tenantId, Long userId) {
        return userPostMapper.selectByTenantIdAndUserId(tenantId, userId);
    }

    @Override
    public void replaceUserPosts(String tenantId, Long userId, List<SysUserPost> bindings) {
        userPostMapper.deleteByTenantIdAndUserId(tenantId, userId);
        for (SysUserPost binding : bindings) {
            userPostMapper.insertUserPost(tenantId, userId, binding.getPostId(), binding.isPrimaryFlag());
        }
    }
}
