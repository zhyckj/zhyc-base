/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.post.service;

/**
 * 系统岗位保存命令。
 */
public class SysPostSaveCommand {

    /** 岗位主键，新增时为空。 */
    private final Long postId;
    /** 租户业务编码。 */
    private final String tenantId;
    /** 所属组织主键。 */
    private final Long orgId;
    /** 岗位编码。 */
    private final String postCode;
    /** 岗位名称。 */
    private final String postName;
    /** 排序号。 */
    private final Integer sortOrder;
    /** 岗位状态。 */
    private final String status;

    public SysPostSaveCommand(Long postId, String tenantId, Long orgId, String postCode, String postName,
                              Integer sortOrder, String status) {
        this.postId = postId;
        this.tenantId = tenantId;
        this.orgId = orgId;
        this.postCode = postCode;
        this.postName = postName;
        this.sortOrder = sortOrder;
        this.status = status;
    }

    public Long getPostId() {
        return postId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public Long getOrgId() {
        return orgId;
    }

    public String getPostCode() {
        return postCode;
    }

    public String getPostName() {
        return postName;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public String getStatus() {
        return status;
    }
}
