/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.securityprotection;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.securityprotection.domain.SysSecurityEvent;
import com.zhyc.system.securityprotection.domain.SysSecurityIpBlock;
import com.zhyc.system.securityprotection.domain.SysSecurityPolicy;
import com.zhyc.system.securityprotection.repository.SysSecurityProtectionRepository;
import com.zhyc.system.securityprotection.service.DefaultSysSecurityProtectionService;
import com.zhyc.system.securityprotection.service.SecurityEventRecordCommand;
import com.zhyc.system.securityprotection.service.SecurityIpBlockCommand;
import com.zhyc.system.securityprotection.service.SecurityOverviewResponse;
import com.zhyc.system.securityprotection.service.SysSecurityProtectionService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 安全防护中心业务服务测试。
 */
class SysSecurityProtectionServiceTest {

  /**
   * 验证安全总览按租户统计今日来源、最高 IP 请求、违规 IP 和封禁 IP。
   */
  @Test
  void shouldBuildSecurityOverviewByTenantAndDate() {
    RecordingSecurityProtectionRepository repository = new RecordingSecurityProtectionRepository();
    SysSecurityProtectionService service = new DefaultSysSecurityProtectionService(repository);
    LocalDateTime now = LocalDateTime.of(2026, 7, 3, 18, 30);

    SecurityOverviewResponse overview = service.overview(" zhyc-platform ", now);

    assertEquals("zhyc-platform", repository.lastTenantId);
    assertEquals(LocalDateTime.of(2026, 7, 3, 0, 0), repository.lastDayStart);
    assertEquals(LocalDateTime.of(2026, 7, 4, 0, 0), repository.lastDayEnd);
    assertEquals(2, overview.getTodaySourceCount());
    assertEquals(19, overview.getMaxIpRequestCount());
    assertEquals(1, overview.getViolationIpCount());
    assertEquals(1, overview.getBlockedIpCount());
    assertEquals("20260703", overview.getStatDate());
  }

  /**
   * 验证安全事件记录会裁剪关键字段并写入仓储。
   */
  @Test
  void shouldRecordSecurityEventWithNormalizedFields() {
    RecordingSecurityProtectionRepository repository = new RecordingSecurityProtectionRepository();
    SysSecurityProtectionService service = new DefaultSysSecurityProtectionService(repository);

    service.recordEvent(new SecurityEventRecordCommand(" zhyc-platform ", " request_rate ",
        " medium ", " 113.240.198.194 ", 1L, " admin ", "/sys/login", " GET ",
        "observe", "blocked", "请求频率过高", LocalDateTime.of(2026, 7, 3, 18, 35)));

    SysSecurityEvent event = repository.lastRecordedEvent;
    assertEquals("zhyc-platform", event.getTenantId());
    assertEquals("request_rate", event.getEventType());
    assertEquals("medium", event.getEventLevel());
    assertEquals("113.240.198.194", event.getSourceIp());
    assertEquals("/sys/login", event.getRequestPath());
    assertEquals("GET", event.getHttpMethod());
    assertEquals("observe", event.getAction());
    assertEquals("blocked", event.getResult());
  }

  /**
   * 验证手动封禁 IP 会写入封禁表并同步拒绝访问限制。
   */
  @Test
  void shouldBlockIpAndSyncAccessRestriction() {
    RecordingSecurityProtectionRepository repository = new RecordingSecurityProtectionRepository();
    SysSecurityProtectionService service = new DefaultSysSecurityProtectionService(repository);
    LocalDateTime startAt = LocalDateTime.of(2026, 7, 3, 18, 40);
    LocalDateTime endAt = startAt.plusMinutes(30);

    service.blockIp(new SecurityIpBlockCommand("zhyc-platform", "113.240.198.194",
        "manual", "人工封禁异常来源", startAt, endAt));

    SysSecurityIpBlock block = repository.lastSavedBlock;
    assertEquals("zhyc-platform", block.getTenantId());
    assertEquals("113.240.198.194", block.getIpValue());
    assertEquals("manual", block.getBlockType());
    assertEquals("active", block.getStatus());
    assertTrue(repository.accessRestrictionSynced);
  }

  /**
   * 验证解封 IP 会把有效封禁状态更新为已解除。
   */
  @Test
  void shouldUnblockIp() {
    RecordingSecurityProtectionRepository repository = new RecordingSecurityProtectionRepository();
    SysSecurityProtectionService service = new DefaultSysSecurityProtectionService(repository);

    service.unblockIp(" zhyc-platform ", " 113.240.198.194 ");

    assertEquals("zhyc-platform", repository.lastUnblockTenantId);
    assertEquals("113.240.198.194", repository.lastUnblockIp);
    assertTrue(repository.accessRestrictionReleased);
  }

