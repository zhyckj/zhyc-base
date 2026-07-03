/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.i18n;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 国际化词条初始化脚本测试。
 */
class I18nSeedSqlTest {

  /** 国际化词条初始化脚本路径。 */
  private static final Path SEED_SQL_PATH = Path.of("src/main/resources/db/V2__i18n_seed.sql");

  /**
   * 验证国际化默认词条覆盖中英文基础菜单、平台名称和常用状态。
   *
   * @throws IOException 读取初始化脚本失败时抛出
   */
  @Test
  void shouldSeedBaselineI18nMessages() throws IOException {
    String sql = Files.readString(SEED_SQL_PATH, StandardCharsets.UTF_8).toLowerCase(Locale.ROOT);

    assertTrue(sql.contains("insert into i18n_message"),
        "国际化初始化脚本必须写入 i18n_message");
    assertTrue(sql.contains("'zhyc-platform', 'zh-cn', 'platform.name', 'zhyc 快速开发平台', 'enabled'"),
        "国际化初始化脚本必须包含中文平台名称");
    assertTrue(sql.contains("'zhyc-platform', 'en-us', 'platform.name', 'zhyc rapid development platform', 'enabled'"),
        "国际化初始化脚本必须包含英文平台名称");
    assertTrue(sql.contains("'menu.system.secret'"),
        "国际化初始化脚本必须覆盖密钥管理菜单词条");
    assertTrue(sql.contains("'button.save'"),
        "国际化初始化脚本必须覆盖常用按钮词条");
    assertTrue(sql.contains("on duplicate key update"),
        "国际化初始化脚本必须具备重复执行幂等性");
  }
}
