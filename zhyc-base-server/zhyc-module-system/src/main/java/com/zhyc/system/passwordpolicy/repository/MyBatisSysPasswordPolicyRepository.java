/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.passwordpolicy.repository;

import com.zhyc.system.passwordpolicy.domain.SysPasswordPolicy;
import com.zhyc.system.passwordpolicy.mapper.SysPasswordPolicyMapper;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;

/**
 * 基于 MyBatis 的系统密码策略仓储实现。
 */
@Repository
public class MyBatisSysPasswordPolicyRepository implements SysPasswordPolicyRepository {

    /** 系统密码策略 Mapper。 */
    private final SysPasswordPolicyMapper passwordPolicyMapper;

    /**
     * 创建系统密码策略仓储实现。
     *
     * @param passwordPolicyMapper 系统密码策略 Mapper
     */
    public MyBatisSysPasswordPolicyRepository(SysPasswordPolicyMapper passwordPolicyMapper) {
        this.passwordPolicyMapper = Objects.requireNonNull(passwordPolicyMapper,
                "系统密码策略 Mapper 不能为空");
    }

    @Override
    public Optional<SysPasswordPolicy> findDefaultByTenantId(String tenantId) {
        return Optional.ofNullable(passwordPolicyMapper.selectDefaultByTenantId(tenantId));
    }

    @Override
    public void save(SysPasswordPolicy policy) {
        passwordPolicyMapper.upsert(policy);
    }
}
