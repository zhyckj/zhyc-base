/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.object;

import com.zhyc.common.api.PageResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.file.object.domain.FileObject;
import com.zhyc.file.object.repository.FileObjectRepository;
import com.zhyc.file.object.service.DefaultFileObjectService;
import com.zhyc.file.object.service.FileObjectQuery;
import com.zhyc.file.object.service.FileObjectRegisterCommand;
import com.zhyc.file.object.service.FileObjectResponse;
import com.zhyc.file.object.service.FileObjectService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 文件对象业务服务测试。
 */
class FileObjectServiceTest {

  /**
   * 验证登记文件对象会生成文件编码并写入租户、存储和文件基础信息。
   */
  @Test
  void shouldRegisterFileObject() {
    RecordingRepository repository = new RecordingRepository();
    FileObjectService service = new DefaultFileObjectService(repository);

    String fileCode = service.register(new FileObjectRegisterCommand(" tenant_a ", " local-default ",
        " 合同.pdf ", "application/pdf", 1024L, "/2026/contract.pdf", 1001L));

    assertTrue(fileCode.startsWith("FILE"));
    assertEquals("tenant_a", repository.lastSaved.getTenantId());
    assertEquals("local-default", repository.lastSaved.getStorageCode());
    assertEquals("合同.pdf", repository.lastSaved.getOriginalName());
    assertEquals("application/pdf", repository.lastSaved.getContentType());
  }

  /**
   * 验证文件对象分页查询会裁剪租户并限制页大小。
   */
  @Test
  void shouldListFileObjectsByTenant() {
    RecordingRepository repository = new RecordingRepository();
    FileObjectService service = new DefaultFileObjectService(repository);

    PageResult<FileObjectResponse> page = service.listFiles(new FileObjectQuery(" tenant_a ", " pdf ", 0, 200));

    assertEquals("tenant_a", repository.lastQuery.tenantId());
    assertEquals("pdf", repository.lastQuery.keyword());
    assertEquals(1, repository.lastQuery.pageNo());
    assertEquals(100, repository.lastQuery.pageSize());
    assertEquals(1, page.getTotal());
  }

  /**
   * 验证文件对象登记拒绝非法文件大小，避免无效元数据进入对象存储链路。
   */
  @Test
  void shouldRejectInvalidFileSize() {
    FileObjectService service = new DefaultFileObjectService(new RecordingRepository());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.register(new FileObjectRegisterCommand("tenant_a", "local-default",
            "合同.pdf", "application/pdf", -1L, "/2026/contract.pdf", 1001L)));

    assertEquals("ZHYC_FILE_OBJECT_SIZE_INVALID", exception.getCode());
    assertEquals("文件大小不能为空", exception.getMessage());
  }

  /**
   * 测试用文件对象仓储。
   */
  private static class RecordingRepository implements FileObjectRepository {

    /** 最近一次保存的文件对象。 */
    private FileObject lastSaved;
    /** 最近一次查询条件。 */
    private FileObjectQuery lastQuery;

    @Override
    public long countByQuery(FileObjectQuery query) {
      lastQuery = query;
      return 1;
    }

    @Override
    public List<FileObject> findPageByQuery(FileObjectQuery query, int offset) {
      lastQuery = query;
      return List.of(new FileObject(1L, query.tenantId(), "FILE001", "local-default",
          "合同.pdf", "application/pdf", 1024L, "/2026/contract.pdf", "stored", 1001L,
          LocalDateTime.now()));
    }

    @Override
    public void save(FileObject fileObject) {
      lastSaved = fileObject;
    }
  }
}
