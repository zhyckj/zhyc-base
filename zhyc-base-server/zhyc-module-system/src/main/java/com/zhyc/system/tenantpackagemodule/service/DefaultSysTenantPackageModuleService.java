/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantpackagemodule.service;

import com.zhyc.system.module.domain.SysModuleResource;
import com.zhyc.system.module.repository.SysModuleRepository;
import com.zhyc.system.tenantpackagemodule.domain.SysTenantPackageModule;
import com.zhyc.system.tenantpackagemodule.repository.SysTenantPackageModuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 默认租户套餐模块授权业务服务实现。
 */
@Service
public class DefaultSysTenantPackageModuleService implements SysTenantPackageModuleService {

    /** 租户套餐模块授权仓储。 */
    private final SysTenantPackageModuleRepository moduleRepository;
    /** 系统模块仓储，用于校验套餐授权资源是否来自模块元数据。 */
    private final SysModuleRepository sysModuleRepository;

    /**
     * 创建默认租户套餐模块授权业务服务。
     *
     * @param moduleRepository 租户套餐模块授权仓储
     * @param sysModuleRepository 系统模块仓储
     */
    public DefaultSysTenantPackageModuleService(SysTenantPackageModuleRepository moduleRepository,
                                                SysModuleRepository sysModuleRepository) {
        this.moduleRepository = Objects.requireNonNull(moduleRepository, "租户套餐模块授权仓储不能为空");
        this.sysModuleRepository = Objects.requireNonNull(sysModuleRepository, "系统模块仓储不能为空");
    }

    @Override
    public List<TenantPackageModuleResponse> listGrants(Long packageId) {
        return moduleRepository.findByPackageId(requireId(packageId, "租户套餐主键不能为空")).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void bindGrants(TenantPackageModuleBindCommand command) {
        Objects.requireNonNull(command, "租户套餐模块授权绑定命令不能为空");
        Long requiredPackageId = requireId(command.getPackageId(), "租户套餐主键不能为空");
        List<SysTenantPackageModule> grants = command.getGrants().stream()
                .map(grant -> toDomain(requiredPackageId, grant))
                .toList();
        grants.forEach(this::validateGrantResource);
        validateDuplicateGrantResources(grants);
        moduleRepository.deleteByPackageId(requiredPackageId);
        if (!grants.isEmpty()) {
            moduleRepository.batchInsert(grants);
        }
    }

    private TenantPackageModuleResponse toResponse(SysTenantPackageModule grant) {
        return new TenantPackageModuleResponse(grant.getId(), grant.getPackageId(), grant.getModuleCode(),
                grant.getMenuCode(), grant.getPermission(), grant.getCreatedAt());
    }

    private SysTenantPackageModule toDomain(Long packageId, TenantPackageModuleGrantCommand grant) {
        Objects.requireNonNull(grant, "租户套餐模块授权项不能为空");
        return new SysTenantPackageModule(null, packageId, requireText(grant.getModuleCode(), "模块编码不能为空"),
                trimToNull(grant.getMenuCode()), trimToNull(grant.getPermission()), null);
    }

    /**
     * 校验套餐授权项引用的菜单和权限均来自模块元数据。
     *
     * <p>该校验阻断前端或接口传入任意权限编码，避免套餐授权越过模块注册表和权限审计边界。</p>
     *
     * @param grant 套餐授权项
     */
    private void validateGrantResource(SysTenantPackageModule grant) {
        List<SysModuleResource> resources = sysModuleRepository.findResourcesByModuleCode(grant.getModuleCode());
        if (resources.isEmpty()) {
            throw com.zhyc.system.support.SystemServiceValidation.businessFailure(
                    "套餐授权模块不存在或未同步资源: " + grant.getModuleCode());
        }
        if (grant.getMenuCode() != null && !resourceCodes(resources, "menu").contains(grant.getMenuCode())) {
            throw com.zhyc.system.support.SystemServiceValidation.businessFailure(
                    "套餐授权菜单不属于模块: " + grant.getMenuCode());
        }
        if (grant.getPermission() != null && !resourceCodes(resources, "permission").contains(grant.getPermission())) {
            throw com.zhyc.system.support.SystemServiceValidation.businessFailure(
                    "套餐授权权限不属于模块: " + grant.getPermission());
        }
    }

    /**
     * 提取指定类型的模块资源编码。
     *
     * @param resources 模块资源列表
     * @param resourceType 资源类型
     * @return 模块资源编码集合
     */
    private Set<String> resourceCodes(List<SysModuleResource> resources, String resourceType) {
        return resources.stream()
                .filter(resource -> resourceType.equals(resource.getResourceType()))
                .map(SysModuleResource::getResourceCode)
                .collect(Collectors.toSet());
    }

    /**
     * 校验同一次套餐授权绑定请求内不存在重复资源。
     *
     * <p>重复授权会触发数据库唯一键冲突；提前用业务异常拦截，可保证旧授权不会被删除。</p>
     *
     * @param grants 套餐授权项列表
     */
    private void validateDuplicateGrantResources(List<SysTenantPackageModule> grants) {
        Set<String> seenKeys = new LinkedHashSet<>();
        for (SysTenantPackageModule grant : grants) {
            String key = grantResourceKey(grant);
            if (!seenKeys.add(key)) {
                throw com.zhyc.system.support.SystemServiceValidation.businessFailure(
                        "套餐授权资源重复: " + key);
            }
        }
    }

    /**
     * 生成套餐授权唯一资源键。
     *
     * @param grant 套餐授权项
     * @return 由模块、菜单和权限组成的资源键
     */
    private String grantResourceKey(SysTenantPackageModule grant) {
        return grant.getModuleCode() + "|" + nullToEmpty(grant.getMenuCode()) + "|"
                + nullToEmpty(grant.getPermission());
    }

    /**
     * 空值转为空字符串。
     *
     * @param value 原始文本
     * @return 非空文本
     */
    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private Long requireId(Long value, String message) {
        if (value == null) {
            throw com.zhyc.system.support.SystemServiceValidation.businessFailure(message);
        }
        return value;
    }

    private String requireText(String value, String message) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw com.zhyc.system.support.SystemServiceValidation.businessFailure(message);
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
