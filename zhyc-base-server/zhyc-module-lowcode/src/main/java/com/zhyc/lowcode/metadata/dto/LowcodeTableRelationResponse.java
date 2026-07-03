/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.dto;

import com.zhyc.lowcode.metadata.domain.LowcodeTableRelation;

/**
 * 低代码表关系响应对象。
 */
public class LowcodeTableRelationResponse {

  /** 表关系主键。 */
  private final Long id;
  /** 租户业务编码。 */
  private final String tenantId;
  /** 主表模型主键。 */
  private final Long mainTableId;
  /** 子表模型主键。 */
  private final Long subTableId;
  /** 关系类型。 */
  private final String relationType;
  /** 主表关联字段编码。 */
  private final String joinColumn;
  /** 子表引用字段编码。 */
  private final String refColumn;

  private LowcodeTableRelationResponse(LowcodeTableRelation relation) {
    this.id = relation.getId();
    this.tenantId = relation.getTenantId();
    this.mainTableId = relation.getMainTableId();
    this.subTableId = relation.getSubTableId();
    this.relationType = relation.getRelationType();
    this.joinColumn = relation.getJoinColumn();
    this.refColumn = relation.getRefColumn();
  }

  /**
   * 从领域对象构造响应。
   *
   * @param relation 表关系领域对象
   * @return 表关系响应
   */
  public static LowcodeTableRelationResponse from(LowcodeTableRelation relation) {
    return new LowcodeTableRelationResponse(relation);
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
}
