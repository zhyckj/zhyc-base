/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import assert from 'node:assert/strict';
import { mkdirSync, mkdtempSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join, resolve } from 'node:path';
import { spawnSync } from 'node:child_process';

const scriptPath = resolve(process.cwd(), 'scripts/verify-build-baseline.mjs');

const failedRoot = mkdtempSync(join(tmpdir(), 'zhyc-build-baseline-fail-'));
writePom(failedRoot, 'pom.xml', `
<project>
  <properties>
    <java.version>17</java.version>
    <maven.compiler.release>17</maven.compiler.release>
    <spring-boot.version>3.5.0</spring-boot.version>
    <mybatis-spring-boot.version>3.0.0</mybatis-spring-boot.version>
    <shiro.version>2.1.0</shiro.version>
  </properties>
</project>
`);
writePom(failedRoot, 'zhyc-module-demo/pom.xml', `
<project>
  <dependencies>
    <dependency>
      <groupId>org.apache.shiro</groupId>
      <artifactId>shiro-core</artifactId>
      <version>2.0.0</version>
    </dependency>
  </dependencies>
</project>
`);

const failedResult = spawnSync('node', [scriptPath, failedRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.notEqual(failedResult.status, 0, '技术基线漂移必须触发门禁失败');
assert.match(failedResult.stderr, /java\.version/, '应报告 Java 版本漂移');
assert.match(failedResult.stderr, /spring-boot\.version/, '应报告 Spring Boot 版本漂移');
assert.match(failedResult.stderr, /shiro-core/, '应报告子模块 Shiro 版本漂移');

const missingValidationRoot = mkdtempSync(join(tmpdir(), 'zhyc-build-baseline-validation-fail-'));
writePom(missingValidationRoot, 'pom.xml', `
<project>
  <properties>
    <java.version>21</java.version>
    <maven.compiler.release>\${java.version}</maven.compiler.release>
    <spring-boot.version>4.1.0</spring-boot.version>
    <mybatis-spring-boot.version>4.0.0</mybatis-spring-boot.version>
    <shiro.version>2.2.1</shiro.version>
  </properties>
</project>
`);
writePom(missingValidationRoot, 'zhyc-common/pom.xml', `
<project>
  <dependencies>
  </dependencies>
</project>
`);
writePom(missingValidationRoot, 'zhyc-platform-app/pom.xml', `
<project>
  <dependencies>
  </dependencies>
</project>
`);

const missingValidationResult = spawnSync('node', [scriptPath, missingValidationRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.notEqual(missingValidationResult.status, 0, '缺少 Jakarta Validation 依赖必须触发门禁失败');
assert.match(missingValidationResult.stderr, /spring-boot-starter-validation/, '应报告缺少 Validation starter');
assert.match(missingValidationResult.stderr, /jakarta\.validation-api/, '应报告缺少 Jakarta Validation API');

const passedRoot = mkdtempSync(join(tmpdir(), 'zhyc-build-baseline-pass-'));
writePom(passedRoot, 'pom.xml', `
<project>
  <properties>
    <java.version>21</java.version>
    <maven.compiler.release>\${java.version}</maven.compiler.release>
    <spring-boot.version>4.1.0</spring-boot.version>
    <mybatis-spring-boot.version>4.0.0</mybatis-spring-boot.version>
    <shiro.version>2.2.1</shiro.version>
  </properties>
  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
        <version>\${spring-boot.version}</version>
      </dependency>
    </dependencies>
  </dependencyManagement>
</project>
`);
writePom(passedRoot, 'zhyc-common/pom.xml', `
<project>
  <dependencies>
    <dependency>
      <groupId>jakarta.validation</groupId>
      <artifactId>jakarta.validation-api</artifactId>
    </dependency>
  </dependencies>
</project>
`);
writePom(passedRoot, 'zhyc-platform-app/pom.xml', `
<project>
  <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
  </dependencies>
</project>
`);
writePom(passedRoot, 'zhyc-module-demo/pom.xml', `
<project>
  <dependencies>
    <dependency>
      <groupId>org.apache.shiro</groupId>
      <artifactId>shiro-core</artifactId>
      <version>\${shiro.version}</version>
    </dependency>
  </dependencies>
</project>
`);

const passedResult = spawnSync('node', [scriptPath, passedRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.equal(passedResult.status, 0, passedResult.stderr || passedResult.stdout);
assert.match(passedResult.stdout, /技术基线门禁通过/);

/**
 * 写入测试用 Maven POM。
 *
 * @param root 测试工作区根目录
 * @param file POM 相对路径
 * @param content POM 内容
 */
function writePom(root, file, content) {
  const absolutePath = join(root, file);
  mkdirSync(resolve(absolutePath, '..'), { recursive: true });
  writeFileSync(absolutePath, content.trim());
}
