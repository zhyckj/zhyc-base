/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.visual;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.visual.domain.VisualDataset;
import com.zhyc.visual.domain.VisualReport;
import com.zhyc.visual.domain.VisualScreen;
import com.zhyc.visual.repository.VisualRepository;
import com.zhyc.visual.service.VisualDatasetPreviewResponse;
import com.zhyc.visual.service.DefaultVisualService;
import com.zhyc.visual.service.VisualDatasetResponse;
import com.zhyc.visual.service.VisualDatasetSaveCommand;
import com.zhyc.visual.service.VisualReportResponse;
import com.zhyc.visual.service.VisualReportSaveCommand;
import com.zhyc.visual.service.VisualScreenResponse;
import com.zhyc.visual.service.VisualScreenSaveCommand;
import com.zhyc.visual.service.VisualService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 可视化报表业务服务测试。
 */
class VisualServiceTest {

  /**
   * 验证保存数据集会裁剪编码、名称和 SQL，并默认启用状态。
   */
  @Test
  void shouldSaveDatasetWithNormalizedFields() {
    RecordingRepository repository = new RecordingRepository();
    VisualService service = new DefaultVisualService(repository);

    service.saveDataset(new VisualDatasetSaveCommand(" tenant_a ", " sales_month ",
        " 月度销售 ", " main_ds ", " select sum(amount) from pur_order ", null));

    assertEquals("tenant_a", repository.lastDataset.getTenantId());
    assertEquals("sales_month", repository.lastDataset.getDatasetCode());
    assertEquals("月度销售", repository.lastDataset.getDatasetName());
    assertEquals("enabled", repository.lastDataset.getStatus());
  }

  /**
   * 验证数据集 SQL 只允许保存单条只读查询，避免报表配置写入危险语句。
   */
  @Test
  void shouldRejectUnsafeDatasetSql() {
    VisualService service = new DefaultVisualService(new RecordingRepository());

    BusinessException updateException = assertThrows(BusinessException.class,
        () -> service.saveDataset(new VisualDatasetSaveCommand("tenant_a", "sales_month",
            "月度销售", "main_ds", "update pur_order set amount = 0", "enabled")));
    BusinessException multiStatementException = assertThrows(BusinessException.class,
        () -> service.saveDataset(new VisualDatasetSaveCommand("tenant_a", "sales_month",
            "月度销售", "main_ds", "select * from pur_order; select * from sys_user", "enabled")));
    BusinessException nonQueryException = assertThrows(BusinessException.class,
        () -> service.saveDataset(new VisualDatasetSaveCommand("tenant_a", "sales_month",
            "月度销售", "main_ds", "show tables", "enabled")));

    assertEquals("ZHYC_VISUAL_DATASET_SQL_UNSAFE", updateException.getCode());
    assertEquals("数据集 SQL 仅允许单条只读查询", updateException.getMessage());
    assertEquals("ZHYC_VISUAL_DATASET_SQL_UNSAFE", multiStatementException.getCode());
    assertEquals("数据集 SQL 仅允许单条只读查询", multiStatementException.getMessage());
    assertEquals("ZHYC_VISUAL_DATASET_SQL_UNSAFE", nonQueryException.getCode());
    assertEquals("数据集 SQL 仅允许单条只读查询", nonQueryException.getMessage());
  }

  /**
   * 验证数据集预览先按租户和编码查询数据集，并返回可绑定字段与样例行。
   */
  @Test
  void shouldPreviewDatasetFieldsWithoutExecutingDatasource() {
    RecordingRepository repository = new RecordingRepository();
    VisualService service = new DefaultVisualService(repository);

    VisualDatasetPreviewResponse response = service.previewDataset(" tenant_a ", " sales_month ", 2);

    assertEquals("tenant_a", repository.lastTenantId);
    assertEquals("sales_month", repository.lastDatasetCode);
    assertEquals("sales_month", response.datasetCode());
    assertEquals(List.of("month", "amount"), response.columns());
    assertEquals(2, response.rows().size());
    assertEquals(false, response.executable());
    assertEquals("数据源执行器未启用，当前返回字段解析和样例数据", response.message());
  }

  /**
   * 验证保存报表会校验数据集编码和报表名称。
   */
  @Test
  void shouldRejectReportWithoutDatasetCode() {
    VisualService service = new DefaultVisualService(new RecordingRepository());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.saveReport(new VisualReportSaveCommand("tenant_a", "order_chart",
            "订单趋势", " ", "line", "{}", "enabled")));

