/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.purchase.request.service;

import com.zhyc.common.api.PageResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.common.workflow.WorkflowService;
import com.zhyc.purchase.request.domain.PurRequest;
import com.zhyc.purchase.request.repository.PurRequestRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 默认采购申请命令服务实现。
 */
@Service
public class DefaultPurRequestCommandService implements PurRequestCommandService {

  /** 采购申请审批流程定义 Key。 */
  private static final String PROCESS_KEY = "purchase.request.approval";
  /** 草稿状态。 */
  private static final String STATUS_DRAFT = "DRAFT";
  /** 审批中状态。 */
  private static final String STATUS_APPROVING = "APPROVING";
  /** 单页最大记录数。 */
  private static final int MAX_PAGE_SIZE = 100;
  /** 创建命令不能为空错误码。 */
  private static final String ERROR_COMMAND_REQUIRED = "ZHYC_PUR_REQUEST_COMMAND_REQUIRED";
  /** 分页查询条件不能为空错误码。 */
  private static final String ERROR_QUERY_REQUIRED = "ZHYC_PUR_REQUEST_QUERY_REQUIRED";
  /** 租户业务编码不能为空错误码。 */
  private static final String ERROR_TENANT_REQUIRED = "ZHYC_PUR_REQUEST_TENANT_REQUIRED";
  /** 采购申请单号不能为空错误码。 */
  private static final String ERROR_REQUEST_NO_REQUIRED = "ZHYC_PUR_REQUEST_NO_REQUIRED";
  /** 采购申请标题不能为空错误码。 */
  private static final String ERROR_TITLE_REQUIRED = "ZHYC_PUR_REQUEST_TITLE_REQUIRED";
  /** 申请人用户 ID 不能为空错误码。 */
  private static final String ERROR_APPLICANT_REQUIRED = "ZHYC_PUR_REQUEST_APPLICANT_REQUIRED";
  /** 申请部门 ID 不能为空错误码。 */
  private static final String ERROR_ORG_REQUIRED = "ZHYC_PUR_REQUEST_ORG_REQUIRED";
  /** 采购申请总金额不能为空错误码。 */
  private static final String ERROR_TOTAL_AMOUNT_REQUIRED = "ZHYC_PUR_REQUEST_TOTAL_AMOUNT_REQUIRED";
  /** 采购申请总金额非法错误码。 */
  private static final String ERROR_TOTAL_AMOUNT_INVALID = "ZHYC_PUR_REQUEST_TOTAL_AMOUNT_INVALID";
  /** 采购申请不存在错误码。 */
  private static final String ERROR_REQUEST_NOT_FOUND = "ZHYC_PUR_REQUEST_NOT_FOUND";
  /** 采购申请提交状态非法错误码。 */
  private static final String ERROR_SUBMIT_STATUS_INVALID = "ZHYC_PUR_REQUEST_SUBMIT_STATUS_INVALID";
  /** 发起人用户 ID 不能为空错误码。 */
  private static final String ERROR_STARTER_REQUIRED = "ZHYC_PUR_REQUEST_STARTER_REQUIRED";

  /** 采购申请仓储。 */
  private final PurRequestRepository purRequestRepository;
  /** 工作流能力门面。 */
  private final WorkflowService workflowService;

  /**
   * 创建默认采购申请命令服务。
   *
   * @param purRequestRepository 采购申请仓储
   * @param workflowService 工作流能力门面
   */
  public DefaultPurRequestCommandService(PurRequestRepository purRequestRepository,
      WorkflowService workflowService) {
    this.purRequestRepository = Objects.requireNonNull(purRequestRepository, "采购申请仓储不能为空");
    this.workflowService = Objects.requireNonNull(workflowService, "工作流能力门面不能为空");
  }

  @Override
  @Transactional
  public String create(PurRequestCreateCommand command) {
    PurRequestCreateCommand requiredCommand = requireObject(command, ERROR_COMMAND_REQUIRED,
        "采购申请创建命令不能为空");
    String tenantId = requireText(requiredCommand.getTenantId(), ERROR_TENANT_REQUIRED,
        "租户业务编码不能为空");
    String requestNo = requireText(requiredCommand.getRequestNo(), ERROR_REQUEST_NO_REQUIRED,
        "采购申请单号不能为空");
    PurRequest purRequest = new PurRequest(null, tenantId, requestNo,
        requireText(requiredCommand.getRequestTitle(), ERROR_TITLE_REQUIRED, "采购申请标题不能为空"),
        requireObject(requiredCommand.getApplicantId(), ERROR_APPLICANT_REQUIRED,
            "申请人用户 ID 不能为空"),
        requireObject(requiredCommand.getOrgId(), ERROR_ORG_REQUIRED, "申请部门 ID 不能为空"),
        requirePositiveAmount(requiredCommand.getTotalAmount()),
        trimToNull(requiredCommand.getRequestReason()),
        STATUS_DRAFT, null, null, null);
    purRequestRepository.save(purRequest);
    return requestNo;
  }

