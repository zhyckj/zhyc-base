/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator.mybatis;

/**
 * 低代码生成文件明细持久化对象。
 *
 * @param id 数据库主键
 * @param tenantId 租户业务编码
 * @param recordId 生成记录主键
 * @param templateCode 模板编码
 * @param filePath 生成文件路径
 * @param fileType 生成文件类型
 * @param overwriteMode 覆盖模式
 * @param contentHash 文件内容哈希
 */
public record LowcodeGenerationFileRecord(
    Long id,
    String tenantId,
    Long recordId,
    String templateCode,
    String filePath,
    String fileType,
    String overwriteMode,
    String contentHash
) {
}
