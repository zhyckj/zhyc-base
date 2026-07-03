/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.passwordpolicy.service;

import java.util.List;

/**
 * 系统密码策略业务服务。
 */
public interface SysPasswordPolicyService {

    /**
     * 查询租户默认密码策略。
     *
     * @param tenantId 租户业务编码
     * @return 默认密码策略
     */
    SysPasswordPolicyResponse getPolicy(String tenantId);

    /**
     * 保存或更新租户密码策略。
     *
     * @param command 系统密码策略保存命令
     */
    void save(SysPasswordPolicySaveCommand command);

    /**
     * 按租户默认策略校验密码强度。
     *
     * @param tenantId 租户业务编码
     * @param password 待校验密码明文，仅用于内存校验，不得持久化或输出日志
     * @return 密码策略校验结果
     */
    PasswordPolicyValidationResult validatePassword(String tenantId, String password);

    /**
     * 按租户默认策略校验密码历史是否复用。
     *
     * @param tenantId 租户业务编码
     * @param passwordHash 新密码哈希值，不得传入密码明文
     * @param recentPasswordHashes 最近密码哈希列表，按从新到旧排序
     * @return 密码历史策略校验结果
     */
    PasswordPolicyValidationResult validatePasswordHistory(String tenantId, String passwordHash,
                                                           List<String> recentPasswordHashes);
}
