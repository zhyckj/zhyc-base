/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.param.repository;

import com.zhyc.system.param.domain.SysParam;
import com.zhyc.system.param.mapper.SysParamMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 基于 MyBatis 的系统参数仓储实现。
 */
@Repository
public class MyBatisSysParamRepository implements SysParamRepository {

    /** 系统参数 Mapper。 */
    private final SysParamMapper paramMapper;

    /**
     * 创建系统参数仓储实现。
     *
     * @param paramMapper 系统参数 Mapper
     */
    public MyBatisSysParamRepository(SysParamMapper paramMapper) {
        this.paramMapper = Objects.requireNonNull(paramMapper, "系统参数 Mapper 不能为空");
    }

    @Override
    public List<SysParam> findByTenantId(String tenantId) {
        return paramMapper.selectByTenantId(tenantId);
    }

    @Override
    public Optional<SysParam> findByTenantIdAndParamKey(String tenantId, String paramKey) {
        return Optional.ofNullable(paramMapper.selectByTenantIdAndParamKey(tenantId, paramKey));
    }

    @Override
    public void save(SysParam param) {
        paramMapper.upsert(param);
    }
}
