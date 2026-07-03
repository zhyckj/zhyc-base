/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.purchase.order.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.api.PageResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.purchase.order.service.PurOrderCommandService;
import com.zhyc.purchase.order.service.PurOrderCreateCommand;
import com.zhyc.purchase.order.service.PurOrderQuery;
import com.zhyc.purchase.order.service.PurOrderResponse;
import java.util.Objects;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 采购订单后台管理接口。
 */
@RestController
@RequestMapping("/purchase/orders")
public class PurOrderAdminController {

  /** 租户业务编码请求头。 */
  public static final String HEADER_TENANT_ID = "X-ZHYC-Tenant-Id";
  /** 采购订单创建请求缺失错误码。 */
  private static final String ERROR_CREATE_REQUEST_REQUIRED =
      "ZHYC_PURCHASE_ORDER_CREATE_REQUEST_REQUIRED";

  /** 采购订单命令服务。 */
  private final PurOrderCommandService purOrderCommandService;

  /**
   * 创建采购订单后台管理接口。
   *
   * @param purOrderCommandService 采购订单命令服务
   */
  public PurOrderAdminController(PurOrderCommandService purOrderCommandService) {
    this.purOrderCommandService = Objects.requireNonNull(purOrderCommandService,
        "采购订单命令服务不能为空");
  }

  /**
   * 创建采购订单。
   *
   * @param command 采购订单创建命令
   * @return 采购订单号
   */
  @RequiresPermissions("purchase:order:create")
  @PostMapping("")
  public ApiResult<String> create(@RequestBody PurOrderCreateCommand command) {
    if (command == null) {
      throw new BusinessException(ERROR_CREATE_REQUEST_REQUIRED, "采购订单创建请求不能为空");
    }
    return ApiResult.ok(purOrderCommandService.create(command));
  }

  /**
   * 分页查询采购订单。
   *
   * @param tenantId 租户业务编码
   * @param orderStatus 订单状态
   * @param pageNo 当前页码
   * @param pageSize 每页记录数
   * @return 采购订单分页响应
   */
  @RequiresPermissions("purchase:order:query")
  @GetMapping("")
  public ApiResult<PageResult<PurOrderResponse>> list(@RequestHeader(HEADER_TENANT_ID) String tenantId,
      @RequestParam(value = "orderStatus", required = false) String orderStatus,
      @RequestParam(value = "pageNo", defaultValue = "1") int pageNo,
      @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
    return ApiResult.ok(purOrderCommandService.list(
        new PurOrderQuery(tenantId, orderStatus, pageNo, pageSize)));
  }

  /**
   * 查询采购订单。
   *
   * @param tenantId 租户业务编码
   * @param orderNo 采购订单号
   * @return 采购订单响应
   */
  @RequiresPermissions("purchase:order:query")
  @GetMapping("/{orderNo}")
  public ApiResult<PurOrderResponse> get(@RequestHeader(HEADER_TENANT_ID) String tenantId,
      @PathVariable("orderNo") String orderNo) {
    return ApiResult.ok(purOrderCommandService.get(tenantId, orderNo));
  }

  /**
   * 确认采购订单。
   *
   * @param tenantId 租户业务编码
   * @param orderNo 采购订单号
   * @return 采购订单响应
   */
  @RequiresPermissions("purchase:order:confirm")
  @PostMapping("/{orderNo}/confirm")
  public ApiResult<PurOrderResponse> confirm(@RequestHeader(HEADER_TENANT_ID) String tenantId,
      @PathVariable("orderNo") String orderNo) {
    return ApiResult.ok(purOrderCommandService.confirm(tenantId, orderNo));
  }

  /**
   * 关闭采购订单。
   *
   * @param tenantId 租户业务编码
   * @param orderNo 采购订单号
   * @return 采购订单响应
   */
  @RequiresPermissions("purchase:order:close")
  @PostMapping("/{orderNo}/close")
  public ApiResult<PurOrderResponse> close(@RequestHeader(HEADER_TENANT_ID) String tenantId,
      @PathVariable("orderNo") String orderNo) {
    return ApiResult.ok(purOrderCommandService.close(tenantId, orderNo));
  }
}
