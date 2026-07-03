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

const scriptPath = resolve(process.cwd(), 'scripts/verify-purchase-service-business-errors.mjs');

const failedRoot = mkdtempSync(join(tmpdir(), 'zhyc-purchase-service-errors-fail-'));
writeJava(failedRoot, 'zhyc-module-purchase/src/main/java/com/zhyc/purchase/demo/service/DemoPurchaseService.java', `
package com.zhyc.purchase.demo.service;

import java.util.Objects;

/**
 * 测试采购服务。
 */
public class DemoPurchaseService {

    /**
     * 校验采购服务入参。
     */
    public void validate(String value) {
        Objects.requireNonNull(value, "采购参数不能为空");
        throw new IllegalArgumentException("采购参数错误");
    }
}
`);

const failedResult = spawnSync('node', [scriptPath, failedRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.notEqual(failedResult.status, 0, '采购服务层裸参数异常必须触发门禁失败');
assert.match(failedResult.stderr, /DemoPurchaseService\.java/, '应报告违规服务文件');
assert.match(failedResult.stderr, /BusinessException/, '应提示改用稳定业务异常');

const passedRoot = mkdtempSync(join(tmpdir(), 'zhyc-purchase-service-errors-pass-'));
writeJava(passedRoot, 'zhyc-module-purchase/src/main/java/com/zhyc/purchase/demo/service/DemoPurchaseService.java', `
package com.zhyc.purchase.demo.service;

import com.zhyc.common.exception.BusinessException;
import java.util.Objects;

/**
 * 测试采购服务。
 */
public class DemoPurchaseService {

    /** 测试依赖。 */
    private final Object dependency;

    /**
     * 创建测试服务。
     *
     * @param dependency 测试依赖
     */
    public DemoPurchaseService(Object dependency) {
        this.dependency = Objects.requireNonNull(dependency, "测试依赖不能为空");
    }

    /**
     * 校验采购服务入参。
     */
    public void validate(String value) {
        if (value == null) {
            throw new BusinessException("ZHYC_PURCHASE_ARGUMENT_INVALID", "采购参数不能为空");
        }
    }
}
`);
writeJava(passedRoot, 'zhyc-module-purchase/src/main/java/com/zhyc/purchase/demo/domain/DemoPurchaseStatus.java', `
package com.zhyc.purchase.demo.domain;

/**
 * 测试采购枚举。
 */
public enum DemoPurchaseStatus {
    ENABLED;

    /**
     * 测试枚举解析。
     *
     * @param code 编码
     * @return 枚举
     */
    public static DemoPurchaseStatus fromCode(String code) {
        throw new IllegalArgumentException("枚举编码错误");
    }
}
`);

const passedResult = spawnSync('node', [scriptPath, passedRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.equal(passedResult.status, 0, passedResult.stderr || passedResult.stdout);
assert.match(passedResult.stdout, /采购服务业务异常门禁通过/);

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
