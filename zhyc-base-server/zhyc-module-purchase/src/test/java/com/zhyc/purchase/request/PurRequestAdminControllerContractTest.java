/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.purchase.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zhyc.common.api.PageResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.purchase.request.controller.PurRequestAdminController;
import com.zhyc.purchase.request.service.PurRequestCommandService;
import com.zhyc.purchase.request.service.PurRequestCreateCommand;
import com.zhyc.purchase.request.service.PurRequestQuery;
import com.zhyc.purchase.request.service.PurRequestStatusResponse;
import com.zhyc.purchase.request.service.PurRequestSubmitResponse;
import java.lang.reflect.Method;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 采购申请后台管理接口契约测试。
 */
class PurRequestAdminControllerContractTest {

  /**
   * 验证采购申请后台创建和提交接口路由及权限编码。
   *
   * @throws NoSuchMethodException 方法不存在时抛出
   */
  @Test
  void shouldExposeAdminCreateAndSubmitRoutesWithPermissions() throws NoSuchMethodException {
    RequestMapping requestMapping = PurRequestAdminController.class.getAnnotation(RequestMapping.class);
    Method createMethod = PurRequestAdminController.class.getMethod("create", PurRequestCreateCommand.class);
    Method listMethod = PurRequestAdminController.class.getMethod("list", String.class, String.class,
        int.class, int.class);
    Method submitMethod = PurRequestAdminController.class.getMethod("submit", String.class, String.class, Long.class);

    assertNotNull(requestMapping);
    assertEquals("/purchase/requests", requestMapping.value()[0]);
    assertEquals("", createMethod.getAnnotation(PostMapping.class).value()[0]);
    assertEquals("purchase:request:create", createMethod.getAnnotation(RequiresPermissions.class).value()[0]);
    assertEquals("", listMethod.getAnnotation(GetMapping.class).value()[0]);
    assertEquals("purchase:request:view", listMethod.getAnnotation(RequiresPermissions.class).value()[0]);
    assertEquals("/{requestNo}/submit", submitMethod.getAnnotation(PostMapping.class).value()[0]);
    assertEquals("purchase:request:submit", submitMethod.getAnnotation(RequiresPermissions.class).value()[0]);
  }

  /**
   * 验证采购申请创建空请求体会返回明确错误，避免服务层收到空命令。
   */
  @Test
  void shouldRejectNullCreateCommand() {
    PurRequestAdminController controller = new PurRequestAdminController(
        new RejectingPurRequestCommandService());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> controller.create(null));

    assertEquals("ZHYC_PURCHASE_REQUEST_CREATE_REQUEST_REQUIRED", exception.getCode());
    assertEquals("采购申请创建请求不能为空", exception.getMessage());
  }

  /**
   * 拒绝被调用的采购申请命令服务测试桩。
   */
  private static final class RejectingPurRequestCommandService implements PurRequestCommandService {

    /**
     * 创建采购申请时直接失败，用于确认空请求体被 Controller 拦截。
     *
     * @param command 采购申请创建命令
     * @return 不会返回
     */
    @Override
    public String create(PurRequestCreateCommand command) {
      throw new AssertionError("采购申请创建服务不应被调用");
    }

    /**
     * 返回空分页结果。
     *
     * @param query 采购申请分页查询条件
     * @return 空分页结果
     */
    @Override
    public PageResult<PurRequestStatusResponse> list(PurRequestQuery query) {
      return null;
    }

    /**
     * 返回空提交响应。
     *
     * @param tenantId 租户业务编码
     * @param requestNo 采购申请单号
     * @param starterUserId 发起人用户 ID
     * @return 空提交响应
     */
    @Override
    public PurRequestSubmitResponse submit(String tenantId, String requestNo, Long starterUserId) {
      return null;
    }
  }
}
