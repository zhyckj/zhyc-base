/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { existsSync, mkdirSync, readFileSync, writeFileSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const serverRoot = resolve(fileURLToPath(new URL('..', import.meta.url)));
const workspaceRoot = resolve(serverRoot, '..');
const defaultOutput = 'db/init-zhyc-base-v1.sql';
const outputPath = resolveOutputPath(process.argv);

const sqlSections = [
  {
    name: '认证中心核心表',
    path: 'zhyc-auth-server/src/main/resources/db/V1__auth_server_core.sql',
  },
  {
    name: '系统与租户核心表',
    path: 'zhyc-module-system/src/main/resources/db/V1__system_core.sql',
  },
  {
    name: '安全防护中心表',
    path: 'zhyc-module-system/src/main/resources/db/V5__system_security_protection.sql',
  },
  {
    name: '低代码元数据表',
    path: 'zhyc-module-lowcode/src/main/resources/db/V1__lowcode_core.sql',
  },
  {
    name: '工作流运行表',
    path: 'zhyc-module-workflow/src/main/resources/db/V1__workflow_core.sql',
  },
  {
    name: 'Flowable 引擎运行表',
    path: 'zhyc-module-workflow/src/main/resources/db/V2__flowable_engine_mysql.sql',
  },
  {
    name: '开放平台管理表',
    path: 'zhyc-module-openapi/src/main/resources/db/V1__openapi_core.sql',
  },
  {
    name: 'AI 能力中心表',
    path: 'zhyc-module-ai/src/main/resources/db/V1__ai_core.sql',
  },
  {
    name: '消息中心表',
    path: 'zhyc-module-message/src/main/resources/db/V1__message_core.sql',
  },
  {
    name: '文件中心表',
    path: 'zhyc-module-file/src/main/resources/db/V1__file_core.sql',
  },
  {
    name: '内容管理表',
    path: 'zhyc-module-cms/src/main/resources/db/V1__cms_core.sql',
  },
  {
    name: '在线作业表',
    path: 'zhyc-module-job/src/main/resources/db/V1__job_core.sql',
  },
  {
    name: '全文检索表',
    path: 'zhyc-module-search/src/main/resources/db/V1__search_core.sql',
  },
  {
    name: '可视化大屏表',
    path: 'zhyc-module-visual/src/main/resources/db/V1__visual_core.sql',
  },
  {
    name: '国际化资源表',
    path: 'zhyc-module-i18n/src/main/resources/db/V1__i18n_core.sql',
  },
  {
    name: '国际化基础词条数据',
    path: 'zhyc-module-i18n/src/main/resources/db/V2__i18n_seed.sql',
  },
  {
    name: '采购样板业务表',
    path: 'zhyc-module-purchase/src/main/resources/db/V1__purchase_core.sql',
  },
  {
    name: '系统基础种子数据',
    path: 'zhyc-module-system/src/main/resources/db/V2__system_seed.sql',
  },
];

buildLocalInitSql();

/**
 * 生成本地 MySQL 5.7 初始化总脚本。
 *
 * <p>该脚本只合并仓库内受控 SQL，不写入本地数据库账号和管理员明文密码，避免初始化文件携带敏感信息。</p>
 */
function buildLocalInitSql() {
  const missingSections = sqlSections.filter((section) => !existsSync(resolve(serverRoot, section.path)));
  if (missingSections.length > 0) {
    console.error('本地初始化 SQL 生成失败。');
    missingSections.forEach((section) => console.error(`- 缺少 SQL 文件：${section.path}`));
    process.exit(1);
  }

  const content = [
    '-- ZHYC 快速开发平台 MySQL 5.7 临时本地初始化脚本',
    '-- 目标数据库：zhyc-base-v1（数据库需提前创建，执行时通过 mysql --database 指定）',
    '-- 说明：管理员 password_hash 保留安全占位符，登录前需使用 Shiro 哈希工具物化真实哈希。',
    'SET NAMES utf8mb4;',
    '',
  ];

  sqlSections.forEach((section, index) => {
    const sourcePath = resolve(serverRoot, section.path);
    const sourceSql = readFileSync(sourcePath, 'utf8').trim();
    content.push(
      `-- ============================================================`,
      `-- ${index + 1}. ${section.name}`,
      `-- 来源：zhyc-base-server/${section.path}`,
      `-- ============================================================`,
      sourceSql,
      '',
    );
  });

  content.push(
    '-- 初始化脚本结束。',
    '-- 如需启用 admin 登录，请运行 PlatformPasswordHashCli 生成 Shiro 密码哈希，并替换 replace_with_shiro_password_hash。',
    '',
  );

  mkdirSync(dirname(outputPath), { recursive: true });
  writeFileSync(outputPath, content.join('\n'), 'utf8');
  console.log(`本地初始化 SQL 已生成：${outputPath}`);
}

/**
 * 解析初始化 SQL 输出路径。
 *
 * @param argv 命令行参数
 * @returns 输出文件绝对路径
 */
function resolveOutputPath(argv) {
  const outputIndex = argv.indexOf('--output');
  const rawOutput = outputIndex >= 0 && argv[outputIndex + 1] ? argv[outputIndex + 1] : defaultOutput;
  return resolve(workspaceRoot, rawOutput);
}
