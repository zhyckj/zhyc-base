/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.domain;

/**
 * 低代码表关系模型。
 */
public class LowcodeTableRelation {

  /** 数据库主键。 */
  private final Long id;
  /** 租户业务编码。 */
  private final String tenantId;
  /** 主表模型主键。 */
  private final Long mainTableId;
  /** 子表模型主键。 */
  private final Long subTableId;
  /** 关系类型，例如 ONE_TO_MANY、ONE_TO_ONE。 */
  private final String relationType;
  /** 主表关联字段编码。 */
  private final String joinColumn;
  /** 子表引用字段编码。 */
  private final String refColumn;

  /**
   * 创建低代码表关系模型。
   *
   * @param id 数据库主键
   * @param tenantId 租户业务编码
   * @param mainTableId 主表模型主键
   * @param subTableId 子表模型主键
   * @param relationType 关系类型
   * @param joinColumn 主表关联字段编码
   * @param refColumn 子表引用字段编码
   */
  public LowcodeTableRelation(Long id, String tenantId, Long mainTableId, Long subTableId,
                              String relationType, String joinColumn, String refColumn) {
    this.id = id;
    this.tenantId = requireText(tenantId, "租户业务编码不能为空");
    this.mainTableId = requireId(mainTableId, "主表模型主键不能为空");
    this.subTableId = requireId(subTableId, "子表模型主键不能为空");
    if (this.mainTableId.equals(this.subTableId)) {
      throw new IllegalArgumentException("主表和子表不能相同");
    }
    this.relationType = LowcodeTableRelationType.fromCode(requireText(relationType, "关系类型不能为空"))
        .getCode();
    this.joinColumn = requireText(joinColumn, "主表关联字段编码不能为空");
    this.refColumn = requireText(refColumn, "子表引用字段编码不能为空");
  }

  public Long getId() {
    return id;
  }

  public String getTenantId() {
    return tenantId;
  }

  public Long getMainTableId() {
    return mainTableId;
  }

  public Long getSubTableId() {
    return subTableId;
  }

  public String getRelationType() {
    return relationType;
  }

  public String getJoinColumn() {
    return joinColumn;
  }

  public String getRefColumn() {
    return refColumn;
  }

  private static Long requireId(Long value, String message) {
    if (value == null || value <= 0) {
      throw new IllegalArgumentException(message);
    }
    return value;
  }

  private static String requireText(String value, String message) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(message);
    }
    return value.trim();
  }
}
