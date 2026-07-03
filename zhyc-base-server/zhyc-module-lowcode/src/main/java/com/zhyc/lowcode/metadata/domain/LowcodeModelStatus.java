/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.domain;

/**
 * 低代码模型发布状态。
 */
public enum LowcodeModelStatus {

  /** 草稿状态，可继续调整字段和生成配置。 */
  DRAFT,
  /** 已发布状态，可用于生成代码、创建数据库表或开放接口。 */
  PUBLISHED
}
