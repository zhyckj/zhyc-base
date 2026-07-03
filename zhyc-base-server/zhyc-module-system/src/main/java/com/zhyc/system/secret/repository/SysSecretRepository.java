/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.secret.repository;

import com.zhyc.system.secret.domain.SysSecret;

import java.util.List;
import java.util.Optional;

/**
 * 系统密钥仓储。
 */
public interface SysSecretRepository {

    /**
     * 查询当前租户的密钥列表。
     *
     * @param tenantId 租户业务编码
     * @return 密钥列表
     */
    List<SysSecret> findByTenantId(String tenantId);

    /**
     * 按租户和主键查询密钥。
     *
     * @param tenantId 租户业务编码
     * @param secretId 密钥主键
     * @return 密钥
     */
    Optional<SysSecret> findByTenantIdAndId(String tenantId, Long secretId);

    /**
     * 按租户和密钥编码查询密钥。
     *
     * @param tenantId 租户业务编码
     * @param secretCode 密钥编码
     * @return 密钥
     */
    Optional<SysSecret> findByTenantIdAndSecretCode(String tenantId, String secretCode);

    /**
     * 查询当前租户可用于下拉选择的密钥。
     *
     * @param tenantId 租户业务编码
     * @param secretKind 密钥类型，可为空；为空时返回数据源兼容类型
     * @param status 密钥状态
     * @return 密钥列表
     */
    List<SysSecret> findSelectableSecrets(String tenantId, String secretKind, String status);

    /**
     * 新增系统密钥。
     *
     * @param secret 系统密钥
     */
    void insert(SysSecret secret);

    /**
     * 更新系统密钥。
     *
     * @param secret 系统密钥
     */
    void update(SysSecret secret);

    /**
     * 删除当前租户的系统密钥。
     *
     * @param tenantId 租户业务编码
     * @param secretId 密钥主键
     */
    void deleteByTenantIdAndId(String tenantId, Long secretId);
}
