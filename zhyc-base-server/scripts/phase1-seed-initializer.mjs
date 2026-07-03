/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { existsSync, readFileSync, writeFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { resolve } from 'node:path';
import { loadRuntimeEnv } from './lib/profile-runtime-env.mjs';

const serverRoot = resolve(fileURLToPath(new URL('..', import.meta.url)));
const passwordHashEnv = 'ZHYC_LOCAL_ADMIN_PASSWORD_HASH';
const runtimeEnv = loadRuntimeEnv(process.argv, serverRoot);
const usage = '支持 --plan、--check、--emit-mysql、--materialize --output <file> 四种模式；可追加 --env dev/test/prod 或 --profile dev/test/prod 从 Spring Profile 配置解析数据库名；兼容 --env-file <file> 覆盖旧本机配置；不传参数时等同 --plan。';
const mode = resolveMode(process.argv);

const seedSteps = [
  {
    name: '系统基础种子数据',
    database: 'zhyc_platform',
    envUrl: 'ZHYC_PLATFORM_DATASOURCE_URL',
    envUser: 'ZHYC_PLATFORM_DATASOURCE_USERNAME',
    envPassword: 'ZHYC_PLATFORM_DATASOURCE_PASSWORD',
    script: 'zhyc-module-system/src/main/resources/db/V2__system_seed.sql',
    note: '初始化默认租户、套餐、组织、管理员账号占位哈希、角色、菜单、字典、参数、编码规则和模块登记。',
  },
];

if (mode === 'check') {
  runSeedCheck();
} else if (mode === 'emit-mysql') {
  printMysqlCommands();
} else if (mode === 'materialize') {
  materializeSeedSql();
} else {
  printSeedPlan();
}

/**
 * 解析种子数据初始化运行模式。
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
  if (argv.includes('--materialize')) {
    return 'materialize';
  }
  return 'plan';
}

/**
 * 输出首期种子数据初始化计划。
 */
function printSeedPlan() {
  console.log('首期种子数据初始化计划');
  console.log(usage);
  seedSteps.forEach((step, index) => {
    console.log(`${index + 1}. ${step.name}`);
    console.log(`   数据库：${resolveDatabaseName(step)}`);
    console.log(`   脚本：${step.script}`);
    console.log(`   说明：${step.note}`);
  });
}

/**
 * 输出可人工执行的 MySQL 种子数据导入命令。
 */
function printMysqlCommands() {
  console.log('首期种子数据初始化 MySQL 命令');
  for (const step of seedSteps) {
    console.log(`# ${step.name}`);
    console.log(`mysql --defaults-extra-file=.mysql-client.cnf --database=${resolveDatabaseName(step)} < ${step.script}`);
  }
  console.log('# 导入前请先设置 ZHYC_LOCAL_ADMIN_PASSWORD，并运行 PlatformPasswordHashCli 生成 Shiro 密码哈希。');
  console.log('# 生成命令：rtk mvn -pl zhyc-platform-app -Dexec.mainClass=com.zhyc.platform.security.PlatformPasswordHashCli exec:java');
  console.log(`# 再设置 ${passwordHashEnv}，运行 --materialize --output 生成本机可导入 SQL。`);
  console.log(`# 导入示例：mysql --defaults-extra-file=.mysql-client.cnf --database=${resolveDatabaseName(seedSteps[0])} < /tmp/zhyc-system-seed.local.sql`);
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
 * 生成本机可导入的种子 SQL，避免修改仓库内占位脚本。
 */
function materializeSeedSql() {
  const errors = [];
  const outputPath = resolveOutputPath(process.argv, errors);
  const passwordHash = resolvePasswordHash(errors);
  const seedStep = seedSteps[0];
  const sourcePath = resolve(serverRoot, seedStep.script);

  if (!existsSync(sourcePath)) {
    errors.push(`缺少种子数据脚本：${seedStep.script}`);
  }
  if (errors.length > 0) {
    console.error('首期种子数据 SQL 物化失败。');
    errors.forEach((error) => console.error(`- ${error}`));
    process.exit(1);
  }

  const sourceSql = readFileSync(sourcePath, 'utf8');
  const materializedSql = sourceSql.replaceAll('replace_with_shiro_password_hash', escapeSqlString(passwordHash));
  if (materializedSql.includes('replace_with_shiro_password_hash')) {
    console.error('首期种子数据 SQL 物化失败。');
    console.error('- 物化 SQL 仍包含密码哈希占位符');
    process.exit(1);
  }
  writeFileSync(outputPath, materializedSql, 'utf8');
  console.log(`首期种子数据 SQL 已生成：${outputPath}`);
}

/**
 * 校验首期种子数据初始化材料是否完整。
 */
function runSeedCheck() {
  const errors = [];
  for (const step of seedSteps) {
    const scriptPath = resolve(serverRoot, step.script);
    if (!existsSync(scriptPath)) {
      errors.push(`缺少种子数据脚本：${step.script}`);
      continue;
    }
    inspectSeedSql(step, scriptPath, errors);
  }

  if (errors.length > 0) {
    console.error('首期种子数据初始化检查失败。');
    errors.forEach((error) => console.error(`- ${error}`));
    process.exit(1);
  }

  console.log('首期种子数据初始化检查通过。');
}

/**
 * 解析物化输出路径。
 *
 * @param argv 命令行参数
 * @param errors 错误收集器
 * @returns 输出文件绝对路径
 */
function resolveOutputPath(argv, errors) {
  const outputIndex = argv.indexOf('--output');
  if (outputIndex < 0 || !argv[outputIndex + 1]) {
    errors.push('缺少物化输出路径：--output <file>');
    return resolve(process.cwd(), 'V2__system_seed.local.sql');
  }
  return resolve(process.cwd(), argv[outputIndex + 1]);
}

/**
 * 读取并校验本地管理员 Shiro 密码哈希。
 *
 * @param errors 错误收集器
 * @returns 本地管理员 Shiro 密码哈希
 */
function resolvePasswordHash(errors) {
  const passwordHash = String(process.env[passwordHashEnv] || '').trim();
  if (!passwordHash) {
    errors.push(`缺少本地管理员 Shiro 密码哈希环境变量：${passwordHashEnv}`);
    return '';
  }
  if (passwordHash.includes('replace_with_')) {
    errors.push(`本地管理员 Shiro 密码哈希仍为占位符：${passwordHashEnv}`);
  }
  if (passwordHash.length < 40) {
    errors.push(`本地管理员 Shiro 密码哈希长度异常：${passwordHashEnv}`);
  }
  return passwordHash;
}

/**
 * 转义 SQL 字符串值中的单引号。
 *
 * @param value 原始字符串
 * @returns 可安全写入单引号字符串的内容
 */
function escapeSqlString(value) {
  return value.replaceAll("'", "''");
}

/**
 * 检查种子 SQL 的幂等性、安全占位和基础数据覆盖。
 *
 * @param step 初始化步骤
 * @param scriptPath SQL 绝对路径
 * @param errors 错误收集器
 */
function inspectSeedSql(step, scriptPath, errors) {
  const content = readFileSync(scriptPath, 'utf8');
  for (const snippet of [
    'INSERT IGNORE INTO sys_tenant',
    'INSERT IGNORE INTO sys_user',
    'INSERT IGNORE INTO sys_role',
    'INTO sys_menu',
    'ON DUPLICATE KEY UPDATE',
    'INSERT IGNORE INTO sys_role_menu',
    'INSERT IGNORE INTO sys_module',
    'tenant_id',
    'zhyc-platform',
    'admin',
    'replace_with_shiro_password_hash',
  ]) {
    if (!content.includes(snippet)) {
      errors.push(`种子数据脚本缺少关键内容：${step.script} -> ${snippet}`);
    }
  }
  if (/password_hash'\s*,\s*'[^']*(admin|password|123456|secret)[^']*'/i.test(content)) {
    errors.push(`种子数据脚本疑似包含弱默认密码或真实密码：${step.script}`);
  }
  if (/INSERT\s+INTO\s+/i.test(content) && !/INSERT\s+IGNORE\s+INTO\s+/i.test(content)) {
    errors.push(`种子数据脚本必须使用 INSERT IGNORE 保证幂等：${step.script}`);
  }
}
