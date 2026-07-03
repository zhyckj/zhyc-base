/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.accessrestriction.repository;

import com.zhyc.system.accessrestriction.domain.SysAccessRestriction;
import com.zhyc.system.accessrestriction.mapper.SysAccessRestrictionMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 基于 MyBatis 的系统访问限制仓储实现。
 */
@Repository
public class MyBatisSysAccessRestrictionRepository implements SysAccessRestrictionRepository {

    /** 系统访问限制 Mapper。 */
    private final SysAccessRestrictionMapper accessRestrictionMapper;

    /**
     * 创建系统访问限制仓储实现。
     *
     * @param accessRestrictionMapper 系统访问限制 Mapper
     */
    public MyBatisSysAccessRestrictionRepository(SysAccessRestrictionMapper accessRestrictionMapper) {
        this.accessRestrictionMapper = Objects.requireNonNull(accessRestrictionMapper,
                "系统访问限制 Mapper 不能为空");
    }

    @Override
    public List<SysAccessRestriction> findActive(String tenantId, String restrictionType, LocalDateTime now) {
        return accessRestrictionMapper.selectActiveRestrictions(tenantId, restrictionType, now);
    }

    @Override
    public void save(SysAccessRestriction restriction) {
        accessRestrictionMapper.saveRestriction(restriction);
    }
}
