/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.mybatis;

/**
 * 低代码字段模型持久化记录。
 *
 * <p>用于保存字段类型、精度、表单展示、列表展示和查询配置。</p>
 *
 * @param id 数据库主键
 * @param tableModelId 所属表模型主键
 * @param fieldCode 字段编码，对应物理列名
 * @param fieldName 字段中文名称
 * @param fieldType 字段类型编码
 * @param lengthValue 字段长度
 * @param scaleValue 小数精度
 * @param required 是否必填
 * @param primaryKey 是否主键字段
 * @param autoIncrement 是否自增字段
 * @param listVisible 是否在列表显示
 * @param formVisible 是否在表单显示
 * @param queryable 是否作为查询条件
 * @param dictCode 绑定的系统字典编码
 * @param sortOrder 字段排序号
 * @param comment 字段说明
 */
public record LowcodeColumnModelRecord(
    Long id,
    Long tableModelId,
    String fieldCode,
    String fieldName,
    String fieldType,
    Integer lengthValue,
    Integer scaleValue,
    Boolean required,
    Boolean primaryKey,
    Boolean autoIncrement,
    Boolean listVisible,
    Boolean formVisible,
    Boolean queryable,
    String dictCode,
    Integer sortOrder,
    String comment
) {
}
