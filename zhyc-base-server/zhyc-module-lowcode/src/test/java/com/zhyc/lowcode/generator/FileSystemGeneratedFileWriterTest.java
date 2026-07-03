/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 文件系统生成文件写入器测试。
 */
class FileSystemGeneratedFileWriterTest {

  /** 临时输出根目录。 */
  @TempDir
  private Path outputRoot;

  /**
   * 验证写入器会创建父目录并把生成文件写入输出根目录内。
   *
   * @throws IOException 读取写入结果失败时抛出
   */
  @Test
  void shouldWriteGeneratedFilesUnderOutputRoot() throws IOException {
    GeneratedFileWriter writer = new FileSystemGeneratedFileWriter(outputRoot);
    GeneratedFile file = new GeneratedFile(
        GenerationTarget.ADMIN_FRONTEND,
        "admin-vue-list",
        "src/views/purchase/order/list.vue",
        "<template>订单列表</template>");

    List<Path> writtenPaths = writer.write(List.of(file));

    Path expectedPath = outputRoot.resolve("src/views/purchase/order/list.vue");
    assertEquals(List.of(expectedPath), writtenPaths);
    assertEquals("<template>订单列表</template>", Files.readString(expectedPath));
  }

  /**
   * 验证写入器在显式覆盖策略下允许覆盖同一路径，便于重新生成模块代码。
   *
   * @throws IOException 读取写入结果失败时抛出
   */
  @Test
  void shouldOverwriteExistingGeneratedFile() throws IOException {
    GeneratedFileWriter writer = new FileSystemGeneratedFileWriter(outputRoot, GeneratedFileOverwriteStrategy.OVERWRITE);
    Path existingFile = outputRoot.resolve("src/api/purchase/order.ts");
    Files.createDirectories(existingFile.getParent());
    Files.writeString(existingFile, "old");
    GeneratedFile file = new GeneratedFile(
        GenerationTarget.ADMIN_FRONTEND,
        "admin-api",
        "src/api/purchase/order.ts",
        "new");

    writer.write(List.of(file));

    assertEquals("new", Files.readString(existingFile));
  }

  /**
   * 验证写入器支持按单次生成请求指定覆盖策略，避免 Spring 单例 Bean 固定策略。
   *
   * @throws IOException 读取写入结果失败时抛出
   */
  @Test
  void shouldApplyRuntimeOverwriteStrategyWhenWriting() throws IOException {
    GeneratedFileWriter writer = new FileSystemGeneratedFileWriter(outputRoot);
    Path existingFile = outputRoot.resolve("src/api/purchase/order.ts");
    Files.createDirectories(existingFile.getParent());
    Files.writeString(existingFile, "old");
    GeneratedFile file = new GeneratedFile(
        GenerationTarget.ADMIN_FRONTEND,
        "admin-api",
        "src/api/purchase/order.ts",
        "new");

    writer.write(List.of(file), GeneratedFileOverwriteStrategy.OVERWRITE);

    assertEquals("new", Files.readString(existingFile));
  }


  /**
   * 验证默认策略会拒绝覆盖已有文件，保护人工修改代码。
   *
   * @throws IOException 创建已有文件失败时抛出
   */
  @Test
  void shouldRejectExistingFileByDefault() throws IOException {
    GeneratedFileWriter writer = new FileSystemGeneratedFileWriter(outputRoot);
    Path existingFile = outputRoot.resolve("src/api/purchase/order.ts");
    Files.createDirectories(existingFile.getParent());
    Files.writeString(existingFile, "manual");
    GeneratedFile file = new GeneratedFile(
        GenerationTarget.ADMIN_FRONTEND,
        "admin-api",
        "src/api/purchase/order.ts",
        "generated");

    IllegalStateException exception = assertThrows(IllegalStateException.class,
        () -> writer.write(List.of(file)));

    assertEquals("生成文件已存在，当前策略不允许覆盖: src/api/purchase/order.ts", exception.getMessage());
    assertEquals("manual", Files.readString(existingFile));
  }

  /**
   * 验证跳过策略会保留已有文件，并且返回清单只包含实际写入的新文件。
   *
   * @throws IOException 读取写入结果失败时抛出
   */
  @Test
  void shouldSkipExistingFileWhenStrategyIsSkip() throws IOException {
    GeneratedFileWriter writer = new FileSystemGeneratedFileWriter(
        outputRoot, GeneratedFileOverwriteStrategy.SKIP_IF_EXISTS);
    Path existingFile = outputRoot.resolve("src/api/purchase/order.ts");
    Files.createDirectories(existingFile.getParent());
    Files.writeString(existingFile, "manual");
    GeneratedFile existing = new GeneratedFile(
        GenerationTarget.ADMIN_FRONTEND,
        "admin-api",
        "src/api/purchase/order.ts",
        "generated");
    GeneratedFile created = new GeneratedFile(
        GenerationTarget.ADMIN_FRONTEND,
        "admin-view",
        "src/views/purchase/order/index.vue",
        "<template />");

    List<Path> writtenPaths = writer.write(List.of(existing, created));

    assertEquals(List.of(outputRoot.resolve("src/views/purchase/order/index.vue")), writtenPaths);
    assertEquals("manual", Files.readString(existingFile));
    assertEquals("<template />", Files.readString(outputRoot.resolve("src/views/purchase/order/index.vue")));
  }

  /**
   * 验证写入器拒绝可能逃逸输出根目录的路径。
   */
  @Test
  void shouldRejectUnsafeOutputPath() {
    GeneratedFileWriter writer = new FileSystemGeneratedFileWriter(outputRoot);

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> writer.write(List.of(new UnsafeGeneratedFile())));

    assertEquals("生成文件路径越界: ../outside.txt", exception.getMessage());
  }

  private static class UnsafeGeneratedFile extends GeneratedFile {

    /**
     * 创建测试用不安全生成文件。
     */
    UnsafeGeneratedFile() {
      super(GenerationTarget.ADMIN_BACKEND, "unsafe", "safe.txt", "unsafe");
    }

    @Override
    public String getPath() {
      return "../outside.txt";
    }
  }
}
