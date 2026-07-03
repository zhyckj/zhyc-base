/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

import java.nio.file.Path;
import java.util.List;

/**
 * 生成文件写入器接口。
 */
public interface GeneratedFileWriter {

  /**
   * 写入生成文件清单。
   *
   * @param files 生成文件清单
   * @return 实际写入的文件路径清单
   */
  List<Path> write(List<GeneratedFile> files);

  /**
   * 按指定覆盖策略写入生成文件清单。
   *
   * @param files 生成文件清单
   * @param overwriteStrategy 生成文件覆盖策略
   * @return 实际写入的文件路径清单
   */
  default List<Path> write(List<GeneratedFile> files, GeneratedFileOverwriteStrategy overwriteStrategy) {
    return write(files);
  }
}
