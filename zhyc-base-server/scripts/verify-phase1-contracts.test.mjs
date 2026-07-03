/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import assert from 'node:assert/strict';
import { readdirSync, readFileSync } from 'node:fs';
import { basename, resolve } from 'node:path';

const scriptsRoot = resolve(process.cwd(), 'scripts');
const runnerFile = resolve(scriptsRoot, 'verify-phase1-contracts.mjs');
const runnerSource = readFileSync(runnerFile, 'utf8');

const registeredScripts = collectRegisteredVerifyScripts(runnerSource);
const expectedScripts = readdirSync(scriptsRoot)
  .filter((file) => /^verify-.*\.mjs$/.test(file))
  .filter((file) => file !== 'verify-phase1-contracts.mjs')
  .sort();

const missingScripts = expectedScripts.filter((script) => !registeredScripts.has(script));

assert.deepEqual(missingScripts, [],
  `以下首期验证脚本未挂载到 verify-phase1-contracts.mjs：${missingScripts.join(', ')}`);

assert.match(runnerSource, /function resolveMavenCommand/,
  '首期 full 验证必须解析可用 Maven 命令，避免 PATH 中没有 mvn 时误报后端编译失败');
assert.match(runnerSource, /process\.env\.MAVEN_CMD/,
  '首期 full 验证必须支持 MAVEN_CMD 覆盖 Maven 命令');
assert.match(runnerSource, /mvnw/,
  '首期 full 验证必须优先支持项目内 Maven Wrapper');
assert.match(runnerSource, /IntelliJ IDEA\.app/,
  '首期 full 验证必须兼容本机 IntelliJ 内置 Maven');

/**
 * 收集首期总体验证入口中已经注册的验证脚本。
 *
 * <p>该检查只识别 scripts/verify-*.mjs 形式的命令，避免把 npm、mvn 等外部命令误判为验证脚本。</p>
 *
 * @param source 总体验证入口源码
 * @returns 已注册验证脚本文件名集合
 */
function collectRegisteredVerifyScripts(source) {
  const registered = new Set();
  const scriptPattern = /['"]scripts\/(verify-[^'"]+\.mjs)['"]/g;
  for (const match of source.matchAll(scriptPattern)) {
    registered.add(basename(match[1]));
  }
  return registered;
}
