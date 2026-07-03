/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.purchase.order.service;

import com.zhyc.common.api.PageResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.purchase.order.domain.PurOrder;
import com.zhyc.purchase.order.domain.PurOrderItem;
import com.zhyc.purchase.order.repository.PurOrderRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 默认采购订单命令服务实现。
 */
@Service
public class DefaultPurOrderCommandService implements PurOrderCommandService {

  /** 新建订单状态。 */
  private static final String STATUS_CREATED = "CREATED";
  /** 已确认订单状态。 */
  private static final String STATUS_CONFIRMED = "CONFIRMED";
  /** 已关闭订单状态。 */
  private static final String STATUS_CLOSED = "CLOSED";
  /** 单页最大记录数。 */
  private static final int MAX_PAGE_SIZE = 100;
  /** 采购订单创建命令不能为空错误码。 */
  private static final String ERROR_COMMAND_REQUIRED = "ZHYC_PUR_ORDER_COMMAND_REQUIRED";
  /** 采购订单分页查询条件不能为空错误码。 */
  private static final String ERROR_QUERY_REQUIRED = "ZHYC_PUR_ORDER_QUERY_REQUIRED";
  /** 租户业务编码不能为空错误码。 */
  private static final String ERROR_TENANT_REQUIRED = "ZHYC_PUR_ORDER_TENANT_REQUIRED";
  /** 采购订单号不能为空错误码。 */
  private static final String ERROR_ORDER_NO_REQUIRED = "ZHYC_PUR_ORDER_NO_REQUIRED";
  /** 采购申请单号不能为空错误码。 */
  private static final String ERROR_REQUEST_NO_REQUIRED = "ZHYC_PUR_ORDER_REQUEST_NO_REQUIRED";
  /** 供应商 ID 不能为空错误码。 */
  private static final String ERROR_SUPPLIER_REQUIRED = "ZHYC_PUR_ORDER_SUPPLIER_REQUIRED";
  /** 采购员用户 ID 不能为空错误码。 */
  private static final String ERROR_BUYER_REQUIRED = "ZHYC_PUR_ORDER_BUYER_REQUIRED";
  /** 采购订单总金额不能为空错误码。 */
  private static final String ERROR_TOTAL_AMOUNT_REQUIRED = "ZHYC_PUR_ORDER_TOTAL_AMOUNT_REQUIRED";
  /** 采购订单明细不能为空错误码。 */
  private static final String ERROR_ITEMS_REQUIRED = "ZHYC_PUR_ORDER_ITEMS_REQUIRED";
  /** 采购订单明细项不能为空错误码。 */
  private static final String ERROR_ITEM_REQUIRED = "ZHYC_PUR_ORDER_ITEM_REQUIRED";
  /** 采购订单明细物品名称不能为空错误码。 */
  private static final String ERROR_ITEM_NAME_REQUIRED = "ZHYC_PUR_ORDER_ITEM_NAME_REQUIRED";
  /** 采购订单明细数量不能为空错误码。 */
  private static final String ERROR_QUANTITY_REQUIRED = "ZHYC_PUR_ORDER_QUANTITY_REQUIRED";
  /** 采购订单明细单价不能为空错误码。 */
  private static final String ERROR_UNIT_PRICE_REQUIRED = "ZHYC_PUR_ORDER_UNIT_PRICE_REQUIRED";
  /** 采购订单明细金额不能为空错误码。 */
  private static final String ERROR_ITEM_AMOUNT_REQUIRED = "ZHYC_PUR_ORDER_ITEM_AMOUNT_REQUIRED";
  /** 金额或数量非法错误码。 */
  private static final String ERROR_AMOUNT_INVALID = "ZHYC_PUR_ORDER_AMOUNT_INVALID";
  /** 采购订单不存在错误码。 */
  private static final String ERROR_ORDER_NOT_FOUND = "ZHYC_PUR_ORDER_NOT_FOUND";
  /** 采购订单状态非法错误码。 */
  private static final String ERROR_STATUS_INVALID = "ZHYC_PUR_ORDER_STATUS_INVALID";

