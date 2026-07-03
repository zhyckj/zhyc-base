/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.audit.service;

import java.util.List;

/**
 * 开放 API 调用审计业务服务。
 */
public interface OpenApiCallAuditService {

    /**
     * 查询租户指定应用的开放 API 调用审计列表。
     *
     * @param tenantId 租户业务编码
     * @param appCode 开发者应用编码
     * @return 开放 API 调用审计列表
     */
    List<OpenApiCallAuditResponse> listAudits(String tenantId, String appCode);

    /**
     * 查询租户指定应用的开放 API 错误日志列表。
     *
     * @param tenantId 租户业务编码
     * @param appCode 开发者应用编码
     * @return 开放 API 错误日志列表
     */
    List<OpenApiCallAuditResponse> listErrorLogs(String tenantId, String appCode);

    /**
     * 记录开放 API 调用审计。
     *
     * @param command 开放 API 调用审计记录命令
     */
    void record(OpenApiCallAuditRecordCommand command);
}