  @Override
  public PageResult<PurRequestStatusResponse> list(PurRequestQuery query) {
    PurRequestQuery requiredQuery = requireObject(query, ERROR_QUERY_REQUIRED,
        "采购申请分页查询条件不能为空");
    String tenantId = requireText(requiredQuery.getTenantId(), ERROR_TENANT_REQUIRED,
        "租户业务编码不能为空");
    String processStatus = trimToNull(requiredQuery.getProcessStatus());
    int pageNo = normalizePageNo(requiredQuery.getPageNo());
    int pageSize = normalizePageSize(requiredQuery.getPageSize());
    long total = purRequestRepository.countByTenantIdAndProcessStatus(tenantId, processStatus);
    long offset = (long) (pageNo - 1) * pageSize;
    return PageResult.of(total, pageNo, pageSize, purRequestRepository
        .findPageByTenantIdAndProcessStatus(tenantId, processStatus, offset, pageSize)
        .stream()
        .map(this::toStatusResponse)
        .toList());
  }

  @Override
  @Transactional
  public PurRequestSubmitResponse submit(String tenantId, String requestNo, Long starterUserId) {
    String requiredTenantId = requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空");
    String requiredRequestNo = requireText(requestNo, ERROR_REQUEST_NO_REQUIRED,
        "采购申请单号不能为空");
    Long requiredStarterUserId = requireObject(starterUserId, ERROR_STARTER_REQUIRED,
        "发起人用户 ID 不能为空");
    PurRequest purRequest = purRequestRepository
        .findByTenantIdAndRequestNo(requiredTenantId, requiredRequestNo)
        .orElseThrow(() -> new BusinessException(ERROR_REQUEST_NOT_FOUND, "采购申请不存在"));
    if (!STATUS_DRAFT.equals(purRequest.getProcessStatus())) {
      throw new BusinessException(ERROR_SUBMIT_STATUS_INVALID, "只有草稿状态的采购申请可以提交");
    }
    String processInstanceId = workflowService.startProcess(PROCESS_KEY, requiredRequestNo,
        Map.of("tenantId", requiredTenantId, "requestNo", requiredRequestNo,
            "starterUserId", requiredStarterUserId, "totalAmount", purRequest.getTotalAmount()));
    purRequestRepository.updateSubmitted(requiredTenantId, requiredRequestNo, processInstanceId,
        STATUS_APPROVING, LocalDateTime.now());
    return new PurRequestSubmitResponse(requiredRequestNo, processInstanceId, STATUS_APPROVING);
  }

  /**
   * 校验金额必须大于等于 0。
   *
   * @param amount 原始金额
   * @return 校验后的金额
   */
  private BigDecimal requirePositiveAmount(BigDecimal amount) {
    BigDecimal requiredAmount = requireObject(amount, ERROR_TOTAL_AMOUNT_REQUIRED,
        "采购申请总金额不能为空");
    if (requiredAmount.compareTo(BigDecimal.ZERO) < 0) {
      throw new BusinessException(ERROR_TOTAL_AMOUNT_INVALID, "采购申请总金额不能小于 0");
    }
    return requiredAmount;
  }

  /**
   * 校验业务对象不能为空。
   *
   * @param value 原始对象
   * @param code 业务错误码
   * @param message 为空时的异常消息
   * @return 校验后的对象
   * @param <T> 对象类型
   */
  private <T> T requireObject(T value, String code, String message) {
    if (value == null) {
      throw new BusinessException(code, message);
    }
    return value;
  }

  /**
   * 校验文本不能为空并去除首尾空白。
   *
   * @param value 原始文本
   * @param message 为空时的异常消息
   * @return 清理后的文本
   */
  private String requireText(String value, String code, String message) {
    String normalized = trimToNull(value);
    if (normalized == null) {
      throw new BusinessException(code, message);
    }
    return normalized;
  }

  /**
   * 将空白文本转换为 {@code null}。
   *
   * @param value 原始文本
   * @return 清理后的文本或 {@code null}
   */
  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  /**
   * 转换采购申请状态响应。
   *
   * @param purRequest 采购申请领域对象
   * @return 采购申请状态响应
   */
  private PurRequestStatusResponse toStatusResponse(PurRequest purRequest) {
    return new PurRequestStatusResponse(purRequest.getRequestNo(), purRequest.getRequestTitle(),
        purRequest.getProcessStatus(), purRequest.getTotalAmount(), purRequest.getSubmittedAt());
  }

  /**
   * 规范化页码。
   *
   * @param pageNo 原始页码
   * @return 有效页码
   */
  private int normalizePageNo(int pageNo) {
    return Math.max(pageNo, 1);
  }

  /**
   * 规范化每页记录数。
   *
   * @param pageSize 原始每页记录数
   * @return 有效每页记录数
   */
  private int normalizePageSize(int pageSize) {
    if (pageSize <= 0) {
      return 10;
    }
    return Math.min(pageSize, MAX_PAGE_SIZE);
  }
}
