/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.exceptionlog.service;

import com.zhyc.system.exceptionlog.domain.SysExceptionLog;
import com.zhyc.system.exceptionlog.repository.SysExceptionLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 默认系统异常日志业务服务实现。
 */
@Service
public class DefaultSysExceptionLogService implements SysExceptionLogService {

    /** 最近异常日志最大查询条数，避免后台页面一次性拉取过多堆栈数据。 */
    private static final int MAX_RECENT_LIMIT = 200;

    /** 系统异常日志仓储。 */
    private final SysExceptionLogRepository exceptionLogRepository;

    /**
     * 创建默认系统异常日志业务服务。
     *
     * @param exceptionLogRepository 系统异常日志仓储
     */
    public DefaultSysExceptionLogService(SysExceptionLogRepository exceptionLogRepository) {
        this.exceptionLogRepository = Objects.requireNonNull(exceptionLogRepository, "系统异常日志仓储不能为空");
    }

    /**
     * 记录一条异常日志，并对租户、请求地址和异常类名等必填字段做基础校验。
     *
     * @param command 异常日志记录命令
     */
    @Override
    @Transactional
    public void record(SysExceptionLogRecordCommand command) {
        Objects.requireNonNull(command, "系统异常日志记录命令不能为空");
        SysExceptionLog exceptionLog = new SysExceptionLog(null, requireText(command.getTenantId(), "租户业务编码不能为空"),
                trimToNull(command.getTraceId()), command.getUserId(), trimToNull(command.getUsername()),
                requireText(command.getRequestUri(), "请求地址不能为空"),
                requireText(command.getRequestMethod(), "请求方法不能为空"),
                requireText(command.getExceptionName(), "异常类名不能为空"), trimToNull(command.getMessage()),
                trimToNull(command.getStackTrace()), trimToNull(command.getClientIp()), LocalDateTime.now());
        exceptionLogRepository.save(exceptionLog);
    }

    /**
     * 查询租户最近异常日志，并限制最大返回条数。
     *
     * @param tenantId 租户业务编码
     * @param limit 查询条数上限
     * @return 最近异常日志列表
     */
    @Override
    public List<SysExceptionLogResponse> listRecent(String tenantId, int limit) {
        String requiredTenantId = requireText(tenantId, "租户业务编码不能为空");
        int boundedLimit = Math.max(1, Math.min(limit, MAX_RECENT_LIMIT));
        return exceptionLogRepository.findRecentByTenantId(requiredTenantId, boundedLimit).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * 将领域模型转换为接口响应对象。
     *
     * @param exceptionLog 系统异常日志领域模型
     * @return 系统异常日志响应对象
     */
    private SysExceptionLogResponse toResponse(SysExceptionLog exceptionLog) {
        return new SysExceptionLogResponse(exceptionLog.getId(), exceptionLog.getTenantId(),
                exceptionLog.getTraceId(), exceptionLog.getUserId(), exceptionLog.getUsername(),
                exceptionLog.getRequestUri(), exceptionLog.getRequestMethod(), exceptionLog.getExceptionName(),
                exceptionLog.getMessage(), exceptionLog.getStackTrace(), exceptionLog.getClientIp(),
                exceptionLog.getCreatedAt());
    }

    /**
     * 校验文本字段不能为空，并返回去除首尾空白后的值。
     *
     * @param value 待校验文本
     * @param message 校验失败提示
     * @return 标准化后的文本
     */
    private String requireText(String value, String message) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw com.zhyc.system.support.SystemServiceValidation.businessFailure(message);
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
