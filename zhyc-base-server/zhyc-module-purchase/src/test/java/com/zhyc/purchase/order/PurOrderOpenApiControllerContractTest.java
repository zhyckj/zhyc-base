/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.purchase.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.common.api.PageResult;
import com.zhyc.purchase.order.controller.PurOrderOpenApiController;
import com.zhyc.purchase.order.service.PurOrderCommandService;
import com.zhyc.purchase.order.service.PurOrderCreateCommand;
import com.zhyc.purchase.order.service.PurOrderQuery;
import com.zhyc.purchase.order.service.PurOrderResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 采购订单开放 API 控制器契约测试。
 */
class PurOrderOpenApiControllerContractTest {

  /**
   * 验证采购订单详情开放 API 的路由和网关上下文请求头契约。
   *
   * @throws NoSuchMethodException 方法不存在时抛出
   */
  @Test
  void shouldExposeOpenApiOrderDetailRoute() throws NoSuchMethodException {
    RequestMapping requestMapping = PurOrderOpenApiController.class.getAnnotation(RequestMapping.class);
    Method method = PurOrderOpenApiController.class.getMethod(
        "get", String.class, String.class, String.class, String.class);
    GetMapping getMapping = method.getAnnotation(GetMapping.class);
    RequestHeader tenantHeader = method.getParameters()[0].getAnnotation(RequestHeader.class);
    RequestHeader appHeader = method.getParameters()[1].getAnnotation(RequestHeader.class);
    RequestHeader apiHeader = method.getParameters()[2].getAnnotation(RequestHeader.class);

    assertNotNull(requestMapping);
    assertEquals("/openapi/v1/purchase/orders", requestMapping.value()[0]);
    assertNotNull(getMapping);
    assertEquals("/{orderNo}", getMapping.value()[0]);
    assertNotNull(tenantHeader);
    assertEquals("X-ZHYC-Tenant-Id", tenantHeader.value());
    assertNotNull(appHeader);
    assertEquals("X-ZHYC-App-Code", appHeader.value());
    assertNotNull(apiHeader);
    assertEquals("X-ZHYC-Api-Code", apiHeader.value());
  }

  /**
   * 验证开放 API 后端控制器拒绝缺少网关内部上下文的采购订单直连请求。
   *
   * @throws NoSuchMethodException 方法不存在时抛出
   */
  @Test
  void shouldRejectOpenApiOrderCallWithoutGatewayContext() throws NoSuchMethodException {
    PurOrderOpenApiController controller = new PurOrderOpenApiController(new RecordingPurOrderCommandService());
    Method method = PurOrderOpenApiController.class.getMethod(
        "get", String.class, String.class, String.class, String.class);

    InvocationTargetException exception = assertThrows(InvocationTargetException.class,
        () -> method.invoke(controller, "tenant_a", "", "purchase-order-detail", "PO202606260001"));

    BusinessException cause = assertInstanceOf(BusinessException.class, exception.getCause());
    assertEquals("ZHYC_PURCHASE_ORDER_OPENAPI_GATEWAY_CONTEXT_REQUIRED", cause.getCode());
    assertEquals("开放 API 网关上下文不能为空", cause.getMessage());
  }

  /**
   * 记录调用的采购订单命令服务桩。
   */
  private static class RecordingPurOrderCommandService implements PurOrderCommandService {

    @Override
    public String create(PurOrderCreateCommand command) {
      throw new AssertionError("开放 API 查询订单详情不应创建采购订单");
    }

    @Override
    public PurOrderResponse get(String tenantId, String orderNo) {
      return new PurOrderResponse(tenantId, orderNo, "PR202606260001",
          2001L, 1001L, BigDecimal.TEN, "CREATED", List.of());
    }

    @Override
    public PageResult<PurOrderResponse> list(PurOrderQuery query) {
      throw new AssertionError("开放 API 查询订单详情不应分页查询采购订单");
    }

    @Override
    public PurOrderResponse confirm(String tenantId, String orderNo) {
      throw new AssertionError("开放 API 查询订单详情不应确认采购订单");
    }

    @Override
    public PurOrderResponse close(String tenantId, String orderNo) {
      throw new AssertionError("开放 API 查询订单详情不应关闭采购订单");
    }
  }
}
