/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.securityprotection.service;

import com.zhyc.system.securityprotection.domain.SysSecurityPolicy;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 安全防护中心业务服务。
 */
public interface SysSecurityProtectionService {

    SecurityOverviewResponse overview(String tenantId, LocalDateTime now);

    List<SysSecurityPolicy> listPolicies(String tenantId);

    void savePolicy(SysSecurityPolicy policy);

    void recordEvent(SecurityEventRecordCommand command);

    List<SecurityEventResponse> recentEvents(String tenantId, int limit);

    List<SecurityRankResponse> topSourceIps(String tenantId, LocalDateTime now, int limit);

    List<SecurityRankResponse> topRequestPaths(String tenantId, LocalDateTime now, int limit);

    void blockIp(SecurityIpBlockCommand command);

    void unblockIp(String tenantId, String ipValue);

    boolean isIpBlocked(String tenantId, String ipValue, LocalDateTime now);
}
