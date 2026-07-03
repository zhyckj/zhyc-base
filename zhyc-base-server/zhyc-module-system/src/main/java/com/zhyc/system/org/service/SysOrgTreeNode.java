/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.org.service;

import com.zhyc.system.org.domain.SysOrg;

import java.util.ArrayList;
import java.util.List;

/**
 * 系统组织机构树节点。
 */
public class SysOrgTreeNode {

    /** 数据库主键。 */
    private final Long id;
    /** 父级组织主键。 */
    private final Long parentId;
    /** 祖级组织路径。 */
    private final String ancestors;
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
    /** 子组织节点。 */
    private final List<SysOrgTreeNode> children = new ArrayList<>();

    private SysOrgTreeNode(SysOrg org) {
        this.id = org.getId();
        this.parentId = org.getParentId();
        this.ancestors = org.getAncestors();
        this.orgCode = org.getOrgCode();
        this.orgName = org.getOrgName();
        this.leaderUserId = org.getLeaderUserId();
        this.sortOrder = org.getSortOrder();
        this.status = org.getStatus();
    }

    /**
     * 从组织领域对象创建树节点。
     *
     * @param org 组织领域对象
     * @return 组织树节点
     */
    public static SysOrgTreeNode from(SysOrg org) {
        return new SysOrgTreeNode(org);
    }

    /**
     * 添加子组织节点。
     *
     * @param child 子组织节点
     */
    public void addChild(SysOrgTreeNode child) {
        children.add(child);
    }

    /**
     * 返回数据库主键。
     *
     * @return 数据库主键
     */
    public Long getId() {
        return id;
    }

    /**
     * 返回父级组织主键。
     *
     * @return 父级组织主键
     */
    public Long getParentId() {
        return parentId;
    }

    /**
     * 返回祖级组织路径。
     *
     * @return 祖级组织路径
     */
    public String getAncestors() {
        return ancestors;
    }

    /**
     * 返回组织编码。
     *
     * @return 组织编码
     */
    public String getOrgCode() {
        return orgCode;
    }

    /**
     * 返回组织名称。
     *
     * @return 组织名称
     */
    public String getOrgName() {
        return orgName;
    }

    /**
     * 返回负责人用户主键。
     *
     * @return 负责人用户主键
     */
    public Long getLeaderUserId() {
        return leaderUserId;
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
     * 返回组织状态。
     *
     * @return 组织状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 返回子组织节点。
     *
     * @return 子组织节点
     */
    public List<SysOrgTreeNode> getChildren() {
        return children;
    }
}
