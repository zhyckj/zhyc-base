/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.i18n.mapper;

import com.zhyc.i18n.domain.I18nMessage;
import java.util.List;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

/**
 * 国际化词条 MyBatis Mapper。
 */
@Mapper
public interface I18nMapper {

  /**
   * 查询国际化词条列表。
   *
   * @param tenantId 租户业务编码
   * @param locale 语言标识
   * @param status 词条状态
   * @return 国际化词条列表
   */
  @SelectProvider(type = I18nSqlProvider.class, method = "selectMessagesForMapper")
  List<I18nMessage> selectMessages(@Param("tenantId") String tenantId,
      @Param("locale") String locale, @Param("status") String status);

  /**
   * 保存或更新国际化词条。
   *
   * @param message 国际化词条实体
   */
  @InsertProvider(type = I18nSqlProvider.class, method = "upsertMessage")
  void upsertMessage(I18nMessage message);

  /**
   * 查询启用状态词条。
   *
   * @param tenantId 租户业务编码
   * @param locale 语言标识
   * @param messageKey 词条键
   * @return 国际化词条
   */
  @SelectProvider(type = I18nSqlProvider.class, method = "selectEnabledMessage")
  I18nMessage selectEnabledMessage(@Param("tenantId") String tenantId,
      @Param("locale") String locale, @Param("messageKey") String messageKey);
}
