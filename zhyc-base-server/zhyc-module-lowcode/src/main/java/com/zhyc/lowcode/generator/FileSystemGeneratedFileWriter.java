/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * 文件系统生成文件写入器。
 */
public class FileSystemGeneratedFileWriter implements GeneratedFileWriter {

  /** 输出根目录。 */
  private final Path outputRoot;
  /** 生成文件覆盖策略。 */
  private final GeneratedFileOverwriteStrategy overwriteStrategy;

  /**
   * 创建文件系统生成文件写入器。
   *
   * @param outputRoot 输出根目录
   */
  public FileSystemGeneratedFileWriter(Path outputRoot) {
    this(outputRoot, GeneratedFileOverwriteStrategy.FAIL_IF_EXISTS);
  }

  /**
   * 创建文件系统生成文件写入器。
   *
   * @param outputRoot 输出根目录
   * @param overwriteStrategy 生成文件覆盖策略
   */
  public FileSystemGeneratedFileWriter(Path outputRoot, GeneratedFileOverwriteStrategy overwriteStrategy) {
    this.outputRoot = Objects.requireNonNull(outputRoot, "输出根目录不能为空")
        .toAbsolutePath()
        .normalize();
    this.overwriteStrategy = Objects.requireNonNull(overwriteStrategy, "生成文件覆盖策略不能为空");
  }

  /**
   * 使用默认覆盖策略写入生成文件。
   *
   * @param files 生成文件列表
   * @return 实际写入的文件路径列表
   */
  @Override
  public List<Path> write(List<GeneratedFile> files) {
    return write(files, overwriteStrategy);
  }

  /**
   * 使用指定覆盖策略写入生成文件。
   *
   * <p>写入前会校验路径是否仍在输出根目录内，防止模板路径越界覆盖工作区外文件。</p>
   *
   * @param files 生成文件列表
   * @param overwriteStrategy 生成文件覆盖策略
   * @return 实际写入的文件路径列表
   */
  @Override
  public List<Path> write(List<GeneratedFile> files, GeneratedFileOverwriteStrategy overwriteStrategy) {
    Objects.requireNonNull(files, "生成文件清单不能为空");
    Objects.requireNonNull(overwriteStrategy, "生成文件覆盖策略不能为空");
    return files.stream()
        .map(file -> writeOne(file, overwriteStrategy))
        .filter(Objects::nonNull)
        .toList();
  }

  private Path writeOne(GeneratedFile file, GeneratedFileOverwriteStrategy overwriteStrategy) {
    Objects.requireNonNull(file, "生成文件不能为空");
    Path targetPath = resolveTargetPath(file);
    try {
      Files.createDirectories(targetPath.getParent());
      if (Files.exists(targetPath)) {
        return handleExistingFile(file, targetPath, overwriteStrategy);
      }
      Files.writeString(targetPath, file.getContent(), StandardCharsets.UTF_8);
      return targetPath;
    } catch (IOException ex) {
      throw new IllegalStateException("写入生成文件失败: " + file.getPath(), ex);
    }
  }

  private Path resolveTargetPath(GeneratedFile file) {
    Path targetPath = outputRoot.resolve(file.getPath()).normalize();
    if (!targetPath.startsWith(outputRoot)) {
      throw new IllegalArgumentException("生成文件路径越界: " + file.getPath());
    }
    return targetPath;
  }

  private Path handleExistingFile(GeneratedFile file, Path targetPath,
                                  GeneratedFileOverwriteStrategy overwriteStrategy) throws IOException {
    if (overwriteStrategy == GeneratedFileOverwriteStrategy.SKIP_IF_EXISTS) {
      return null;
    }
    if (overwriteStrategy == GeneratedFileOverwriteStrategy.FAIL_IF_EXISTS) {
      throw new IllegalStateException("生成文件已存在，当前策略不允许覆盖: " + file.getPath());
    }
    Files.writeString(targetPath, file.getContent(), StandardCharsets.UTF_8);
    return targetPath;
  }
}
