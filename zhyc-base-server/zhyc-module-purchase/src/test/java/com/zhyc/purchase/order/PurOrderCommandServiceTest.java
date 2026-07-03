/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.purchase.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zhyc.common.api.PageResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.purchase.order.domain.PurOrder;
import com.zhyc.purchase.order.repository.PurOrderRepository;
import com.zhyc.purchase.order.service.DefaultPurOrderCommandService;
import com.zhyc.purchase.order.service.PurOrderCreateCommand;
import com.zhyc.purchase.order.service.PurOrderItemCommand;
import com.zhyc.purchase.order.service.PurOrderQuery;
import com.zhyc.purchase.order.service.PurOrderResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * 采购订单命令服务测试。
 */
class PurOrderCommandServiceTest {

  /**
   * 验证可创建采购订单并按租户和订单号查询。
   */
  @Test
  void shouldCreateAndGetPurchaseOrder() {
    MemoryPurOrderRepository repository = new MemoryPurOrderRepository();
    DefaultPurOrderCommandService service = new DefaultPurOrderCommandService(repository);

    String orderNo = service.create(new PurOrderCreateCommand("tenant_a", "PO202606240001",
        "PR202606240001", 2001L, 1001L, BigDecimal.valueOf(1200),
        List.of(new PurOrderItemCommand("办公电脑", BigDecimal.valueOf(2),
            BigDecimal.valueOf(600), BigDecimal.valueOf(1200)))));
    PurOrderResponse response = service.get("tenant_a", orderNo);

    assertEquals("PO202606240001", orderNo);
    assertEquals("PR202606240001", response.getRequestNo());
    assertEquals("CREATED", response.getOrderStatus());
    assertEquals(1, response.getItems().size());
    assertEquals("办公电脑", response.getItems().get(0).getItemName());
  }

  /**
   * 验证采购订单必须包含明细，避免生成无业务意义的空订单。
   */
  @Test
  void shouldRejectOrderWithoutItems() {
    DefaultPurOrderCommandService service = new DefaultPurOrderCommandService(new MemoryPurOrderRepository());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.create(new PurOrderCreateCommand(
        "tenant_a", "PO202606240001", "PR202606240001", 2001L, 1001L,
        BigDecimal.valueOf(1200), List.of())));

