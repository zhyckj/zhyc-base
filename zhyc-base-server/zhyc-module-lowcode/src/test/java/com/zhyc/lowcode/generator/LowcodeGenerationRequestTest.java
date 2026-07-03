/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * 低代码生成请求转换测试。
 */
class LowcodeGenerationRequestTest {

  /**
   * 验证预览请求使用稳定生成目标编码转换命令。
   */
  @Test
  void shouldConvertPreviewRequestByTargetCode() {
    LowcodeGenerationPreviewRequest request = new LowcodeGenerationPreviewRequest();
    request.setTenantId("tenant-a");
    request.setTableModelCode("purchase_order");
    request.setTarget("admin-backend");
    request.setModuleName("purchase");
    request.setEntityName("purchaseOrder");

    LowcodeGenerationPreviewCommand command = request.toCommand();

    assertEquals(GenerationTarget.ADMIN_BACKEND, command.getTarget());
  }

  /**
   * 验证执行请求使用稳定生成目标编码转换命令。
   */
  @Test
  void shouldConvertExecuteRequestByTargetCode() {
    LowcodeGenerationExecuteRequest request = new LowcodeGenerationExecuteRequest();
    request.setTenantId("tenant-a");
    request.setTableModelCode("purchase_order");
    request.setTarget("open-api-portal");
    request.setModuleName("purchase");
    request.setEntityName("purchaseOrder");

    LowcodeGenerationExecuteCommand command = request.toCommand();

    assertEquals(GenerationTarget.OPEN_API_PORTAL, command.getPreviewCommand().getTarget());
  }
}
