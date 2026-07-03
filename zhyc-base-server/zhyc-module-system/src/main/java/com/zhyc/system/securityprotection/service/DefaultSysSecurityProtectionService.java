/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.securityprotection.service;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.common.util.TextHelper;
import com.zhyc.system.securityprotection.domain.SysSecurityEvent;
import com.zhyc.system.securityprotection.domain.SysSecurityIpBlock;
import com.zhyc.system.securityprotection.domain.SysSecurityPolicy;
import com.zhyc.system.securityprotection.repository.SysSecurityProtectionRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 默认安全防护中心业务服务。
 */
@Service
public class DefaultSysSecurityProtectionService implements SysSecurityProtectionService {

    /** 租户业务编码不能为空错误码。 */
    private static final String ERROR_TENANT_REQUIRED = "ZHYC_SYS_SECURITY_TENANT_REQUIRED";
    /** IP 不能为空错误码。 */
    private static final String ERROR_IP_REQUIRED = "ZHYC_SYS_SECURITY_IP_REQUIRED";
    /** IP 格式无效错误码。 */
    private static final String ERROR_IP_INVALID = "ZHYC_SYS_SECURITY_IP_INVALID";
    /** 安全事件不能为空错误码。 */
    private static final String ERROR_EVENT_REQUIRED = "ZHYC_SYS_SECURITY_EVENT_REQUIRED";
    /** 事件类型不能为空错误码。 */
    private static final String ERROR_EVENT_TYPE_REQUIRED = "ZHYC_SYS_SECURITY_EVENT_TYPE_REQUIRED";
    /** 策略不能为空错误码。 */
    private static final String ERROR_POLICY_REQUIRED = "ZHYC_SYS_SECURITY_POLICY_REQUIRED";
    /** 策略编码不能为空错误码。 */
    private static final String ERROR_POLICY_CODE_REQUIRED = "ZHYC_SYS_SECURITY_POLICY_CODE_REQUIRED";
    /** 策略阈值必须大于 0 错误码。 */
    private static final String ERROR_POLICY_THRESHOLD_INVALID = "ZHYC_SYS_SECURITY_POLICY_THRESHOLD_INVALID";
    /** 封禁命令不能为空错误码。 */
    private static final String ERROR_BLOCK_COMMAND_REQUIRED = "ZHYC_SYS_SECURITY_BLOCK_COMMAND_REQUIRED";
    /** IPv6 简化校验表达式。 */
    private static final Pattern IPV6_OR_CIDR_PATTERN = Pattern.compile("^[0-9a-fA-F:.]+(/[0-9]{1,3})?$");
    /** 统计日期格式。 */
    private static final DateTimeFormatter STAT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** 安全防护中心仓储。 */
    private final SysSecurityProtectionRepository repository;

    /**
     * 创建默认安全防护中心业务服务。
     *
     * @param repository 安全防护中心仓储
     */
    public DefaultSysSecurityProtectionService(SysSecurityProtectionRepository repository) {
        this.repository = Objects.requireNonNull(repository, "安全防护中心仓储不能为空");
    }

