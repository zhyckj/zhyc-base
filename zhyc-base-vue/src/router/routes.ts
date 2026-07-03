/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';

import { hasAuthenticatedAdminContext } from '@/utils/adminContext';

/**
 * 后台路由元信息。
 */
export interface AdminRouteMeta {
  /** 页面标题。 */
  title: string;
  /** 菜单访问权限编码；登录和回调页不进入菜单，可为空。 */
  permission?: string;
  /** 是否使用独立页面布局；登录、认证回调等页面不展示后台菜单和顶部栏。 */
  standalone?: boolean;
}

/**
 * 首期后台管理端核心路由。
 */
export const adminRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/dashboard',
  },
  {
    path: '/login',
    name: 'AdminLogin',
    component: () => import('@/views/auth/login.vue'),
    meta: { title: '统一认证登录', standalone: true } satisfies AdminRouteMeta,
  },
  {
    path: '/auth/callback',
    name: 'AdminAuthCallback',
    component: () => import('@/views/auth/callback.vue'),
    meta: { title: '统一认证回调', standalone: true } satisfies AdminRouteMeta,
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/dashboard/index.vue'),
    meta: { title: '个人工作台', permission: 'dashboard:view' } satisfies AdminRouteMeta,
  },
  {
    path: '/workflow/tasks/todo',
    name: 'WorkflowTodo',
    component: () => import('@/views/workflow/task/todo.vue'),
    meta: { title: '流程待办', permission: 'workflow:task:todo' } satisfies AdminRouteMeta,
  },
  {
    path: '/workflow/tasks/done',
    name: 'WorkflowDone',
    component: () => import('@/views/workflow/task/done.vue'),
    meta: { title: '流程已办', permission: 'workflow:task:done' } satisfies AdminRouteMeta,
  },
  {
    path: '/system/tenants',
    name: 'SystemTenant',
    component: () => import('@/views/system/tenant/index.vue'),
    meta: { title: '租户管理', permission: 'system:tenant:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/system/tenant-packages',
    name: 'SystemTenantPackage',
    component: () => import('@/views/system/tenant-package/index.vue'),
    meta: { title: '租户套餐', permission: 'system:tenant-package:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/system/tenant-package-modules',
    name: 'SystemTenantPackageModule',
    component: () => import('@/views/system/tenant-package-module/index.vue'),
    meta: { title: '套餐授权', permission: 'system:tenant-package-module:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/system/tenant-params',
    name: 'SystemTenantParam',
    component: () => import('@/views/system/tenant-param/index.vue'),
    meta: { title: '租户参数', permission: 'system:tenant-param:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/system/security-protection',
    name: 'SystemSecurityProtection',
    component: () => import('@/views/system/security-protection/index.vue'),
    meta: { title: '安全防护中心', permission: 'system:security-protection:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/system/password-policies',
    name: 'SystemPasswordPolicy',
    component: () => import('@/views/system/password-policy/index.vue'),
    meta: { title: '密码策略', permission: 'system:password-policy:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/system/secrets',
    name: 'SystemSecret',
    component: () => import('@/views/system/secret/index.vue'),
    meta: { title: '密钥管理', permission: 'system:secret:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/system/modules',
    name: 'SystemModule',
    component: () => import('@/views/system/module/index.vue'),
    meta: { title: '模块管理', permission: 'system:module:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/system/code-rules',
    name: 'SystemCodeRule',
    component: () => import('@/views/system/code-rule/index.vue'),
    meta: { title: '编码规则', permission: 'system:code-rule:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/system/params',
    name: 'SystemParam',
    component: () => import('@/views/system/param/index.vue'),
    meta: { title: '系统参数', permission: 'system:param:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/system/dicts',
    name: 'SystemDict',
    component: () => import('@/views/system/dict/index.vue'),
    meta: { title: '字典管理', permission: 'system:dict:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/system/permission-audits',
    name: 'SystemPermissionAudit',
    component: () => import('@/views/system/permission-audit/index.vue'),
    meta: { title: '权限审计', permission: 'system:audit:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/system/audit-logs',
    name: 'SystemAuditLog',
    component: () => import('@/views/system/audit-log/index.vue'),
    meta: { title: '审计日志', permission: 'system:audit:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/system/login-logs',
    name: 'SystemLoginLog',
    component: () => import('@/views/system/login-log/index.vue'),
    meta: { title: '登录日志', permission: 'system:audit:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/system/exception-logs',
    name: 'SystemExceptionLog',
    component: () => import('@/views/system/exception-log/index.vue'),
    meta: { title: '异常日志', permission: 'system:audit:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/system/orgs',
    name: 'SystemOrg',
    component: () => import('@/views/system/org/index.vue'),
    meta: { title: '组织机构', permission: 'system:org:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/system/posts',
    name: 'SystemPost',
    component: () => import('@/views/system/post/index.vue'),
    meta: { title: '岗位管理', permission: 'system:post:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/system/users',
    name: 'SystemUser',
    component: () => import('@/views/system/user/index.vue'),
    meta: { title: '用户管理', permission: 'system:user:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/system/roles',
    name: 'SystemRole',
    component: () => import('@/views/system/role/index.vue'),
    meta: { title: '角色管理', permission: 'system:role:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/system/menus',
    name: 'SystemMenu',
    component: () => import('@/views/system/menu/index.vue'),
    meta: { title: '菜单权限', permission: 'system:permission:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/system/admin-scopes',
    name: 'SystemAdminScope',
    component: () => import('@/views/system/admin-scope/index.vue'),
    meta: { title: '管理员范围', permission: 'system:admin:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/workflow/categories',
    name: 'WorkflowCategory',
    component: () => import('@/views/workflow/category/index.vue'),
    meta: { title: '流程分类', permission: 'workflow:model:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/workflow/models',
    name: 'WorkflowProcessModel',
    component: () => import('@/views/workflow/model/index.vue'),
    meta: { title: '流程模型', permission: 'workflow:model:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/workflow/form-bindings',
    name: 'WorkflowFormBinding',
    component: () => import('@/views/workflow/binding/index.vue'),
    meta: { title: '表单绑定', permission: 'workflow:binding:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/workflow/definitions',
    name: 'WorkflowDefinition',
    component: () => import('@/views/workflow/definition/index.vue'),
    meta: { title: '流程定义', permission: 'workflow:model:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/workflow/tasks/started',
    name: 'WorkflowStartedProcess',
    component: () => import('@/views/workflow/task/started.vue'),
    meta: { title: '我发起的', permission: 'workflow:task:started' } satisfies AdminRouteMeta,
  },
  {
    path: '/workflow/tasks/cc',
    name: 'WorkflowCcTask',
    component: () => import('@/views/workflow/task/cc.vue'),
    meta: { title: '抄送我的', permission: 'workflow:task:cc' } satisfies AdminRouteMeta,
  },
  {
    path: '/workflow/tasks/monitor',
    name: 'WorkflowProcessMonitor',
    component: () => import('@/views/workflow/task/monitor.vue'),
    meta: { title: '流程监控', permission: 'workflow:task:monitor' } satisfies AdminRouteMeta,
  },
  {
    path: '/purchase/requests',
    name: 'PurchaseRequest',
    component: () => import('@/views/purchase/request/index.vue'),
    meta: { title: '采购申请', permission: 'purchase:request:view' } satisfies AdminRouteMeta,
  },
  {
    path: '/purchase/orders',
    name: 'PurchaseOrder',
    component: () => import('@/views/purchase/order/index.vue'),
    meta: { title: '采购订单', permission: 'purchase:order:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/purchase/approvals',
    name: 'PurchaseApprovalRecord',
    component: () => import('@/views/purchase/approval-record/index.vue'),
    meta: { title: '采购审批记录', permission: 'purchase:approval:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/message/templates',
    name: 'MessageTemplate',
    component: () => import('@/views/message/template/index.vue'),
    meta: { title: '消息模板', permission: 'message:template:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/message/inbox',
    name: 'MessageInbox',
    component: () => import('@/views/message/inbox/index.vue'),
    meta: { title: '站内消息', permission: 'message:inbox:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/file/storage-configs',
    name: 'FileStorageConfig',
    component: () => import('@/views/file/storage/index.vue'),
    meta: { title: '存储配置', permission: 'file:storage:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/file/objects',
    name: 'FileObject',
    component: () => import('@/views/file/object/index.vue'),
    meta: { title: '文件对象', permission: 'file:object:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/file/preview-logs',
    name: 'FilePreviewLog',
    component: () => import('@/views/file/preview/index.vue'),
    meta: { title: '预览记录', permission: 'file:preview:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/job/tasks',
    name: 'JobTask',
    component: () => import('@/views/job/task/index.vue'),
    meta: { title: '在线作业', permission: 'job:task:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/cms/channels',
    name: 'CmsChannel',
    component: () => import('@/views/cms/channel/index.vue'),
    meta: { title: '内容栏目', permission: 'cms:channel:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/cms/contents',
    name: 'CmsContent',
    component: () => import('@/views/cms/content/index.vue'),
    meta: { title: '内容文章', permission: 'cms:content:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/visual/datasets',
    name: 'VisualDataset',
    component: () => import('@/views/visual/dataset/index.vue'),
    meta: { title: '报表数据集', permission: 'visual:dataset:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/visual/reports',
    name: 'VisualReport',
    component: () => import('@/views/visual/report/index.vue'),
    meta: { title: '报表设计器', permission: 'visual:report:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/visual/screens',
    name: 'VisualScreen',
    component: () => import('@/views/visual/screen/index.vue'),
    meta: { title: '可视化数据大屏', permission: 'visual:screen:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/visual/public/reports/:tenantId/:reportCode',
    name: 'VisualPublicReport',
    component: () => import('@/views/visual/report/public.vue'),
    meta: { title: '可视化报表公开预览', standalone: true } satisfies AdminRouteMeta,
  },
  {
    path: '/visual/public/screens/:tenantId/:screenCode',
    name: 'VisualPublicScreen',
    component: () => import('@/views/visual/screen/public.vue'),
    meta: { title: '可视化大屏公开访问', standalone: true } satisfies AdminRouteMeta,
  },
  {
    path: '/i18n/messages',
    name: 'I18nMessage',
    component: () => import('@/views/i18n/message/index.vue'),
    meta: { title: '国际化词条', permission: 'i18n:message:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/search/index-configs',
    name: 'SearchIndexConfig',
    component: () => import('@/views/search/index-config/index.vue'),
    meta: { title: '全文检索', permission: 'search:index:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/lowcode/datasource',
    name: 'LowcodeDatasource',
    component: () => import('@/views/lowcode/datasource/index.vue'),
    meta: { title: '数据源管理', permission: 'lowcode:datasource:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/lowcode/model',
    name: 'LowcodeModel',
    component: () => import('@/views/lowcode/model/index.vue'),
    meta: { title: '数据表建模', permission: 'lowcode:table:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/lowcode/relations',
    name: 'LowcodeRelation',
    component: () => import('@/views/lowcode/relation/index.vue'),
    meta: { title: '表关系建模', permission: 'lowcode:relation:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/lowcode/pages',
    name: 'LowcodePageModel',
    component: () => import('@/views/lowcode/page/index.vue'),
    meta: { title: '页面模型', permission: 'lowcode:page:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/lowcode/templates',
    redirect: '/lowcode/generator',
  },
  {
    path: '/lowcode/generator',
    name: 'LowcodeGenerator',
    component: () => import('@/views/lowcode/generator/index.vue'),
    meta: { title: '代码生成', permission: 'lowcode:generator:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/lowcode/records',
    redirect: '/lowcode/generator',
  },
  {
    path: '/ai/providers',
    name: 'AiProvider',
    component: () => import('@/views/ai/core/index.vue'),
    meta: { title: '供应商', permission: 'ai:provider:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/ai/models',
    name: 'AiModel',
    component: () => import('@/views/ai/core/index.vue'),
    meta: { title: '模型配置', permission: 'ai:model:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/ai/apps',
    name: 'AiApp',
    component: () => import('@/views/ai/core/index.vue'),
    meta: { title: '应用接入', permission: 'ai:app:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/ai/prompts',
    name: 'AiPrompt',
    component: () => import('@/views/ai/core/index.vue'),
    meta: { title: '提示词', permission: 'ai:prompt:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/ai/invocation-audits',
    name: 'AiInvocationAudit',
    component: () => import('@/views/ai/core/index.vue'),
    meta: { title: '调用审计', permission: 'ai:audit:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/openapi/apps',
    name: 'OpenApiApp',
    component: () => import('@/views/openapi/app/index.vue'),
    meta: { title: '开发者应用', permission: 'openapi:app:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/openapi/api-keys',
    name: 'OpenApiApiKey',
    component: () => import('@/views/openapi/api-key/index.vue'),
    meta: { title: 'API Key', permission: 'openapi:api-key:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/openapi/catalogs',
    name: 'OpenApiCatalog',
    component: () => import('@/views/openapi/catalog/index.vue'),
    meta: { title: 'API 目录', permission: 'openapi:catalog:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/openapi/versions',
    name: 'OpenApiVersion',
    component: () => import('@/views/openapi/version/index.vue'),
    meta: { title: 'API 发布', permission: 'openapi:catalog:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/openapi/api-permissions',
    name: 'OpenApiPermission',
    component: () => import('@/views/openapi/api-permission/index.vue'),
    meta: { title: 'API 授权', permission: 'openapi:api-permission:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/openapi/oauth-clients',
    name: 'OpenApiOauthClient',
    component: () => import('@/views/openapi/oauth-client/index.vue'),
    meta: { title: 'OAuth2 客户端', permission: 'openapi:oauth-client:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/openapi/signature-policies',
    name: 'OpenApiSignaturePolicy',
    component: () => import('@/views/openapi/signature-policy/index.vue'),
    meta: { title: '签名策略', permission: 'openapi:signature-policy:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/openapi/rate-limit-policies',
    name: 'OpenApiRateLimitPolicy',
    component: () => import('@/views/openapi/rate-limit-policy/index.vue'),
    meta: { title: '限流策略', permission: 'openapi:rate-limit-policy:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/openapi/call-audits',
    name: 'OpenApiCallAudit',
    component: () => import('@/views/openapi/call-audit/index.vue'),
    meta: { title: '调用审计', permission: 'openapi:call-audit:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/openapi/error-logs',
    name: 'OpenApiErrorLog',
    component: () => import('@/views/openapi/error-log/index.vue'),
    meta: { title: '错误日志', permission: 'openapi:error-log:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/developer/portal',
    name: 'DeveloperPortal',
    component: () => import('@/views/developer/portal/index.vue'),
    meta: { title: '开发者门户', permission: 'openapi:developer:portal' } satisfies AdminRouteMeta,
  },
  {
    path: '/monitor/services',
    name: 'MonitorService',
    component: () => import('@/views/monitor/service/index.vue'),
    meta: { title: '服务监控', permission: 'monitor:service:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/monitor/data-sources',
    name: 'MonitorDataSource',
    component: () => import('@/views/monitor/datasource/index.vue'),
    meta: { title: '数据源监控', permission: 'monitor:data-source:query' } satisfies AdminRouteMeta,
  },
  {
    path: '/monitor/sql',
    name: 'MonitorSql',
    component: () => import('@/views/monitor/sql/index.vue'),
    meta: { title: 'SQL 监控', permission: 'monitor:sql:query' } satisfies AdminRouteMeta,
  },
];

export const router = createRouter({
  history: createWebHistory(),
  routes: adminRoutes,
});

router.beforeEach((to) => {
  if (to.meta.standalone) {
    return true;
  }
  if (hasAuthenticatedAdminContext()) {
    return true;
  }
  return {
    path: '/login',
    query: { returnTo: to.fullPath },
    replace: true,
  };
});
