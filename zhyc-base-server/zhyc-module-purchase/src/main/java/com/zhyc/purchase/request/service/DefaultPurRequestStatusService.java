/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.purchase.request.service;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.purchase.request.domain.PurRequest;
import com.zhyc.purchase.request.repository.PurRequestRepository;
import java.util.Objects;
import org.springframework.stereotype.Service;

/**
 * 默认采购申请状态查询服务实现。
 */
@Service
public class DefaultPurRequestStatusService implements PurRequestStatusService {

  /** 租户业务编码不能为空错误码。 */
  private static final String ERROR_TENANT_REQUIRED = "ZHYC_PUR_REQUEST_TENANT_REQUIRED";
  /** 采购申请单号不能为空错误码。 */
  private static final String ERROR_REQUEST_NO_REQUIRED = "ZHYC_PUR_REQUEST_NO_REQUIRED";
  /** 采购申请不存在错误码。 */
  private static final String ERROR_REQUEST_NOT_FOUND = "ZHYC_PUR_REQUEST_NOT_FOUND";

  /** 采购申请仓储。 */
  private final PurRequestRepository purRequestRepository;

  /**
   * 创建默认采购申请状态查询服务。
   *
   * @param purRequestRepository 采购申请仓储
   */
  public DefaultPurRequestStatusService(PurRequestRepository purRequestRepository) {
    this.purRequestRepository = Objects.requireNonNull(purRequestRepository, "采购申请仓储不能为空");
  }

  @Override
  public PurRequestStatusResponse queryStatus(String tenantId, String requestNo) {
    String requiredTenantId = requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空");
    String requiredRequestNo = requireText(requestNo, ERROR_REQUEST_NO_REQUIRED,
        "采购申请单号不能为空");
    PurRequest purRequest = purRequestRepository.findByTenantIdAndRequestNo(requiredTenantId, requiredRequestNo)
        .orElseThrow(() -> new BusinessException(ERROR_REQUEST_NOT_FOUND, "采购申请不存在"));
    return new PurRequestStatusResponse(purRequest.getRequestNo(), purRequest.getRequestTitle(),
        purRequest.getProcessStatus(), purRequest.getTotalAmount(), purRequest.getSubmittedAt());
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
}
