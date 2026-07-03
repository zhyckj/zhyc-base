/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user.repository;

import com.zhyc.system.user.domain.SysUserPost;

import java.util.List;

/**
 * 系统用户岗位仓储接口。
 */
public interface SysUserPostRepository {

    /**
     * 查询租户内指定用户的岗位绑定。
     *
     * @param tenantId 租户业务编码
     * @param userId 用户主键
     * @return 用户岗位绑定列表
     */
    List<SysUserPost> findByTenantIdAndUserId(String tenantId, Long userId);

    /**
     * 替换租户内指定用户的岗位绑定。
     *
     * @param tenantId 租户业务编码
     * @param userId 用户主键
     * @param bindings 岗位绑定列表
     */
    void replaceUserPosts(String tenantId, Long userId, List<SysUserPost> bindings);
}
