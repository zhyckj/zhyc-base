/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { existsSync, readFileSync, readdirSync, statSync } from 'node:fs';
import { join, relative, resolve } from 'node:path';

const root = resolve(process.argv[2] || process.cwd());
const scanRoot = resolveLowcodeSourceRoot(root);
const violations = [];

for (const file of listLowcodeServiceFiles(scanRoot)) {
  const lines = readFileSync(file, 'utf8').split(/\r?\n/);
  lines.forEach((line, index) => {
    if (isAllowedLine(line)) {
      return;
    }
    const trimmed = line.trim();
    if (trimmed.includes('throw new IllegalArgumentException(')) {
      violations.push(`${relative(root, file)}:${index + 1} -> 低代码服务层不得直接抛裸参数异常: ${trimmed}`);
    }
    if (trimmed.includes('Objects.requireNonNull(') && !trimmed.startsWith('this.')) {
      violations.push(`${relative(root, file)}:${index + 1} -> 低代码服务公开参数不得使用 Objects.requireNonNull 暴露 NPE: ${trimmed}`);
    }
  });
}
verifyRequiredLowcodeServiceProtections(root);

if (violations.length > 0) {
  console.error('低代码服务业务异常门禁失败。服务层面向调用方的参数或业务错误必须使用带稳定错误码的 BusinessException：');
  for (const violation of violations) {
    console.error(`- ${violation}`);
  }
  process.exit(1);
}

console.log('低代码服务业务异常门禁通过。');

/**
 * 解析低代码模块生产源码根目录。
 *
 * @param startRoot 命令执行根目录或测试工程根目录
 * @returns 低代码模块生产源码根目录
 */
function resolveLowcodeSourceRoot(startRoot) {
  const candidates = [
    resolve(startRoot, 'zhyc-module-lowcode/src/main/java/com/zhyc/lowcode'),
    resolve(startRoot, 'zhyc-base-server/zhyc-module-lowcode/src/main/java/com/zhyc/lowcode'),
  ];
  const matchedRoot = candidates.find((candidate) => existsSync(candidate));
  if (matchedRoot) {
    return matchedRoot;
  }
  console.error(`低代码服务业务异常门禁失败。未找到低代码模块生产源码目录：${candidates.join(' 或 ')}`);
  process.exit(1);
}

/**
 * 递归列出低代码生产服务源码文件。
 *
 * @param dir 当前扫描目录
 * @returns 服务源码文件路径
 */
function listLowcodeServiceFiles(dir) {
  const rootStat = statSync(dir);
  if (rootStat.isFile()) {
    return isLowcodeServiceFile(dir) ? [dir] : [];
  }
  return readdirSync(dir, { withFileTypes: true }).flatMap((entry) => {
    const path = join(dir, entry.name);
    if (entry.isDirectory()) {
      if (['target', 'node_modules', '.git'].includes(entry.name)) {
        return [];
      }
      if (path.includes('/src/test/') || path.endsWith('/test')) {
        return [];
      }
      return listLowcodeServiceFiles(path);
    }
    return entry.isFile() && isLowcodeServiceFile(path) ? [path] : [];
  });
}

/**
 * 判断文件是否为低代码服务源码文件。
 *
 * @param file 文件路径
 * @returns 属于扫描范围时返回 true
 */
function isLowcodeServiceFile(file) {
  const normalizedPath = file.split('\\').join('/');
  return normalizedPath.endsWith('Service.java')
    && normalizedPath.includes('/src/main/java/com/zhyc/lowcode/')
    && !normalizedPath.includes('/repository/')
    && !normalizedPath.includes('/src/test/')
    && !normalizedPath.includes('/target/');
}

/**
 * 判断源码行是否属于允许跳过的注释内容。
 *
 * @param line 源码行
 * @returns 允许跳过时返回 true
 */
function isAllowedLine(line) {
  const trimmed = line.trim();
  return trimmed.startsWith('//') || trimmed.startsWith('*') || trimmed.startsWith('/*');
}

/**
 * 校验低代码关键服务保存前置保护。
 *
 * @param startRoot 命令执行根目录或测试工程根目录
 */
function verifyRequiredLowcodeServiceProtections(startRoot) {
  const requirements = [
    {
      file: 'zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/metadata/service/DefaultLowcodeMetadataService.java',
      snippets: [
        'validateTableModelDataSource(tableModel);',
        'dataSourceRepository.findByTenantIdAndId(tableModel.getTenantId(), dataSourceId)',
        'ZHYC_LOWCODE_METADATA_TABLE_DATASOURCE_NOT_FOUND',
        '表模型绑定的数据源不属于当前租户',
        'tableModelRepository.findByTenantIdAndId(tenantId, tableId)',
      ],
      description: '低代码元数据服务必须使用租户精确查询校验数据源和表模型主键归属，避免跨租户绑定或全量扫描',
    },
  ];

  for (const requirement of requirements) {
    const file = resolveRequiredFile(startRoot, requirement.file);
    if (!file) {
      continue;
    }
    const content = readFileSync(file, 'utf8');
    for (const snippet of requirement.snippets) {
      if (!content.includes(snippet)) {
        violations.push(`${relative(startRoot, file)} -> ${requirement.description}，缺少关键片段: ${snippet}`);
      }
    }
  }
}

/**
 * 解析真实工程或测试工程中的指定源码文件。
 *
 * @param startRoot 命令执行根目录或测试工程根目录
 * @param relativeFile 模块内源码相对路径
 * @returns 匹配到的源码文件路径，不存在时返回空
 */
function resolveRequiredFile(startRoot, relativeFile) {
  const candidates = [
    resolve(startRoot, relativeFile),
    resolve(startRoot, 'zhyc-base-server', relativeFile),
  ];
  return candidates.find((candidate) => existsSync(candidate));
}
