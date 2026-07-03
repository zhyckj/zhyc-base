/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user.service;

/**
 * 用户岗位绑定项。
 */
public class SysUserPostBindItem {

    /** 岗位主键。 */
    private final Long postId;
    /** 是否主岗位。 */
    private final boolean primaryFlag;

    /**
     * 创建用户岗位绑定项。
     *
     * @param postId 岗位主键
     * @param primaryFlag 是否主岗位
     */
    public SysUserPostBindItem(Long postId, boolean primaryFlag) {
        this.postId = postId;
        this.primaryFlag = primaryFlag;
    }

    /**
     * 返回岗位主键。
     *
     * @return 岗位主键
     */
    public Long getPostId() {
        return postId;
    }

    /**
     * 返回是否主岗位。
     *
     * @return 是否主岗位
     */
    public boolean isPrimaryFlag() {
        return primaryFlag;
    }
}
