/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.message.template.repository;

import com.zhyc.message.template.domain.MsgTemplate;
import java.util.List;

/**
 * 消息模板仓储接口。
 */
public interface MsgTemplateRepository {

  /**
   * 查询租户消息模板。
   *
   * @param tenantId 租户业务编码
   * @return 消息模板列表
   */
  List<MsgTemplate> findByTenantId(String tenantId);

  /**
   * 查询租户内启用状态的消息模板。
   *
   * @param tenantId 租户业务编码
   * @param templateCode 模板编码
   * @return 启用模板，不存在时返回 {@code null}
   */
  MsgTemplate findEnabledByTenantIdAndTemplateCode(String tenantId, String templateCode);

  /**
   * 保存或更新消息模板。
   *
   * @param template 消息模板
   */
  void save(MsgTemplate template);
}
