/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { existsSync, readFileSync, readdirSync, statSync } from 'node:fs';
import { join, relative, resolve } from 'node:path';

const serverRoot = resolve(process.argv[2] || process.cwd());
const violations = [];
const allowedAggregators = new Set(['zhyc-platform-app']);
const modulePackagePrefixes = new Map([
  ['zhyc-module-system', 'com.zhyc.system'],
  ['zhyc-module-lowcode', 'com.zhyc.lowcode'],
  ['zhyc-module-openapi', 'com.zhyc.openapi'],
  ['zhyc-module-workflow', 'com.zhyc.workflow'],
  ['zhyc-module-purchase', 'com.zhyc.purchase'],
  ['zhyc-module-message', 'com.zhyc.message'],
  ['zhyc-module-file', 'com.zhyc.file'],
  ['zhyc-module-job', 'com.zhyc.job'],
  ['zhyc-module-cms', 'com.zhyc.cms'],
  ['zhyc-module-visual', 'com.zhyc.visual'],
  ['zhyc-module-i18n', 'com.zhyc.i18n'],
  ['zhyc-module-search', 'com.zhyc.search'],
]);

for (const pomFile of listPomFiles(serverRoot)) {
  const relativePath = relative(serverRoot, pomFile);
  const content = readFileSync(pomFile, 'utf8');
  const artifactId = readProjectArtifactId(content);
  if (!artifactId || !artifactId.startsWith('zhyc-module-')) {
    continue;
  }
  for (const dependencyBlock of readDependencyBlocks(content)) {
    const groupId = readTag(dependencyBlock, 'groupId');
    const dependencyArtifactId = readTag(dependencyBlock, 'artifactId');
    if (groupId !== 'com.zhyc' || !dependencyArtifactId?.startsWith('zhyc-module-')) {
      continue;
    }
    violations.push(`${relativePath} -> ${artifactId} 不得直接依赖业务模块 ${dependencyArtifactId}，请上移到 zhyc-common 公共契约或由 zhyc-platform-app 聚合`);
  }
}

for (const pomFile of listPomFiles(serverRoot)) {
  const relativePath = relative(serverRoot, pomFile);
  const content = readFileSync(pomFile, 'utf8');
  const artifactId = readProjectArtifactId(content);
  if (!artifactId || artifactId.startsWith('zhyc-module-') || allowedAggregators.has(artifactId)) {
    continue;
  }
  for (const dependencyBlock of readDependencyBlocks(content)) {
    const groupId = readTag(dependencyBlock, 'groupId');
    const dependencyArtifactId = readTag(dependencyBlock, 'artifactId');
    if (groupId === 'com.zhyc' && dependencyArtifactId?.startsWith('zhyc-module-')) {
      violations.push(`${relativePath} -> ${artifactId} 不得直接依赖业务模块 ${dependencyArtifactId}，只有 zhyc-platform-app 可聚合业务模块`);
    }
  }
}

if (!existsSync(serverRoot)) {
  violations.push(`后端工程目录不存在：${serverRoot}`);
}

checkJavaImportBoundaries();

if (violations.length > 0) {
  console.error('模块边界门禁失败。业务模块之间禁止直接依赖，跨模块协作必须通过 zhyc-common 公共契约或平台聚合层：');
  for (const violation of violations) {
    console.error(`- ${violation}`);
  }
  process.exit(1);
}

/**
 * 检查业务模块生产源码是否直接 import 其他业务模块包。
 */
function checkJavaImportBoundaries() {
  for (const [moduleName, ownPackagePrefix] of modulePackagePrefixes) {
    const sourceRoot = resolve(serverRoot, moduleName, 'src/main/java');
    if (!existsSync(sourceRoot)) {
      continue;
    }
    for (const javaFile of listJavaFiles(sourceRoot)) {
      const content = readFileSync(javaFile, 'utf8');
      for (const importedPackage of readJavaImports(content)) {
        for (const [dependencyModuleName, dependencyPackagePrefix] of modulePackagePrefixes) {
          if (dependencyModuleName === moduleName) {
            continue;
          }
          if (importedPackage === dependencyPackagePrefix
              || importedPackage.startsWith(`${dependencyPackagePrefix}.`)) {
            violations.push(`${relative(serverRoot, javaFile)} -> ${moduleName} 不得 import 业务模块包 ${importedPackage}，请改用 zhyc-common 公共契约或平台聚合层`);
          }
        }
      }
    }
  }
}

console.log('模块边界门禁通过。');

/**
 * 递归列出 Maven POM 文件。
 *
 * @param dir 当前目录
 * @returns POM 文件路径列表
 */
function listPomFiles(dir) {
  if (!existsSync(dir)) {
    return [];
  }
  return readdirSync(dir, { withFileTypes: true }).flatMap((entry) => {
    const path = join(dir, entry.name);
    if (entry.isDirectory()) {
      if (['target', '.git', 'node_modules', 'dist'].includes(entry.name)) {
        return [];
      }
      return listPomFiles(path);
    }
    return entry.isFile() && entry.name === 'pom.xml' ? [path] : [];
  });
}

/**
 * 递归列出 Java 生产源码文件。
 *
 * @param dir 当前目录
 * @returns Java 文件路径列表
 */
function listJavaFiles(dir) {
  if (!existsSync(dir)) {
    return [];
  }
  const rootStat = statSync(dir);
  if (rootStat.isFile()) {
    return dir.endsWith('.java') ? [dir] : [];
  }
  return readdirSync(dir, { withFileTypes: true }).flatMap((entry) => {
    const path = join(dir, entry.name);
    if (entry.isDirectory()) {
      if (['target', '.git'].includes(entry.name)) {
        return [];
      }
      return listJavaFiles(path);
    }
    return entry.isFile() && entry.name.endsWith('.java') ? [path] : [];
  });
}

/**
 * 读取 Java import 包名。
 *
 * @param content Java 源码内容
 * @returns import 包名列表
 */
function readJavaImports(content) {
  return [...content.matchAll(/^\s*import\s+(?:static\s+)?([a-zA-Z_][\w.]*)(?:\.\*)?;\s*$/gm)]
      .map((match) => match[1]);
}

/**
 * 读取 XML 中指定标签的文本值。
 *
 * @param content XML 内容
 * @param tagName 标签名称
 * @returns 标签文本值，未声明时返回 undefined
 */
function readTag(content, tagName) {
  const escapedTagName = tagName.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  const match = content.match(new RegExp(`<${escapedTagName}>\\s*([^<]+?)\\s*</${escapedTagName}>`));
  return match ? match[1].trim() : undefined;
}

/**
 * 读取当前 POM 自身 artifactId，避免误读 parent 中的 artifactId。
 *
 * @param content POM 内容
 * @returns 当前项目 artifactId，未声明时返回 undefined
 */
function readProjectArtifactId(content) {
  return readTag(content.replace(/<parent>[\s\S]*?<\/parent>/, ''), 'artifactId');
}

/**
 * 读取 POM 中的 dependency 片段。
 *
 * @param content POM 内容
 * @returns dependency XML 片段列表
 */
function readDependencyBlocks(content) {
  return [...content.matchAll(/<dependency>[\s\S]*?<\/dependency>/g)].map((match) => match[0]);
}
