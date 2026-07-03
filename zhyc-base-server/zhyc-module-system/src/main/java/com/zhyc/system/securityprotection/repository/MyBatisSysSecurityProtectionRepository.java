/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.securityprotection.repository;

import com.zhyc.system.securityprotection.domain.SysSecurityEvent;
import com.zhyc.system.securityprotection.domain.SysSecurityIpBlock;
import com.zhyc.system.securityprotection.domain.SysSecurityPolicy;
import com.zhyc.system.securityprotection.mapper.SysSecurityProtectionMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Repository;

/**
 * 基于 MyBatis 的系统安全防护中心仓储实现。
 */
@Repository
public class MyBatisSysSecurityProtectionRepository implements SysSecurityProtectionRepository {

    /** 系统安全防护中心 Mapper。 */
    private final SysSecurityProtectionMapper securityProtectionMapper;

    /**
     * 创建系统安全防护中心仓储实现。
     *
     * @param securityProtectionMapper 系统安全防护中心 Mapper
     */
    public MyBatisSysSecurityProtectionRepository(SysSecurityProtectionMapper securityProtectionMapper) {
        this.securityProtectionMapper = Objects.requireNonNull(securityProtectionMapper,
                "系统安全防护中心 Mapper 不能为空");
    }

    @Override
    public long countDistinctSourceIp(String tenantId, LocalDateTime dayStart, LocalDateTime dayEnd) {
        return securityProtectionMapper.countDistinctSourceIp(tenantId, dayStart, dayEnd);
    }

    @Override
    public long maxIpRequestCount(String tenantId, LocalDateTime dayStart, LocalDateTime dayEnd) {
        return securityProtectionMapper.maxIpRequestCount(tenantId, dayStart, dayEnd);
    }

    @Override
    public long countViolationIp(String tenantId, LocalDateTime dayStart, LocalDateTime dayEnd) {
        return securityProtectionMapper.countViolationIp(tenantId, dayStart, dayEnd);
    }

    @Override
    public long countActiveIpBlock(String tenantId, LocalDateTime now) {
        return securityProtectionMapper.countActiveIpBlock(tenantId, now);
    }

    @Override
    public List<SysSecurityPolicy> findPolicies(String tenantId) {
        return securityProtectionMapper.selectPolicies(tenantId);
    }

    @Override
    public void savePolicy(SysSecurityPolicy policy) {
        securityProtectionMapper.savePolicy(policy);
    }

    @Override
    public void insertEvent(SysSecurityEvent event) {
        securityProtectionMapper.insertEvent(event);
    }

    @Override
    public List<SysSecurityEvent> findRecentEvents(String tenantId, int limit) {
        return securityProtectionMapper.selectRecentEvents(tenantId, limit);
    }

    @Override
    public List<SecurityRankRow> topSourceIps(String tenantId, LocalDateTime dayStart,
                                              LocalDateTime dayEnd, int limit) {
        return securityProtectionMapper.selectTopSourceIps(tenantId, dayStart, dayEnd, limit);
    }

    @Override
    public List<SecurityRankRow> topRequestPaths(String tenantId, LocalDateTime dayStart,
                                                 LocalDateTime dayEnd, int limit) {
        return securityProtectionMapper.selectTopRequestPaths(tenantId, dayStart, dayEnd, limit);
    }

    @Override
    public void saveIpBlock(SysSecurityIpBlock block) {
        securityProtectionMapper.saveIpBlock(block);
    }

    @Override
    public void syncDenyAccessRestriction(SysSecurityIpBlock block) {
        securityProtectionMapper.syncDenyAccessRestriction(block);
    }

    @Override
    public void deactivateIpBlock(String tenantId, String ipValue) {
        securityProtectionMapper.deactivateIpBlock(tenantId, ipValue);
    }

    @Override
    public void deactivateDenyAccessRestriction(String tenantId, String ipValue) {
        securityProtectionMapper.deactivateDenyAccessRestriction(tenantId, ipValue);
    }

    @Override
    public List<String> findActiveIpBlockRules(String tenantId, LocalDateTime now) {
        return securityProtectionMapper.selectActiveIpBlockRules(tenantId, now);
    }

    @Override
    public boolean existsActiveIpBlock(String tenantId, String ipValue, LocalDateTime now) {
        return securityProtectionMapper.countActiveIpBlockByValue(tenantId, ipValue, now) > 0;
    }
}
