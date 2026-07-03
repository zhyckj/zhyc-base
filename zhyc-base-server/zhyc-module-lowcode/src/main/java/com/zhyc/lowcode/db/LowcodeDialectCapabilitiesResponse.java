/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.db;

import java.util.List;

/**
 * 低代码数据库方言能力清单响应。
 *
 * <p>用于后台管理端展示当前平台注册的 DDL 生成、字段类型映射和分页方言能力，
 * 便于数据源建模和代码生成时选择可用数据库方言。</p>
 */
public class LowcodeDialectCapabilitiesResponse {

  /** 已注册的 DDL 生成器方言编码清单。 */
  private final List<String> ddlDialectCodes;

  /** 已注册的字段类型映射方言编码清单。 */
  private final List<String> fieldTypeDialectCodes;

  /** 已注册的分页方言编码清单。 */
  private final List<String> paginationDialectCodes;

  /**
   * 创建方言能力清单响应。
   *
   * @param ddlDialectCodes DDL 生成器方言编码清单
   * @param fieldTypeDialectCodes 字段类型映射方言编码清单
   * @param paginationDialectCodes 分页方言编码清单
   */
  public LowcodeDialectCapabilitiesResponse(List<String> ddlDialectCodes,
                                            List<String> fieldTypeDialectCodes,
                                            List<String> paginationDialectCodes) {
    this.ddlDialectCodes = copyOrEmpty(ddlDialectCodes);
    this.fieldTypeDialectCodes = copyOrEmpty(fieldTypeDialectCodes);
    this.paginationDialectCodes = copyOrEmpty(paginationDialectCodes);
  }

  /**
   * 从低代码数据库方言服务读取当前方言能力。
   *
   * @param dialectService 低代码数据库方言服务
   * @return 方言能力清单响应
   */
  public static LowcodeDialectCapabilitiesResponse from(LowcodeDbDialectService dialectService) {
    return new LowcodeDialectCapabilitiesResponse(
        dialectService.listDdlDialectCodes(),
        dialectService.listFieldTypeDialectCodes(),
        dialectService.listPaginationDialectCodes());
  }

  /**
   * 返回已注册的 DDL 生成器方言编码清单。
   *
   * @return DDL 生成器方言编码清单
   */
  public List<String> getDdlDialectCodes() {
    return ddlDialectCodes;
  }

  /**
   * 返回已注册的字段类型映射方言编码清单。
   *
   * @return 字段类型映射方言编码清单
   */
  public List<String> getFieldTypeDialectCodes() {
    return fieldTypeDialectCodes;
  }

  /**
   * 返回已注册的分页方言编码清单。
   *
   * @return 分页方言编码清单
   */
  public List<String> getPaginationDialectCodes() {
    return paginationDialectCodes;
  }

  private static List<String> copyOrEmpty(List<String> values) {
    return values == null ? List.of() : List.copyOf(values);
  }
}
