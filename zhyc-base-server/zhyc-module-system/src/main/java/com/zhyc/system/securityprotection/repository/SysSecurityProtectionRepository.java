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
import java.time.LocalDateTime;
import java.util.List;

/**
 * 安全防护中心仓储。
 *
 * <p>隔离安全防护服务和 MyBatis 实现，统一承载策略、事件、排行和封禁数据访问。</p>
 */
public interface SysSecurityProtectionRepository {

    long countDistinctSourceIp(String tenantId, LocalDateTime dayStart, LocalDateTime dayEnd);

    long maxIpRequestCount(String tenantId, LocalDateTime dayStart, LocalDateTime dayEnd);

    long countViolationIp(String tenantId, LocalDateTime dayStart, LocalDateTime dayEnd);

    long countActiveIpBlock(String tenantId, LocalDateTime now);

    List<SysSecurityPolicy> findPolicies(String tenantId);

    void savePolicy(SysSecurityPolicy policy);

    void insertEvent(SysSecurityEvent event);

    List<SysSecurityEvent> findRecentEvents(String tenantId, int limit);

    List<SecurityRankRow> topSourceIps(String tenantId, LocalDateTime dayStart, LocalDateTime dayEnd, int limit);

    List<SecurityRankRow> topRequestPaths(String tenantId, LocalDateTime dayStart, LocalDateTime dayEnd, int limit);

    void saveIpBlock(SysSecurityIpBlock block);

    void syncDenyAccessRestriction(SysSecurityIpBlock block);

    void deactivateIpBlock(String tenantId, String ipValue);

    void deactivateDenyAccessRestriction(String tenantId, String ipValue);

    List<String> findActiveIpBlockRules(String tenantId, LocalDateTime now);

    boolean existsActiveIpBlock(String tenantId, String ipValue, LocalDateTime now);

    /**
     * 安全排行行。
     */
    class SecurityRankRow {
        /** 排行名称，例如 IP 或接口路径。 */
        private String name;
        /** 请求次数。 */
        private long requestCount;

        public SecurityRankRow() {
        }

        public SecurityRankRow(String name, long requestCount) {
            this.name = name;
            this.requestCount = requestCount;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getRequestCount() {
            return requestCount;
        }

        public void setRequestCount(long requestCount) {
            this.requestCount = requestCount;
        }
    }
}
