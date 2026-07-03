/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user.controller;

/**
 * 用户岗位绑定项请求。
 */
public class UserPostBindItemRequest {

    /** 岗位主键。 */
    private Long postId;
    /** 是否主岗位。 */
    private boolean primaryFlag;

    /**
     * 返回岗位主键。
     *
     * @return 岗位主键
     */
    public Long getPostId() {
        return postId;
    }

    /**
     * 设置岗位主键。
     *
     * @param postId 岗位主键
     */
    public void setPostId(Long postId) {
        this.postId = postId;
    }

    /**
     * 返回是否主岗位。
     *
     * @return 是否主岗位
     */
    public boolean isPrimaryFlag() {
        return primaryFlag;
    }

    /**
     * 设置是否主岗位。
     *
     * @param primaryFlag 是否主岗位
     */
    public void setPrimaryFlag(boolean primaryFlag) {
        this.primaryFlag = primaryFlag;
    }
}
