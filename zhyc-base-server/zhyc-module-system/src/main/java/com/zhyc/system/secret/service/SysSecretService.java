/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.secret.service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统密钥业务服务。
 */
public interface SysSecretService {

    /**
     * 查询当前租户的密钥列表。
     *
     * @param tenantId 租户业务编码
     * @return 密钥列表
     */
    List<SysSecretResponse> listSecrets(String tenantId);

    /**
     * 查询当前租户的密钥详情。
     *
     * @param tenantId 租户业务编码
     * @param secretId 密钥主键
     * @return 密钥详情
     */
    SysSecretResponse getSecret(String tenantId, Long secretId);

    /**
     * 查询当前租户可用于下拉选择的密钥。
     *
     * @param tenantId 租户业务编码
     * @param secretKind 密钥类型，可为空；为空时返回默认业务兼容类型
     * @param status 密钥状态，可为空；为空时默认启用
     * @return 密钥选项
     */
    List<SysSecretResponse> listOptions(String tenantId, String secretKind, String status);

    /**
     * 保存或更新系统密钥。
     *
     * @param command 密钥保存命令
     */
    void saveSecret(SysSecretSaveCommand command);

    /**
     * 修改系统密钥状态。
     *
     * @param tenantId 租户业务编码
     * @param secretId 密钥主键
     * @param status 新状态
     */
    void updateStatus(String tenantId, Long secretId, String status);

    /**
     * 轮换系统密钥。
     *
     * @param tenantId 租户业务编码
     * @param secretId 密钥主键
     * @param secretPlaintext 新明文密钥
     * @param expireAt 新到期时间
     */
    void rotateSecret(String tenantId, Long secretId, String secretPlaintext, LocalDateTime expireAt);

    /**
     * 删除系统密钥。
     *
     * @param tenantId 租户业务编码
     * @param secretId 密钥主键
     */
    void deleteSecret(String tenantId, Long secretId);
}
