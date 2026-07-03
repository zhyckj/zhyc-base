/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.storage;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.file.storage.domain.FileStorageConfig;
import com.zhyc.file.storage.repository.FileStorageConfigRepository;
import com.zhyc.file.storage.service.DefaultFileStorageConfigService;
import com.zhyc.file.storage.service.FileStorageConfigResponse;
import com.zhyc.file.storage.service.FileStorageConfigSaveCommand;
import com.zhyc.file.storage.service.FileStorageConfigService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 文件存储配置业务服务测试。
 */
class FileStorageConfigServiceTest {

  /**
   * 验证文件存储配置按租户查询，防止跨租户读取对象存储配置。
   */
  @Test
  void shouldListStorageConfigsByTenant() {
    RecordingRepository repository = new RecordingRepository();
    FileStorageConfigService service = new DefaultFileStorageConfigService(repository);

    List<FileStorageConfigResponse> responses = service.listConfigs(" tenant_a ");

    assertEquals("tenant_a", repository.lastTenantId);
    assertEquals(1, responses.size());
    assertEquals("local-default", responses.get(0).storageCode());
  }

  /**
   * 验证保存文件存储配置会裁剪字段并默认启用。
   */
  @Test
  void shouldSaveStorageConfigWithDefaults() {
    RecordingRepository repository = new RecordingRepository();
    FileStorageConfigService service = new DefaultFileStorageConfigService(repository);

    service.save(new FileStorageConfigSaveCommand(" tenant_a ", " local-default ", " 本地存储 ",
        " local ", "/data/files", null, true));

    assertEquals("tenant_a", repository.lastSaved.getTenantId());
    assertEquals("local-default", repository.lastSaved.getStorageCode());
    assertEquals("本地存储", repository.lastSaved.getStorageName());
    assertEquals("local", repository.lastSaved.getStorageType());
    assertEquals("enabled", repository.lastSaved.getStatus());
  }

  /**
   * 验证文件存储配置拒绝非法存储类型和状态，避免不可用配置进入对象存储链路。
   */
  @Test
  void shouldRejectInvalidStorageTypeAndStatus() {
    FileStorageConfigService service = new DefaultFileStorageConfigService(new RecordingRepository());

    BusinessException typeException = assertThrows(BusinessException.class,
        () -> service.save(new FileStorageConfigSaveCommand("tenant_a", "ftp-default", "FTP 存储",
            "ftp", "ftp://files", "enabled", false)));
    BusinessException statusException = assertThrows(BusinessException.class,
        () -> service.save(new FileStorageConfigSaveCommand("tenant_a", "local-default", "本地存储",
            "local", "/data/files", "archived", true)));

    assertEquals("ZHYC_FILE_STORAGE_TYPE_UNSUPPORTED", typeException.getCode());
    assertEquals("存储类型不支持: ftp", typeException.getMessage());
    assertEquals("ZHYC_FILE_STORAGE_STATUS_UNSUPPORTED", statusException.getCode());
    assertEquals("配置状态不支持: archived", statusException.getMessage());
  }

  /**
   * 测试用文件存储配置仓储。
   */
  private static class RecordingRepository implements FileStorageConfigRepository {

    /** 最近一次查询的租户业务编码。 */
    private String lastTenantId;
    /** 最近一次保存的文件存储配置。 */
    private FileStorageConfig lastSaved;

    @Override
    public List<FileStorageConfig> findByTenantId(String tenantId) {
      lastTenantId = tenantId;
      return List.of(new FileStorageConfig(1L, tenantId, "local-default", "本地存储", "local",
          "/data/files", "enabled", true, LocalDateTime.now(), LocalDateTime.now()));
    }

    @Override
    public void save(FileStorageConfig config) {
      lastSaved = config;
    }
  }
}
