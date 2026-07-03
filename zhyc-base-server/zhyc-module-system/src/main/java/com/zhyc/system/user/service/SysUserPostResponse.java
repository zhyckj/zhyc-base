/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user.service;

/**
 * 系统用户岗位响应对象。
 */
public class SysUserPostResponse {

    /** 岗位主键。 */
    private final Long postId;
    /** 岗位编码。 */
    private final String postCode;
    /** 岗位名称。 */
    private final String postName;
    /** 是否主岗位。 */
    private final boolean primaryFlag;

    /**
     * 创建系统用户岗位响应对象。
     *
     * @param postId 岗位主键
     * @param postCode 岗位编码
     * @param postName 岗位名称
     * @param primaryFlag 是否主岗位
     */
    public SysUserPostResponse(Long postId, String postCode, String postName, boolean primaryFlag) {
        this.postId = postId;
        this.postCode = postCode;
        this.postName = postName;
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
     * 返回岗位编码。
     *
     * @return 岗位编码
     */
    public String getPostCode() {
        return postCode;
    }

    /**
     * 返回岗位名称。
     *
     * @return 岗位名称
     */
    public String getPostName() {
        return postName;
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
