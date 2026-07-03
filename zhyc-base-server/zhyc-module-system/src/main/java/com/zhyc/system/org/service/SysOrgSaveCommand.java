/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.org.service;

/**
 * 系统组织机构保存命令。
 */
public class SysOrgSaveCommand {

    /** 组织主键，新增时为空。 */
    private final Long orgId;
    /** 租户业务编码。 */
    private final String tenantId;
    /** 父级组织主键。 */
    private final Long parentId;
    /** 组织编码。 */
    private final String orgCode;
    /** 组织名称。 */
    private final String orgName;
    /** 负责人用户主键。 */
    private final Long leaderUserId;
    /** 排序号。 */
    private final Integer sortOrder;
    /** 组织状态。 */
    private final String status;

    public SysOrgSaveCommand(Long orgId, String tenantId, Long parentId, String orgCode, String orgName,
                             Long leaderUserId, Integer sortOrder, String status) {
        this.orgId = orgId;
        this.tenantId = tenantId;
        this.parentId = parentId;
        this.orgCode = orgCode;
        this.orgName = orgName;
        this.leaderUserId = leaderUserId;
        this.sortOrder = sortOrder;
        this.status = status;
    }

    public Long getOrgId() {
        return orgId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public Long getParentId() {
        return parentId;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public String getOrgName() {
        return orgName;
    }

    public Long getLeaderUserId() {
        return leaderUserId;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public String getStatus() {
        return status;
    }
}
