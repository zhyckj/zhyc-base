/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.secret.repository;

import com.zhyc.system.secret.domain.SysSecret;
import com.zhyc.system.secret.mapper.SysSecretMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 基于 MyBatis 的系统密钥仓储实现。
 */
@Repository
public class MyBatisSysSecretRepository implements SysSecretRepository {

    /** 系统密钥 Mapper。 */
    private final SysSecretMapper secretMapper;

    /**
     * 创建系统密钥仓储实现。
     *
     * @param secretMapper 系统密钥 Mapper
     */
    public MyBatisSysSecretRepository(SysSecretMapper secretMapper) {
        this.secretMapper = Objects.requireNonNull(secretMapper, "系统密钥 Mapper 不能为空");
    }

    @Override
    public List<SysSecret> findByTenantId(String tenantId) {
        return secretMapper.selectByTenantId(tenantId);
    }

    @Override
    public Optional<SysSecret> findByTenantIdAndId(String tenantId, Long secretId) {
        return secretMapper.selectByTenantIdAndId(tenantId, secretId).stream().findFirst();
    }

    @Override
    public Optional<SysSecret> findByTenantIdAndSecretCode(String tenantId, String secretCode) {
        return secretMapper.selectByTenantIdAndSecretCode(tenantId, secretCode).stream().findFirst();
    }

    @Override
    public List<SysSecret> findSelectableSecrets(String tenantId, String secretKind, String status) {
        return secretMapper.selectSelectableSecrets(tenantId, secretKind, status);
    }

    @Override
    public void insert(SysSecret secret) {
        secretMapper.insert(secret);
    }

    @Override
    public void update(SysSecret secret) {
        secretMapper.update(secret);
    }

    @Override
    public void deleteByTenantIdAndId(String tenantId, Long secretId) {
        secretMapper.deleteByTenantIdAndId(tenantId, secretId);
    }
}
