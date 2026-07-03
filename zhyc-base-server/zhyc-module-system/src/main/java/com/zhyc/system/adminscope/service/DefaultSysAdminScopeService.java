/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.adminscope.service;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.adminscope.domain.SysAdminScope;
import com.zhyc.system.adminscope.domain.SysAdminScopeType;
import com.zhyc.system.adminscope.repository.SysAdminScopeRepository;
import com.zhyc.system.module.domain.SysModule;
import com.zhyc.system.module.repository.SysModuleRepository;
import com.zhyc.system.org.domain.SysOrg;
import com.zhyc.system.org.repository.SysOrgRepository;
import com.zhyc.system.user.domain.SysUser;
import com.zhyc.system.user.repository.SysUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 默认系统管理员管理范围业务服务实现。
 */
@Service
public class DefaultSysAdminScopeService implements SysAdminScopeService {

    /** 管理员范围绑定命令不能为空错误码。 */
    private static final String ERROR_COMMAND_REQUIRED = "ZHYC_SYS_ADMIN_SCOPE_COMMAND_REQUIRED";
    /** 租户业务编码不能为空错误码。 */
    private static final String ERROR_TENANT_REQUIRED = "ZHYC_SYS_ADMIN_SCOPE_TENANT_REQUIRED";
    /** 管理员用户主键不能为空错误码。 */
    private static final String ERROR_USER_REQUIRED = "ZHYC_SYS_ADMIN_SCOPE_USER_REQUIRED";
    /** 管理员管理范围类型不支持错误码。 */
    private static final String ERROR_SCOPE_TYPE_UNSUPPORTED = "ZHYC_SYS_ADMIN_SCOPE_TYPE_UNSUPPORTED";
    /** 管理员管理范围引用无效错误码。 */
    private static final String ERROR_SCOPE_REF_INVALID = "ZHYC_SYS_ADMIN_SCOPE_REF_INVALID";

    /** 系统管理员管理范围仓储。 */
    private final SysAdminScopeRepository adminScopeRepository;
    /** 系统用户仓储，用于校验管理员用户是否属于当前租户。 */
    private final SysUserRepository userRepository;
    /** 系统组织仓储，用于校验组织范围是否属于当前租户。 */
    private final SysOrgRepository orgRepository;
    /** 系统模块仓储，用于校验模块范围是否存在且启用。 */
    private final SysModuleRepository moduleRepository;

    /**
     * 创建默认系统管理员管理范围业务服务。
     *
     * @param adminScopeRepository 系统管理员管理范围仓储
     * @param userRepository 系统用户仓储
     * @param orgRepository 系统组织仓储
     * @param moduleRepository 系统模块仓储
     */
    public DefaultSysAdminScopeService(SysAdminScopeRepository adminScopeRepository,
                                       SysUserRepository userRepository,
                                       SysOrgRepository orgRepository,
                                       SysModuleRepository moduleRepository) {
        this.adminScopeRepository = Objects.requireNonNull(adminScopeRepository,
                "系统管理员管理范围仓储不能为空");
        this.userRepository = Objects.requireNonNull(userRepository, "系统用户仓储不能为空");
        this.orgRepository = Objects.requireNonNull(orgRepository, "系统组织仓储不能为空");
        this.moduleRepository = Objects.requireNonNull(moduleRepository, "系统模块仓储不能为空");
    }

    @Override
    public List<SysAdminScopeResponse> listAdminScopes(String tenantId, Long userId) {
        String requiredTenantId = requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空");
        Long requiredUserId = requirePositive(userId, ERROR_USER_REQUIRED, "管理员用户主键不能为空");
        return adminScopeRepository.findByTenantIdAndUserId(requiredTenantId, requiredUserId).stream()
                .sorted(Comparator.comparing(SysAdminScope::getScopeType, Comparator.nullsLast(String::compareTo))
                        .thenComparing(SysAdminScope::getScopeRefCode, Comparator.nullsLast(String::compareTo)))
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void bindAdminScopes(AdminScopeBindCommand command) {
        AdminScopeBindCommand requiredCommand = requireObject(command, ERROR_COMMAND_REQUIRED,
                "管理员管理范围绑定命令不能为空");
        String requiredTenantId = requireText(requiredCommand.getTenantId(), ERROR_TENANT_REQUIRED,
                "租户业务编码不能为空");
        Long requiredUserId = requirePositive(requiredCommand.getUserId(), ERROR_USER_REQUIRED,
                "管理员用户主键不能为空");
        validateTenantAdminUser(requiredTenantId, requiredUserId);
        Map<String, SysAdminScope> scopes = new LinkedHashMap<>();
        if (requiredCommand.getScopes() != null) {
            for (AdminScopeBindItem item : requiredCommand.getScopes()) {
                normalizeScope(requiredTenantId, requiredUserId, item, scopes);
            }
        }
        validateScopeRefs(requiredTenantId, scopes.values());
        adminScopeRepository.replaceAdminScopes(requiredTenantId, requiredUserId, List.copyOf(scopes.values()));
    }

    private void normalizeScope(String tenantId, Long userId, AdminScopeBindItem item,
                                Map<String, SysAdminScope> scopes) {
        if (item == null || isBlank(item.getScopeType()) || isBlank(item.getScopeRefCode())) {
            return;
        }
        String scopeType = normalizeScopeType(item.getScopeType().trim());
        String scopeRefCode = item.getScopeRefCode().trim();
        String scopeKey = scopeType + ":" + scopeRefCode;
        scopes.putIfAbsent(scopeKey, new SysAdminScope(null, tenantId, userId, scopeType, scopeRefCode,
                null, null));
    }

    private String normalizeScopeType(String scopeType) {
        try {
            return SysAdminScopeType.fromCode(scopeType).getCode();
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ERROR_SCOPE_TYPE_UNSUPPORTED, ex.getMessage());
        }
    }

