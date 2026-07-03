/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.purchase.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.purchase.request.controller.PurRequestOpenApiController;
import com.zhyc.purchase.request.service.PurRequestStatusResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 采购申请开放 API 控制器契约测试。
 */
class PurRequestOpenApiControllerContractTest {

  /**
   * 验证采购申请状态查询开放 API 的路由契约。
   *
   * @throws NoSuchMethodException 方法不存在时抛出
   */
  @Test
  void shouldExposeOpenApiStatusRoute() throws NoSuchMethodException {
    RequestMapping requestMapping = PurRequestOpenApiController.class.getAnnotation(RequestMapping.class);
    Method method = PurRequestOpenApiController.class.getMethod(
        "queryStatus", String.class, String.class, String.class, String.class);
    GetMapping getMapping = method.getAnnotation(GetMapping.class);
    RequestHeader tenantHeader = method.getParameters()[0].getAnnotation(RequestHeader.class);
    RequestHeader appHeader = method.getParameters()[1].getAnnotation(RequestHeader.class);
    RequestHeader apiHeader = method.getParameters()[2].getAnnotation(RequestHeader.class);

    assertNotNull(requestMapping);
    assertEquals("/openapi/v1/purchase/requests", requestMapping.value()[0]);
    assertNotNull(getMapping);
    assertEquals("/{requestNo}/status", getMapping.value()[0]);
    assertNotNull(tenantHeader);
    assertEquals("X-ZHYC-Tenant-Id", tenantHeader.value());
    assertNotNull(appHeader);
    assertEquals("X-ZHYC-App-Code", appHeader.value());
    assertNotNull(apiHeader);
    assertEquals("X-ZHYC-Api-Code", apiHeader.value());
  }

  /**
   * 验证开放 API 后端控制器拒绝缺少网关内部上下文的直连请求。
   *
   * @throws NoSuchMethodException 方法不存在时抛出
   */
  @Test
  void shouldRejectOpenApiCallWithoutGatewayContext() throws NoSuchMethodException {
    PurRequestOpenApiController controller = new PurRequestOpenApiController(
        (tenantId, requestNo) -> new PurRequestStatusResponse(
            requestNo, "采购申请", "APPROVING", BigDecimal.ONE, LocalDateTime.now()));
    Method method = PurRequestOpenApiController.class.getMethod(
        "queryStatus", String.class, String.class, String.class, String.class);

    InvocationTargetException exception = assertThrows(InvocationTargetException.class,
        () -> method.invoke(controller, "tenant_a", "", "purchase-request-status", "PR202606250001"));

    BusinessException cause = assertInstanceOf(BusinessException.class, exception.getCause());
    assertEquals("ZHYC_PURCHASE_REQUEST_OPENAPI_GATEWAY_CONTEXT_REQUIRED", cause.getCode());
    assertEquals("开放 API 网关上下文不能为空", cause.getMessage());
  }
}