  /** 采购订单仓储。 */
  private final PurOrderRepository purOrderRepository;

  /**
   * 创建默认采购订单命令服务。
   *
   * @param purOrderRepository 采购订单仓储
   */
  public DefaultPurOrderCommandService(PurOrderRepository purOrderRepository) {
    this.purOrderRepository = Objects.requireNonNull(purOrderRepository, "采购订单仓储不能为空");
  }

  @Override
  @Transactional
  public String create(PurOrderCreateCommand command) {
    PurOrderCreateCommand requiredCommand = requireObject(command, ERROR_COMMAND_REQUIRED,
        "采购订单创建命令不能为空");
    List<PurOrderItem> items = toItems(requiredCommand.getItems());
    if (items.isEmpty()) {
      throw new BusinessException(ERROR_ITEMS_REQUIRED, "采购订单明细不能为空");
    }
    PurOrder purOrder = new PurOrder(requireText(requiredCommand.getTenantId(), ERROR_TENANT_REQUIRED,
        "租户业务编码不能为空"),
        requireText(requiredCommand.getOrderNo(), ERROR_ORDER_NO_REQUIRED, "采购订单号不能为空"),
        requireText(requiredCommand.getRequestNo(), ERROR_REQUEST_NO_REQUIRED, "采购申请单号不能为空"),
        requireObject(requiredCommand.getSupplierId(), ERROR_SUPPLIER_REQUIRED, "供应商 ID 不能为空"),
        requireObject(requiredCommand.getBuyerId(), ERROR_BUYER_REQUIRED, "采购员用户 ID 不能为空"),
        requireNonNegative(requiredCommand.getTotalAmount(), ERROR_TOTAL_AMOUNT_REQUIRED,
            "采购订单总金额不能为空"),
        STATUS_CREATED, items);
    purOrderRepository.save(purOrder);
    return purOrder.getOrderNo();
  }

  @Override
  public PurOrderResponse get(String tenantId, String orderNo) {
    return toResponse(getRequiredOrder(tenantId, orderNo));
  }

  @Override
  public PageResult<PurOrderResponse> list(PurOrderQuery query) {
    PurOrderQuery requiredQuery = requireObject(query, ERROR_QUERY_REQUIRED,
        "采购订单分页查询条件不能为空");
    String tenantId = requireText(requiredQuery.getTenantId(), ERROR_TENANT_REQUIRED,
        "租户业务编码不能为空");
    String orderStatus = normalizeOptionalText(requiredQuery.getOrderStatus());
    int pageNo = normalizePageNo(requiredQuery.getPageNo());
    int pageSize = normalizePageSize(requiredQuery.getPageSize());
    long total = purOrderRepository.countByTenantIdAndStatus(tenantId, orderStatus);
    long offset = (long) (pageNo - 1) * pageSize;
    List<PurOrderResponse> records = purOrderRepository
        .findPageByTenantIdAndStatus(tenantId, orderStatus, offset, pageSize).stream()
        .map(this::toResponse)
        .toList();
    return PageResult.of(total, pageNo, pageSize, records);
  }

  @Override
  @Transactional
  public PurOrderResponse confirm(String tenantId, String orderNo) {
    return changeStatus(tenantId, orderNo, STATUS_CONFIRMED);
  }

  @Override
  @Transactional
  public PurOrderResponse close(String tenantId, String orderNo) {
    return changeStatus(tenantId, orderNo, STATUS_CLOSED);
  }

  /**
   * 修改采购订单状态。
   *
   * @param tenantId 租户业务编码
   * @param orderNo 采购订单号
   * @param nextStatus 下一订单状态
   * @return 修改后的采购订单响应
   */
  private PurOrderResponse changeStatus(String tenantId, String orderNo, String nextStatus) {
    PurOrder purOrder = getRequiredOrder(tenantId, orderNo);
    if (!STATUS_CREATED.equals(purOrder.getOrderStatus())) {
      throw new BusinessException(ERROR_STATUS_INVALID, "只有新建状态的采购订单可以流转");
    }
    purOrderRepository.updateStatus(purOrder.getTenantId(), purOrder.getOrderNo(), nextStatus);
    return toResponse(new PurOrder(purOrder.getTenantId(), purOrder.getOrderNo(),
        purOrder.getRequestNo(), purOrder.getSupplierId(), purOrder.getBuyerId(),
        purOrder.getTotalAmount(), nextStatus, purOrder.getItems()));
  }

