/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.passwordpolicy.controller;

import java.util.List;

/**
 * 密码历史策略校验请求。
 */
public class PasswordPolicyHistoryValidateRequest {

    /** 租户业务编码。 */
    private String tenantId;
    /** 新密码哈希值；不得传输密码明文。 */
    private String passwordHash;
    /** 最近密码哈希列表，按从新到旧排序。 */
    private List<String> recentPasswordHashes;

    /**
     * 返回租户业务编码。
     *
     * @return 租户业务编码
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * 设置租户业务编码。
     *
     * @param tenantId 租户业务编码
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    /**
     * 返回新密码哈希值。
     *
     * @return 新密码哈希值
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * 设置新密码哈希值。
     *
     * @param passwordHash 新密码哈希值
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * 返回最近密码哈希列表。
     *
     * @return 最近密码哈希列表
     */
    public List<String> getRecentPasswordHashes() {
        return recentPasswordHashes;
    }

    /**
     * 设置最近密码哈希列表。
     *
     * @param recentPasswordHashes 最近密码哈希列表
     */
    public void setRecentPasswordHashes(List<String> recentPasswordHashes) {
        this.recentPasswordHashes = recentPasswordHashes;
    }
}
