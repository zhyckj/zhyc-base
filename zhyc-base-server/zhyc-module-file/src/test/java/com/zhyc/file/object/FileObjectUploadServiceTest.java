/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.object;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.file.object.service.FileObjectRegisterCommand;
import com.zhyc.file.object.service.FileObjectService;
import com.zhyc.file.object.service.FileObjectUploadCommand;
import com.zhyc.file.object.service.FileObjectUploadResponse;
import com.zhyc.file.object.service.LocalFileObjectUploadService;
import com.zhyc.file.storage.domain.FileStorageConfig;
import com.zhyc.file.storage.repository.FileStorageConfigRepository;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 文件对象上传服务测试。
 */
class FileObjectUploadServiceTest {

  /** 测试临时目录。 */
  @TempDir
  private Path tempDir;

  /**
   * 验证上传文件会保存到本地存储并自动登记文件对象。
   *
   * @throws Exception 文件读写失败时抛出
   */
  @Test
  void shouldStoreUploadedFileAndRegisterObject() throws Exception {
    RecordingObjectService objectService = new RecordingObjectService();
    FileObjectUploadCommand command = new FileObjectUploadCommand(" tenant_a ", "local-default",
        1001L, new StubMultipartFile("合同.pdf", "application/pdf", "hello".getBytes()));
    LocalFileObjectUploadService service = new LocalFileObjectUploadService(objectService,
        new RecordingStorageRepository(tempDir));

    FileObjectUploadResponse response = service.upload(command);

    assertEquals("FILE001", response.fileCode());
    assertEquals("local-default", response.storageCode());
    assertEquals("合同.pdf", objectService.lastCommand.originalName());
    assertEquals("application/pdf", objectService.lastCommand.contentType());
    assertEquals(5L, objectService.lastCommand.fileSize());
    assertTrue(objectService.lastCommand.objectKey().startsWith("tenant_a/"));
    assertArrayEquals("hello".getBytes(), Files.readAllBytes(tempDir.resolve(objectService.lastCommand.objectKey())));
  }

  /**
   * 验证空文件不会写入对象表。
   */
  @Test
  void shouldRejectEmptyUploadedFile() {
    LocalFileObjectUploadService service = new LocalFileObjectUploadService(new RecordingObjectService(),
        new RecordingStorageRepository(tempDir));

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.upload(new FileObjectUploadCommand("tenant_a", "local-default", 1001L,
            new StubMultipartFile("empty.txt", "text/plain", new byte[0]))));

    assertEquals("ZHYC_FILE_OBJECT_UPLOAD_EMPTY", exception.getCode());
    assertEquals("上传文件不能为空", exception.getMessage());
  }

  /**
   * 测试用文件对象服务。
   */
  private static final class RecordingObjectService implements FileObjectService {

    /** 最近一次登记命令。 */
    private FileObjectRegisterCommand lastCommand;

    @Override
    public String register(FileObjectRegisterCommand command) {
      this.lastCommand = command;
      return "FILE001";
    }

    @Override
    public com.zhyc.common.api.PageResult<com.zhyc.file.object.service.FileObjectResponse> listFiles(
        com.zhyc.file.object.service.FileObjectQuery query) {
      throw new AssertionError("上传测试不应查询文件对象");
    }
  }

  /**
   * 测试用存储配置仓储。
   */
  private static final class RecordingStorageRepository implements FileStorageConfigRepository {

    /** 本地存储根目录。 */
    private final Path rootPath;

    private RecordingStorageRepository(Path rootPath) {
      this.rootPath = rootPath;
    }

    @Override
    public List<FileStorageConfig> findByTenantId(String tenantId) {
      return List.of(new FileStorageConfig(1L, tenantId, "local-default", "本地存储",
          "local", rootPath.toString(), "enabled", true, null, null));
    }

    @Override
    public void save(FileStorageConfig config) {
      throw new AssertionError("上传测试不应保存存储配置");
    }
  }

  /**
   * 测试用 MultipartFile。
   */
  private static final class StubMultipartFile implements MultipartFile {

    /** 原始文件名。 */
    private final String originalFilename;
    /** 文件内容类型。 */
    private final String contentType;
    /** 文件内容。 */
    private final byte[] content;

    private StubMultipartFile(String originalFilename, String contentType, byte[] content) {
      this.originalFilename = originalFilename;
      this.contentType = contentType;
      this.content = content.clone();
    }

    @Override
    public String getName() {
      return "file";
    }

    @Override
    public String getOriginalFilename() {
      return originalFilename;
    }

    @Override
    public String getContentType() {
      return contentType;
    }

    @Override
    public boolean isEmpty() {
      return content.length == 0;
    }

    @Override
    public long getSize() {
      return content.length;
    }

    @Override
    public byte[] getBytes() {
      return content.clone();
    }

    @Override
    public InputStream getInputStream() {
      return new ByteArrayInputStream(content);
    }

    @Override
    public void transferTo(java.io.File dest) throws IOException {
      Files.write(dest.toPath(), content);
    }

    @Override
    public void transferTo(Path dest) throws IOException {
      Files.write(dest, content);
    }
  }
}
