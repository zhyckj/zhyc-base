/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantpackage.service;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.tenantpackage.domain.SysTenantPackage;
import com.zhyc.system.tenantpackage.domain.SysTenantPackageStatus;
import com.zhyc.system.tenantpackage.repository.SysTenantPackageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 默认系统租户套餐业务服务实现。
 */
@Service
public class DefaultSysTenantPackageService implements SysTenantPackageService {

    /** 套餐编码不能为空错误码。 */
    private static final String ERROR_PACKAGE_CODE_REQUIRED = "ZHYC_SYS_TENANT_PACKAGE_CODE_REQUIRED";
    /** 套餐名称不能为空错误码。 */
    private static final String ERROR_PACKAGE_NAME_REQUIRED = "ZHYC_SYS_TENANT_PACKAGE_NAME_REQUIRED";
    /** 套餐状态不能为空错误码。 */
    private static final String ERROR_STATUS_REQUIRED = "ZHYC_SYS_TENANT_PACKAGE_STATUS_REQUIRED";
    /** 套餐状态不支持错误码。 */
    private static final String ERROR_STATUS_UNSUPPORTED =
            "ZHYC_SYS_TENANT_PACKAGE_STATUS_UNSUPPORTED";
    /** 套餐容量限制非法错误码。 */
    private static final String ERROR_LIMIT_INVALID = "ZHYC_SYS_TENANT_PACKAGE_LIMIT_INVALID";
    /** 套餐编码重复错误码。 */
    private static final String ERROR_PACKAGE_CODE_DUPLICATED = "ZHYC_SYS_TENANT_PACKAGE_CODE_DUPLICATED";

    /** 系统租户套餐仓储。 */
    private final SysTenantPackageRepository tenantPackageRepository;

    /**
     * 创建默认系统租户套餐业务服务。
     *
     * @param tenantPackageRepository 系统租户套餐仓储
     */
    public DefaultSysTenantPackageService(SysTenantPackageRepository tenantPackageRepository) {
        this.tenantPackageRepository = Objects.requireNonNull(tenantPackageRepository, "系统租户套餐仓储不能为空");
    }

    @Override
    public List<SysTenantPackageResponse> listPackages(String status) {
        String requiredStatus = normalizeStatus(requireText(status, ERROR_STATUS_REQUIRED, "套餐状态不能为空"));
        return tenantPackageRepository.findByStatus(requiredStatus).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public SysTenantPackageResponse createPackage(TenantPackageCreateCommand command) {
        if (command == null) {
            throw new BusinessException(ERROR_PACKAGE_CODE_REQUIRED, "套餐创建命令不能为空");
        }
        String packageCode = requireText(command.getPackageCode(), ERROR_PACKAGE_CODE_REQUIRED, "套餐编码不能为空");
        String packageName = requireText(command.getPackageName(), ERROR_PACKAGE_NAME_REQUIRED, "套餐名称不能为空");
        String packageStatus = normalizeStatus(requireText(command.getStatus(), ERROR_STATUS_REQUIRED, "套餐状态不能为空"));
        Integer maxUserCount = normalizeLimit(command.getMaxUserCount());
        Integer maxStorageMb = normalizeLimit(command.getMaxStorageMb());
        if (tenantPackageRepository.findByCode(packageCode).isPresent()) {
            throw new BusinessException(ERROR_PACKAGE_CODE_DUPLICATED, "套餐编码已存在");
        }

        SysTenantPackage tenantPackage = new SysTenantPackage(null, packageCode, packageName, packageStatus,
                maxUserCount, maxStorageMb, null, null);
        return toResponse(tenantPackageRepository.save(tenantPackage));
    }

    @Override
    @Transactional
    public void changeStatus(String packageCode, String status) {
        tenantPackageRepository.updateStatus(requireText(packageCode, ERROR_PACKAGE_CODE_REQUIRED, "套餐编码不能为空"),
                normalizeStatus(requireText(status, ERROR_STATUS_REQUIRED, "套餐状态不能为空")));
    }

    private SysTenantPackageResponse toResponse(SysTenantPackage tenantPackage) {
        return new SysTenantPackageResponse(tenantPackage.getId(), tenantPackage.getPackageCode(),
                tenantPackage.getPackageName(), tenantPackage.getStatus(), tenantPackage.getMaxUserCount(),
                tenantPackage.getMaxStorageMb(), tenantPackage.getCreatedAt(), tenantPackage.getUpdatedAt());
    }

    private String normalizeStatus(String status) {
        try {
            return SysTenantPackageStatus.fromCode(status).getCode();
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ERROR_STATUS_UNSUPPORTED, ex.getMessage());
        }
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

    private Integer normalizeLimit(Integer value) {
        int normalized = value == null ? 0 : value;
        if (normalized < 0) {
            throw new BusinessException(ERROR_LIMIT_INVALID, "套餐容量限制不能小于 0");
        }
        return normalized;
    }
}