    private SysAdminScopeResponse toResponse(SysAdminScope scope) {
        return new SysAdminScopeResponse(scope.getScopeType(), scope.getScopeRefCode(), scope.getScopeName());
    }

    /**
     * 校验管理员用户属于当前租户。
     *
     * <p>管理员范围绑定必须先确认用户归属，避免跨租户用户被写入管理范围。</p>
     *
     * @param tenantId 租户业务编码
     * @param userId 管理员用户主键
     */
    private void validateTenantAdminUser(String tenantId, Long userId) {
        boolean exists = userRepository.findByTenantId(tenantId).stream()
                .map(SysUser::getId)
                .filter(Objects::nonNull)
                .anyMatch(userId::equals);
        if (!exists) {
            throw new BusinessException(ERROR_SCOPE_REF_INVALID, "管理员用户主键不属于当前租户：" + userId);
        }
    }

    /**
     * 校验管理员范围引用值有效。
     *
     * <p>范围引用必须在替换前统一校验，避免非法范围导致旧授权被提前清空。</p>
     *
     * @param tenantId 租户业务编码
     * @param scopes 待绑定管理员范围集合
     */
    private void validateScopeRefs(String tenantId, Iterable<SysAdminScope> scopes) {
        Set<Long> tenantOrgIds = null;
        Set<String> enabledModuleCodes = null;
        for (SysAdminScope scope : scopes) {
            SysAdminScopeType scopeType = SysAdminScopeType.fromCode(scope.getScopeType());
            if (scopeType == SysAdminScopeType.TENANT) {
                validateTenantScope(tenantId, scope.getScopeRefCode());
            } else if (scopeType == SysAdminScopeType.ORG) {
                if (tenantOrgIds == null) {
                    tenantOrgIds = orgRepository.findByTenantId(tenantId).stream()
                            .map(SysOrg::getId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
                }
                validateOrgScope(scope.getScopeRefCode(), tenantOrgIds);
            } else if (scopeType == SysAdminScopeType.MODULE) {
                if (enabledModuleCodes == null) {
                    enabledModuleCodes = moduleRepository.findAll().stream()
                            .filter(SysModule::isEnabled)
                            .map(SysModule::getModuleCode)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
                }
                validateModuleScope(scope.getScopeRefCode(), enabledModuleCodes);
            }
        }
    }

    /**
     * 校验租户范围只能指向当前租户。
     *
     * @param tenantId 当前租户业务编码
     * @param scopeRefCode 租户范围引用编码
     */
    private void validateTenantScope(String tenantId, String scopeRefCode) {
        if (!tenantId.equals(scopeRefCode)) {
            throw new BusinessException(ERROR_SCOPE_REF_INVALID, "租户范围必须等于当前租户：" + scopeRefCode);
        }
    }

    /**
     * 校验组织范围属于当前租户。
     *
     * @param scopeRefCode 组织范围引用编码
     * @param tenantOrgIds 当前租户组织主键集合
     */
    private void validateOrgScope(String scopeRefCode, Set<Long> tenantOrgIds) {
        Long orgId;
        try {
            orgId = Long.valueOf(scopeRefCode);
        } catch (NumberFormatException ex) {
            throw new BusinessException(ERROR_SCOPE_REF_INVALID, "组织范围不属于当前租户：" + scopeRefCode);
        }
        if (orgId <= 0 || !tenantOrgIds.contains(orgId)) {
            throw new BusinessException(ERROR_SCOPE_REF_INVALID, "组织范围不属于当前租户：" + scopeRefCode);
        }
    }

    /**
     * 校验模块范围存在且启用。
     *
     * @param scopeRefCode 模块范围引用编码
     * @param enabledModuleCodes 已启用模块编码集合
     */
    private void validateModuleScope(String scopeRefCode, Set<String> enabledModuleCodes) {
        if (!enabledModuleCodes.contains(scopeRefCode)) {
            throw new BusinessException(ERROR_SCOPE_REF_INVALID, "模块范围不存在或未启用：" + scopeRefCode);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
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
        if (isBlank(value)) {
            throw new BusinessException(code, message);
        }
        return value.trim();
    }

    /**
     * 校验长整型主键必须为正数。
     *
     * @param value 原始主键
     * @param code 业务错误码
     * @param message 校验失败提示
     * @return 校验后的主键
     */
    private Long requirePositive(Long value, String code, String message) {
        if (value == null || value <= 0) {
            throw new BusinessException(code, message);
        }
        return value;
    }
}
