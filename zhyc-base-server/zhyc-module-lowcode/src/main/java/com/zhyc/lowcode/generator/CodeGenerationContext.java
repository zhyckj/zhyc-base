/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

import com.zhyc.lowcode.metadata.domain.LowcodeTableModel;

/**
 * 代码生成上下文。
 *
 * <p>模板渲染器通过该对象读取业务模块、实体、表模型等稳定输入。</p>
 */
public class CodeGenerationContext {

  /** 原始代码生成请求。 */
  private final CodeGenerationRequest request;

  /**
   * 创建代码生成上下文。
   *
   * @param request 原始代码生成请求
   */
  public CodeGenerationContext(CodeGenerationRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("代码生成请求不能为空");
    }
    this.request = request;
  }

  /**
   * 返回生成目标端。
   *
   * @return 生成目标端
   */
  public GenerationTarget getTarget() {
    return request.getTarget();
  }

  /**
   * 返回业务模块名称。
   *
   * @return 业务模块名称
   */
  public String getModuleName() {
    return request.getModuleName();
  }

  /**
   * 返回业务实体名称。
   *
   * @return 业务实体名称
   */
  public String getEntityName() {
    return request.getEntityName();
  }

  /**
   * 返回低代码表模型。
   *
   * @return 低代码表模型
   */
  public LowcodeTableModel getTableModel() {
    return request.getTableModel();
  }

  /**
   * 返回按目标数据源方言生成的建表 DDL。
   *
   * @return 建表 DDL，未预生成时返回 {@code null}
   */
  public String getDdl() {
    return request.getDdl();
  }
}
