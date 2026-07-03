/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.purchase.order.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.purchase.order.service.PurOrderCommandService;
import com.zhyc.purchase.order.service.PurOrderResponse;
import java.util.Objects;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 采购订单开放 API。
 *
 * <p>该接口面向通过开放 API 网关鉴权后的系统集成调用方，租户业务编码和应用上下文必须由网关或可信上游写入请求头。</p>
 */
@RestController
@RequestMapping("/openapi/v1/purchase/orders")
public class PurOrderOpenApiController {

  /** 租户业务编码请求头。 */
  public static final String HEADER_TENANT_ID = "X-ZHYC-Tenant-Id";
  /** 开放 API 应用编码请求头。 */
  public static final String HEADER_APP_CODE = "X-ZHYC-App-Code";
  /** 开放 API 接口编码请求头。 */
  public static final String HEADER_API_CODE = "X-ZHYC-Api-Code";
  /** 开放 API 网关上下文为空错误码。 */
  private static final String ERROR_GATEWAY_CONTEXT_REQUIRED =
      "ZHYC_PURCHASE_ORDER_OPENAPI_GATEWAY_CONTEXT_REQUIRED";

  /** 采购订单命令服务。 */
  private final PurOrderCommandService purOrderCommandService;

  /**
   * 创建采购订单开放 API。
   *
   * @param purOrderCommandService 采购订单命令服务
   */
  public PurOrderOpenApiController(PurOrderCommandService purOrderCommandService) {
    this.purOrderCommandService = Objects.requireNonNull(purOrderCommandService,
        "采购订单命令服务不能为空");
  }

  /**
   * 查询采购订单详情。
   *
   * @param tenantId 租户业务编码
   * @param appCode 开放 API 应用编码
   * @param apiCode 开放 API 接口编码
   * @param orderNo 采购订单号
   * @return 采购订单响应
   */
  @GetMapping("/{orderNo}")
  public ApiResult<PurOrderResponse> get(@RequestHeader(HEADER_TENANT_ID) String tenantId,
      @RequestHeader(HEADER_APP_CODE) String appCode,
      @RequestHeader(HEADER_API_CODE) String apiCode,
      @PathVariable("orderNo") String orderNo) {
    requireGatewayContext(appCode, apiCode);
    return ApiResult.ok(purOrderCommandService.get(tenantId, orderNo));
  }

  /**
   * 校验开放 API 网关注入的内部上下文。
   *
   * @param appCode 开放 API 应用编码
   * @param apiCode 开放 API 接口编码
   */
  private void requireGatewayContext(String appCode, String apiCode) {
    if (appCode == null || appCode.isBlank() || apiCode == null || apiCode.isBlank()) {
      throw new BusinessException(ERROR_GATEWAY_CONTEXT_REQUIRED, "开放 API 网关上下文不能为空");
    }
  }
}
