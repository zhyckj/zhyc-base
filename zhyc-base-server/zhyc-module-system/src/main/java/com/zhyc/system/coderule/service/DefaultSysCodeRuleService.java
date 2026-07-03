/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.coderule.service;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.coderule.domain.SysCodeRule;
import com.zhyc.system.coderule.repository.SysCodeRuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * 默认系统编码规则业务服务实现。
 */
@Service
public class DefaultSysCodeRuleService implements SysCodeRuleService {

    /** 编码规则保存命令不能为空错误码。 */
    private static final String ERROR_COMMAND_REQUIRED = "ZHYC_SYS_CODE_RULE_COMMAND_REQUIRED";
    /** 租户业务编码不能为空错误码。 */
    private static final String ERROR_TENANT_REQUIRED = "ZHYC_SYS_CODE_RULE_TENANT_REQUIRED";
    /** 编码规则编码不能为空错误码。 */
    private static final String ERROR_RULE_CODE_REQUIRED = "ZHYC_SYS_CODE_RULE_CODE_REQUIRED";
    /** 编码规则名称不能为空错误码。 */
    private static final String ERROR_RULE_NAME_REQUIRED = "ZHYC_SYS_CODE_RULE_NAME_REQUIRED";
    /** 业务日期不能为空错误码。 */
    private static final String ERROR_BUSINESS_DATE_REQUIRED = "ZHYC_SYS_CODE_RULE_BUSINESS_DATE_REQUIRED";
    /** 编码规则不存在或未启用错误码。 */
    private static final String ERROR_RULE_NOT_AVAILABLE = "ZHYC_SYS_CODE_RULE_NOT_AVAILABLE";
    /** 日期格式非法错误码。 */
    private static final String ERROR_DATE_PATTERN_INVALID = "ZHYC_SYS_CODE_RULE_DATE_PATTERN_INVALID";
    /** 序列号长度不能为空错误码。 */
    private static final String ERROR_SEQUENCE_LENGTH_REQUIRED = "ZHYC_SYS_CODE_RULE_SEQUENCE_LENGTH_REQUIRED";

    /** 系统编码规则仓储。 */
    private final SysCodeRuleRepository codeRuleRepository;

    /**
     * 创建默认系统编码规则业务服务。
     *
     * @param codeRuleRepository 系统编码规则仓储
     */
    public DefaultSysCodeRuleService(SysCodeRuleRepository codeRuleRepository) {
        this.codeRuleRepository = Objects.requireNonNull(codeRuleRepository, "系统编码规则仓储不能为空");
    }

    @Override
    public List<SysCodeRuleResponse> listRules(String tenantId) {
        String requiredTenantId = requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空");
        return codeRuleRepository.findByTenantId(requiredTenantId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void save(SysCodeRuleSaveCommand command) {
        SysCodeRuleSaveCommand requiredCommand = requireObject(command, ERROR_COMMAND_REQUIRED,
                "系统编码规则保存命令不能为空");
        SysCodeRule rule = new SysCodeRule(null,
                requireText(requiredCommand.getTenantId(), ERROR_TENANT_REQUIRED, "租户业务编码不能为空"),
                requireText(requiredCommand.getRuleCode(), ERROR_RULE_CODE_REQUIRED, "编码规则编码不能为空"),
                requireText(requiredCommand.getRuleName(), ERROR_RULE_NAME_REQUIRED, "编码规则名称不能为空"),
                trimToNull(requiredCommand.getPrefix()), normalizeDatePattern(requiredCommand.getDatePattern()),
                requirePositive(requiredCommand.getSequenceLength(), ERROR_SEQUENCE_LENGTH_REQUIRED,
                        "序列号长度不能为空"),
                normalizeCurrentValue(requiredCommand.getCurrentValue()), requiredCommand.isEnabled(), null, null);
        codeRuleRepository.save(rule);
    }

    @Override
    @Transactional
    public String generateNextCode(String tenantId, String ruleCode, LocalDate businessDate) {
        String requiredTenantId = requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空");
        String requiredRuleCode = requireText(ruleCode, ERROR_RULE_CODE_REQUIRED, "编码规则编码不能为空");
        LocalDate requiredBusinessDate = requireObject(businessDate, ERROR_BUSINESS_DATE_REQUIRED,
                "业务日期不能为空");
        SysCodeRule rule = codeRuleRepository.findByTenantIdAndRuleCode(requiredTenantId, requiredRuleCode)
                .filter(SysCodeRule::isEnabled)
                .orElseThrow(() -> new BusinessException(ERROR_RULE_NOT_AVAILABLE, "编码规则不存在或未启用"));
        int nextValue = normalizeCurrentValue(rule.getCurrentValue()) + 1;
        codeRuleRepository.updateCurrentValue(requiredTenantId, requiredRuleCode, nextValue);
        return buildCode(rule, requiredBusinessDate, nextValue);
    }

    private String buildCode(SysCodeRule rule, LocalDate businessDate, int nextValue) {
        String prefix = rule.getPrefix() == null ? "" : rule.getPrefix();
        String datePart = rule.getDatePattern() == null || rule.getDatePattern().isBlank()
                ? ""
                : businessDate.format(DateTimeFormatter.ofPattern(rule.getDatePattern()));
        int sequenceLength = requirePositive(rule.getSequenceLength(), ERROR_SEQUENCE_LENGTH_REQUIRED,
                "序列号长度不能为空");
        String sequence = String.format("%0" + sequenceLength + "d", nextValue);
        return prefix + datePart + sequence;
    }

    private SysCodeRuleResponse toResponse(SysCodeRule rule) {
        return new SysCodeRuleResponse(rule.getRuleCode(), rule.getRuleName(), rule.getPrefix(),
                rule.getDatePattern(), rule.getSequenceLength(), rule.getCurrentValue(), rule.isEnabled());
    }

    private String normalizeDatePattern(String datePattern) {
        String normalized = trimToNull(datePattern);
        if (normalized == null) {
            return null;
        }
        try {
            DateTimeFormatter.ofPattern(normalized);
            return normalized;
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ERROR_DATE_PATTERN_INVALID, "编码规则日期格式不合法: " + normalized);
        }
    }

    private Integer normalizeCurrentValue(Integer currentValue) {
        return currentValue == null || currentValue < 0 ? 0 : currentValue;
    }

    /**
     * 校验整数必须大于 0。
     *
     * @param value 原始整数
     * @param code 业务错误码
     * @param message 校验失败提示
     * @return 校验后的整数
     */
    private Integer requirePositive(Integer value, String code, String message) {
        if (value == null || value <= 0) {
            throw new BusinessException(code, message);
        }
        return value;
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
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new BusinessException(code, message);
        }
        return normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
