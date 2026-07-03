/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.purchase.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.purchase.request.domain.PurRequest;
import com.zhyc.purchase.request.repository.PurRequestRepository;
import com.zhyc.purchase.request.service.DefaultPurRequestStatusService;
import com.zhyc.purchase.request.service.PurRequestStatusResponse;
import com.zhyc.purchase.request.service.PurRequestStatusService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * 采购申请状态查询服务测试。
 */
class PurRequestStatusServiceTest {

  /**
   * 验证按租户和申请单号查询采购申请状态，并清理入参空白。
   */
  @Test
  void shouldQueryStatusByTenantIdAndRequestNo() {
    RecordingPurRequestRepository repository = new RecordingPurRequestRepository();
    PurRequestStatusService service = new DefaultPurRequestStatusService(repository);

    PurRequestStatusResponse response = service.queryStatus(" tenant_a ", " PR202606240001 ");

    assertEquals("tenant_a", repository.lastTenantId);
    assertEquals("PR202606240001", repository.lastRequestNo);
    assertEquals("PR202606240001", response.getRequestNo());
    assertEquals("办公设备采购", response.getRequestTitle());
    assertEquals("APPROVING", response.getProcessStatus());
    assertEquals(new BigDecimal("12800.00"), response.getTotalAmount());
  }

  /**
   * 验证租户内找不到采购申请时返回明确业务异常。
   */
  @Test
  void shouldRejectWhenRequestNotFoundInTenant() {
    RecordingPurRequestRepository repository = new RecordingPurRequestRepository();
    repository.result = Optional.empty();
    PurRequestStatusService service = new DefaultPurRequestStatusService(repository);

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.queryStatus("tenant_a", "PR404"));

    assertEquals("ZHYC_PUR_REQUEST_NOT_FOUND", exception.getCode());
    assertEquals("采购申请不存在", exception.getMessage());
  }

  /**
   * 测试用采购申请仓储。
   */
  private static class RecordingPurRequestRepository implements PurRequestRepository {

    /** 最近一次查询的租户业务编码。 */
    private String lastTenantId;
    /** 最近一次查询的采购申请单号。 */
    private String lastRequestNo;
    /** 查询返回结果。 */
    private Optional<PurRequest> result = Optional.of(new PurRequest(1L, "tenant_a",
        "PR202606240001", "办公设备采购", 1001L, 2001L, new BigDecimal("12800.00"),
        "新员工工位设备", "APPROVING", LocalDateTime.parse("2026-06-24T10:30:00"),
        LocalDateTime.parse("2026-06-24T09:00:00"), LocalDateTime.parse("2026-06-24T09:30:00")));

    @Override
    public Optional<PurRequest> findByTenantIdAndRequestNo(String tenantId, String requestNo) {
      this.lastTenantId = tenantId;
      this.lastRequestNo = requestNo;
      return result;
    }

    @Override
    public void save(PurRequest purRequest) {
    }

    @Override
    public void updateSubmitted(String tenantId, String requestNo, String processInstanceId,
        String processStatus, LocalDateTime submittedAt) {
    }

    @Override
    public long countByTenantIdAndProcessStatus(String tenantId, String processStatus) {
      return 0;
    }

    @Override
    public List<PurRequest> findPageByTenantIdAndProcessStatus(String tenantId, String processStatus,
        long offset, int pageSize) {
      return List.of();
    }

    @Override
    public void updateProcessStatus(String tenantId, String requestNo, String processStatus,
        LocalDateTime updatedAt) {
    }
  }
}
