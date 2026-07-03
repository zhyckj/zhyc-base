/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user.service;

import java.util.List;

/**
 * 用户岗位绑定命令。
 */
public class SysUserPostBindCommand {

    /** 租户业务编码。 */
    private final String tenantId;
    /** 用户主键。 */
    private final Long userId;
    /** 岗位绑定项列表。 */
    private final List<SysUserPostBindItem> posts;

    /**
     * 创建用户岗位绑定命令。
     *
     * @param tenantId 租户业务编码
     * @param userId 用户主键
     * @param posts 岗位绑定项列表
     */
    public SysUserPostBindCommand(String tenantId, Long userId, List<SysUserPostBindItem> posts) {
        this.tenantId = tenantId;
        this.userId = userId;
        this.posts = posts;
    }

    /**
     * 返回租户业务编码。
     *
     * @return 租户业务编码
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * 返回用户主键。
     *
     * @return 用户主键
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 返回岗位绑定项列表。
     *
     * @return 岗位绑定项列表
     */
    public List<SysUserPostBindItem> getPosts() {
        return posts;
    }
}
