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

const scriptPath = resolve(process.cwd(), 'scripts/verify-system-service-business-errors.mjs');

const failedRoot = mkdtempSync(join(tmpdir(), 'zhyc-system-service-errors-fail-'));
writeJava(failedRoot, 'zhyc-module-system/src/main/java/com/zhyc/system/demo/service/DemoSystemService.java', `
package com.zhyc.system.demo.service;

/**
 * 测试系统服务。
 */
public class DemoSystemService {

    /**
     * 校验系统服务入参。
     */
    public void validate() {
        throw new IllegalArgumentException("系统服务参数错误");
    }
}
`);

const failedResult = spawnSync('node', [scriptPath, failedRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.notEqual(failedResult.status, 0, '系统服务层裸参数异常必须触发门禁失败');
assert.match(failedResult.stderr, /DemoSystemService\.java/, '应报告违规服务文件');
assert.match(failedResult.stderr, /BusinessException/, '应提示改用稳定业务异常');

const bindingFailedRoot = mkdtempSync(join(tmpdir(), 'zhyc-system-service-binding-fail-'));
writeJava(bindingFailedRoot,
  'zhyc-module-system/src/main/java/com/zhyc/system/user/service/DefaultSysUserRoleService.java', `
package com.zhyc.system.user.service;

/**
 * 测试用户角色绑定服务。
 */
public class DefaultSysUserRoleService {

    /**
     * 测试绑定用户角色。
     */
    public void bindUserRoles() {
        userRoleRepository.replaceUserRoles("tenant_a", 1L, java.util.List.of());
    }
}
`);

const bindingFailedResult = spawnSync('node', [scriptPath, bindingFailedRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.notEqual(bindingFailedResult.status, 0, '用户角色绑定缺少租户归属校验时必须触发门禁失败');
assert.match(bindingFailedResult.stderr, /DefaultSysUserRoleService\.java/, '应报告缺少归属校验的服务文件');
assert.match(bindingFailedResult.stderr, /validateTenantRoles/, '应提示用户角色绑定前必须校验租户角色归属');

const roleMenuFailedRoot = mkdtempSync(join(tmpdir(), 'zhyc-system-service-role-menu-fail-'));
writeJava(roleMenuFailedRoot,
  'zhyc-module-system/src/main/java/com/zhyc/system/role/service/DefaultSysRoleService.java', `
package com.zhyc.system.role.service;

/**
 * 测试角色菜单绑定服务。
 */
public class DefaultSysRoleService {

    /**
     * 测试绑定角色菜单。
     */
    public void bindRoleMenus() {
        roleRepository.replaceRoleMenus("tenant_a", 1L, java.util.List.of());
    }
}
`);

const roleMenuFailedResult = spawnSync('node', [scriptPath, roleMenuFailedRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.notEqual(roleMenuFailedResult.status, 0, '角色菜单绑定缺少租户归属校验时必须触发门禁失败');
assert.match(roleMenuFailedResult.stderr, /DefaultSysRoleService\.java/, '应报告缺少角色菜单归属校验的服务文件');
assert.match(roleMenuFailedResult.stderr, /validateTenantMenus/, '应提示角色菜单绑定前必须校验租户菜单归属');

const roleDataScopeFailedRoot = mkdtempSync(join(tmpdir(), 'zhyc-system-service-role-data-scope-fail-'));
writeJava(roleDataScopeFailedRoot,
  'zhyc-module-system/src/main/java/com/zhyc/system/role/service/DefaultSysRoleDataScopeService.java', `
package com.zhyc.system.role.service;

/**
 * 测试角色数据权限绑定服务。
 */
public class DefaultSysRoleDataScopeService {

    /**
     * 测试绑定角色数据权限。
     */
    public void bindRoleDataScopes() {
        roleDataScopeRepository.replaceRoleDataScopes("tenant_a", 1L, java.util.List.of());
    }
}
`);

const roleDataScopeFailedResult = spawnSync('node', [scriptPath, roleDataScopeFailedRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.notEqual(roleDataScopeFailedResult.status, 0, '角色数据权限绑定缺少租户归属校验时必须触发门禁失败');
assert.match(roleDataScopeFailedResult.stderr, /DefaultSysRoleDataScopeService\.java/, '应报告缺少角色数据权限归属校验的服务文件');
assert.match(roleDataScopeFailedResult.stderr, /validateTenantOrgs/, '应提示角色数据权限绑定前必须校验租户组织归属');

const adminScopeFailedRoot = mkdtempSync(join(tmpdir(), 'zhyc-system-service-admin-scope-fail-'));
writeJava(adminScopeFailedRoot,
  'zhyc-module-system/src/main/java/com/zhyc/system/adminscope/service/DefaultSysAdminScopeService.java', `
package com.zhyc.system.adminscope.service;

/**
 * 测试管理员范围绑定服务。
 */
public class DefaultSysAdminScopeService {

    /**
     * 测试绑定管理员范围。
     */
    public void bindAdminScopes() {
        adminScopeRepository.replaceAdminScopes("tenant_a", 1L, java.util.List.of());
    }
}
`);

const adminScopeFailedResult = spawnSync('node', [scriptPath, adminScopeFailedRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.notEqual(adminScopeFailedResult.status, 0, '管理员范围绑定缺少租户归属校验时必须触发门禁失败');
assert.match(adminScopeFailedResult.stderr, /DefaultSysAdminScopeService\.java/, '应报告缺少管理员范围归属校验的服务文件');
assert.match(adminScopeFailedResult.stderr, /validateScopeRefs/, '应提示管理员范围绑定前必须校验范围引用');

const passedRoot = mkdtempSync(join(tmpdir(), 'zhyc-system-service-errors-pass-'));
writeJava(passedRoot, 'zhyc-module-system/src/main/java/com/zhyc/system/demo/service/DemoSystemService.java', `
package com.zhyc.system.demo.service;

import com.zhyc.common.exception.BusinessException;

/**
 * 测试系统服务。
 */
public class DemoSystemService {

    /**
     * 校验系统服务入参。
     */
    public void validate() {
        throw new BusinessException("ZHYC_SYSTEM_ARGUMENT_INVALID", "系统服务参数错误");
    }
}
`);
writeJava(passedRoot,
  'zhyc-module-system/src/main/java/com/zhyc/system/role/service/DefaultSysRoleService.java', `
package com.zhyc.system.role.service;

/**
 * 测试角色菜单绑定服务。
 */
public class DefaultSysRoleService {

    /**
     * 测试绑定角色菜单。
     */
    public void bindRoleMenus() {
        validateTenantRole(requiredTenantId, requiredRoleId);
        validateTenantMenus(requiredTenantId, menuIds);
        roleRepository.replaceRoleMenus("tenant_a", 1L, java.util.List.of());
    }

    /**
     * 校验租户角色归属。
     */
    private void validateTenantRole(String tenantId, Long roleId) {
        roleRepository.findByTenantId(tenantId);
        throw com.zhyc.system.support.SystemServiceValidation.businessFailure("角色主键不属于当前租户：999");
    }

    /**
     * 校验租户菜单归属。
     */
    private void validateTenantMenus(String tenantId, java.util.Set<Long> menuIds) {
        menuRepository.findEnabledByTenantId(tenantId);
        throw com.zhyc.system.support.SystemServiceValidation.businessFailure("菜单主键不属于当前租户：999");
    }
}
`);
writeJava(passedRoot,
  'zhyc-module-system/src/main/java/com/zhyc/system/role/service/DefaultSysRoleDataScopeService.java', `
package com.zhyc.system.role.service;

/**
 * 测试角色数据权限绑定服务。
 */
public class DefaultSysRoleDataScopeService {

    /**
     * 测试绑定角色数据权限。
     */
    public void bindRoleDataScopes() {
        validateTenantRole(requiredTenantId, requiredRoleId);
        validateTenantOrgs(requiredTenantId, orgIds);
        roleDataScopeRepository.replaceRoleDataScopes("tenant_a", 1L, java.util.List.of());
    }

    /**
     * 校验租户角色归属。
     */
    private void validateTenantRole(String tenantId, Long roleId) {
        roleRepository.findByTenantId(tenantId);
        throw com.zhyc.system.support.SystemServiceValidation.businessFailure("角色主键不属于当前租户：999");
    }

    /**
     * 校验租户组织归属。
     */
    private void validateTenantOrgs(String tenantId, java.util.Set<Long> orgIds) {
        orgRepository.findByTenantId(tenantId);
        throw com.zhyc.system.support.SystemServiceValidation.businessFailure("组织主键不属于当前租户：999");
    }
}
`);
writeJava(passedRoot,
  'zhyc-module-system/src/main/java/com/zhyc/system/adminscope/service/DefaultSysAdminScopeService.java', `
package com.zhyc.system.adminscope.service;

/**
 * 测试管理员范围绑定服务。
 */
public class DefaultSysAdminScopeService {

    /**
     * 测试绑定管理员范围。
     */
    public void bindAdminScopes() {
        validateTenantAdminUser(requiredTenantId, requiredUserId);
        validateScopeRefs(requiredTenantId, scopes.values());
        adminScopeRepository.replaceAdminScopes("tenant_a", 1L, java.util.List.of());
    }

    /**
     * 校验管理员用户归属。
     */
    private void validateTenantAdminUser(String tenantId, Long userId) {
        userRepository.findByTenantId(tenantId);
        throw new com.zhyc.common.exception.BusinessException("ZHYC_SYS_ADMIN_SCOPE_REF_INVALID", "管理员用户主键不属于当前租户：999");
    }

    /**
     * 校验范围引用。
     */
    private void validateScopeRefs(String tenantId, Iterable<Object> scopes) {
        validateTenantScope(tenantId, "tenant_b");
        validateOrgScope("999", java.util.Set.of(10L));
        validateModuleScope("ghost", java.util.Set.of("system"));
    }

    /**
     * 校验租户范围。
     */
    private void validateTenantScope(String tenantId, String scopeRefCode) {
        throw new com.zhyc.common.exception.BusinessException("ZHYC_SYS_ADMIN_SCOPE_REF_INVALID", "租户范围必须等于当前租户：tenant_b");
    }

    /**
     * 校验组织范围。
     */
    private void validateOrgScope(String scopeRefCode, java.util.Set<Long> tenantOrgIds) {
        orgRepository.findByTenantId(tenantId);
        throw new com.zhyc.common.exception.BusinessException("ZHYC_SYS_ADMIN_SCOPE_REF_INVALID", "组织范围不属于当前租户：999");
    }

    /**
     * 校验模块范围。
     */
    private void validateModuleScope(String scopeRefCode, java.util.Set<String> enabledModuleCodes) {
        moduleRepository.findAll();
        throw new com.zhyc.common.exception.BusinessException("ZHYC_SYS_ADMIN_SCOPE_REF_INVALID", "模块范围不存在或未启用：ghost");
    }
}
`);
writeJava(passedRoot,
  'zhyc-module-system/src/main/java/com/zhyc/system/user/service/DefaultSysUserRoleService.java', `
package com.zhyc.system.user.service;

/**
 * 测试用户角色绑定服务。
 */
public class DefaultSysUserRoleService {

    /**
     * 测试绑定用户角色。
     */
    public void bindUserRoles() {
        validateTenantRoles(requiredTenantId, bindings.keySet());
        userRoleRepository.replaceUserRoles("tenant_a", 1L, java.util.List.of());
    }

    /**
     * 校验租户角色归属。
     */
    private void validateTenantRoles(String tenantId, java.util.Set<Long> roleIds) {
        roleRepository.findByTenantId(tenantId);
        throw com.zhyc.system.support.SystemServiceValidation.businessFailure("角色主键不属于当前租户：999");
    }
}
`);
writeJava(passedRoot,
  'zhyc-module-system/src/main/java/com/zhyc/system/user/service/DefaultSysUserPostService.java', `
package com.zhyc.system.user.service;

/**
 * 测试用户岗位绑定服务。
 */
public class DefaultSysUserPostService {

    /**
     * 测试绑定用户岗位。
     */
    public void bindUserPosts() {
        validateTenantPosts(requiredTenantId, bindings.keySet());
        userPostRepository.replaceUserPosts("tenant_a", 1L, java.util.List.of());
    }

    /**
     * 校验租户岗位归属。
     */
    private void validateTenantPosts(String tenantId, java.util.Set<Long> postIds) {
        postRepository.findByTenantIdAndOrgId(tenantId, null);
        throw com.zhyc.system.support.SystemServiceValidation.businessFailure("岗位主键不属于当前租户：999");
    }
}
`);
writeJava(passedRoot, 'zhyc-module-system/src/main/java/com/zhyc/system/demo/domain/DemoSystemStatus.java', `
package com.zhyc.system.demo.domain;

/**
 * 测试系统枚举。
 */
public enum DemoSystemStatus {
    ENABLED;

    /**
     * 测试枚举解析。
     *
     * @param code 编码
     * @return 枚举
     */
    public static DemoSystemStatus fromCode(String code) {
        throw new IllegalArgumentException("枚举编码错误");
    }
}
`);

const passedResult = spawnSync('node', [scriptPath, passedRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.equal(passedResult.status, 0, passedResult.stderr || passedResult.stdout);
assert.match(passedResult.stdout, /系统服务业务异常门禁通过/);

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
