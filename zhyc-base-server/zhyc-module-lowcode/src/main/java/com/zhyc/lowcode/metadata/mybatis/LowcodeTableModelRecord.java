/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.mybatis;

/**
 * 低代码表模型持久化记录。
 *
 * <p>用于保存在线建模中的表级元数据，后续字段明细由字段模型记录维护。</p>
 *
 * @param id 数据库主键
 * @param tenantId 租户业务编码
 * @param dataSourceId 所属低代码数据源主键
 * @param code 表模型编码，租户内唯一
 * @param name 表模型名称
 * @param tableName 物理表名
 * @param status 模型状态编码
 */
public record LowcodeTableModelRecord(
    Long id,
    String tenantId,
    Long dataSourceId,
    String code,
    String name,
    String tableName,
    String status
) {
}
