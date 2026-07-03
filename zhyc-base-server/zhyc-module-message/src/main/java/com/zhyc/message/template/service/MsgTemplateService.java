/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.message.template.service;

import java.util.List;

/**
 * 消息模板业务服务。
 */
public interface MsgTemplateService {

  /**
   * 查询租户消息模板。
   *
   * @param tenantId 租户业务编码
   * @return 消息模板列表
   */
  List<MsgTemplateResponse> listTemplates(String tenantId);

  /**
   * 保存或更新消息模板。
   *
   * @param command 消息模板保存命令
   */
  void save(MsgTemplateSaveCommand command);
}
