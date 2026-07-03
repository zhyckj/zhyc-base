/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.post.service;

/**
 * 系统岗位响应对象。
 */
public class SysPostResponse {

    /** 岗位主键。 */
    private final Long id;
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

    /**
     * 创建系统岗位响应对象。
     *
     * @param id 岗位主键
     * @param tenantId 租户业务编码
     * @param orgId 所属组织主键
     * @param postCode 岗位编码
     * @param postName 岗位名称
     * @param sortOrder 排序号
     * @param status 岗位状态
     */
    public SysPostResponse(Long id, String tenantId, Long orgId, String postCode, String postName,
                           Integer sortOrder, String status) {
        this.id = id;
        this.tenantId = tenantId;
        this.orgId = orgId;
        this.postCode = postCode;
        this.postName = postName;
        this.sortOrder = sortOrder;
        this.status = status;
    }

    /**
     * 返回岗位主键。
     *
     * @return 岗位主键
     */
    public Long getId() {
        return id;
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
     * 返回所属组织主键。
     *
     * @return 所属组织主键
     */
    public Long getOrgId() {
        return orgId;
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
     * 返回排序号。
     *
     * @return 排序号
     */
    public Integer getSortOrder() {
        return sortOrder;
    }

    /**
     * 返回岗位状态。
     *
     * @return 岗位状态
     */
    public String getStatus() {
        return status;
    }
}
