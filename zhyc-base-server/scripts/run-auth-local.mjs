/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { existsSync, readFileSync } from 'node:fs';
import { spawn, spawnSync } from 'node:child_process';
import { resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { createServer } from 'node:net';

const serverRoot = resolve(fileURLToPath(new URL('..', import.meta.url)));
const args = process.argv.slice(2);
const checkOnly = args.includes('--check');
const springProfile = resolveSpringProfile(args);
const mavenCommand = resolveMavenCommand();
const springBootRunGoal = resolveSpringBootRunGoal();
const runtimeEnvMap = new Map(Object.entries(process.env));

const errors = [];
const serverPort = resolveServerPort(args, runtimeEnvMap, errors);

if (errors.length > 0) {
  console.error('认证中心本地启动环境检查失败。');
  errors.forEach((error) => console.error(`- ${error}`));
  process.exit(1);
}

if (checkOnly) {
  console.log('认证中心本地启动环境检查通过。');
  console.log(`Spring Profile：${springProfile}`);
  console.log(`Maven 命令：${mavenCommand[0]}`);
  console.log(`服务端口：${serverPort}`);
  process.exit(0);
}

await assertPortAvailable(serverPort);
console.log(`正在启动认证中心，使用 Spring Profile：${springProfile}。`);
const runtimeEnv = process.env;

runMavenStep(
  '正在构建并安装认证中心依赖模块。',
  [...mavenCommand.slice(1), '-pl', 'zhyc-auth-server', '-am', '-DskipTests', 'install'],
  runtimeEnv,
  () => runMavenStep(
    '正在启动 zhyc-auth-server。',
    [
      ...mavenCommand.slice(1),
      '-pl',
      'zhyc-auth-server',
      springBootRunGoal,
      '-Dspring-boot.run.mainClass=com.zhyc.auth.ZhycAuthServerApplication',
      `-Dspring-boot.run.profiles=${springProfile}`,
      `-Dspring-boot.run.arguments=--server.port=${serverPort}`,
    ],
    runtimeEnv,
    () => process.exit(0),
  ),
);

/**
 * 执行一个 Maven 步骤，并在成功后继续下一个步骤。
 *
 * @param message 当前步骤说明
 * @param stepArgs Maven 参数
 * @param runtimeEnv 运行环境变量
 * @param onSuccess 成功回调
 */
function runMavenStep(message, stepArgs, runtimeEnv, onSuccess) {
  console.log(message);
  const child = spawn(mavenCommand[0], stepArgs, {
    cwd: serverRoot,
    env: runtimeEnv,
    stdio: 'inherit',
  });

  child.on('exit', (code, signal) => {
    if (signal) {
      process.kill(process.pid, signal);
      return;
    }
    if (code !== 0) {
      process.exit(code ?? 1);
      return;
    }
    onSuccess();
  });
}

/**
 * 解析认证中心本地启动端口。
 *
 * @param argv 命令行参数
 * @param env 环境变量映射
 * @param outputErrors 错误收集器
 * @returns HTTP 服务端口
 */
function resolveServerPort(argv, env, outputErrors) {
  const portIndex = argv.indexOf('--port');
  const rawPort = portIndex >= 0 && argv[portIndex + 1]
      ? argv[portIndex + 1]
      : env.get('ZHYC_AUTH_PORT') || '8090';
  const port = Number.parseInt(rawPort, 10);
  if (!Number.isInteger(port) || port < 1 || port > 65535) {
    outputErrors.push(`认证中心端口不合法：${rawPort}`);
    return 8090;
  }
  return port;
}

/**
 * 检查认证中心 HTTP 端口是否已被占用。
 *
 * @param port HTTP 服务端口
 */
async function assertPortAvailable(port) {
  const lsofResult = spawnSync('lsof', ['-nP', `-iTCP:${port}`, '-sTCP:LISTEN'], {
    encoding: 'utf8',
  });
  if (lsofResult.status === 0) {
    printPortOccupied(port);
    if (lsofResult.stdout.trim()) {
      console.error(lsofResult.stdout.trim());
    }
    process.exit(1);
  }

  const available = await new Promise((resolveAvailability) => {
    const server = createServer()
        .once('error', () => resolveAvailability(false))
        .once('listening', () => server.close(() => resolveAvailability(true)))
        .listen(port);
  });
  if (!available) {
    printPortOccupied(port);
    process.exit(1);
  }
}

/**
 * 输出端口占用处理提示。
 *
 * @param port HTTP 服务端口
 */
function printPortOccupied(port) {
  console.error(`认证中心端口已被占用：${port}`);
  console.error(`请先执行：rtk lsof -nP -iTCP:${port} -sTCP:LISTEN`);
  console.error('或使用其他端口启动：rtk node scripts/run-auth-local.mjs --port 18090');
}

/**
 * 解析 Spring Boot 运行环境。
 *
 * @param argv 命令行参数
 * @returns Spring Profile 名称
 */
function resolveSpringProfile(argv) {
  const environmentIndex = argv.indexOf('--env');
  if (environmentIndex >= 0 && argv[environmentIndex + 1]) {
    return argv[environmentIndex + 1];
  }
  const profileIndex = argv.indexOf('--profile');
  if (profileIndex >= 0 && argv[profileIndex + 1]) {
    return argv[profileIndex + 1];
  }
  return process.env.ZHYC_ENV || process.env.SPRING_PROFILES_ACTIVE || 'dev';
}

/**
 * 解析 Spring Boot Maven 插件完整运行目标，避免本机仓库无法解析 spring-boot 前缀。
 *
 * @returns Spring Boot Maven 插件完整运行目标
 */
function resolveSpringBootRunGoal() {
  const pomPath = resolve(serverRoot, 'pom.xml');
  const pomContent = readFileSync(pomPath, 'utf8');
  const versionMatch = pomContent.match(/<spring-boot\.version>([^<]+)<\/spring-boot\.version>/);
  const springBootVersion = versionMatch ? versionMatch[1].trim() : '4.1.0';
  return `org.springframework.boot:spring-boot-maven-plugin:${springBootVersion}:run`;
}

/**
 * 解析本机 Maven 命令。
 *
 * @returns Maven 可执行命令
 */
function resolveMavenCommand() {
  return ['mvn'];
}
