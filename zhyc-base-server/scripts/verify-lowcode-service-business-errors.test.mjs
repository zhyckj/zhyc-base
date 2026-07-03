/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import assert from 'node:assert/strict';
import { spawnSync } from 'node:child_process';
import { mkdirSync, mkdtempSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { dirname, join, resolve } from 'node:path';

const scriptPath = resolve(process.cwd(), 'scripts/verify-lowcode-service-business-errors.mjs');

const failedRoot = mkdtempSync(join(tmpdir(), 'zhyc-lowcode-service-errors-fail-'));
writeJava(failedRoot, 'zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/demo/service/DemoLowcodeService.java', `
package com.zhyc.lowcode.demo.service;

import java.util.Objects;

/**
 * 测试低代码服务。
 */
public class DemoLowcodeService {

    /**
     * 校验低代码服务入参。
     */
    public void validate(String value) {
        Objects.requireNonNull(value, "低代码参数不能为空");
        throw new IllegalArgumentException("低代码参数错误");
    }
}
`);

const failedResult = spawnSync('node', [scriptPath, failedRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.notEqual(failedResult.status, 0, '低代码服务层裸参数异常必须触发门禁失败');
assert.match(failedResult.stderr, /DemoLowcodeService\.java/, '应报告违规服务文件');
assert.match(failedResult.stderr, /BusinessException/, '应提示改用稳定业务异常');

const tableDataSourceFailedRoot = mkdtempSync(join(tmpdir(), 'zhyc-lowcode-table-datasource-fail-'));
writeJava(tableDataSourceFailedRoot,
  'zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/metadata/service/DefaultLowcodeMetadataService.java', `
package com.zhyc.lowcode.metadata.service;

/**
 * 测试低代码元数据服务。
 */
public class DefaultLowcodeMetadataService {

    /**
     * 保存表模型。
     */
    public Object saveTableModel(Object tableModel) {
        validateTableModel(tableModel);
        return tableModelRepository.save(tableModel);
    }
}
`);

const tableDataSourceFailedResult = spawnSync('node', [scriptPath, tableDataSourceFailedRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.notEqual(tableDataSourceFailedResult.status, 0, '表模型保存缺少数据源归属校验时必须触发门禁失败');
assert.match(tableDataSourceFailedResult.stderr, /DefaultLowcodeMetadataService\.java/, '应报告缺少数据源归属校验的服务文件');
assert.match(tableDataSourceFailedResult.stderr, /validateTableModelDataSource/, '应提示保存表模型前必须校验数据源归属');

const passedRoot = mkdtempSync(join(tmpdir(), 'zhyc-lowcode-service-errors-pass-'));
writeJava(passedRoot, 'zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/demo/service/DemoLowcodeService.java', `
package com.zhyc.lowcode.demo.service;

import com.zhyc.common.exception.BusinessException;
import java.util.Objects;

/**
 * 测试低代码服务。
 */
public class DemoLowcodeService {

    /** 测试依赖。 */
    private final Object dependency;

    /**
     * 创建测试服务。
     *
     * @param dependency 测试依赖
     */
    public DemoLowcodeService(Object dependency) {
        this.dependency = Objects.requireNonNull(dependency, "测试依赖不能为空");
    }

    /**
     * 校验低代码服务入参。
     */
    public void validate(String value) {
        if (value == null) {
            throw new BusinessException("ZHYC_LOWCODE_ARGUMENT_INVALID", "低代码参数不能为空");
        }
    }
}
`);
writeJava(passedRoot,
  'zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/metadata/service/DefaultLowcodeMetadataService.java', `
package com.zhyc.lowcode.metadata.service;

/**
 * 测试低代码元数据服务。
 */
public class DefaultLowcodeMetadataService {

    /**
     * 保存表模型。
     */
    public Object saveTableModel(Object tableModel) {
        validateTableModel(tableModel);
        validateTableModelDataSource(tableModel);
        return tableModelRepository.save(tableModel);
    }

    /**
     * 校验表模型绑定数据源。
     */
    private void validateTableModelDataSource(Object tableModel) {
        Long dataSourceId = tableModel.getDataSourceId();
        dataSourceRepository.findByTenantIdAndId(tableModel.getTenantId(), dataSourceId);
        throw new com.zhyc.common.exception.BusinessException("ZHYC_LOWCODE_METADATA_TABLE_DATASOURCE_NOT_FOUND",
            "表模型绑定的数据源不属于当前租户: 20");
    }

    /**
     * 校验表模型主键归属。
     */
    private Object findTableByTenantAndId(String tenantId, Long tableId) {
        return tableModelRepository.findByTenantIdAndId(tenantId, tableId);
    }
}
`);
writeJava(passedRoot, 'zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/demo/domain/DemoLowcodeStatus.java', `
package com.zhyc.lowcode.demo.domain;

/**
 * 测试低代码枚举。
 */
public enum DemoLowcodeStatus {
    ENABLED;

    /**
     * 测试枚举解析。
     *
     * @param code 编码
     * @return 枚举
     */
    public static DemoLowcodeStatus fromCode(String code) {
        throw new IllegalArgumentException("枚举编码错误");
    }
}
`);

const passedResult = spawnSync('node', [scriptPath, passedRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.equal(passedResult.status, 0, passedResult.stderr || passedResult.stdout);
assert.match(passedResult.stdout, /低代码服务业务异常门禁通过/);

/**
 * 写入测试用 Java 源码。
 *
 * @param root 测试工程根目录
 * @param file Java 源码相对路径
 * @param content Java 源码内容
 */
function writeJava(root, file, content) {
  const absolutePath = join(root, file);
  mkdirSync(dirname(absolutePath), { recursive: true });
  writeFileSync(absolutePath, content.trim());
}
