/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.purchase.request.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.api.PageResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.purchase.request.service.PurRequestCommandService;
import com.zhyc.purchase.request.service.PurRequestCreateCommand;
import com.zhyc.purchase.request.service.PurRequestQuery;
import com.zhyc.purchase.request.service.PurRequestStatusResponse;
import com.zhyc.purchase.request.service.PurRequestSubmitResponse;
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
 * 采购申请后台管理接口。
 */
@RestController
@RequestMapping("/purchase/requests")
public class PurRequestAdminController {

  /** 租户业务编码请求头。 */
  public static final String HEADER_TENANT_ID = "X-ZHYC-Tenant-Id";
  /** 当前操作用户 ID 请求头。 */
  public static final String HEADER_USER_ID = "X-ZHYC-User-Id";
  /** 采购申请创建请求缺失错误码。 */
  private static final String ERROR_CREATE_REQUEST_REQUIRED =
      "ZHYC_PURCHASE_REQUEST_CREATE_REQUEST_REQUIRED";

  /** 采购申请命令服务。 */
  private final PurRequestCommandService purRequestCommandService;

  /**
   * 创建采购申请后台管理接口。
   *
   * @param purRequestCommandService 采购申请命令服务
   */
  public PurRequestAdminController(PurRequestCommandService purRequestCommandService) {
    this.purRequestCommandService = Objects.requireNonNull(purRequestCommandService,
        "采购申请命令服务不能为空");
  }

  /**
   * 创建采购申请草稿。
   *
   * @param command 采购申请创建命令
   * @return 采购申请单号
   */
  @RequiresPermissions("purchase:request:create")
  @PostMapping("")
  public ApiResult<String> create(@RequestBody PurRequestCreateCommand command) {
    if (command == null) {
      throw new BusinessException(ERROR_CREATE_REQUEST_REQUIRED, "采购申请创建请求不能为空");
    }
    return ApiResult.ok(purRequestCommandService.create(command));
  }

  /**
   * 分页查询采购申请。
   *
   * @param tenantId 租户业务编码
   * @param processStatus 流程状态
   * @param pageNo 当前页码
   * @param pageSize 每页记录数
   * @return 采购申请分页响应
   */
  @RequiresPermissions("purchase:request:view")
  @GetMapping("")
  public ApiResult<PageResult<PurRequestStatusResponse>> list(
      @RequestHeader(HEADER_TENANT_ID) String tenantId,
      @RequestParam(value = "processStatus", required = false) String processStatus,
      @RequestParam(value = "pageNo", defaultValue = "1") int pageNo,
      @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
    return ApiResult.ok(purRequestCommandService.list(
        new PurRequestQuery(tenantId, processStatus, pageNo, pageSize)));
  }

  /**
   * 提交采购申请审批。
   *
   * @param tenantId 租户业务编码
   * @param requestNo 采购申请单号
   * @param starterUserId 发起人用户 ID
   * @return 采购申请提交审批响应
   */
  @RequiresPermissions("purchase:request:submit")
  @PostMapping("/{requestNo}/submit")
  public ApiResult<PurRequestSubmitResponse> submit(
      @RequestHeader(HEADER_TENANT_ID) String tenantId,
      @PathVariable("requestNo") String requestNo,
      @RequestHeader(HEADER_USER_ID) Long starterUserId) {
    return ApiResult.ok(purRequestCommandService.submit(tenantId, requestNo, starterUserId));
  }
}
