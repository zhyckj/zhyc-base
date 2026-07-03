/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.purchase.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zhyc.common.api.PageResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.purchase.order.controller.PurOrderAdminController;
import com.zhyc.purchase.order.service.PurOrderCommandService;
import com.zhyc.purchase.order.service.PurOrderCreateCommand;
import com.zhyc.purchase.order.service.PurOrderQuery;
import com.zhyc.purchase.order.service.PurOrderResponse;
import java.lang.reflect.Method;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 采购订单后台管理接口契约测试。
 */
class PurOrderAdminControllerContractTest {

  /**
   * 验证采购订单后台创建和查询接口路由及权限编码。
   *
   * @throws NoSuchMethodException 方法不存在时抛出
   */
  @Test
  void shouldExposeAdminCreateAndGetRoutesWithPermissions() throws NoSuchMethodException {
    RequestMapping requestMapping = PurOrderAdminController.class.getAnnotation(RequestMapping.class);
    Method createMethod = PurOrderAdminController.class.getMethod("create", PurOrderCreateCommand.class);
    Method listMethod = PurOrderAdminController.class.getMethod("list", String.class, String.class,
        int.class, int.class);
    Method getMethod = PurOrderAdminController.class.getMethod("get", String.class, String.class);
    Method confirmMethod = PurOrderAdminController.class.getMethod("confirm", String.class, String.class);
    Method closeMethod = PurOrderAdminController.class.getMethod("close", String.class, String.class);

    assertNotNull(requestMapping);
    assertEquals("/purchase/orders", requestMapping.value()[0]);
    assertEquals("", createMethod.getAnnotation(PostMapping.class).value()[0]);
    assertEquals("purchase:order:create", createMethod.getAnnotation(RequiresPermissions.class).value()[0]);
    assertEquals("", listMethod.getAnnotation(GetMapping.class).value()[0]);
    assertEquals("purchase:order:query", listMethod.getAnnotation(RequiresPermissions.class).value()[0]);
    assertEquals("/{orderNo}", getMethod.getAnnotation(GetMapping.class).value()[0]);
    assertEquals("purchase:order:query", getMethod.getAnnotation(RequiresPermissions.class).value()[0]);
    assertEquals("/{orderNo}/confirm", confirmMethod.getAnnotation(PostMapping.class).value()[0]);
    assertEquals("purchase:order:confirm", confirmMethod.getAnnotation(RequiresPermissions.class).value()[0]);
    assertEquals("/{orderNo}/close", closeMethod.getAnnotation(PostMapping.class).value()[0]);
    assertEquals("purchase:order:close", closeMethod.getAnnotation(RequiresPermissions.class).value()[0]);
  }

  /**
   * 验证采购订单创建空请求体会返回明确错误，避免服务层收到空命令。
   */
  @Test
  void shouldRejectNullCreateCommand() {
    PurOrderAdminController controller = new PurOrderAdminController(
        new RejectingPurOrderCommandService());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> controller.create(null));

    assertEquals("ZHYC_PURCHASE_ORDER_CREATE_REQUEST_REQUIRED", exception.getCode());
    assertEquals("采购订单创建请求不能为空", exception.getMessage());
  }

  /**
   * 拒绝被调用的采购订单命令服务测试桩。
   */
  private static final class RejectingPurOrderCommandService implements PurOrderCommandService {

    /**
     * 创建采购订单时直接失败，用于确认空请求体被 Controller 拦截。
     *
     * @param command 采购订单创建命令
     * @return 不会返回
     */
    @Override
    public String create(PurOrderCreateCommand command) {
      throw new AssertionError("采购订单创建服务不应被调用");
    }

    /**
     * 返回空采购订单响应。
     *
     * @param tenantId 租户业务编码
     * @param orderNo 采购订单号
     * @return 空采购订单响应
     */
    @Override
    public PurOrderResponse get(String tenantId, String orderNo) {
      return null;
    }

    /**
     * 返回空分页结果。
     *
     * @param query 采购订单分页查询条件
     * @return 空分页结果
     */
    @Override
    public PageResult<PurOrderResponse> list(PurOrderQuery query) {
      return null;
    }

    /**
     * 返回空确认响应。
     *
     * @param tenantId 租户业务编码
     * @param orderNo 采购订单号
     * @return 空确认响应
     */
    @Override
    public PurOrderResponse confirm(String tenantId, String orderNo) {
      return null;
    }

    /**
     * 返回空关闭响应。
     *
     * @param tenantId 租户业务编码
     * @param orderNo 采购订单号
     * @return 空关闭响应
     */
    @Override
    public PurOrderResponse close(String tenantId, String orderNo) {
      return null;
    }
  }
}
