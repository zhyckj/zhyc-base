/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { existsSync, readFileSync, readdirSync } from 'node:fs';
import { join, relative, resolve } from 'node:path';

const serverRoot = resolve(process.argv[2] || process.cwd());
const rootPom = resolve(serverRoot, 'pom.xml');
const requiredRootProperties = [
  ['java.version', '21'],
  ['maven.compiler.release', '${java.version}'],
  ['spring-boot.version', '4.1.0'],
  ['mybatis-spring-boot.version', '4.0.0'],
  ['shiro.version', '2.2.1'],
];
const managedVersionProperties = new Map([
  ['org.springframework.boot', '${spring-boot.version}'],
  ['org.apache.shiro', '${shiro.version}'],
  ['org.mybatis.spring.boot', '${mybatis-spring-boot.version}'],
]);
const baselinePropertyNames = new Set(requiredRootProperties.map(([name]) => name));
const requiredDependencySnippets = [
  ['pom.xml', '<artifactId>spring-boot-starter-validation</artifactId>',
    '父工程必须统一管理 Spring Boot Validation starter，支撑 Controller 入参校验'],
  ['zhyc-common/pom.xml', '<artifactId>jakarta.validation-api</artifactId>',
    'zhyc-common 必须暴露 Jakarta Validation API，支撑业务模块编译生成 DTO 校验注解'],
  ['zhyc-platform-app/pom.xml', '<artifactId>spring-boot-starter-validation</artifactId>',
    'zhyc-platform-app 必须引入 Spring Boot Validation starter，支撑运行时参数校验'],
];
const violations = [];

if (!existsSync(rootPom)) {
  violations.push('缺少后端父工程 pom.xml');
} else {
  const rootContent = readFileSync(rootPom, 'utf8');
  for (const [propertyName, expectedValue] of requiredRootProperties) {
    const actualValue = readProperty(rootContent, propertyName);
    if (actualValue !== expectedValue) {
      violations.push(`pom.xml -> ${propertyName} 必须为 ${expectedValue}，当前为 ${actualValue || '未声明'}`);
    }
  }
}

for (const [relativePath, snippet, message] of requiredDependencySnippets) {
  checkRequiredDependencySnippet(relativePath, snippet, message);
}

for (const pomFile of listPomFiles(serverRoot)) {
  const relativePath = relative(serverRoot, pomFile);
  const content = readFileSync(pomFile, 'utf8');
  if (relativePath !== 'pom.xml') {
    checkChildBaselineOverrides(relativePath, content);
  }
  checkManagedDependencyVersions(relativePath, content);
}

if (violations.length > 0) {
  console.error('技术基线门禁失败。Spring Boot、Shiro、MyBatis 和 Java 版本必须由父工程统一锁定：');
  for (const violation of violations) {
    console.error(`- ${violation}`);
  }
  process.exit(1);
}

console.log('技术基线门禁通过。');

/**
 * 递归列出 Maven POM 文件。
 *
 * @param dir 当前目录
 * @returns POM 文件路径列表
 */
function listPomFiles(dir) {
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
 * 检查子 POM 是否覆盖技术基线属性。
 *
 * @param relativePath POM 相对路径
 * @param content POM 内容
 */
function checkChildBaselineOverrides(relativePath, content) {
  for (const propertyName of baselinePropertyNames) {
    const value = readProperty(content, propertyName);
    if (value !== undefined) {
      violations.push(`${relativePath} -> 子模块不得覆盖技术基线属性 ${propertyName}`);
    }
  }
}

/**
 * 检查受控依赖是否使用统一版本属性。
 *
 * @param relativePath POM 相对路径
 * @param content POM 内容
 */
function checkManagedDependencyVersions(relativePath, content) {
  for (const dependencyBlock of readDependencyBlocks(content)) {
    const groupId = readTag(dependencyBlock, 'groupId');
    const artifactId = readTag(dependencyBlock, 'artifactId');
    const version = readTag(dependencyBlock, 'version');
    const expectedVersion = managedVersionProperties.get(groupId);
    if (!expectedVersion || !version) {
      continue;
    }
    if (version !== expectedVersion) {
      violations.push(`${relativePath} -> ${groupId}:${artifactId} 必须使用统一版本 ${expectedVersion}，当前为 ${version}`);
    }
  }
}

/**
 * 检查关键依赖片段是否存在。
 *
 * @param relativePath POM 相对路径
 * @param snippet 必须存在的 XML 片段
 * @param message 缺失时的说明
 */
function checkRequiredDependencySnippet(relativePath, snippet, message) {
  const pomFile = resolve(serverRoot, relativePath);
  if (!existsSync(pomFile)) {
    violations.push(`${relativePath} -> 缺少 POM 文件，无法验证依赖基线`);
    return;
  }
  const content = readFileSync(pomFile, 'utf8');
  if (!content.includes(snippet)) {
    violations.push(`${relativePath} -> 缺少 ${snippet}，${message}`);
  }
}

/**
 * 读取 Maven properties 中的单个属性。
 *
 * @param content POM 内容
 * @param propertyName 属性名称
 * @returns 属性值，未声明时返回 undefined
 */
function readProperty(content, propertyName) {
  return readTag(content, propertyName);
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
 * 读取 POM 中的 dependency 片段。
 *
 * @param content POM 内容
 * @returns dependency XML 片段列表
 */
function readDependencyBlocks(content) {
  return [...content.matchAll(/<dependency>[\s\S]*?<\/dependency>/g)].map((match) => match[0]);
}
