/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.accessrestriction.service;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.accessrestriction.domain.SysAccessRestriction;
import com.zhyc.system.accessrestriction.domain.SysAccessRestrictionEffect;
import com.zhyc.system.accessrestriction.domain.SysAccessRestrictionType;
import com.zhyc.system.accessrestriction.repository.SysAccessRestrictionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 默认系统访问限制业务服务实现。
 */
@Service
public class DefaultSysAccessRestrictionService implements SysAccessRestrictionService {

    /** 访问限制保存命令不能为空错误码。 */
    private static final String ERROR_COMMAND_REQUIRED = "ZHYC_SYS_ACCESS_RESTRICTION_COMMAND_REQUIRED";
    /** 租户业务编码不能为空错误码。 */
    private static final String ERROR_TENANT_REQUIRED = "ZHYC_SYS_ACCESS_RESTRICTION_TENANT_REQUIRED";
    /** 限制类型不能为空错误码。 */
    private static final String ERROR_TYPE_REQUIRED = "ZHYC_SYS_ACCESS_RESTRICTION_TYPE_REQUIRED";
    /** 限制类型不支持错误码。 */
    private static final String ERROR_TYPE_UNSUPPORTED = "ZHYC_SYS_ACCESS_RESTRICTION_TYPE_UNSUPPORTED";
    /** 待判定访问标识不能为空错误码。 */
    private static final String ERROR_ACCESS_VALUE_REQUIRED = "ZHYC_SYS_ACCESS_RESTRICTION_ACCESS_VALUE_REQUIRED";
    /** 当前时间不能为空错误码。 */
    private static final String ERROR_NOW_REQUIRED = "ZHYC_SYS_ACCESS_RESTRICTION_NOW_REQUIRED";
    /** 规则值不能为空错误码。 */
    private static final String ERROR_RULE_VALUE_REQUIRED = "ZHYC_SYS_ACCESS_RESTRICTION_RULE_VALUE_REQUIRED";
    /** 生效动作不能为空错误码。 */
    private static final String ERROR_EFFECT_REQUIRED = "ZHYC_SYS_ACCESS_RESTRICTION_EFFECT_REQUIRED";
    /** 生效动作不支持错误码。 */
    private static final String ERROR_EFFECT_UNSUPPORTED = "ZHYC_SYS_ACCESS_RESTRICTION_EFFECT_UNSUPPORTED";

    /** 系统访问限制仓储。 */
    private final SysAccessRestrictionRepository accessRestrictionRepository;

    /**
     * 创建默认系统访问限制业务服务。
     *
     * @param accessRestrictionRepository 系统访问限制仓储
     */
    public DefaultSysAccessRestrictionService(SysAccessRestrictionRepository accessRestrictionRepository) {
        this.accessRestrictionRepository = Objects.requireNonNull(accessRestrictionRepository,
                "系统访问限制仓储不能为空");
    }