  /**
   * 查询必然存在的采购订单。
   *
   * @param tenantId 租户业务编码
   * @param orderNo 采购订单号
   * @return 采购订单
   */
  private PurOrder getRequiredOrder(String tenantId, String orderNo) {
    PurOrder purOrder = purOrderRepository.findByTenantIdAndOrderNo(
        requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空"),
        requireText(orderNo, ERROR_ORDER_NO_REQUIRED, "采购订单号不能为空"))
        .orElseThrow(() -> new BusinessException(ERROR_ORDER_NOT_FOUND, "采购订单不存在"));
    return purOrder;
  }

  /**
   * 转换采购订单明细。
   *
   * @param commands 明细命令
   * @return 明细领域对象
   */
  private List<PurOrderItem> toItems(List<PurOrderItemCommand> commands) {
    if (commands == null) {
      throw new BusinessException(ERROR_ITEMS_REQUIRED, "采购订单明细不能为空");
    }
    return commands.stream()
        .map(item -> {
          PurOrderItemCommand requiredItem = requireObject(item, ERROR_ITEM_REQUIRED,
              "采购订单明细项不能为空");
          return new PurOrderItem(requireText(requiredItem.getItemName(), ERROR_ITEM_NAME_REQUIRED,
              "物品名称不能为空"),
              requireNonNegative(requiredItem.getQuantity(), ERROR_QUANTITY_REQUIRED,
                  "采购数量不能为空"),
              requireNonNegative(requiredItem.getUnitPrice(), ERROR_UNIT_PRICE_REQUIRED,
                  "采购单价不能为空"),
              requireNonNegative(requiredItem.getAmount(), ERROR_ITEM_AMOUNT_REQUIRED,
                  "明细金额不能为空"));
        })
        .toList();
  }

  /**
   * 转换采购订单响应。
   *
   * @param purOrder 采购订单领域对象
   * @return 采购订单响应
   */
  private PurOrderResponse toResponse(PurOrder purOrder) {
    return new PurOrderResponse(purOrder.getTenantId(), purOrder.getOrderNo(),
        purOrder.getRequestNo(), purOrder.getSupplierId(), purOrder.getBuyerId(),
        purOrder.getTotalAmount(), purOrder.getOrderStatus(), purOrder.getItems().stream()
        .map(item -> new PurOrderItemResponse(item.getItemName(), item.getQuantity(),
            item.getUnitPrice(), item.getAmount()))
        .toList());
  }

  /**
   * 校验金额不能小于 0。
   *
   * @param value 原始金额
   * @param message 为空时异常消息
   * @return 校验后的金额
   */
  private BigDecimal requireNonNegative(BigDecimal value, String code, String message) {
    BigDecimal requiredValue = requireObject(value, code, message);
    if (requiredValue.compareTo(BigDecimal.ZERO) < 0) {
      throw new BusinessException(ERROR_AMOUNT_INVALID, "金额或数量不能小于 0");
    }
    return requiredValue;
  }

  /**
   * 校验业务对象不能为空。
   *
   * @param value 原始对象
   * @param code 业务错误码
   * @param message 为空时异常消息
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
   * @param message 为空时异常消息
   * @return 清理后的文本
   */
  private String requireText(String value, String code, String message) {
    if (value == null) {
      throw new BusinessException(code, message);
    }
    String trimmed = value.trim();
    if (trimmed.isEmpty()) {
      throw new BusinessException(code, message);
    }
    return trimmed;
  }

  /**
   * 规范化可选文本。
   *
   * @param value 原始文本
   * @return 清理后的文本，空白文本返回 null
   */
  private String normalizeOptionalText(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
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
