/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.message.template.mapper;

import com.zhyc.message.template.domain.MsgTemplate;
import java.util.List;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

/**
 * 消息模板 MyBatis Mapper。
 */
@Mapper
public interface MsgTemplateMapper {

  /**
   * 查询租户消息模板。
   *
   * @param tenantId 租户业务编码
   * @return 消息模板列表
   */
  @SelectProvider(type = MsgTemplateSqlProvider.class, method = "selectByTenantId")
  List<MsgTemplate> selectByTenantId(@Param("tenantId") String tenantId);

  /**
   * 查询租户内启用状态的消息模板。
   *
   * @param tenantId 租户业务编码
   * @param templateCode 模板编码
   * @return 启用消息模板，不存在时返回 {@code null}
   */
  @SelectProvider(type = MsgTemplateSqlProvider.class, method = "selectEnabledByTenantIdAndTemplateCode")
  MsgTemplate selectEnabledByTenantIdAndTemplateCode(@Param("tenantId") String tenantId,
                                                     @Param("templateCode") String templateCode);

  /**
   * 保存或更新消息模板。
   *
   * @param template 消息模板
   */
  @InsertProvider(type = MsgTemplateSqlProvider.class, method = "upsert")
  void upsert(MsgTemplate template);
}