    assertEquals("ZHYC_PUR_ORDER_ITEMS_REQUIRED", exception.getCode());
    assertEquals("采购订单明细不能为空", exception.getMessage());
  }

  /**
   * 验证采购订单可从新建状态确认，并禁止重复确认。
   */
  @Test
  void shouldConfirmCreatedPurchaseOrder() {
    MemoryPurOrderRepository repository = new MemoryPurOrderRepository();
    DefaultPurOrderCommandService service = new DefaultPurOrderCommandService(repository);
    service.create(new PurOrderCreateCommand("tenant_a", "PO202606240001",
        "PR202606240001", 2001L, 1001L, BigDecimal.valueOf(1200),
        List.of(new PurOrderItemCommand("办公电脑", BigDecimal.ONE,
            BigDecimal.valueOf(1200), BigDecimal.valueOf(1200)))));

    PurOrderResponse response = service.confirm("tenant_a", "PO202606240001");

    assertEquals("CONFIRMED", response.getOrderStatus());
    assertEquals("CONFIRMED", repository.lastStatus);
    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.confirm("tenant_a", "PO202606240001"));

    assertEquals("ZHYC_PUR_ORDER_STATUS_INVALID", exception.getCode());
    assertEquals("只有新建状态的采购订单可以流转", exception.getMessage());
  }

  /**
   * 验证采购订单可从新建状态关闭。
   */
  @Test
  void shouldCloseCreatedPurchaseOrder() {
    MemoryPurOrderRepository repository = new MemoryPurOrderRepository();
    DefaultPurOrderCommandService service = new DefaultPurOrderCommandService(repository);
    service.create(new PurOrderCreateCommand("tenant_a", "PO202606240002",
        "PR202606240002", 2001L, 1001L, BigDecimal.valueOf(800),
        List.of(new PurOrderItemCommand("办公桌", BigDecimal.ONE,
            BigDecimal.valueOf(800), BigDecimal.valueOf(800)))));

    PurOrderResponse response = service.close("tenant_a", "PO202606240002");

    assertEquals("CLOSED", response.getOrderStatus());
    assertEquals("CLOSED", repository.lastStatus);
  }

  /**
   * 验证可按租户、状态分页查询采购订单列表。
   */
  @Test
  void shouldListPurchaseOrdersByStatusWithPagination() {
    MemoryPurOrderRepository repository = new MemoryPurOrderRepository();
    DefaultPurOrderCommandService service = new DefaultPurOrderCommandService(repository);
    service.create(new PurOrderCreateCommand("tenant_a", "PO202606240001",
        "PR202606240001", 2001L, 1001L, BigDecimal.valueOf(1200),
        List.of(new PurOrderItemCommand("办公电脑", BigDecimal.ONE,
            BigDecimal.valueOf(1200), BigDecimal.valueOf(1200)))));
    service.create(new PurOrderCreateCommand("tenant_a", "PO202606240002",
        "PR202606240002", 2001L, 1001L, BigDecimal.valueOf(800),
        List.of(new PurOrderItemCommand("办公桌", BigDecimal.ONE,
            BigDecimal.valueOf(800), BigDecimal.valueOf(800)))));
    service.create(new PurOrderCreateCommand("tenant_b", "PO202606240003",
        "PR202606240003", 2001L, 1001L, BigDecimal.valueOf(500),
        List.of(new PurOrderItemCommand("办公椅", BigDecimal.ONE,
            BigDecimal.valueOf(500), BigDecimal.valueOf(500)))));
    service.confirm("tenant_a", "PO202606240001");

    PageResult<PurOrderResponse> response =
        service.list(new PurOrderQuery("tenant_a", "CONFIRMED", 1, 10));

    assertEquals(1, response.getTotal());
    assertEquals(1, response.getPageNo());
    assertEquals(10, response.getPageSize());
    assertEquals(1, response.getRecords().size());
    assertEquals("PO202606240001", response.getRecords().get(0).getOrderNo());
    assertEquals("CONFIRMED", response.getRecords().get(0).getOrderStatus());
  }

  /**
   * 内存采购订单仓储。
   */
  private static class MemoryPurOrderRepository implements PurOrderRepository {

    /** 已保存订单。 */
    private final List<PurOrder> orders = new ArrayList<>();
    /** 最近一次更新后的订单状态。 */
    private String lastStatus;

    @Override
    public void save(PurOrder purOrder) {
      orders.add(purOrder);
    }

    @Override
    public Optional<PurOrder> findByTenantIdAndOrderNo(String tenantId, String orderNo) {
      return orders.stream()
          .filter(order -> tenantId.equals(order.getTenantId()) && orderNo.equals(order.getOrderNo()))
          .findFirst();
    }

    @Override
    public long countByTenantIdAndStatus(String tenantId, String orderStatus) {
      return orders.stream()
          .filter(order -> tenantId.equals(order.getTenantId()))
          .filter(order -> orderStatus == null || orderStatus.equals(order.getOrderStatus()))
          .count();
    }

    @Override
    public List<PurOrder> findPageByTenantIdAndStatus(String tenantId, String orderStatus,
        long offset, int pageSize) {
      return orders.stream()
          .filter(order -> tenantId.equals(order.getTenantId()))
          .filter(order -> orderStatus == null || orderStatus.equals(order.getOrderStatus()))
          .skip(offset)
          .limit(pageSize)
          .toList();
    }

    @Override
    public void updateStatus(String tenantId, String orderNo, String orderStatus) {
      PurOrder current = findByTenantIdAndOrderNo(tenantId, orderNo).orElseThrow();
      orders.remove(current);
      orders.add(new PurOrder(current.getTenantId(), current.getOrderNo(), current.getRequestNo(),
          current.getSupplierId(), current.getBuyerId(), current.getTotalAmount(), orderStatus,
          current.getItems()));
      this.lastStatus = orderStatus;
    }
  }
}
