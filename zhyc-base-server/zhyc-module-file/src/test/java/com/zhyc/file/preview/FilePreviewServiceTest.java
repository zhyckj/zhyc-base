/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.preview;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.file.preview.domain.FilePreviewLog;
import com.zhyc.file.preview.repository.FilePreviewRepository;
import com.zhyc.file.preview.service.DefaultFilePreviewService;
import com.zhyc.file.preview.service.FilePreviewCreateCommand;
import com.zhyc.file.preview.service.FilePreviewLogQuery;
import com.zhyc.file.preview.service.FilePreviewLogResponse;
import com.zhyc.file.preview.service.FilePreviewRenderResponse;
import com.zhyc.file.preview.service.FilePreviewResponse;
import com.zhyc.file.preview.service.FilePreviewService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 文件预览业务服务测试。
 */
class FilePreviewServiceTest {

  /**
   * 验证创建预览会裁剪租户和文件编码，生成预览地址并记录成功日志。
   */
  @Test
  void shouldCreatePreviewAndRecordSuccessLog() {
    RecordingRepository repository = new RecordingRepository();
    FilePreviewService service = new DefaultFilePreviewService(repository);

    FilePreviewResponse response = service.createPreview(new FilePreviewCreateCommand(" tenant_a ",
        " FILE001 ", " pdf "));

    assertEquals("FILE001", response.fileCode());
    assertEquals("pdf", response.previewType());
    assertTrue(response.previewUrl().contains("/file/preview/render/FILE001"));
    assertEquals("tenant_a", repository.lastSaved.getTenantId());
    assertEquals("FILE001", repository.lastSaved.getFileCode());
    assertEquals("pdf", repository.lastSaved.getPreviewType());
    assertEquals("success", repository.lastSaved.getResult());
  }

  /**
   * 验证渲染预览会裁剪租户和文件编码，生成可访问地址并记录成功日志。
   */
  @Test
  void shouldRenderPreviewAndRecordSuccessLog() {
    RecordingRepository repository = new RecordingRepository();
    FilePreviewService service = new DefaultFilePreviewService(repository);

    FilePreviewRenderResponse response = service.renderPreview(" tenant_a ", " FILE001 ", " pdf ");

    assertEquals("FILE001", response.fileCode());
    assertEquals("pdf", response.previewType());
    assertEquals("/file/preview/render/FILE001?type=pdf", response.previewUrl());
    assertEquals("success", response.result());
    assertEquals("tenant_a", repository.lastSaved.getTenantId());
    assertEquals("FILE001", repository.lastSaved.getFileCode());
    assertEquals("pdf", repository.lastSaved.getPreviewType());
    assertEquals("success", repository.lastSaved.getResult());
  }

  /**
   * 验证查询预览日志会按租户和文件编码过滤。
   */
  @Test
  void shouldListPreviewLogsByTenantAndFileCode() {
    RecordingRepository repository = new RecordingRepository();
    FilePreviewService service = new DefaultFilePreviewService(repository);

    List<FilePreviewLogResponse> logs = service.listPreviewLogs(new FilePreviewLogQuery(" tenant_a ",
        " FILE001 "));

    assertEquals("tenant_a", repository.lastQuery.tenantId());
    assertEquals("FILE001", repository.lastQuery.fileCode());
    assertEquals("success", logs.getFirst().result());
  }

  /**
   * 验证文件预览拒绝非法预览类型，避免任意字符串进入预览地址。
   */
  @Test
  void shouldRejectInvalidPreviewType() {
    FilePreviewService service = new DefaultFilePreviewService(new RecordingRepository());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.createPreview(new FilePreviewCreateCommand("tenant_a", "FILE001", "html<script>")));

    assertEquals("ZHYC_FILE_PREVIEW_TYPE_UNSUPPORTED", exception.getCode());
    assertEquals("预览类型不支持: html<script>", exception.getMessage());
  }

  /**
   * 测试用文件预览仓储。
   */
  private static class RecordingRepository implements FilePreviewRepository {

    /** 最近一次写入的预览日志。 */
    private FilePreviewLog lastSaved;
    /** 最近一次查询条件。 */
    private FilePreviewLogQuery lastQuery;

    @Override
    public void saveLog(FilePreviewLog log) {
      lastSaved = log;
    }

    @Override
    public List<FilePreviewLog> findLogs(FilePreviewLogQuery query) {
      lastQuery = query;
      return List.of(new FilePreviewLog(1L, query.tenantId(), query.fileCode(), "pdf",
          "/file/preview/render/FILE001?type=pdf", "success", 1L, LocalDateTime.now()));
    }
  }
}
