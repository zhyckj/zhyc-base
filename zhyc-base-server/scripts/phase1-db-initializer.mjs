/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { existsSync, readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { resolve } from 'node:path';
import { loadRuntimeEnv } from './lib/profile-runtime-env.mjs';

const serverRoot = resolve(fileURLToPath(new URL('..', import.meta.url)));
const runtimeEnv = loadRuntimeEnv(process.argv, serverRoot);
const usage = '支持 --plan、--check、--emit-mysql、--emit-postgresql、--emit-oracle、--emit-sqlserver、--emit-dm 七种模式；可追加 --env dev/test/prod 或 --profile dev/test/prod 从 Spring Profile 配置解析数据库名；兼容 --env-file <file> 覆盖旧本机配置；不传参数时等同 --plan。';
const mode = resolveMode(process.argv);

const initializationSteps = [
  {
    name: '认证中心核心表',
    database: 'zhyc_auth',
    envUrl: 'ZHYC_AUTH_DATASOURCE_URL',
    envUser: 'ZHYC_AUTH_DATASOURCE_USERNAME',
    envPassword: 'ZHYC_AUTH_DATASOURCE_PASSWORD',
    ddl: 'zhyc-auth-server/src/main/resources/db/V1__auth_server_core.sql',
    note: '创建 Spring Authorization Server 注册客户端、授权记录和授权确认表。',
  },
  {
    name: '系统与租户核心表',
    database: 'zhyc_platform',
    envUrl: 'ZHYC_PLATFORM_DATASOURCE_URL',
    envUser: 'ZHYC_PLATFORM_DATASOURCE_USERNAME',
    envPassword: 'ZHYC_PLATFORM_DATASOURCE_PASSWORD',
    ddl: 'zhyc-module-system/src/main/resources/db/V1__system_core.sql',
    note: '创建租户、用户、角色、组织、岗位、菜单、参数、字典、审计和权限基础表。',
  },
  {
    name: '安全防护中心表',
    database: 'zhyc_platform',
    envUrl: 'ZHYC_PLATFORM_DATASOURCE_URL',
    envUser: 'ZHYC_PLATFORM_DATASOURCE_USERNAME',
    envPassword: 'ZHYC_PLATFORM_DATASOURCE_PASSWORD',
    ddl: 'zhyc-module-system/src/main/resources/db/V5__system_security_protection.sql',
    note: '创建安全策略、安全事件和 IP 封禁表，并初始化基础防护策略。',
  },
  {
    name: '低代码元数据表',
    database: 'zhyc_platform',
    envUrl: 'ZHYC_PLATFORM_DATASOURCE_URL',
    envUser: 'ZHYC_PLATFORM_DATASOURCE_USERNAME',
    envPassword: 'ZHYC_PLATFORM_DATASOURCE_PASSWORD',
    ddl: 'zhyc-module-lowcode/src/main/resources/db/V1__lowcode_core.sql',
    note: '创建数据源、表建模、字段建模、页面模型、生成记录和模板扩展表。',
  },
  {
    name: '工作流运行表',
    database: 'zhyc_platform',
    envUrl: 'ZHYC_PLATFORM_DATASOURCE_URL',
    envUser: 'ZHYC_PLATFORM_DATASOURCE_USERNAME',
    envPassword: 'ZHYC_PLATFORM_DATASOURCE_PASSWORD',
    ddl: 'zhyc-module-workflow/src/main/resources/db/V1__workflow_core.sql',
    note: '创建平台工作流门面使用的定义、实例、任务和操作审计表。',
  },
  {
    name: 'Flowable 引擎运行表',
    database: 'zhyc_platform',
    tenantScoped: false,
    envUrl: 'ZHYC_PLATFORM_DATASOURCE_URL',
    envUser: 'ZHYC_PLATFORM_DATASOURCE_USERNAME',
    envPassword: 'ZHYC_PLATFORM_DATASOURCE_PASSWORD',
    ddl: 'zhyc-module-workflow/src/main/resources/db/V2__flowable_engine_mysql.sql',
    note: '创建 Flowable 引擎自身使用的 ACT_* 和 FLW_* 运行表，只包含结构，不包含流程实例数据。',
  },
  {
    name: '开放平台管理表',
    database: 'zhyc_platform',
    envUrl: 'ZHYC_PLATFORM_DATASOURCE_URL',
    envUser: 'ZHYC_PLATFORM_DATASOURCE_USERNAME',
    envPassword: 'ZHYC_PLATFORM_DATASOURCE_PASSWORD',
    ddl: 'zhyc-module-openapi/src/main/resources/db/V1__openapi_core.sql',
    note: '创建 API Key、签名策略、调用审计和开放接口管理表。',
  },
  {
    name: 'AI 能力中心表',
    database: 'zhyc_platform',
    envUrl: 'ZHYC_PLATFORM_DATASOURCE_URL',
    envUser: 'ZHYC_PLATFORM_DATASOURCE_USERNAME',
    envPassword: 'ZHYC_PLATFORM_DATASOURCE_PASSWORD',
    ddl: 'zhyc-module-ai/src/main/resources/db/V1__ai_core.sql',
    note: '创建模型供应商、模型配置、AI 应用、提示词模板和调用审计表。',
  },
  {
    name: '消息中心表',
    database: 'zhyc_platform',
    envUrl: 'ZHYC_PLATFORM_DATASOURCE_URL',
    envUser: 'ZHYC_PLATFORM_DATASOURCE_USERNAME',
    envPassword: 'ZHYC_PLATFORM_DATASOURCE_PASSWORD',
    ddl: 'zhyc-module-message/src/main/resources/db/V1__message_core.sql',
    note: '创建站内消息、消息模板、推送通道和发送记录表。',
  },
  {
    name: '文件中心表',
    database: 'zhyc_platform',
    envUrl: 'ZHYC_PLATFORM_DATASOURCE_URL',
    envUser: 'ZHYC_PLATFORM_DATASOURCE_USERNAME',
    envPassword: 'ZHYC_PLATFORM_DATASOURCE_PASSWORD',
    ddl: 'zhyc-module-file/src/main/resources/db/V1__file_core.sql',
    note: '创建对象存储配置、文件对象和在线预览记录表。',
  },
  {
    name: '内容管理表',
    database: 'zhyc_platform',
    envUrl: 'ZHYC_PLATFORM_DATASOURCE_URL',
    envUser: 'ZHYC_PLATFORM_DATASOURCE_USERNAME',
    envPassword: 'ZHYC_PLATFORM_DATASOURCE_PASSWORD',
    ddl: 'zhyc-module-cms/src/main/resources/db/V1__cms_core.sql',
    note: '创建栏目、内容、标签和发布审计表。',
  },
  {
    name: '在线作业表',
    database: 'zhyc_platform',
    envUrl: 'ZHYC_PLATFORM_DATASOURCE_URL',
    envUser: 'ZHYC_PLATFORM_DATASOURCE_USERNAME',
    envPassword: 'ZHYC_PLATFORM_DATASOURCE_PASSWORD',
    ddl: 'zhyc-module-job/src/main/resources/db/V1__job_core.sql',
    note: '创建作业定义、作业日志和调度策略表。',
  },
  {
    name: '全文检索表',
    database: 'zhyc_platform',
    envUrl: 'ZHYC_PLATFORM_DATASOURCE_URL',
    envUser: 'ZHYC_PLATFORM_DATASOURCE_USERNAME',
    envPassword: 'ZHYC_PLATFORM_DATASOURCE_PASSWORD',
    ddl: 'zhyc-module-search/src/main/resources/db/V1__search_core.sql',
    note: '创建索引配置、重建任务和查询日志表。',
  },
  {
    name: '可视化大屏表',
    database: 'zhyc_platform',
    envUrl: 'ZHYC_PLATFORM_DATASOURCE_URL',
    envUser: 'ZHYC_PLATFORM_DATASOURCE_USERNAME',
    envPassword: 'ZHYC_PLATFORM_DATASOURCE_PASSWORD',
    ddl: 'zhyc-module-visual/src/main/resources/db/V1__visual_core.sql',
    note: '创建数据集、大屏、报表和发布配置表。',
  },
  {
    name: '国际化资源表',
    database: 'zhyc_platform',
    envUrl: 'ZHYC_PLATFORM_DATASOURCE_URL',
    envUser: 'ZHYC_PLATFORM_DATASOURCE_USERNAME',
    envPassword: 'ZHYC_PLATFORM_DATASOURCE_PASSWORD',
    ddl: 'zhyc-module-i18n/src/main/resources/db/V1__i18n_core.sql',
    note: '创建语言、资源项和翻译文本表。',
  },
  {
    name: '采购样板业务表',
    database: 'zhyc_platform',
    envUrl: 'ZHYC_PLATFORM_DATASOURCE_URL',
    envUser: 'ZHYC_PLATFORM_DATASOURCE_USERNAME',
    envPassword: 'ZHYC_PLATFORM_DATASOURCE_PASSWORD',
    ddl: 'zhyc-module-purchase/src/main/resources/db/V1__purchase_core.sql',
    note: '创建用于验证低代码和工作流联动的采购样板业务表。',
  },
];

if (mode === 'check') {
  runInitializationCheck();
} else if (mode === 'emit-mysql') {
  printMysqlCommands();
} else if (mode === 'emit-postgresql') {
  printDialectInitCommand('PostgreSQL', 'psql', 'db/init-zhyc-base-v1.postgresql.sql');
} else if (mode === 'emit-oracle') {
  printDialectInitCommand('Oracle', 'sqlplus', 'db/init-zhyc-base-v1.oracle.sql');
} else if (mode === 'emit-sqlserver') {
  printDialectInitCommand('SQL Server', 'sqlcmd', 'db/init-zhyc-base-v1.sqlserver.sql');
} else if (mode === 'emit-dm') {
  printDialectInitCommand('达梦数据库', 'disql', 'db/init-zhyc-base-v1.dm.sql');
} else {
  printInitializationPlan();
}

/**
 * 解析数据库初始化脚本运行模式。
 *
 * @param argv 命令行参数
 * @returns 运行模式
 */
function resolveMode(argv) {
  if (argv.includes('--check')) {
    return 'check';
  }
  if (argv.includes('--emit-mysql')) {
    return 'emit-mysql';
  }
  if (argv.includes('--emit-postgresql')) {
    return 'emit-postgresql';
  }
  if (argv.includes('--emit-oracle')) {
    return 'emit-oracle';
  }
  if (argv.includes('--emit-sqlserver')) {
    return 'emit-sqlserver';
  }
  if (argv.includes('--emit-dm')) {
    return 'emit-dm';
  }
  return 'plan';
}

/**
 * 输出首期数据库初始化计划。
 */
function printInitializationPlan() {
  console.log('首期数据库初始化计划');
  console.log(usage);
  initializationSteps.forEach((step, index) => {
    console.log(`${index + 1}. ${step.name}`);
    console.log(`   数据库：${resolveDatabaseName(step)}`);
    console.log(`   DDL：${step.ddl}`);
    console.log(`   说明：${step.note}`);
  });
}

/**
 * 输出可人工执行的 MySQL 初始化命令。
 */
function printMysqlCommands() {
  console.log('首期数据库初始化 MySQL 命令');
  for (const step of initializationSteps) {
    console.log(`# ${step.name}`);
    console.log(`mysql --defaults-extra-file=.mysql-client.cnf --database=${resolveDatabaseName(step)} < ${step.ddl}`);
  }
  console.log('# .mysql-client.cnf 仅保存在本机，示例：');
  console.log('# [client]');
  console.log('# user=replace_with_db_user');
  console.log('# password=replace_with_db_password');
  console.log('# host=127.0.0.1');
  console.log('# port=3306');
}

/**
 * 输出可人工执行的指定数据库初始化命令。
 *
 * @param databaseName 数据库显示名称
 * @param clientCommand 客户端命令名称
 * @param scriptPath 根目录初始化脚本路径
 */
function printDialectInitCommand(databaseName, clientCommand, scriptPath) {
  console.log(`首期数据库初始化 ${databaseName} 命令`);
  console.log(`# 脚本来源：${scriptPath}`);
  if (clientCommand === 'psql') {
    console.log(`psql "$ZHYC_PLATFORM_DATABASE_URL" -f ../${scriptPath}`);
    return;
  }
  if (clientCommand === 'sqlplus') {
    console.log(`sqlplus "$ZHYC_PLATFORM_DATABASE_USER/$ZHYC_PLATFORM_DATABASE_PASSWORD@$ZHYC_PLATFORM_DATABASE_TNS" @../${scriptPath}`);
    return;
  }
  if (clientCommand === 'disql') {
    console.log(`disql "$ZHYC_PLATFORM_DATABASE_USER/$ZHYC_PLATFORM_DATABASE_PASSWORD@$ZHYC_PLATFORM_DATABASE_HOST:5236" \`../${scriptPath}\``);
    return;
  }
  console.log(`sqlcmd -S "$ZHYC_PLATFORM_DATABASE_HOST" -d "${resolveDatabaseName(initializationSteps[1])}" -U "$ZHYC_PLATFORM_DATABASE_USER" -P "$ZHYC_PLATFORM_DATABASE_PASSWORD" -i ../${scriptPath}`);
}

/**
 * 按环境变量中的 JDBC URL 解析数据库名，未配置时使用脚本默认库名。
 *
 * @param step 初始化步骤
 * @returns 数据库名
 */
function resolveDatabaseName(step) {
  const datasourceUrl = runtimeEnv.get(step.envUrl);
  if (!datasourceUrl) {
    return step.database;
  }
  const match = datasourceUrl.match(/^jdbc:mysql:\/\/[^/]+\/([^?]+)/i);
  return match ? decodeURIComponent(match[1]) : step.database;
}

/**
 * 校验首期数据库初始化材料是否完整。
 */
function runInitializationCheck() {
  const errors = [];
  const seenDdl = new Set();

  for (const step of initializationSteps) {
    const ddlPath = resolve(serverRoot, step.ddl);
    if (!existsSync(ddlPath)) {
      errors.push(`缺少数据库初始化 DDL：${step.ddl}`);
      continue;
    }
    if (seenDdl.has(step.ddl)) {
      errors.push(`数据库初始化 DDL 重复登记：${step.ddl}`);
    }
    seenDdl.add(step.ddl);
    inspectDdl(step, ddlPath, errors);
  }

  if (errors.length > 0) {
    console.error('首期数据库初始化检查失败。');
    errors.forEach((error) => console.error(`- ${error}`));
    process.exit(1);
  }

  console.log('首期数据库初始化检查通过。');
}

/**
 * 检查单个 DDL 的基本质量。
 *
 * @param step 初始化步骤
 * @param ddlPath DDL 绝对路径
 * @param errors 错误收集器
 */
function inspectDdl(step, ddlPath, errors) {
  const content = readFileSync(ddlPath, 'utf8');
  const createTableCount = (content.match(/CREATE\s+TABLE/gi) || []).length;
  if (createTableCount === 0) {
    errors.push(`DDL 未声明建表语句：${step.ddl}`);
  }
  if (!/\bCOMMENT\b/i.test(content)) {
    errors.push(`DDL 缺少中文注释声明：${step.ddl}`);
  }
  if (!/IF\s+NOT\s+EXISTS/i.test(content)) {
    errors.push(`DDL 建表语句必须具备幂等性：${step.ddl}`);
  }
  if (step.database === 'zhyc_platform' && step.tenantScoped !== false && !/\btenant_id\b/i.test(content)) {
    errors.push(`平台业务 DDL 必须包含 tenant_id 隔离字段：${step.ddl}`);
  }
}
