/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator.mybatis;

/**
 * 低代码生成记录持久化对象。
 *
 * @param id 数据库主键
 * @param tenantId 租户业务编码
 * @param tableModelCode 表模型编码
 * @param target 生成目标端
 * @param moduleName 业务模块名称
 * @param entityName 业务实体名称
 * @param overwriteStrategy 生成文件覆盖策略
 * @param fileCount 生成文件数量
 * @param fileManifestJson 生成文件清单 JSON
 * @param status 生成状态
 * @param errorMessage 失败原因
 */
public record LowcodeGenerationRecordRecord(
    Long id,
    String tenantId,
    String tableModelCode,
    String target,
    String moduleName,
    String entityName,
    String overwriteStrategy,
    Integer fileCount,
    String fileManifestJson,
    String status,
    String errorMessage
) {
}