    @Override
    public List<SysAccessRestrictionResponse> listActiveRestrictions(String tenantId, String restrictionType,
                                                                     LocalDateTime now) {
        String requiredTenantId = requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空");
        String requiredRestrictionType = normalizeRestrictionType(requireText(restrictionType, ERROR_TYPE_REQUIRED,
                "限制类型不能为空"));
        LocalDateTime requiredNow = requireObject(now, ERROR_NOW_REQUIRED, "当前时间不能为空");
        return accessRestrictionRepository.findActive(requiredTenantId, requiredRestrictionType, requiredNow).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public SysAccessRestrictionEvaluationResult evaluateAccess(String tenantId, String restrictionType,
                                                               String accessValue, LocalDateTime now) {
        String requiredTenantId = requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空");
        String requiredRestrictionType = normalizeRestrictionType(requireText(restrictionType, ERROR_TYPE_REQUIRED,
                "限制类型不能为空"));
        String requiredAccessValue = requireText(accessValue, ERROR_ACCESS_VALUE_REQUIRED, "待判定访问标识不能为空");
        LocalDateTime requiredNow = requireObject(now, ERROR_NOW_REQUIRED, "当前时间不能为空");
        List<SysAccessRestriction> matchedRestrictions = accessRestrictionRepository
                .findActive(requiredTenantId, requiredRestrictionType, requiredNow).stream()
                .filter(restriction -> matchesRule(requiredRestrictionType, requiredAccessValue,
                        restriction.getRuleValue()))
                .toList();
        return matchedRestrictions.stream()
                .filter(restriction -> "deny".equalsIgnoreCase(restriction.getEffect()))
                .findFirst()
                .map(restriction -> new SysAccessRestrictionEvaluationResult(false, "deny",
                        restriction.getRuleValue()))
                .orElseGet(() -> matchedRestrictions.stream()
                        .findFirst()
                        .map(restriction -> new SysAccessRestrictionEvaluationResult(true,
                                restriction.getEffect(), restriction.getRuleValue()))
                        .orElseGet(() -> new SysAccessRestrictionEvaluationResult(true, "allow", null)));
    }

    /**
     * 判断访问标识是否命中规则值。
     *
     * @param restrictionType 限制类型
     * @param accessValue 访问标识
     * @param ruleValue 规则值
     * @return 命中时返回 {@code true}
     */
    private boolean matchesRule(String restrictionType, String accessValue, String ruleValue) {
        if ("ip".equalsIgnoreCase(restrictionType) && ruleValue != null && ruleValue.contains("/")) {
            return matchesIpv4Cidr(accessValue, ruleValue);
        }
        return accessValue.equalsIgnoreCase(ruleValue);
    }

    /**
     * 判断 IPv4 地址是否命中 CIDR 网段。
     *
     * @param ipAddress IPv4 地址
     * @param cidrRule CIDR 规则，例如 192.168.1.0/24
     * @return 命中时返回 {@code true}
     */
    private boolean matchesIpv4Cidr(String ipAddress, String cidrRule) {
        String[] cidrParts = cidrRule.split("/", -1);
        if (cidrParts.length != 2) {
            return false;
        }
        long ipValue = parseIpv4(ipAddress);
        long networkValue = parseIpv4(cidrParts[0]);
        int prefixLength;
        try {
            prefixLength = Integer.parseInt(cidrParts[1]);
        } catch (NumberFormatException ex) {
            return false;
        }
        if (prefixLength < 0 || prefixLength > 32 || ipValue < 0 || networkValue < 0) {
            return false;
        }
        long mask = prefixLength == 0 ? 0L : 0xFFFFFFFFL << (32 - prefixLength) & 0xFFFFFFFFL;
        return (ipValue & mask) == (networkValue & mask);
    }

    /**
     * 解析 IPv4 地址为无符号整数。
     *
     * @param ipAddress IPv4 地址
     * @return 解析成功返回 0 到 4294967295，失败返回 -1
     */
    private long parseIpv4(String ipAddress) {
        if (ipAddress == null) {
            return -1;
        }
        String[] parts = ipAddress.trim().split("\\.", -1);
        if (parts.length != 4) {
            return -1;
        }
        long value = 0;
        for (String part : parts) {
            int octet;
            try {
                octet = Integer.parseInt(part);
            } catch (NumberFormatException ex) {
                return -1;
            }
            if (octet < 0 || octet > 255) {
                return -1;
            }
            value = (value << 8) | octet;
        }
        return value;
    }

    @Override
    @Transactional
    public void save(SysAccessRestrictionSaveCommand command) {
        SysAccessRestrictionSaveCommand requiredCommand = requireObject(command, ERROR_COMMAND_REQUIRED,
                "系统访问限制保存命令不能为空");
        SysAccessRestriction restriction = new SysAccessRestriction(null,
                requireText(requiredCommand.getTenantId(), ERROR_TENANT_REQUIRED, "租户业务编码不能为空"),
                normalizeRestrictionType(requireText(requiredCommand.getRestrictionType(), ERROR_TYPE_REQUIRED,
                        "限制类型不能为空")),
                requireText(requiredCommand.getRuleValue(), ERROR_RULE_VALUE_REQUIRED, "规则值不能为空"),
                normalizeEffect(requireText(requiredCommand.getEffect(), ERROR_EFFECT_REQUIRED, "生效动作不能为空")),
                requiredCommand.getStartAt(), requiredCommand.getEndAt(), null, null);
        accessRestrictionRepository.save(restriction);
    }

    private SysAccessRestrictionResponse toResponse(SysAccessRestriction restriction) {
        return new SysAccessRestrictionResponse(restriction.getId(), restriction.getTenantId(),
                restriction.getRestrictionType(), restriction.getRuleValue(), restriction.getEffect(),
                restriction.getStartAt(), restriction.getEndAt());
    }

    private String normalizeRestrictionType(String restrictionType) {
        try {
            return SysAccessRestrictionType.fromCode(restrictionType).getCode();
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ERROR_TYPE_UNSUPPORTED, ex.getMessage());
        }
    }

    private String normalizeEffect(String effect) {
        try {
            return SysAccessRestrictionEffect.fromCode(effect).getCode();
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ERROR_EFFECT_UNSUPPORTED, ex.getMessage());
        }
    }

    /**
     * 校验业务对象不能为空。
     *
     * @param value 原始对象
     * @param code 业务错误码
     * @param message 为空时的异常消息
     * @return 校验后的对象
     * @param <T> 对象类型
     */
    private <T> T requireObject(T value, String code, String message) {
        if (value == null) {
            throw new BusinessException(code, message);
        }
        return value;
    }

    /**
     * 校验文本不能为空并去除首尾空白。
     *
     * @param value 原始文本
     * @param code 业务错误码
     * @param message 为空时的异常消息
     * @return 清理后的文本
     */
    private String requireText(String value, String code, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException(code, message);
        }
        return value.trim();
    }
}
