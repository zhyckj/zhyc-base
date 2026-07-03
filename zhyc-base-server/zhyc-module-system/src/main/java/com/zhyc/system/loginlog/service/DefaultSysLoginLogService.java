/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.loginlog.service;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.loginlog.domain.SysLoginLog;
import com.zhyc.system.loginlog.domain.SysLoginResult;
import com.zhyc.system.loginlog.repository.SysLoginLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 默认系统登录日志业务服务实现。
 */
@Service
public class DefaultSysLoginLogService implements SysLoginLogService {

    /** 最近登录日志最大查询条数，避免后台页面一次性拉取过多数据。 */
    private static final int MAX_RECENT_LIMIT = 200;
    /** 登录日志记录命令不能为空错误码。 */
    private static final String ERROR_COMMAND_REQUIRED = "ZHYC_SYS_LOGIN_LOG_COMMAND_REQUIRED";
    /** 租户业务编码不能为空错误码。 */
    private static final String ERROR_TENANT_REQUIRED = "ZHYC_SYS_LOGIN_LOG_TENANT_REQUIRED";
    /** 登录方式不能为空错误码。 */
    private static final String ERROR_LOGIN_TYPE_REQUIRED = "ZHYC_SYS_LOGIN_LOG_TYPE_REQUIRED";
    /** 登录结果不能为空错误码。 */
    private static final String ERROR_RESULT_REQUIRED = "ZHYC_SYS_LOGIN_LOG_RESULT_REQUIRED";
    /** 登录结果不支持错误码。 */
    private static final String ERROR_RESULT_UNSUPPORTED = "ZHYC_SYS_LOGIN_LOG_RESULT_UNSUPPORTED";

    /** 系统登录日志仓储。 */
    private final SysLoginLogRepository loginLogRepository;

    /**
     * 创建默认系统登录日志业务服务。
     *
     * @param loginLogRepository 系统登录日志仓储
     */
    public DefaultSysLoginLogService(SysLoginLogRepository loginLogRepository) {
        this.loginLogRepository = Objects.requireNonNull(loginLogRepository, "系统登录日志仓储不能为空");
    }

    /**
     * 记录一条登录日志，并对租户、登录方式、登录结果等必填字段做基础校验。
     *
     * @param command 登录日志记录命令
     */
    @Override
    @Transactional
    public void record(SysLoginLogRecordCommand command) {
        SysLoginLogRecordCommand requiredCommand = requireObject(command, ERROR_COMMAND_REQUIRED,
                "系统登录日志记录命令不能为空");
        SysLoginLog loginLog = new SysLoginLog(null,
                requireText(requiredCommand.getTenantId(), ERROR_TENANT_REQUIRED, "租户业务编码不能为空"),
                requiredCommand.getUserId(), trimToNull(requiredCommand.getUsername()),
                requireText(requiredCommand.getLoginType(), ERROR_LOGIN_TYPE_REQUIRED, "登录方式不能为空"),
                normalizeResult(requireText(requiredCommand.getResult(), ERROR_RESULT_REQUIRED, "登录结果不能为空")),
                trimToNull(requiredCommand.getClientIp()), trimToNull(requiredCommand.getUserAgent()),
                LocalDateTime.now());
        loginLogRepository.save(loginLog);
    }

    /**
     * 查询租户最近登录日志，并限制最大返回条数。
     *
     * @param tenantId 租户业务编码
     * @param limit 查询条数上限
     * @return 最近登录日志列表
     */
    @Override
    public List<SysLoginLogResponse> listRecent(String tenantId, int limit) {
        String requiredTenantId = requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空");
        int boundedLimit = Math.max(1, Math.min(limit, MAX_RECENT_LIMIT));
        return loginLogRepository.findRecentByTenantId(requiredTenantId, boundedLimit).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * 将领域模型转换为接口响应对象。
     *
     * @param loginLog 系统登录日志领域模型
     * @return 系统登录日志响应对象
     */
    private SysLoginLogResponse toResponse(SysLoginLog loginLog) {
        return new SysLoginLogResponse(loginLog.getId(), loginLog.getTenantId(), loginLog.getUserId(),
                loginLog.getUsername(), loginLog.getLoginType(), loginLog.getResult(), loginLog.getClientIp(),
                loginLog.getUserAgent(), loginLog.getCreatedAt());
    }

    /**
     * 校验并规范化登录结果。
     *
     * @param result 登录结果编码
     * @return 规范化后的登录结果编码
     */
    private String normalizeResult(String result) {
        try {
            return SysLoginResult.fromCode(result).getCode();
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ERROR_RESULT_UNSUPPORTED, ex.getMessage());
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
     * 校验文本字段不能为空，并返回去除首尾空白后的值。
     *
     * @param value 待校验文本
     * @param message 校验失败提示
     * @return 标准化后的文本
     */
    private String requireText(String value, String code, String message) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new BusinessException(code, message);
        }
        return normalized;
    }

    /**
     * 去除文本首尾空白，空文本统一转换为 {@code null}。
     *
     * @param value 原始文本
     * @return 标准化后的文本
     */
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