    @Override
    public SecurityOverviewResponse overview(String tenantId, LocalDateTime now) {
        String requiredTenantId = requireTenantId(tenantId);
        LocalDateTime requiredNow = now == null ? LocalDateTime.now() : now;
        LocalDateTime dayStart = requiredNow.toLocalDate().atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);
        return new SecurityOverviewResponse(requiredNow.format(STAT_DATE_FORMATTER),
                repository.countDistinctSourceIp(requiredTenantId, dayStart, dayEnd),
                repository.maxIpRequestCount(requiredTenantId, dayStart, dayEnd),
                repository.countViolationIp(requiredTenantId, dayStart, dayEnd),
                repository.countActiveIpBlock(requiredTenantId, requiredNow));
    }

    @Override
    public List<SysSecurityPolicy> listPolicies(String tenantId) {
        return repository.findPolicies(requireTenantId(tenantId));
    }

    @Override
    @Transactional
    public void savePolicy(SysSecurityPolicy policy) {
        SysSecurityPolicy requiredPolicy = requireObject(policy, ERROR_POLICY_REQUIRED, "安全防护策略不能为空");
        requiredPolicy.setTenantId(requireTenantId(requiredPolicy.getTenantId()));
        requiredPolicy.setPolicyCode(requireText(requiredPolicy.getPolicyCode(), ERROR_POLICY_CODE_REQUIRED,
                "策略编码不能为空"));
        requiredPolicy.setPolicyName(TextHelper.defaultIfBlank(requiredPolicy.getPolicyName(),
                requiredPolicy.getPolicyCode()));
        requiredPolicy.setProtectionScope(TextHelper.defaultIfBlank(requiredPolicy.getProtectionScope(), "admin_api"));
        requiredPolicy.setTargetPattern(TextHelper.defaultIfBlank(requiredPolicy.getTargetPattern(), "*"));
        requiredPolicy.setAction(TextHelper.defaultIfBlank(requiredPolicy.getAction(), "observe"));
        requiredPolicy.setStatus(TextHelper.defaultIfBlank(requiredPolicy.getStatus(), "enabled"));
        if (requiredPolicy.getThresholdLimit() == null || requiredPolicy.getThresholdLimit() <= 0
                || requiredPolicy.getWindowSeconds() == null || requiredPolicy.getWindowSeconds() <= 0) {
            throw new BusinessException(ERROR_POLICY_THRESHOLD_INVALID, "策略阈值和时间窗口必须大于 0");
        }
        repository.savePolicy(requiredPolicy);
    }

    @Override
    @Transactional
    public void recordEvent(SecurityEventRecordCommand command) {
        SecurityEventRecordCommand requiredCommand = requireObject(command, ERROR_EVENT_REQUIRED, "安全事件不能为空");
        SysSecurityEvent event = new SysSecurityEvent();
        event.setTenantId(requireTenantId(requiredCommand.getTenantId()));
        event.setEventType(requireText(requiredCommand.getEventType(), ERROR_EVENT_TYPE_REQUIRED, "事件类型不能为空"));
        event.setEventLevel(TextHelper.defaultIfBlank(requiredCommand.getEventLevel(), "low"));
        event.setSourceIp(TextHelper.trimToNull(requiredCommand.getSourceIp()));
        event.setUserId(requiredCommand.getUserId());
        event.setUsername(TextHelper.trimToNull(requiredCommand.getUsername()));
        event.setRequestPath(TextHelper.trimToNull(requiredCommand.getRequestPath()));
        event.setHttpMethod(TextHelper.trimToNull(requiredCommand.getHttpMethod()));
        event.setAction(TextHelper.defaultIfBlank(requiredCommand.getAction(), "observe"));
        event.setResult(TextHelper.defaultIfBlank(requiredCommand.getResult(), "recorded"));
        event.setMessage(TextHelper.trimToNull(requiredCommand.getMessage()));
        event.setOccurredAt(requiredCommand.getOccurredAt() == null ? LocalDateTime.now() : requiredCommand.getOccurredAt());
        repository.insertEvent(event);
    }

    @Override
    public List<SecurityEventResponse> recentEvents(String tenantId, int limit) {
        String requiredTenantId = requireTenantId(tenantId);
        int safeLimit = normalizeLimit(limit);
        return repository.findRecentEvents(requiredTenantId, safeLimit).stream().map(this::toEventResponse).toList();
    }

    @Override
    public List<SecurityRankResponse> topSourceIps(String tenantId, LocalDateTime now, int limit) {
        return listRank(tenantId, now, limit, true);
    }

    @Override
    public List<SecurityRankResponse> topRequestPaths(String tenantId, LocalDateTime now, int limit) {
        return listRank(tenantId, now, limit, false);
    }

    @Override
    @Transactional
    public void blockIp(SecurityIpBlockCommand command) {
        SecurityIpBlockCommand requiredCommand = requireObject(command, ERROR_BLOCK_COMMAND_REQUIRED, "IP 封禁命令不能为空");
        String requiredTenantId = requireTenantId(requiredCommand.getTenantId());
        String ipValue = requireValidIpValue(requiredCommand.getIpValue());
        SysSecurityIpBlock block = new SysSecurityIpBlock();
        block.setTenantId(requiredTenantId);
        block.setIpValue(ipValue);
        block.setBlockType(TextHelper.defaultIfBlank(requiredCommand.getBlockType(), "manual"));
        block.setReason(TextHelper.defaultIfBlank(requiredCommand.getReason(), "安全防护中心封禁"));
        block.setStartAt(requiredCommand.getStartAt() == null ? LocalDateTime.now() : requiredCommand.getStartAt());
        block.setEndAt(requiredCommand.getEndAt());
        block.setStatus("active");
        repository.saveIpBlock(block);
        repository.syncDenyAccessRestriction(block);
        recordEvent(new SecurityEventRecordCommand(requiredTenantId, "ip_block", "high", ipValue, null, null,
                null, null, "block", "blocked", block.getReason(), block.getStartAt()));
    }

    @Override
    @Transactional
    public void unblockIp(String tenantId, String ipValue) {
        String requiredTenantId = requireTenantId(tenantId);
        String requiredIpValue = requireValidIpValue(ipValue);
        repository.deactivateIpBlock(requiredTenantId, requiredIpValue);
        repository.deactivateDenyAccessRestriction(requiredTenantId, requiredIpValue);
    }

    @Override
    public boolean isIpBlocked(String tenantId, String ipValue, LocalDateTime now) {
        String normalizedTenantId = TextHelper.trimToNull(tenantId);
        String normalizedIpValue = TextHelper.trimToNull(ipValue);
        if (normalizedTenantId == null || normalizedIpValue == null || !isValidIpOrCidr(normalizedIpValue)) {
            return false;
        }
        LocalDateTime requiredNow = now == null ? LocalDateTime.now() : now;
        return repository.findActiveIpBlockRules(normalizedTenantId, requiredNow).stream()
                .anyMatch(ruleValue -> matchesIpRule(normalizedIpValue, ruleValue));
    }

    private List<SecurityRankResponse> listRank(String tenantId, LocalDateTime now, int limit, boolean ipRank) {
        String requiredTenantId = requireTenantId(tenantId);
        LocalDateTime requiredNow = now == null ? LocalDateTime.now() : now;
        LocalDateTime dayStart = requiredNow.toLocalDate().atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);
        int safeLimit = normalizeLimit(limit);
        return (ipRank
                ? repository.topSourceIps(requiredTenantId, dayStart, dayEnd, safeLimit)
                : repository.topRequestPaths(requiredTenantId, dayStart, dayEnd, safeLimit)).stream()
                .map(row -> new SecurityRankResponse(row.getName(), row.getRequestCount()))
                .toList();
    }

    private SecurityEventResponse toEventResponse(SysSecurityEvent event) {
        return new SecurityEventResponse(event.getId(), event.getEventType(), event.getEventLevel(),
                event.getSourceIp(), event.getUsername(), event.getRequestPath(), event.getHttpMethod(),
                event.getAction(), event.getResult(), event.getMessage(), event.getOccurredAt());
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return 10;
        }
        return Math.min(limit, 100);
    }

    private String requireTenantId(String tenantId) {
        return requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空");
    }

    private String requireValidIpValue(String ipValue) {
        String normalized = requireText(ipValue, ERROR_IP_REQUIRED, "IP 不能为空");
        if (!isValidIpOrCidr(normalized)) {
            throw new BusinessException(ERROR_IP_INVALID, "IP 格式无效: " + normalized);
        }
        return normalized;
    }

    private boolean isValidIpOrCidr(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        if (value.contains(".")) {
            return isValidIpv4OrCidr(value);
        }
        return value.contains(":") && IPV6_OR_CIDR_PATTERN.matcher(value).matches();
    }

    private boolean isValidIpv4OrCidr(String value) {
        String[] parts = value.split("/", -1);
        if (parts.length > 2) {
            return false;
        }
        if (parts.length == 2) {
            try {
                int prefix = Integer.parseInt(parts[1]);
                if (prefix < 0 || prefix > 32) {
                    return false;
                }
            } catch (NumberFormatException ex) {
                return false;
            }
        }
        return parseIpv4(parts[0]) >= 0;
    }

    private boolean matchesIpRule(String ipValue, String ruleValue) {
        String normalizedRuleValue = TextHelper.trimToNull(ruleValue);
        if (normalizedRuleValue == null) {
            return false;
        }
        if (normalizedRuleValue.contains("/") && normalizedRuleValue.contains(".")) {
            return matchesIpv4Cidr(ipValue, normalizedRuleValue);
        }
        return ipValue.equalsIgnoreCase(normalizedRuleValue);
    }

    private boolean matchesIpv4Cidr(String ipValue, String cidrRule) {
        String[] cidrParts = cidrRule.split("/", -1);
        if (cidrParts.length != 2) {
            return false;
        }
        long ipLongValue = parseIpv4(ipValue);
        long networkValue = parseIpv4(cidrParts[0]);
        int prefixLength;
        try {
            prefixLength = Integer.parseInt(cidrParts[1]);
        } catch (NumberFormatException ex) {
            return false;
        }
        if (prefixLength < 0 || prefixLength > 32 || ipLongValue < 0 || networkValue < 0) {
            return false;
        }
        long mask = prefixLength == 0 ? 0L : 0xFFFFFFFFL << (32 - prefixLength) & 0xFFFFFFFFL;
        return (ipLongValue & mask) == (networkValue & mask);
    }

    private long parseIpv4(String ipAddress) {
        String[] parts = ipAddress.trim().split("\\.", -1);
        if (parts.length != 4) {
            return -1L;
        }
        long value = 0L;
        for (String part : parts) {
            try {
                int octet = Integer.parseInt(part);
                if (octet < 0 || octet > 255) {
                    return -1L;
                }
                value = (value << 8) | octet;
            } catch (NumberFormatException ex) {
                return -1L;
            }
        }
        return value;
    }

    private <T> T requireObject(T value, String code, String message) {
        if (value == null) {
            throw new BusinessException(code, message);
        }
        return value;
    }

    private String requireText(String value, String code, String message) {
        String normalized = TextHelper.trimToNull(value);
        if (normalized == null) {
            throw new BusinessException(code, message);
        }
        return normalized;
    }
}
