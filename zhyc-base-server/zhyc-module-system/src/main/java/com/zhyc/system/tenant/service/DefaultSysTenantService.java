/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenant.service;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.common.tenant.TenantIsolationMode;
import com.zhyc.system.tenant.domain.Tenant;
import com.zhyc.system.tenant.domain.SysTenantStatus;
import com.zhyc.system.tenant.repository.SysTenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 默认系统租户业务服务实现。
 */
@Service
public class DefaultSysTenantService implements SysTenantService {

    /** 默认启用状态。 */
    private static final String DEFAULT_STATUS = SysTenantStatus.ENABLED.getCode();
    /** 租户创建命令不能为空错误码。 */
    private static final String ERROR_COMMAND_REQUIRED = "ZHYC_SYS_TENANT_COMMAND_REQUIRED";
    /** 租户业务编码不能为空错误码。 */
    private static final String ERROR_TENANT_REQUIRED = "ZHYC_SYS_TENANT_ID_REQUIRED";
    /** 租户名称不能为空错误码。 */
    private static final String ERROR_NAME_REQUIRED = "ZHYC_SYS_TENANT_NAME_REQUIRED";
    /** 租户不存在错误码。 */
    private static final String ERROR_TENANT_NOT_FOUND = "ZHYC_SYS_TENANT_NOT_FOUND";
    /** 租户状态不能为空错误码。 */
    private static final String ERROR_STATUS_REQUIRED = "ZHYC_SYS_TENANT_STATUS_REQUIRED";
    /** 租户状态不支持错误码。 */
    private static final String ERROR_STATUS_UNSUPPORTED = "ZHYC_SYS_TENANT_STATUS_UNSUPPORTED";

    /** 系统租户仓储。 */
    private final SysTenantRepository tenantRepository;

    /**
     * 创建默认系统租户业务服务。
     *
     * @param tenantRepository 系统租户仓储
     */
    public DefaultSysTenantService(SysTenantRepository tenantRepository) {
        this.tenantRepository = Objects.requireNonNull(tenantRepository, "系统租户仓储不能为空");
    }

    @Override
    public List<SysTenantResponse> listTenants(String status) {
        String requiredStatus = normalizeStatus(requireText(status, ERROR_STATUS_REQUIRED, "租户状态不能为空"));
        return tenantRepository.findByStatus(requiredStatus).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * 查询登录账号可访问的启用租户列表。
     *
     * <p>首期以租户内启用用户账号作为访问依据，返回结果只包含启用租户。</p>
     *
     * @param username 登录账号
     * @return 授权租户列表
     */
    @Override
    public List<SysTenantResponse> listAuthorizedTenants(String username) {
        String requiredUsername = requireText(username, "ZHYC_SYS_TENANT_AUTH_USERNAME_REQUIRED",
                "登录账号不能为空");
        return tenantRepository.findAuthorizedByUsername(requiredUsername).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void createTenant(SysTenantCreateCommand command) {
        SysTenantCreateCommand requiredCommand = requireObject(command, ERROR_COMMAND_REQUIRED,
                "系统租户创建命令不能为空");
        Tenant tenant = new Tenant();
        tenant.setTenantId(requireText(requiredCommand.getTenantId(), ERROR_TENANT_REQUIRED, "租户业务编码不能为空"));
        tenant.setName(requireText(requiredCommand.getName(), ERROR_NAME_REQUIRED, "租户名称不能为空"));
        tenant.setPackageId(requiredCommand.getPackageId());
        tenant.setIsolationMode(defaultIsolationMode(requiredCommand.getIsolationMode()));
        tenant.setStatus(normalizeStatus(defaultText(requiredCommand.getStatus(), DEFAULT_STATUS)));
        tenant.setContactName(trimToNull(requiredCommand.getContactName()));
        tenant.setContactPhone(trimToNull(requiredCommand.getContactPhone()));
        tenant.setExpireAt(requiredCommand.getExpireAt());
        tenantRepository.save(tenant);
    }

    @Override
    @Transactional
    public void updateTenant(String tenantId, SysTenantCreateCommand command) {
        String requiredTenantId = requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空");
        SysTenantCreateCommand requiredCommand = requireObject(command, ERROR_COMMAND_REQUIRED,
                "系统租户更新命令不能为空");
        ensureTenantExists(requiredTenantId);
        validateTenantId(requiredTenantId, requiredCommand.getTenantId());

        Tenant tenant = new Tenant();
        tenant.setTenantId(requiredTenantId);
        tenant.setName(requireText(requiredCommand.getName(), ERROR_NAME_REQUIRED, "租户名称不能为空"));
        tenant.setPackageId(requiredCommand.getPackageId());
        tenant.setIsolationMode(defaultIsolationMode(requiredCommand.getIsolationMode()));
        tenant.setStatus(normalizeStatus(defaultText(requiredCommand.getStatus(), DEFAULT_STATUS)));
        tenant.setContactName(trimToNull(requiredCommand.getContactName()));
        tenant.setContactPhone(trimToNull(requiredCommand.getContactPhone()));
        tenant.setExpireAt(requiredCommand.getExpireAt());
        tenantRepository.update(tenant);
    }

    @Override
    @Transactional
    public void changeStatus(String tenantId, String status) {
        tenantRepository.updateStatus(requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空"),
                normalizeStatus(requireText(status, ERROR_STATUS_REQUIRED, "租户状态不能为空")));
    }

    @Override
    @Transactional
    public void deleteTenant(String tenantId) {
        tenantRepository.deleteByTenantId(requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空"));
    }

    private SysTenantResponse toResponse(Tenant tenant) {
        return new SysTenantResponse(tenant.getId(), tenant.getTenantId(), tenant.getName(), tenant.getPackageId(),
                tenant.getIsolationMode(), tenant.getStatus(), tenant.getContactName(), tenant.getContactPhone(),
                tenant.getExpireAt(), tenant.getCreatedAt(), tenant.getUpdatedAt());
    }

    private TenantIsolationMode defaultIsolationMode(TenantIsolationMode isolationMode) {
        return isolationMode == null ? TenantIsolationMode.TENANT_COLUMN : isolationMode;
    }

    private String normalizeStatus(String status) {
        try {
            return SysTenantStatus.fromCode(status).getCode();
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ERROR_STATUS_UNSUPPORTED, ex.getMessage());
        }
    }

    private String defaultText(String value, String defaultValue) {
        String normalized = trimToNull(value);
        return normalized == null ? defaultValue : normalized;
    }

    /**
     * 校验租户是否存在。
     *
     * @param tenantId 租户业务编码
     */
    private void ensureTenantExists(String tenantId) {
        if (tenantRepository.findByTenantId(tenantId) == null) {
            throw new BusinessException(ERROR_TENANT_NOT_FOUND, "租户不存在: " + tenantId);
        }
    }

    /**
     * 校验请求体中的租户编码与路径参数一致。
     *
     * @param pathTenantId 路径租户编码
     * @param bodyTenantId 请求体租户编码
     */
    private void validateTenantId(String pathTenantId, String bodyTenantId) {
        String normalizedBodyTenantId = trimToNull(bodyTenantId);
        if (normalizedBodyTenantId != null && !pathTenantId.equals(normalizedBodyTenantId)) {
            throw new BusinessException("ZHYC_SYS_TENANT_ID_MISMATCH", "请求租户编码与路径租户编码不一致");
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
