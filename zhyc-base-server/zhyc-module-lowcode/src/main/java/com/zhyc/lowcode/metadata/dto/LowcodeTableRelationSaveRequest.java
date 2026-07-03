/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.dto;

import com.zhyc.lowcode.metadata.domain.LowcodeTableRelation;

/**
 * 低代码表关系保存请求。
 *
 * @param tenantId 租户业务编码
 * @param mainTableId 主表模型主键
 * @param subTableId 子表模型主键
 * @param relationType 关系类型
 * @param joinColumn 主表关联字段编码
 * @param refColumn 子表引用字段编码
 */
public record LowcodeTableRelationSaveRequest(
    String tenantId,
    Long mainTableId,
    Long subTableId,
    String relationType,
    String joinColumn,
    String refColumn) {

  /**
   * 转换为表关系领域对象。
   *
   * @return 表关系领域对象
   */
  public LowcodeTableRelation toDomain() {
    return new LowcodeTableRelation(null, tenantId, mainTableId, subTableId, relationType, joinColumn, refColumn);
  }
}
