/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { existsSync, readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { resolve } from 'node:path';

const serverRoot = resolve(fileURLToPath(new URL('..', import.meta.url)));

const requiredSnippets = [
  ['zhyc-module-system/src/main/java/com/zhyc/system/tenant/controller/SysTenantController.java', 'ZHYC_SYSTEM_TENANT_CREATE_REQUEST_REQUIRED'],
  ['zhyc-module-system/src/main/java/com/zhyc/system/tenant/controller/SysTenantController.java', 'ZHYC_SYSTEM_TENANT_STATUS_REQUEST_REQUIRED'],
  ['zhyc-module-system/src/main/java/com/zhyc/system/tenantpackage/controller/SysTenantPackageController.java', 'ZHYC_SYSTEM_TENANT_PACKAGE_STATUS_REQUEST_REQUIRED'],
  ['zhyc-module-system/src/main/java/com/zhyc/system/tenantpackagemodule/controller/SysTenantPackageModuleController.java', 'ZHYC_SYSTEM_TENANT_PACKAGE_MODULE_BIND_REQUEST_REQUIRED'],
  ['zhyc-module-system/src/main/java/com/zhyc/system/tenantparam/controller/SysTenantParamController.java', 'ZHYC_SYSTEM_TENANT_PARAM_SAVE_REQUEST_REQUIRED'],
  ['zhyc-module-system/src/main/java/com/zhyc/system/accessrestriction/controller/SysAccessRestrictionController.java', 'ZHYC_SYSTEM_ACCESS_RESTRICTION_EVALUATE_REQUEST_REQUIRED'],
  ['zhyc-module-system/src/main/java/com/zhyc/system/accessrestriction/controller/SysAccessRestrictionController.java', 'ZHYC_SYSTEM_ACCESS_RESTRICTION_SAVE_REQUEST_REQUIRED'],
  ['zhyc-module-system/src/main/java/com/zhyc/system/adminscope/controller/SysAdminScopeController.java', 'ZHYC_SYSTEM_ADMIN_SCOPE_BIND_REQUEST_REQUIRED'],
  ['zhyc-module-system/src/main/java/com/zhyc/system/coderule/controller/SysCodeRuleController.java', 'ZHYC_SYSTEM_CODE_RULE_SAVE_REQUEST_REQUIRED'],
  ['zhyc-module-system/src/main/java/com/zhyc/system/coderule/controller/SysCodeRuleController.java', 'ZHYC_SYSTEM_CODE_RULE_GENERATE_REQUEST_REQUIRED'],
  ['zhyc-module-system/src/main/java/com/zhyc/system/module/controller/SysModuleController.java', 'ZHYC_SYSTEM_MODULE_ENABLED_REQUEST_REQUIRED'],
  ['zhyc-module-system/src/main/java/com/zhyc/system/param/controller/SysParamController.java', 'ZHYC_SYSTEM_PARAM_SAVE_REQUEST_REQUIRED'],
  ['zhyc-module-system/src/main/java/com/zhyc/system/passwordpolicy/controller/SysPasswordPolicyController.java', 'ZHYC_SYSTEM_PASSWORD_POLICY_SAVE_REQUEST_REQUIRED'],
  ['zhyc-module-system/src/main/java/com/zhyc/system/passwordpolicy/controller/SysPasswordPolicyController.java', 'ZHYC_SYSTEM_PASSWORD_POLICY_VALIDATE_REQUEST_REQUIRED'],
  ['zhyc-module-system/src/main/java/com/zhyc/system/passwordpolicy/controller/SysPasswordPolicyController.java', 'ZHYC_SYSTEM_PASSWORD_POLICY_HISTORY_VALIDATE_REQUEST_REQUIRED'],
  ['zhyc-module-system/src/main/java/com/zhyc/system/role/controller/SysRoleController.java', 'ZHYC_SYSTEM_ROLE_MENU_BIND_REQUEST_REQUIRED'],
  ['zhyc-module-system/src/main/java/com/zhyc/system/role/controller/SysRoleDataScopeController.java', 'ZHYC_SYSTEM_ROLE_DATA_SCOPE_BIND_REQUEST_REQUIRED'],
  ['zhyc-module-system/src/main/java/com/zhyc/system/user/controller/SysUserPostController.java', 'ZHYC_SYSTEM_USER_POST_BIND_REQUEST_REQUIRED'],
  ['zhyc-module-system/src/main/java/com/zhyc/system/user/controller/SysUserRoleController.java', 'ZHYC_SYSTEM_USER_ROLE_BIND_REQUEST_REQUIRED'],
];

const forbiddenSnippets = [
  ['zhyc-module-system/src/main/java/com/zhyc/system/accessrestriction/controller/SysAccessRestrictionController.java', 'throw new IllegalArgumentException("系统访问限制判定请求不能为空")'],
  ['zhyc-module-system/src/main/java/com/zhyc/system/accessrestriction/controller/SysAccessRestrictionController.java', 'throw new IllegalArgumentException("系统访问限制保存请求不能为空")'],
  ['zhyc-module-system/src/main/java/com/zhyc/system/adminscope/controller/SysAdminScopeController.java', 'throw new IllegalArgumentException("管理员管理范围绑定请求不能为空")'],
  ['zhyc-module-system/src/main/java/com/zhyc/system/coderule/controller/SysCodeRuleController.java', 'throw new IllegalArgumentException("系统编码规则保存请求不能为空")'],
  ['zhyc-module-system/src/main/java/com/zhyc/system/coderule/controller/SysCodeRuleController.java', 'throw new IllegalArgumentException("系统编码规则生成请求不能为空")'],
  ['zhyc-module-system/src/main/java/com/zhyc/system/module/controller/SysModuleController.java', 'throw new IllegalArgumentException("模块启用状态不能为空")'],
  ['zhyc-module-system/src/main/java/com/zhyc/system/param/controller/SysParamController.java', 'throw new IllegalArgumentException("系统参数保存请求不能为空")'],
  ['zhyc-module-system/src/main/java/com/zhyc/system/passwordpolicy/controller/SysPasswordPolicyController.java', 'throw new IllegalArgumentException("系统密码策略保存请求不能为空")'],
  ['zhyc-module-system/src/main/java/com/zhyc/system/passwordpolicy/controller/SysPasswordPolicyController.java', 'throw new IllegalArgumentException("密码策略校验请求不能为空")'],
  ['zhyc-module-system/src/main/java/com/zhyc/system/passwordpolicy/controller/SysPasswordPolicyController.java', 'throw new IllegalArgumentException("密码历史策略校验请求不能为空")'],
  ['zhyc-module-system/src/main/java/com/zhyc/system/role/controller/SysRoleController.java', 'throw new IllegalArgumentException("角色菜单绑定请求不能为空")'],
  ['zhyc-module-system/src/main/java/com/zhyc/system/role/controller/SysRoleDataScopeController.java', 'throw new IllegalArgumentException("角色数据权限绑定请求不能为空")'],
  ['zhyc-module-system/src/main/java/com/zhyc/system/user/controller/SysUserPostController.java', 'throw new IllegalArgumentException("用户岗位绑定请求不能为空")'],
  ['zhyc-module-system/src/main/java/com/zhyc/system/user/controller/SysUserRoleController.java', 'throw new IllegalArgumentException("用户角色绑定请求不能为空")'],
];

const errors = [];

for (const [file, snippet] of requiredSnippets) {
  const content = readExistingFile(file);
  if (content !== null && !content.includes(snippet)) {
    errors.push(`缺少关键内容：${file} -> ${snippet}`);
  }
}

for (const [file, snippet] of forbiddenSnippets) {
  const content = readExistingFile(file);
  if (content !== null && content.includes(snippet)) {
    errors.push(`存在过期裸异常：${file} -> ${snippet}`);
  }
}

if (errors.length > 0) {
  console.error('系统 Controller 业务异常契约校验失败。');
  for (const error of errors) {
    console.error(`- ${error}`);
  }
  process.exit(1);
}

console.log('系统 Controller 业务异常契约校验通过。');

/**
 * 读取必须存在的源码文件。
 *
 * @param file 相对后端根目录的文件路径
 * @returns 文件内容，文件缺失时返回 null
 */
function readExistingFile(file) {
  const absolutePath = resolve(serverRoot, file);
  if (!existsSync(absolutePath)) {
    errors.push(`缺少文件：${file}`);
    return null;
  }
  return readFileSync(absolutePath, 'utf8');
}
