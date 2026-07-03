/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.org.service;

import java.util.List;

/**
 * 系统组织机构业务服务。
 */
public interface SysOrgService {

    /**
     * 查询租户组织机构树。
     *
     * @param tenantId 租户业务编码
     * @return 组织机构树节点列表
     */
    List<SysOrgTreeNode> listOrgTree(String tenantId);

    /**
     * 新增或编辑系统组织机构。
     *
     * @param command 组织保存命令
     */
    void saveOrg(SysOrgSaveCommand command);

    /**
     * 调整系统组织机构状态。
     *
     * @param tenantId 租户业务编码
     * @param orgId 组织主键
     * @param status 组织状态
     */
    void updateStatus(String tenantId, Long orgId, String status);

    /**
     * 删除系统组织机构。
     *
     * @param tenantId 租户业务编码
     * @param orgId 组织主键
     */
    void deleteOrg(String tenantId, Long orgId);
}
