/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.i18n.domain;

import java.time.LocalDateTime;

/**
 * 国际化词条实体。
 *
 * <p>词条用于后台、移动端和低代码生成模板的多语言展示，按租户和语言隔离。</p>
 */
public class I18nMessage {

  /** 主键。 */
  private Long id;
  /** 租户业务编码，用于共享表模式下的数据隔离。 */
  private String tenantId;
  /** 语言标识，例如 zh-CN、en-US。 */
  private String locale;
  /** 词条键，租户和语言内唯一。 */
  private String messageKey;
  /** 词条值，用于界面展示或生成模板替换。 */
  private String messageValue;
  /** 词条状态，取值如 enabled、disabled。 */
  private String status;
  /** 创建时间。 */
  private LocalDateTime createdAt;
  /** 更新时间。 */
  private LocalDateTime updatedAt;

  /**
   * 创建国际化词条实体。
   *
   * @param id 主键
   * @param tenantId 租户业务编码
   * @param locale 语言标识
   * @param messageKey 词条键
   * @param messageValue 词条值
   * @param status 词条状态
   * @param createdAt 创建时间
   * @param updatedAt 更新时间
   */
  public I18nMessage(Long id, String tenantId, String locale, String messageKey, String messageValue,
      String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
    this.id = id;
    this.tenantId = tenantId;
    this.locale = locale;
    this.messageKey = messageKey;
    this.messageValue = messageValue;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  /** @return 主键 */
  public Long getId() {
    return id;
  }

  /** @return 租户业务编码 */
  public String getTenantId() {
    return tenantId;
  }

  /** @return 语言标识 */
  public String getLocale() {
    return locale;
  }

  /** @return 词条键 */
  public String getMessageKey() {
    return messageKey;
  }

  /** @return 词条值 */
  public String getMessageValue() {
    return messageValue;
  }

  /** @return 词条状态 */
  public String getStatus() {
    return status;
  }

  /** @return 创建时间 */
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  /** @return 更新时间 */
  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
