/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.db;

import com.zhyc.common.api.ApiResult;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 低代码数据库方言管理接口。
 *
 * <p>面向后台管理端提供当前平台注册的数据库方言能力清单，
 * 便于数据源管理、表建模和代码生成界面展示可选能力。</p>
 */
@RestController
@RequestMapping("/lowcode/dialects")
public class LowcodeDialectController {

  /** 低代码数据库方言服务。 */
  private final LowcodeDbDialectService dialectService;

  /**
   * 创建低代码数据库方言管理接口。
   *
   * @param dialectService 低代码数据库方言服务
   */
  public LowcodeDialectController(LowcodeDbDialectService dialectService) {
    this.dialectService = Objects.requireNonNull(dialectService, "低代码数据库方言服务不能为空");
  }

  /**
   * 查询当前平台注册的数据库方言能力。
   *
   * @return DDL 生成、字段类型映射和分页方言能力清单
   */
  @RequiresPermissions("lowcode:dialect:query")
  @GetMapping("/capabilities")
  public ApiResult<LowcodeDialectCapabilitiesResponse> capabilities() {
    return ApiResult.ok(LowcodeDialectCapabilitiesResponse.from(dialectService));
  }
}
