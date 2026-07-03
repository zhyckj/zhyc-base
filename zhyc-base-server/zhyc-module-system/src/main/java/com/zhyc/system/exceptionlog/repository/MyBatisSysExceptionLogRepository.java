/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.exceptionlog.repository;

import com.zhyc.system.exceptionlog.domain.SysExceptionLog;
import com.zhyc.system.exceptionlog.mapper.SysExceptionLogMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * 基于 MyBatis 的系统异常日志仓储实现。
 */
@Repository
public class MyBatisSysExceptionLogRepository implements SysExceptionLogRepository {

    /** 系统异常日志 Mapper。 */
    private final SysExceptionLogMapper exceptionLogMapper;

    /**
     * 创建系统异常日志仓储实现。
     *
     * @param exceptionLogMapper 系统异常日志 Mapper
     */
    public MyBatisSysExceptionLogRepository(SysExceptionLogMapper exceptionLogMapper) {
        this.exceptionLogMapper = Objects.requireNonNull(exceptionLogMapper, "系统异常日志 Mapper 不能为空");
    }

    /**
     * 保存系统异常日志。
     *
     * @param exceptionLog 系统异常日志
     */
    @Override
    public void save(SysExceptionLog exceptionLog) {
        exceptionLogMapper.insert(exceptionLog);
    }

    /**
     * 查询租户最近异常日志。
     *
     * @param tenantId 租户业务编码
     * @param limit 查询条数上限
     * @return 最近异常日志列表
     */
    @Override
    public List<SysExceptionLog> findRecentByTenantId(String tenantId, int limit) {
        return exceptionLogMapper.selectRecentByTenantId(tenantId, limit);
    }
}