    assertEquals("ZHYC_VISUAL_DATASET_CODE_REQUIRED", exception.getCode());
    assertEquals("数据集编码不能为空", exception.getMessage());
  }

  /**
   * 验证保存看板会裁剪布局 JSON 并默认草稿状态。
   */
  @Test
  void shouldSaveScreenWithDraftStatus() {
    RecordingRepository repository = new RecordingRepository();
    VisualService service = new DefaultVisualService(repository);

    service.saveScreen(new VisualScreenSaveCommand(" tenant_a ", " ops_screen ",
        " 运营大屏 ", " [{\"reportCode\":\"order_chart\"}] ", null));

    assertEquals("tenant_a", repository.lastScreen.getTenantId());
    assertEquals("ops_screen", repository.lastScreen.getScreenCode());
    assertEquals("运营大屏", repository.lastScreen.getScreenName());
    assertEquals("draft", repository.lastScreen.getStatus());
  }

  /**
   * 验证公开访问只读取指定租户下已发布的大屏。
   */
  @Test
  void shouldGetPublishedScreenForPublicAccess() {
    RecordingRepository repository = new RecordingRepository();
    VisualService service = new DefaultVisualService(repository);

    VisualScreenResponse response = service.getPublishedScreen(" tenant_a ", " ops_screen ");

    assertEquals("tenant_a", repository.lastTenantId);
    assertEquals("ops_screen", repository.lastScreenCode);
    assertEquals("ops_screen", response.screenCode());
    assertEquals("published", response.status());
  }

  /**
   * 验证公开大屏数据预览只能读取已发布大屏布局中引用的数据集。
   */
  @Test
  void shouldPreviewDatasetReferencedByPublishedScreen() {
    RecordingRepository repository = new RecordingRepository();
    VisualService service = new DefaultVisualService(repository);

    VisualDatasetPreviewResponse response = service.previewPublishedScreenDataset(" tenant_a ",
        " ops_screen ", " sales_month ", 2);

    assertEquals("tenant_a", repository.lastTenantId);
    assertEquals("ops_screen", repository.lastScreenCode);
    assertEquals("sales_month", repository.lastDatasetCode);
    assertEquals(List.of("month", "amount"), response.columns());
    assertEquals(2, response.rows().size());
  }

  /**
   * 验证公开访问只读取指定租户下已发布的报表。
   */
  @Test
  void shouldGetPublishedReportForPublicAccess() {
    RecordingRepository repository = new RecordingRepository();
    VisualService service = new DefaultVisualService(repository);

    VisualReportResponse response = service.getPublishedReport(" tenant_a ", " order_chart ");

    assertEquals("tenant_a", repository.lastTenantId);
    assertEquals("order_chart", repository.lastReportCode);
    assertEquals("order_chart", response.reportCode());
    assertEquals("published", response.status());
  }

  /**
   * 验证公开报表数据预览只能读取已发布报表配置中引用的数据集。
   */
  @Test
  void shouldPreviewDatasetReferencedByPublishedReport() {
    RecordingRepository repository = new RecordingRepository();
    VisualService service = new DefaultVisualService(repository);

    VisualDatasetPreviewResponse response = service.previewPublishedReportDataset(" tenant_a ",
        " order_chart ", " sales_month ", 2);

    assertEquals("tenant_a", repository.lastTenantId);
    assertEquals("order_chart", repository.lastReportCode);
    assertEquals("sales_month", repository.lastDatasetCode);
    assertEquals(List.of("month", "amount"), response.columns());
    assertEquals(2, response.rows().size());
  }

  /**
   * 验证列表查询会按租户隔离并透传状态过滤。
   */
  @Test
  void shouldListVisualResourcesByTenant() {
    RecordingRepository repository = new RecordingRepository();
    VisualService service = new DefaultVisualService(repository);

    List<VisualDatasetResponse> datasets = service.listDatasets(" tenant_a ", " enabled ");
    List<VisualReportResponse> reports = service.listReports(" tenant_a ", " enabled ");
    List<VisualScreenResponse> screens = service.listScreens(" tenant_a ", " published ");

    assertEquals("tenant_a", repository.lastTenantId);
    assertEquals("published", repository.lastStatus);
    assertEquals("sales_month", datasets.getFirst().datasetCode());
    assertEquals("订单趋势", reports.getFirst().reportName());
    assertEquals("运营大屏", screens.getFirst().screenName());
  }

  /**
   * 验证可视化资源拒绝非法状态，避免任意状态写入数据集、报表和大屏。
   */
  @Test
  void shouldRejectInvalidVisualStatuses() {
    VisualService service = new DefaultVisualService(new RecordingRepository());

    BusinessException datasetException = assertThrows(BusinessException.class,
        () -> service.saveDataset(new VisualDatasetSaveCommand("tenant_a", "sales_month",
            "月度销售", "main_ds", "select 1", "archived")));
    BusinessException reportException = assertThrows(BusinessException.class,
        () -> service.saveReport(new VisualReportSaveCommand("tenant_a", "order_chart",
            "订单趋势", "sales_month", "line", "{}", "archived")));
    BusinessException transitionReportException = assertThrows(BusinessException.class,
        () -> service.updateReportStatus("tenant_a", 1L, "enabled"));
    BusinessException screenException = assertThrows(BusinessException.class,
        () -> service.saveScreen(new VisualScreenSaveCommand("tenant_a", "ops_screen",
            "运营大屏", "[]", "enabled")));
    BusinessException transitionException = assertThrows(BusinessException.class,
        () -> service.updateScreenStatus("tenant_a", 1L, "enabled"));

    assertEquals("ZHYC_VISUAL_DATASET_STATUS_UNSUPPORTED", datasetException.getCode());
    assertEquals("数据集状态不支持: archived", datasetException.getMessage());
    assertEquals("ZHYC_VISUAL_REPORT_STATUS_UNSUPPORTED", reportException.getCode());
    assertEquals("报表状态不支持: archived", reportException.getMessage());
    assertEquals("ZHYC_VISUAL_REPORT_STATUS_UNSUPPORTED", transitionReportException.getCode());
    assertEquals("报表状态不支持: enabled", transitionReportException.getMessage());
    assertEquals("ZHYC_VISUAL_SCREEN_STATUS_UNSUPPORTED", screenException.getCode());
    assertEquals("大屏状态不支持: enabled", screenException.getMessage());
    assertEquals("ZHYC_VISUAL_SCREEN_STATUS_UNSUPPORTED", transitionException.getCode());
    assertEquals("大屏状态不支持: enabled", transitionException.getMessage());
  }

  /**
   * 验证报表支持发布状态写入，供报表公开访问入口使用。
   */
  @Test
  void shouldPublishReportStatus() {
    RecordingRepository repository = new RecordingRepository();
    VisualService service = new DefaultVisualService(repository);

    service.saveReport(new VisualReportSaveCommand("tenant_a", "order_chart",
        "订单趋势", "sales_month", "line", "{}", "published"));
    service.updateReportStatus(" tenant_a ", 10L, " published ");

    assertEquals("published", repository.lastReport.getStatus());
    assertEquals("tenant_a", repository.lastTenantId);
    assertEquals("published", repository.lastStatus);
  }

  /**
   * 测试用可视化仓储。
   */
  private static class RecordingRepository implements VisualRepository {

    /** 最近一次保存的数据集。 */
    private VisualDataset lastDataset;
    /** 最近一次保存的报表。 */
    private VisualReport lastReport;
    /** 最近一次保存的看板。 */
    private VisualScreen lastScreen;
    /** 最近一次查询租户。 */
    private String lastTenantId;
    /** 最近一次状态条件。 */
    private String lastStatus;
    /** 最近一次数据集编码。 */
    private String lastDatasetCode;
    /** 最近一次报表编码。 */
    private String lastReportCode;
    /** 最近一次大屏编码。 */
    private String lastScreenCode;

    @Override
    public List<VisualDataset> findDatasets(String tenantId, String status) {
      lastTenantId = tenantId;
      lastStatus = status;
      return List.of(new VisualDataset(1L, tenantId, "sales_month", "月度销售",
          "main_ds", "select sum(amount) from pur_order", "enabled", LocalDateTime.now(),
          LocalDateTime.now()));
    }

    @Override
    public Optional<VisualDataset> findDatasetByCode(String tenantId, String datasetCode) {
      lastTenantId = tenantId;
      lastDatasetCode = datasetCode;
      return Optional.of(new VisualDataset(1L, tenantId, "sales_month", "月度销售",
          "main_ds", "select month as month, sum(amount) as amount from pur_order group by month",
          "enabled", LocalDateTime.now(), LocalDateTime.now()));
    }

    @Override
    public void saveDataset(VisualDataset dataset) {
      lastDataset = dataset;
    }

    @Override
    public List<VisualReport> findReports(String tenantId, String status) {
      lastTenantId = tenantId;
      lastStatus = status;
      return List.of(new VisualReport(1L, tenantId, "order_chart", "订单趋势",
          "sales_month", "line", "{}", "enabled", LocalDateTime.now(), LocalDateTime.now()));
    }

    @Override
    public void saveReport(VisualReport report) {
      lastReport = report;
    }

    @Override
    public Optional<VisualReport> findPublishedReport(String tenantId, String reportCode) {
      lastTenantId = tenantId;
      lastReportCode = reportCode;
      return Optional.of(new VisualReport(1L, tenantId, reportCode, "订单趋势",
          "sales_month", "line",
          "{\"components\":[{\"datasetCode\":\"sales_month\"}]}",
          "published", LocalDateTime.now(), LocalDateTime.now()));
    }

    @Override
    public void updateReportStatus(String tenantId, Long id, String status) {
      lastTenantId = tenantId;
      lastStatus = status;
    }

    @Override
    public List<VisualScreen> findScreens(String tenantId, String status) {
      lastTenantId = tenantId;
      lastStatus = status;
      return List.of(new VisualScreen(1L, tenantId, "ops_screen", "运营大屏",
          "[]", "published", LocalDateTime.now(), LocalDateTime.now()));
    }

    @Override
    public Optional<VisualScreen> findPublishedScreen(String tenantId, String screenCode) {
      lastTenantId = tenantId;
      lastScreenCode = screenCode;
      return Optional.of(new VisualScreen(1L, tenantId, screenCode, "运营大屏",
          "{\"widgets\":[{\"datasetCode\":\"sales_month\"}]}",
          "published", LocalDateTime.now(), LocalDateTime.now()));
    }

    @Override
    public void saveScreen(VisualScreen screen) {
      lastScreen = screen;
    }

    @Override
    public void updateScreenStatus(String tenantId, Long id, String status) {
      lastTenantId = tenantId;
      lastStatus = status;
    }
  }
}