  /**
   * 验证封禁 IP 必须是有效 IPv4、IPv6 或 CIDR 文本。
   */
  @Test
  void shouldRejectBlankOrInvalidBlockIp() {
    RecordingSecurityProtectionRepository repository = new RecordingSecurityProtectionRepository();
    SysSecurityProtectionService service = new DefaultSysSecurityProtectionService(repository);

    BusinessException blankException = assertThrows(BusinessException.class,
        () -> service.blockIp(new SecurityIpBlockCommand("zhyc-platform", " ", "manual",
            "空 IP", null, null)));
    BusinessException invalidException = assertThrows(BusinessException.class,
        () -> service.blockIp(new SecurityIpBlockCommand("zhyc-platform", "999.1.1.1", "manual",
            "无效 IP", null, null)));

    assertEquals("ZHYC_SYS_SECURITY_IP_REQUIRED", blankException.getCode());
    assertEquals("ZHYC_SYS_SECURITY_IP_INVALID", invalidException.getCode());
  }

  /**
   * 测试用安全防护仓储。
   */
  private static class RecordingSecurityProtectionRepository implements SysSecurityProtectionRepository {

    /** 最近一次查询租户。 */
    private String lastTenantId;
    /** 最近一次查询开始时间。 */
    private LocalDateTime lastDayStart;
    /** 最近一次查询结束时间。 */
    private LocalDateTime lastDayEnd;
    /** 最近一次记录的安全事件。 */
    private SysSecurityEvent lastRecordedEvent;
    /** 最近一次保存的 IP 封禁。 */
    private SysSecurityIpBlock lastSavedBlock;
    /** 是否同步访问限制。 */
    private boolean accessRestrictionSynced;
    /** 最近一次解封租户。 */
    private String lastUnblockTenantId;
    /** 最近一次解封 IP。 */
    private String lastUnblockIp;
    /** 是否同步解除访问限制。 */
    private boolean accessRestrictionReleased;

    @Override
    public long countDistinctSourceIp(String tenantId, LocalDateTime dayStart, LocalDateTime dayEnd) {
      captureOverviewArgs(tenantId, dayStart, dayEnd);
      return 2L;
    }

    @Override
    public long maxIpRequestCount(String tenantId, LocalDateTime dayStart, LocalDateTime dayEnd) {
      captureOverviewArgs(tenantId, dayStart, dayEnd);
      return 19L;
    }

    @Override
    public long countViolationIp(String tenantId, LocalDateTime dayStart, LocalDateTime dayEnd) {
      captureOverviewArgs(tenantId, dayStart, dayEnd);
      return 1L;
    }

    @Override
    public long countActiveIpBlock(String tenantId, LocalDateTime now) {
      lastTenantId = tenantId;
      return 1L;
    }

    @Override
    public List<SysSecurityPolicy> findPolicies(String tenantId) {
      lastTenantId = tenantId;
      return List.of();
    }

    @Override
    public void savePolicy(SysSecurityPolicy policy) {
    }

    @Override
    public void insertEvent(SysSecurityEvent event) {
      lastRecordedEvent = event;
    }

    @Override
    public List<SysSecurityEvent> findRecentEvents(String tenantId, int limit) {
      lastTenantId = tenantId;
      return new ArrayList<>();
    }

    @Override
    public List<SysSecurityProtectionRepository.SecurityRankRow> topSourceIps(
        String tenantId, LocalDateTime dayStart, LocalDateTime dayEnd, int limit) {
      captureOverviewArgs(tenantId, dayStart, dayEnd);
      return List.of();
    }

    @Override
    public List<SysSecurityProtectionRepository.SecurityRankRow> topRequestPaths(
        String tenantId, LocalDateTime dayStart, LocalDateTime dayEnd, int limit) {
      captureOverviewArgs(tenantId, dayStart, dayEnd);
      return List.of();
    }

    @Override
    public void saveIpBlock(SysSecurityIpBlock block) {
      lastSavedBlock = block;
    }

    @Override
    public void syncDenyAccessRestriction(SysSecurityIpBlock block) {
      accessRestrictionSynced = true;
    }

    @Override
    public void deactivateIpBlock(String tenantId, String ipValue) {
      lastUnblockTenantId = tenantId;
      lastUnblockIp = ipValue;
    }

    @Override
    public void deactivateDenyAccessRestriction(String tenantId, String ipValue) {
      lastUnblockTenantId = tenantId;
      lastUnblockIp = ipValue;
      accessRestrictionReleased = true;
    }

    @Override
    public List<String> findActiveIpBlockRules(String tenantId, LocalDateTime now) {
      lastTenantId = tenantId;
      return List.of();
    }

    @Override
    public boolean existsActiveIpBlock(String tenantId, String ipValue, LocalDateTime now) {
      lastTenantId = tenantId;
      return false;
    }

    private void captureOverviewArgs(String tenantId, LocalDateTime dayStart, LocalDateTime dayEnd) {
      this.lastTenantId = tenantId;
      this.lastDayStart = dayStart;
      this.lastDayEnd = dayEnd;
    }
  }
}
