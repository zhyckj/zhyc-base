/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantparam.service;

import com.zhyc.common.cache.ZhycCacheNames;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.param.domain.SysParamValueType;
import com.zhyc.system.tenantparam.domain.SysTenantParam;
import com.zhyc.system.tenantparam.repository.SysTenantParamRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 默认租户参数业务服务实现。
 */
@Service
public class DefaultSysTenantParamService implements SysTenantParamService {

    /** 租户参数保存命令不能为空错误码。 */
    private static final String ERROR_COMMAND_REQUIRED = "ZHYC_SYS_TENANT_PARAM_COMMAND_REQUIRED";
    /** 租户业务编码不能为空错误码。 */
    private static final String ERROR_TENANT_REQUIRED = "ZHYC_SYS_TENANT_PARAM_TENANT_REQUIRED";
    /** 参数键不能为空错误码。 */
    private static final String ERROR_PARAM_KEY_REQUIRED = "ZHYC_SYS_TENANT_PARAM_KEY_REQUIRED";
    /** 参数值类型不能为空错误码。 */
    private static final String ERROR_VALUE_TYPE_REQUIRED = "ZHYC_SYS_TENANT_PARAM_VALUE_TYPE_REQUIRED";
    /** 参数值类型不支持错误码。 */
    private static final String ERROR_VALUE_TYPE_UNSUPPORTED =
            "ZHYC_SYS_TENANT_PARAM_VALUE_TYPE_UNSUPPORTED";

    /** 租户参数仓储。 */
    private final SysTenantParamRepository tenantParamRepository;

    /**
     * 创建默认租户参数业务服务。
     *
     * @param tenantParamRepository 租户参数仓储
     */
    public DefaultSysTenantParamService(SysTenantParamRepository tenantParamRepository) {
        this.tenantParamRepository = Objects.requireNonNull(tenantParamRepository, "租户参数仓储不能为空");
    }

    @Override
    @Cacheable(cacheNames = ZhycCacheNames.SYS_TENANT_PARAMS,
            key = "#tenantId == null ? '' : #tenantId.trim()")
    public List<SysTenantParamResponse> listParams(String tenantId) {
        String requiredTenantId = requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空");
        return tenantParamRepository.findByTenantId(requiredTenantId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public Optional<SysTenantParamResponse> findByKey(String tenantId, String paramKey) {
        String requiredTenantId = requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空");
        String requiredParamKey = requireText(paramKey, ERROR_PARAM_KEY_REQUIRED, "参数键不能为空");
        return tenantParamRepository.findByTenantIdAndParamKey(requiredTenantId, requiredParamKey)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = ZhycCacheNames.SYS_TENANT_PARAMS, allEntries = true)
    public void save(SysTenantParamSaveCommand command) {
        SysTenantParamSaveCommand requiredCommand = requireObject(command, ERROR_COMMAND_REQUIRED,
                "租户参数保存命令不能为空");
        SysTenantParam param = new SysTenantParam(null,
                requireText(requiredCommand.getTenantId(), ERROR_TENANT_REQUIRED, "租户业务编码不能为空"),
                requireText(requiredCommand.getParamKey(), ERROR_PARAM_KEY_REQUIRED, "参数键不能为空"),
                trimToNull(requiredCommand.getParamValue()),
                normalizeValueType(requireText(requiredCommand.getValueType(), ERROR_VALUE_TYPE_REQUIRED,
                        "参数值类型不能为空")), requiredCommand.isVisible(), null, null);
        tenantParamRepository.save(param);
    }

    private SysTenantParamResponse toResponse(SysTenantParam param) {
        return new SysTenantParamResponse(param.getId(), param.getTenantId(), param.getParamKey(),
                param.getParamValue(), param.getValueType(), param.isVisible());
    }

    private String normalizeValueType(String valueType) {
        try {
            return SysParamValueType.fromCode(valueType).getCode();
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ERROR_VALUE_TYPE_UNSUPPORTED, ex.getMessage());
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
