/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.loginlog.repository;

import com.zhyc.system.loginlog.domain.SysLoginLog;
import com.zhyc.system.loginlog.mapper.SysLoginLogMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * 基于 MyBatis 的系统登录日志仓储实现。
 */
@Repository
public class MyBatisSysLoginLogRepository implements SysLoginLogRepository {

    /** 系统登录日志 Mapper。 */
    private final SysLoginLogMapper loginLogMapper;

    /**
     * 创建系统登录日志仓储实现。
     *
     * @param loginLogMapper 系统登录日志 Mapper
     */
    public MyBatisSysLoginLogRepository(SysLoginLogMapper loginLogMapper) {
        this.loginLogMapper = Objects.requireNonNull(loginLogMapper, "系统登录日志 Mapper 不能为空");
    }

    /**
     * 保存系统登录日志。
     *
     * @param loginLog 系统登录日志
     */
    @Override
    public void save(SysLoginLog loginLog) {
        loginLogMapper.insert(loginLog);
    }

    /**
     * 查询租户最近登录日志。
     *
     * @param tenantId 租户业务编码
     * @param limit 查询条数上限
     * @return 最近登录日志列表
     */
    @Override
    public List<SysLoginLog> findRecentByTenantId(String tenantId, int limit) {
        return loginLogMapper.selectRecentByTenantId(tenantId, limit);
    }
}
