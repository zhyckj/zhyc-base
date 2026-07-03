/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

import java.util.List;

/**
 * 低代码生成应用服务接口。
 */
public interface LowcodeGeneratorService {

  /**
   * 查询指定目标端可用模板。
   *
   * @param target 生成目标端
   * @return 模板描述列表
   */
  List<CodeTemplateDescriptor> listTemplates(GenerationTarget target);

  /**
   * 执行低代码生成前校验。
   *
   * <p>用于在预览和执行生成前检查表模型、字段编码、租户边界和命名约定，避免输出不可编译或越权的代码。</p>
   *
   * @param command 生成预览命令
   * @return 生成前校验结果
   */
  LowcodeGenerationValidationResult validate(LowcodeGenerationPreviewCommand command);

  /**
   * 预览低代码生成文件。
   *
   * @param command 生成预览命令
   * @return 生成文件清单，不落盘
   */
  List<GeneratedFile> preview(LowcodeGenerationPreviewCommand command);

  /**
   * 执行低代码生成并记录生成结果。
   *
   * @param command 生成执行命令
   * @return 生成记录
   */
  LowcodeGenerationRecord execute(LowcodeGenerationExecuteCommand command);

  /**
   * 查询租户内生成记录。
   *
   * @param tenantId 租户业务编码
   * @return 生成记录列表
   */
  List<LowcodeGenerationRecord> listRecords(String tenantId);

  /**
   * 查询生成记录对应的文件明细。
   *
   * @param tenantId 租户业务编码
   * @param recordId 生成记录主键
   * @return 生成文件明细列表
   */
  List<LowcodeGenerationFile> listGenerationFiles(String tenantId, Long recordId);
}
