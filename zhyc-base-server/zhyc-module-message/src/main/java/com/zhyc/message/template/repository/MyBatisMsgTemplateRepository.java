/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.message.template.repository;

import com.zhyc.message.template.domain.MsgTemplate;
import com.zhyc.message.template.mapper.MsgTemplateMapper;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Repository;

/**
 * 基于 MyBatis 的消息模板仓储实现。
 */
@Repository
public class MyBatisMsgTemplateRepository implements MsgTemplateRepository {

  /** 消息模板 Mapper。 */
  private final MsgTemplateMapper templateMapper;

  /**
   * 创建消息模板仓储实现。
   *
   * @param templateMapper 消息模板 Mapper
   */
  public MyBatisMsgTemplateRepository(MsgTemplateMapper templateMapper) {
    this.templateMapper = Objects.requireNonNull(templateMapper, "消息模板 Mapper 不能为空");
  }

  @Override
  public List<MsgTemplate> findByTenantId(String tenantId) {
    return templateMapper.selectByTenantId(tenantId);
  }

  @Override
  public MsgTemplate findEnabledByTenantIdAndTemplateCode(String tenantId, String templateCode) {
    return templateMapper.selectEnabledByTenantIdAndTemplateCode(tenantId, templateCode);
  }

  @Override
  public void save(MsgTemplate template) {
    templateMapper.upsert(template);
  }
}
