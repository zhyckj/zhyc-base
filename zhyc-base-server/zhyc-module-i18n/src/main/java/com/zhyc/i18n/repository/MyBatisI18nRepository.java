/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.i18n.repository;

import com.zhyc.i18n.domain.I18nMessage;
import com.zhyc.i18n.mapper.I18nMapper;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * 基于 MyBatis 的国际化词条仓储实现。
 */
@Repository
public class MyBatisI18nRepository implements I18nRepository {

  /** 国际化词条 Mapper。 */
  private final I18nMapper i18nMapper;

  /**
   * 创建国际化词条仓储。
   *
   * @param i18nMapper 国际化词条 Mapper
   */
  public MyBatisI18nRepository(I18nMapper i18nMapper) {
    this.i18nMapper = Objects.requireNonNull(i18nMapper, "国际化词条 Mapper 不能为空");
  }

  @Override
  public List<I18nMessage> findMessages(String tenantId, String locale, String status) {
    return i18nMapper.selectMessages(tenantId, locale, status);
  }

  @Override
  public void saveMessage(I18nMessage message) {
    i18nMapper.upsertMessage(message);
  }

  @Override
  public Optional<I18nMessage> findEnabledMessage(String tenantId, String locale, String messageKey) {
    return Optional.ofNullable(i18nMapper.selectEnabledMessage(tenantId, locale, messageKey));
  }
}
