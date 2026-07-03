/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.post.service;

import java.util.List;

/**
 * 系统岗位业务服务。
 */
public interface SysPostService {

    /**
     * 查询租户内岗位列表。
     *
     * @param tenantId 租户业务编码
     * @param orgId 所属组织主键，为空时查询租户内全部岗位
     * @return 岗位响应列表
     */
    List<SysPostResponse> listPosts(String tenantId, Long orgId);

    void savePost(SysPostSaveCommand command);

    void updateStatus(String tenantId, Long postId, String status);

    void deletePost(String tenantId, Long postId);
}
