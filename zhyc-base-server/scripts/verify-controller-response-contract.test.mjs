/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import assert from 'node:assert/strict';
import { mkdirSync, mkdtempSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { dirname, join, resolve } from 'node:path';
import { spawnSync } from 'node:child_process';

const scriptPath = resolve(process.cwd(), 'scripts/verify-controller-response-contract.mjs');

const failedRoot = mkdtempSync(join(tmpdir(), 'zhyc-controller-response-fail-'));
writeJava(failedRoot, 'zhyc-module-demo/src/main/java/com/zhyc/demo/controller/DemoController.java', `
package com.zhyc.demo.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试控制器。
 */
@RestController
public class DemoController {

    /**
     * 返回裸字符串。
     *
     * @return 裸字符串
     */
    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    /**
     * 返回裸 Map。
     *
     * @return 裸 Map
     */
    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of();
    }
}
`);
writeJava(failedRoot, 'zhyc-platform-app/src/main/java/com/zhyc/platform/web/DemoExceptionHandler.java', `
package com.zhyc.platform.web;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 测试异常处理器。
 */
@RestControllerAdvice
public class DemoExceptionHandler {

    /**
     * 返回裸异常消息。
     *
     * @param exception 异常对象
     * @return 裸异常消息
     */
    @ExceptionHandler(Exception.class)
    public String handleException(Exception exception) {
        return exception.getMessage();
    }
}
`);

const failedResult = spawnSync('node', [scriptPath, failedRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.notEqual(failedResult.status, 0, 'Controller 裸返回值必须触发门禁失败');
assert.match(failedResult.stderr, /public String ping/, '应报告裸 String 返回值');
assert.match(failedResult.stderr, /public Map<String, Object> status/, '应报告裸 Map 返回值');
assert.match(failedResult.stderr, /public String handleException/, '应报告异常处理器裸 String 返回值');

const passedRoot = mkdtempSync(join(tmpdir(), 'zhyc-controller-response-pass-'));
writeJava(passedRoot, 'zhyc-module-demo/src/main/java/com/zhyc/demo/controller/DemoController.java', `
package com.zhyc.demo.controller;

import com.zhyc.common.api.ApiResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试控制器。
 */
@RestController
public class DemoController {

    /**
     * 返回统一响应。
     *
     * @return 统一响应
     */
    @GetMapping("/ping")
    public ApiResult<String> ping() {
        return ApiResult.ok("pong");
    }
}
`);
writeJava(passedRoot, 'zhyc-platform-app/src/main/java/com/zhyc/platform/web/DemoExceptionHandler.java', `
package com.zhyc.platform.web;

import com.zhyc.common.api.ApiResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 测试异常处理器。
 */
@RestControllerAdvice
public class DemoExceptionHandler {

    /**
     * 返回统一异常响应。
     *
     * @param exception 异常对象
     * @return 统一异常响应
     */
    @ExceptionHandler(Exception.class)
    public ApiResult<Void> handleException(Exception exception) {
        return ApiResult.fail("SYSTEM_ERROR", "系统繁忙，请稍后重试");
    }
}
`);

const passedResult = spawnSync('node', [scriptPath, passedRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.equal(passedResult.status, 0, passedResult.stderr || passedResult.stdout);
assert.match(passedResult.stdout, /Controller 统一响应门禁通过/);

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
