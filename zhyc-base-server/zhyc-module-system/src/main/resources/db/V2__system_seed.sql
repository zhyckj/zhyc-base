-- Copyright (c) 2026 众汇云创科技（深圳）有限公司.
-- This file is part of ZHYC and is licensed for non-commercial use only.
-- Commercial use requires a separate written license from the copyright holder.
-- SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial

-- 首期本地初始化种子数据，仅用于搭建可联调的基础租户、用户、角色、菜单和模块目录。
-- password_hash 必须在本地部署前替换为 Shiro PasswordService 生成的哈希值，禁止提交真实默认密码。

INSERT IGNORE INTO sys_tenant_package (id, package_code, package_name, status, max_user_count, max_storage_mb)
VALUES (1, 'default-enterprise', '默认企业版套餐', 'enabled', 1000, 102400);

INSERT IGNORE INTO sys_tenant (id, tenant_id, tenant_name, package_id, isolation_mode, status, contact_name)
VALUES (1, 'zhyc-platform', '众汇云创演示租户', 1, 'TENANT_COLUMN', 'enabled', '平台管理员');

INSERT IGNORE INTO sys_org (id, tenant_id, parent_id, ancestors, org_code, org_name, leader_user_id, sort_order, status)
VALUES (1, 'zhyc-platform', NULL, '0', 'HQ', '总部', NULL, 1, 'enabled');

INSERT IGNORE INTO sys_post (id, tenant_id, org_id, post_code, post_name, sort_order, status)
VALUES (1, 'zhyc-platform', 1, 'platform-admin', '平台管理员', 1, 'enabled');

INSERT IGNORE INTO sys_user (id, tenant_id, username, nickname, password_hash, status)
VALUES (1, 'zhyc-platform', 'admin', '平台管理员', 'replace_with_shiro_password_hash', 'enabled');

INSERT IGNORE INTO sys_role (id, tenant_id, role_code, name, data_scope, status)
VALUES (1, 'zhyc-platform', 'platform-admin', '平台管理员', 'ALL', 'enabled');

INSERT IGNORE INTO sys_user_post (tenant_id, user_id, post_id, primary_flag)
VALUES ('zhyc-platform', 1, 1, 1);

INSERT IGNORE INTO sys_user_role (tenant_id, user_id, role_id)
VALUES ('zhyc-platform', 1, 1);

INSERT IGNORE INTO sys_admin_scope (tenant_id, user_id, scope_type, scope_ref_code, scope_name)
VALUES
    ('zhyc-platform', 1, 'tenant', 'zhyc-platform', '众汇云创演示租户'),
    ('zhyc-platform', 1, 'org', 'HQ', '总部'),
    ('zhyc-platform', 1, 'module', 'system', '系统管理'),
    ('zhyc-platform', 1, 'module', 'lowcode', '低代码中心'),
    ('zhyc-platform', 1, 'module', 'ai', 'AI 能力中心'),
    ('zhyc-platform', 1, 'module', 'workflow', '工作流中心'),
    ('zhyc-platform', 1, 'module', 'openapi', '开放平台');

INSERT INTO sys_menu (id, tenant_id, parent_id, menu_code, menu_name, menu_type, path, component, permission, sort_order, status)
VALUES
    (100, 'zhyc-platform', NULL, 'dashboard', '个人工作台', 'menu', '/dashboard', 'dashboard/index', 'dashboard:view', 10, 'enabled'),
    (200, 'zhyc-platform', NULL, 'system', '系统管理', 'directory', '/system', 'LAYOUT', 'system:view', 20, 'enabled'),
    (210, 'zhyc-platform', 200, 'system-tenant', '租户管理', 'menu', '/system/tenants', 'system/tenant/index', 'system:tenant:query', 10, 'enabled'),
    (211, 'zhyc-platform', 200, 'system-tenant-package', '租户套餐', 'menu', '/system/tenant-packages', 'system/tenant-package/index', 'system:tenant-package:query', 20, 'enabled'),
    (212, 'zhyc-platform', 200, 'system-tenant-package-module', '套餐授权', 'menu', '/system/tenant-package-modules', 'system/tenant-package-module/index', 'system:tenant-package-module:query', 30, 'enabled'),
    (213, 'zhyc-platform', 200, 'system-tenant-param', '租户参数', 'menu', '/system/tenant-params', 'system/tenant-param/index', 'system:tenant-param:query', 40, 'enabled'),
    (214, 'zhyc-platform', 200, 'system-admin-scope', '管理员范围', 'menu', '/system/admin-scopes', 'system/admin-scope/index', 'system:admin:query', 50, 'enabled'),
    (220, 'zhyc-platform', 200, 'system-org', '组织机构', 'menu', '/system/orgs', 'system/org/index', 'system:org:query', 60, 'enabled'),
    (221, 'zhyc-platform', 200, 'system-post', '岗位管理', 'menu', '/system/posts', 'system/post/index', 'system:post:query', 70, 'enabled'),
    (222, 'zhyc-platform', 200, 'system-user', '用户管理', 'menu', '/system/users', 'system/user/index', 'system:user:query', 80, 'enabled'),
    (223, 'zhyc-platform', 200, 'system-user-post', '用户岗位', 'menu', '/system/user-posts', 'system/user-post/index', 'system:user:query', 90, 'disabled'),
    (224, 'zhyc-platform', 200, 'system-user-role', '用户角色', 'menu', '/system/user-roles', 'system/user-role/index', 'system:user:query', 100, 'disabled'),
    (230, 'zhyc-platform', 200, 'system-role', '角色管理', 'menu', '/system/roles', 'system/role/index', 'system:role:query', 110, 'enabled'),
    (240, 'zhyc-platform', 200, 'system-menu', '菜单权限', 'menu', '/system/menus', 'system/menu/index', 'system:permission:query', 120, 'enabled'),
    (241, 'zhyc-platform', 200, 'system-role-data-scope', '角色数据权限', 'menu', '/system/role-data-scopes', 'system/role-data-scope/index', 'system:role:query', 130, 'disabled'),
    (242, 'zhyc-platform', 200, 'system-permission-audit', '权限审计', 'menu', '/system/permission-audits', 'system/permission-audit/index', 'system:audit:query', 140, 'enabled'),
    (250, 'zhyc-platform', 260, 'system-access-restriction', '访问限制查询', 'button', NULL, NULL, 'system:access-restriction:query', 5, 'enabled'),
    (251, 'zhyc-platform', 200, 'system-password-policy', '密码策略', 'menu', '/system/password-policies', 'system/password-policy/index', 'system:password-policy:query', 160, 'enabled'),
    (252, 'zhyc-platform', 200, 'system-module', '模块管理', 'menu', '/system/modules', 'system/module/index', 'system:module:query', 170, 'enabled'),
    (253, 'zhyc-platform', 200, 'system-code-rule', '编码规则', 'menu', '/system/code-rules', 'system/code-rule/index', 'system:code-rule:query', 180, 'enabled'),
    (254, 'zhyc-platform', 200, 'system-param', '系统参数', 'menu', '/system/params', 'system/param/index', 'system:param:query', 190, 'enabled'),
    (255, 'zhyc-platform', 200, 'system-dict', '字典管理', 'menu', '/system/dicts', 'system/dict/index', 'system:dict:query', 200, 'enabled'),
    (256, 'zhyc-platform', 200, 'system-audit-log', '审计日志', 'menu', '/system/audit-logs', 'system/audit-log/index', 'system:audit:query', 210, 'enabled'),
    (257, 'zhyc-platform', 200, 'system-login-log', '登录日志', 'menu', '/system/login-logs', 'system/login-log/index', 'system:audit:query', 220, 'enabled'),
    (258, 'zhyc-platform', 200, 'system-exception-log', '异常日志', 'menu', '/system/exception-logs', 'system/exception-log/index', 'system:audit:query', 230, 'enabled'),
    (259, 'zhyc-platform', 200, 'system-secret', '密钥管理', 'menu', '/system/secrets', 'system/secret/index', 'system:secret:query', 240, 'enabled'),
    (260, 'zhyc-platform', 200, 'system-security-protection', '安全防护中心', 'menu', '/system/security-protection', 'system/security-protection/index', 'system:security-protection:query', 150, 'enabled'),
    (300, 'zhyc-platform', NULL, 'lowcode', '低代码中心', 'directory', '/lowcode', 'LAYOUT', 'lowcode:view', 30, 'enabled'),
    (310, 'zhyc-platform', 300, 'lowcode-datasource', '数据源管理', 'menu', '/lowcode/datasource', 'lowcode/datasource/index', 'lowcode:datasource:query', 10, 'enabled'),
    (320, 'zhyc-platform', 300, 'lowcode-model', '数据表建模', 'menu', '/lowcode/model', 'lowcode/model/index', 'lowcode:table:query', 20, 'enabled'),
    (32001, 'zhyc-platform', 320, 'lowcode-table-save', '保存模型', 'button', NULL, NULL, 'lowcode:table:save', 1, 'enabled'),
    (32002, 'zhyc-platform', 320, 'lowcode-table-import', '导入表结构', 'button', NULL, NULL, 'lowcode:table:import', 2, 'enabled'),
    (32003, 'zhyc-platform', 320, 'lowcode-table-publish', '发布建表', 'button', NULL, NULL, 'lowcode:table:publish', 3, 'enabled'),
    (321, 'zhyc-platform', 300, 'lowcode-relation', '表关系建模', 'menu', '/lowcode/relations', 'lowcode/relation/index', 'lowcode:relation:query', 30, 'enabled'),
    (322, 'zhyc-platform', 300, 'lowcode-page', '页面模型', 'menu', '/lowcode/pages', 'lowcode/page/index', 'lowcode:page:query', 40, 'enabled'),
    (323, 'zhyc-platform', 300, 'lowcode-template', '生成模板', 'menu', '/lowcode/templates', 'lowcode/generator/index', 'lowcode:template:query', 50, 'disabled'),
    (330, 'zhyc-platform', 300, 'lowcode-generator', '代码生成', 'menu', '/lowcode/generator', 'lowcode/generator/index', 'lowcode:generator:query', 50, 'enabled'),
    (331, 'zhyc-platform', 300, 'lowcode-record', '生成记录', 'menu', '/lowcode/records', 'lowcode/generator/index', 'lowcode:generator:query', 70, 'disabled'),
    (350, 'zhyc-platform', NULL, 'ai', 'AI 能力中心', 'directory', '/ai', 'LAYOUT', 'ai:view', 35, 'enabled'),
    (351, 'zhyc-platform', 350, 'ai-provider', '供应商', 'menu', '/ai/providers', 'ai/core/index', 'ai:provider:query', 10, 'enabled'),
    (352, 'zhyc-platform', 350, 'ai-model', '模型配置', 'menu', '/ai/models', 'ai/core/index', 'ai:model:query', 20, 'enabled'),
    (353, 'zhyc-platform', 350, 'ai-app', '应用接入', 'menu', '/ai/apps', 'ai/core/index', 'ai:app:query', 30, 'enabled'),
    (354, 'zhyc-platform', 350, 'ai-prompt', '提示词', 'menu', '/ai/prompts', 'ai/core/index', 'ai:prompt:query', 40, 'enabled'),
    (355, 'zhyc-platform', 350, 'ai-invocation-audit', '调用审计', 'menu', '/ai/invocation-audits', 'ai/core/index', 'ai:audit:query', 50, 'enabled'),
    (35101, 'zhyc-platform', 351, 'ai-provider-save', '保存供应商', 'button', NULL, NULL, 'ai:provider:save', 1, 'enabled'),
    (35201, 'zhyc-platform', 352, 'ai-model-save', '保存模型', 'button', NULL, NULL, 'ai:model:save', 1, 'enabled'),
    (35301, 'zhyc-platform', 353, 'ai-app-save', '保存应用', 'button', NULL, NULL, 'ai:app:save', 1, 'enabled'),
    (35302, 'zhyc-platform', 353, 'ai-runtime-chat', '测试调用', 'button', NULL, NULL, 'ai:runtime:chat', 2, 'enabled'),
    (35401, 'zhyc-platform', 354, 'ai-prompt-save', '保存提示词', 'button', NULL, NULL, 'ai:prompt:save', 1, 'enabled'),
    (35501, 'zhyc-platform', 355, 'ai-audit-record', '记录调用审计', 'button', NULL, NULL, 'ai:audit:record', 1, 'enabled'),
    (400, 'zhyc-platform', NULL, 'workflow', '工作流中心', 'directory', '/workflow', 'LAYOUT', 'workflow:view', 40, 'enabled'),
    (410, 'zhyc-platform', 400, 'workflow-task-todo', '流程待办', 'menu', '/workflow/tasks/todo', 'workflow/task/todo', 'workflow:task:todo', 10, 'enabled'),
    (411, 'zhyc-platform', 400, 'workflow-task-done', '流程已办', 'menu', '/workflow/tasks/done', 'workflow/task/done', 'workflow:task:done', 20, 'enabled'),
    (412, 'zhyc-platform', 400, 'workflow-task-started', '我发起的', 'menu', '/workflow/tasks/started', 'workflow/task/started', 'workflow:task:started', 30, 'enabled'),
    (413, 'zhyc-platform', 400, 'workflow-task-cc', '抄送我的', 'menu', '/workflow/tasks/cc', 'workflow/task/cc', 'workflow:task:cc', 40, 'enabled'),
    (414, 'zhyc-platform', 400, 'workflow-task-monitor', '流程监控', 'menu', '/workflow/tasks/monitor', 'workflow/task/monitor', 'workflow:task:monitor', 50, 'enabled'),
    (420, 'zhyc-platform', 400, 'workflow-category', '流程分类', 'menu', '/workflow/categories', 'workflow/category/index', 'workflow:model:query', 60, 'enabled'),
    (421, 'zhyc-platform', 400, 'workflow-model', '流程模型', 'menu', '/workflow/models', 'workflow/model/index', 'workflow:model:query', 70, 'enabled'),
    (422, 'zhyc-platform', 400, 'workflow-form-binding', '表单绑定', 'menu', '/workflow/form-bindings', 'workflow/binding/index', 'workflow:binding:query', 80, 'enabled'),
    (423, 'zhyc-platform', 400, 'workflow-definition', '流程定义', 'menu', '/workflow/definitions', 'workflow/definition/index', 'workflow:model:query', 90, 'enabled'),
    (500, 'zhyc-platform', NULL, 'openapi', '开放平台', 'directory', '/openapi', 'LAYOUT', 'openapi:view', 50, 'enabled'),
    (505, 'zhyc-platform', 500, 'developer-portal', '开发者门户', 'menu', '/developer/portal', 'developer/portal/index', 'openapi:developer:portal', 10, 'enabled'),
    (510, 'zhyc-platform', 500, 'openapi-app', '开发者应用', 'menu', '/openapi/apps', 'openapi/app/index', 'openapi:app:query', 20, 'enabled'),
    (511, 'zhyc-platform', 500, 'openapi-api-key', 'API Key', 'menu', '/openapi/api-keys', 'openapi/api-key/index', 'openapi:api-key:query', 30, 'enabled'),
    (512, 'zhyc-platform', 500, 'openapi-oauth-client', 'OAuth2 客户端', 'menu', '/openapi/oauth-clients', 'openapi/oauth-client/index', 'openapi:oauth-client:query', 40, 'enabled'),
    (520, 'zhyc-platform', 500, 'openapi-catalog', 'API 目录', 'menu', '/openapi/catalogs', 'openapi/catalog/index', 'openapi:catalog:query', 50, 'enabled'),
    (521, 'zhyc-platform', 500, 'openapi-version', 'API 发布', 'menu', '/openapi/versions', 'openapi/version/index', 'openapi:catalog:query', 60, 'enabled'),
    (522, 'zhyc-platform', 500, 'openapi-permission', 'API 授权', 'menu', '/openapi/api-permissions', 'openapi/api-permission/index', 'openapi:api-permission:query', 70, 'enabled'),
    (523, 'zhyc-platform', 500, 'openapi-signature-policy', '签名策略', 'menu', '/openapi/signature-policies', 'openapi/signature-policy/index', 'openapi:signature-policy:query', 80, 'enabled'),
    (524, 'zhyc-platform', 500, 'openapi-rate-limit-policy', '限流策略', 'menu', '/openapi/rate-limit-policies', 'openapi/rate-limit-policy/index', 'openapi:rate-limit-policy:query', 90, 'enabled'),
    (525, 'zhyc-platform', 500, 'openapi-call-audit', '调用审计', 'menu', '/openapi/call-audits', 'openapi/call-audit/index', 'openapi:call-audit:query', 100, 'enabled'),
    (526, 'zhyc-platform', 500, 'openapi-error-log', '错误日志', 'menu', '/openapi/error-logs', 'openapi/error-log/index', 'openapi:error-log:query', 110, 'enabled'),
    (600, 'zhyc-platform', NULL, 'purchase', '采购样板', 'directory', '/purchase', 'LAYOUT', 'purchase:view', 60, 'enabled'),
    (610, 'zhyc-platform', 600, 'purchase-request', '采购申请', 'menu', '/purchase/requests', 'purchase/request/index', 'purchase:request:view', 10, 'enabled'),
    (611, 'zhyc-platform', 600, 'purchase-order', '采购订单', 'menu', '/purchase/orders', 'purchase/order/index', 'purchase:order:query', 20, 'enabled'),
    (612, 'zhyc-platform', 600, 'purchase-approval', '采购审批记录', 'menu', '/purchase/approvals', 'purchase/approval-record/index', 'purchase:approval:query', 30, 'enabled'),
    (700, 'zhyc-platform', NULL, 'message', '消息中心', 'directory', '/message', 'LAYOUT', 'message:view', 70, 'enabled'),
    (710, 'zhyc-platform', 700, 'message-inbox', '站内消息', 'menu', '/message/inbox', 'message/inbox/index', 'message:inbox:query', 10, 'enabled'),
    (711, 'zhyc-platform', 700, 'message-template', '消息模板', 'menu', '/message/templates', 'message/template/index', 'message:template:query', 20, 'enabled'),
    (800, 'zhyc-platform', NULL, 'file', '文件中心', 'directory', '/file', 'LAYOUT', 'file:view', 80, 'enabled'),
    (810, 'zhyc-platform', 800, 'file-storage-config', '存储配置', 'menu', '/file/storage-configs', 'file/storage/index', 'file:storage:query', 10, 'enabled'),
    (811, 'zhyc-platform', 800, 'file-object', '文件对象', 'menu', '/file/objects', 'file/object/index', 'file:object:query', 20, 'enabled'),
    (812, 'zhyc-platform', 800, 'file-preview-log', '预览记录', 'menu', '/file/preview-logs', 'file/preview/index', 'file:preview:query', 30, 'enabled'),
    (900, 'zhyc-platform', NULL, 'cms', '内容管理', 'directory', '/cms', 'LAYOUT', 'cms:view', 90, 'enabled'),
    (910, 'zhyc-platform', 900, 'cms-channel', '内容栏目', 'menu', '/cms/channels', 'cms/channel/index', 'cms:channel:query', 10, 'enabled'),
    (911, 'zhyc-platform', 900, 'cms-content', '内容文章', 'menu', '/cms/contents', 'cms/content/index', 'cms:content:query', 20, 'enabled'),
    (1000, 'zhyc-platform', NULL, 'visual', '报表大屏', 'directory', '/visual', 'LAYOUT', 'visual:view', 100, 'enabled'),
    (1010, 'zhyc-platform', 1000, 'visual-dataset', '报表数据集', 'menu', '/visual/datasets', 'visual/dataset/index', 'visual:dataset:query', 10, 'enabled'),
    (1011, 'zhyc-platform', 1000, 'visual-report', '报表设计器', 'menu', '/visual/reports', 'visual/report/index', 'visual:report:query', 20, 'enabled'),
    (1012, 'zhyc-platform', 1000, 'visual-screen', '可视化数据大屏', 'menu', '/visual/screens', 'visual/screen/index', 'visual:screen:query', 30, 'enabled'),
    (1100, 'zhyc-platform', NULL, 'job', '在线作业', 'directory', '/job', 'LAYOUT', 'job:view', 110, 'enabled'),
    (1110, 'zhyc-platform', 1100, 'job-task', '在线作业', 'menu', '/job/tasks', 'job/task/index', 'job:task:query', 10, 'enabled'),
    (1200, 'zhyc-platform', NULL, 'i18n', '国际化', 'directory', '/i18n', 'LAYOUT', 'i18n:view', 120, 'enabled'),
    (1210, 'zhyc-platform', 1200, 'i18n-message', '国际化词条', 'menu', '/i18n/messages', 'i18n/message/index', 'i18n:message:query', 10, 'enabled'),
    (121001, 'zhyc-platform', 1210, 'i18n-message-save', '词条保存', 'button', NULL, NULL, 'i18n:message:save', 1, 'enabled'),
    (121002, 'zhyc-platform', 1210, 'i18n-message-resolve', '词条解析', 'button', NULL, NULL, 'i18n:message:resolve', 2, 'enabled'),
    (1300, 'zhyc-platform', NULL, 'search', '全文检索', 'directory', '/search', 'LAYOUT', 'search:view', 130, 'enabled'),
    (1310, 'zhyc-platform', 1300, 'search-index-config', '全文检索', 'menu', '/search/index-configs', 'search/index-config/index', 'search:index:query', 10, 'enabled'),
    (1400, 'zhyc-platform', NULL, 'monitor', '系统监控', 'directory', '/monitor', 'LAYOUT', 'monitor:view', 140, 'enabled'),
    (1410, 'zhyc-platform', 1400, 'monitor-service', '服务监控', 'menu', '/monitor/services', 'monitor/service/index', 'monitor:service:query', 10, 'enabled'),
    (1411, 'zhyc-platform', 1400, 'monitor-data-source', '数据源监控', 'menu', '/monitor/data-sources', 'monitor/datasource/index', 'monitor:data-source:query', 20, 'enabled'),
    (1412, 'zhyc-platform', 1400, 'monitor-sql', 'SQL 监控', 'menu', '/monitor/sql', 'monitor/sql/index', 'monitor:sql:query', 30, 'enabled'),
    (21001, 'zhyc-platform', 210, 'system-tenant-create', '租户新增', 'button', NULL, NULL, 'system:tenant:create', 1, 'enabled'),
    (21002, 'zhyc-platform', 210, 'system-tenant-update', '租户编辑', 'button', NULL, NULL, 'system:tenant:update', 2, 'enabled'),
    (21003, 'zhyc-platform', 210, 'system-tenant-status', '租户启停', 'button', NULL, NULL, 'system:tenant:update-status', 3, 'enabled'),
    (21004, 'zhyc-platform', 210, 'system-tenant-delete', '租户删除', 'button', NULL, NULL, 'system:tenant:delete', 4, 'enabled'),
    (21101, 'zhyc-platform', 211, 'system-tenant-package-status', '套餐启停', 'button', NULL, NULL, 'system:tenant-package:update', 1, 'enabled'),
    (21201, 'zhyc-platform', 212, 'system-tenant-package-module-bind', '套餐授权绑定', 'button', NULL, NULL, 'system:tenant-package:update', 1, 'enabled'),
    (21301, 'zhyc-platform', 213, 'system-tenant-param-save', '租户参数保存', 'button', NULL, NULL, 'system:tenant-param:save', 1, 'enabled'),
    (21401, 'zhyc-platform', 214, 'system-admin-scope-edit', '管理员范围编辑', 'button', NULL, NULL, 'system:admin:edit', 1, 'enabled'),
    (22001, 'zhyc-platform', 220, 'system-org-create', '组织新增', 'button', NULL, NULL, 'system:org:create', 1, 'enabled'),
    (22002, 'zhyc-platform', 220, 'system-org-update', '组织编辑', 'button', NULL, NULL, 'system:org:update', 2, 'enabled'),
    (22003, 'zhyc-platform', 220, 'system-org-status', '组织启停', 'button', NULL, NULL, 'system:org:update-status', 3, 'enabled'),
    (22004, 'zhyc-platform', 220, 'system-org-delete', '组织删除', 'button', NULL, NULL, 'system:org:delete', 4, 'enabled'),
    (22101, 'zhyc-platform', 221, 'system-post-create', '岗位新增', 'button', NULL, NULL, 'system:post:create', 1, 'enabled'),
    (22102, 'zhyc-platform', 221, 'system-post-update', '岗位编辑', 'button', NULL, NULL, 'system:post:update', 2, 'enabled'),
    (22103, 'zhyc-platform', 221, 'system-post-status', '岗位启停', 'button', NULL, NULL, 'system:post:update-status', 3, 'enabled'),
    (22104, 'zhyc-platform', 221, 'system-post-delete', '岗位删除', 'button', NULL, NULL, 'system:post:delete', 4, 'enabled'),
    (22201, 'zhyc-platform', 222, 'system-user-create', '用户新增', 'button', NULL, NULL, 'system:user:create', 1, 'enabled'),
    (22202, 'zhyc-platform', 222, 'system-user-update', '用户编辑', 'button', NULL, NULL, 'system:user:update', 2, 'enabled'),
    (22203, 'zhyc-platform', 222, 'system-user-status', '用户启停', 'button', NULL, NULL, 'system:user:update-status', 3, 'enabled'),
    (22204, 'zhyc-platform', 222, 'system-user-delete', '用户删除', 'button', NULL, NULL, 'system:user:delete', 4, 'enabled'),
    (22205, 'zhyc-platform', 222, 'system-user-reset-password', '重置密码', 'button', NULL, NULL, 'system:user:reset-password', 5, 'enabled'),
    (22301, 'zhyc-platform', 222, 'system-user-post-bind', '绑定用户岗位', 'button', NULL, NULL, 'system:user:edit', 6, 'enabled'),
    (22401, 'zhyc-platform', 222, 'system-user-role-bind', '绑定用户角色', 'button', NULL, NULL, 'system:user:edit', 7, 'enabled'),
    (23001, 'zhyc-platform', 230, 'system-role-create', '角色新增', 'button', NULL, NULL, 'system:role:create', 1, 'enabled'),
    (23002, 'zhyc-platform', 230, 'system-role-update', '角色编辑', 'button', NULL, NULL, 'system:role:update', 2, 'enabled'),
    (23003, 'zhyc-platform', 230, 'system-role-status', '角色启停', 'button', NULL, NULL, 'system:role:update-status', 3, 'enabled'),
    (23004, 'zhyc-platform', 230, 'system-role-delete', '角色删除', 'button', NULL, NULL, 'system:role:delete', 4, 'enabled'),
    (23005, 'zhyc-platform', 230, 'system-role-authorize', '角色菜单授权', 'button', NULL, NULL, 'system:role:authorize', 5, 'enabled'),
    (24001, 'zhyc-platform', 240, 'system-menu-create', '菜单新增', 'button', NULL, NULL, 'system:permission:create', 1, 'enabled'),
    (24002, 'zhyc-platform', 240, 'system-menu-update', '菜单编辑', 'button', NULL, NULL, 'system:permission:update', 2, 'enabled'),
    (24003, 'zhyc-platform', 240, 'system-menu-status', '菜单启停', 'button', NULL, NULL, 'system:permission:update-status', 3, 'enabled'),
    (24004, 'zhyc-platform', 240, 'system-menu-delete', '菜单删除', 'button', NULL, NULL, 'system:permission:delete', 4, 'enabled'),
    (24101, 'zhyc-platform', 230, 'system-role-data-scope-edit', '角色数据权限编辑', 'button', NULL, NULL, 'system:role:edit', 6, 'enabled'),
    (25001, 'zhyc-platform', 260, 'system-access-restriction-save', '访问限制保存', 'button', NULL, NULL, 'system:access-restriction:save', 6, 'enabled'),
    (25002, 'zhyc-platform', 260, 'system-access-restriction-evaluate', '访问限制校验', 'button', NULL, NULL, 'system:access-restriction:evaluate', 7, 'enabled'),
    (25101, 'zhyc-platform', 251, 'system-password-policy-save', '密码策略保存', 'button', NULL, NULL, 'system:password-policy:save', 1, 'enabled'),
    (25102, 'zhyc-platform', 251, 'system-password-policy-validate', '密码策略校验', 'button', NULL, NULL, 'system:password-policy:validate', 2, 'enabled'),
    (25201, 'zhyc-platform', 252, 'system-module-update', '模块启停', 'button', NULL, NULL, 'system:module:update', 1, 'enabled'),
    (25301, 'zhyc-platform', 253, 'system-code-rule-save', '编码规则保存', 'button', NULL, NULL, 'system:code-rule:save', 1, 'enabled'),
    (25302, 'zhyc-platform', 253, 'system-code-rule-generate', '编码规则生成', 'button', NULL, NULL, 'system:code-rule:generate', 2, 'enabled'),
    (25401, 'zhyc-platform', 254, 'system-param-save', '系统参数保存', 'button', NULL, NULL, 'system:param:save', 1, 'enabled'),
    (25901, 'zhyc-platform', 259, 'system-secret-query', '密钥查看', 'button', NULL, NULL, 'system:secret:query', 1, 'enabled'),
    (25902, 'zhyc-platform', 259, 'system-secret-create', '密钥新增', 'button', NULL, NULL, 'system:secret:create', 2, 'enabled'),
    (25903, 'zhyc-platform', 259, 'system-secret-update', '密钥编辑', 'button', NULL, NULL, 'system:secret:update', 3, 'enabled'),
    (25904, 'zhyc-platform', 259, 'system-secret-delete', '密钥删除', 'button', NULL, NULL, 'system:secret:delete', 4, 'enabled'),
    (25905, 'zhyc-platform', 259, 'system-secret-enable', '密钥启用', 'button', NULL, NULL, 'system:secret:enable', 5, 'enabled'),
    (25906, 'zhyc-platform', 259, 'system-secret-disable', '密钥禁用', 'button', NULL, NULL, 'system:secret:disable', 6, 'enabled'),
    (25907, 'zhyc-platform', 259, 'system-secret-rotate', '密钥轮换', 'button', NULL, NULL, 'system:secret:rotate', 7, 'enabled'),
    (25908, 'zhyc-platform', 259, 'system-secret-copy-ref', '复制引用', 'button', NULL, NULL, 'system:secret:copy-ref', 8, 'enabled'),
    (26001, 'zhyc-platform', 260, 'system-security-protection-save', '保存策略', 'button', NULL, NULL, 'system:security-protection:save', 1, 'enabled'),
    (26002, 'zhyc-platform', 260, 'system-security-protection-block', '封禁 IP', 'button', NULL, NULL, 'system:security-protection:block', 2, 'enabled'),
    (26003, 'zhyc-platform', 260, 'system-security-protection-unblock', '解封 IP', 'button', NULL, NULL, 'system:security-protection:unblock', 3, 'enabled'),
    (26004, 'zhyc-platform', 260, 'system-security-protection-record', '记录事件', 'button', NULL, NULL, 'system:security-protection:record', 4, 'enabled'),
    (25501, 'zhyc-platform', 255, 'system-dict-type-create', '字典类型新增', 'button', NULL, NULL, 'system:dict:create', 1, 'enabled'),
    (25502, 'zhyc-platform', 255, 'system-dict-type-update', '字典类型编辑', 'button', NULL, NULL, 'system:dict:update', 2, 'enabled'),
    (25503, 'zhyc-platform', 255, 'system-dict-type-delete', '字典类型删除', 'button', NULL, NULL, 'system:dict:delete', 3, 'enabled'),
    (25504, 'zhyc-platform', 255, 'system-dict-item-create', '字典项新增', 'button', NULL, NULL, 'system:dict:item:create', 4, 'enabled'),
    (25505, 'zhyc-platform', 255, 'system-dict-item-update', '字典项编辑', 'button', NULL, NULL, 'system:dict:item:update', 5, 'enabled'),
    (25506, 'zhyc-platform', 255, 'system-dict-item-delete', '字典项删除', 'button', NULL, NULL, 'system:dict:item:delete', 6, 'enabled'),
    (81101, 'zhyc-platform', 811, 'file-object-upload', '文件上传', 'button', NULL, NULL, 'file:object:upload', 1, 'enabled')
ON DUPLICATE KEY UPDATE
    parent_id = VALUES(parent_id),
    menu_name = VALUES(menu_name),
    menu_type = VALUES(menu_type),
    path = VALUES(path),
    component = VALUES(component),
    permission = VALUES(permission),
    sort_order = VALUES(sort_order),
    status = VALUES(status);

INSERT IGNORE INTO sys_role_menu (tenant_id, role_id, menu_id)
SELECT 'zhyc-platform', 1, id
FROM sys_menu
WHERE tenant_id = 'zhyc-platform';

INSERT IGNORE INTO sys_password_policy (
    tenant_id, policy_code, policy_name, min_length, require_uppercase, require_lowercase,
    require_digit, require_special, expire_days, history_count, max_retry_count, lock_minutes, enabled
)
VALUES ('zhyc-platform', 'default', '默认密码策略', 10, 0, 1, 1, 0, 90, 3, 5, 30, 1);

INSERT IGNORE INTO sys_code_rule (tenant_id, rule_code, rule_name, prefix, date_pattern, sequence_length, current_value, enabled)
VALUES
    ('zhyc-platform', 'purchase-request', '采购申请编码', 'PR', 'yyyyMMdd', 5, 0, 1),
    ('zhyc-platform', 'purchase-order', '采购订单编码', 'PO', 'yyyyMMdd', 5, 0, 1);

INSERT IGNORE INTO sys_param (tenant_id, param_key, param_value, value_type, system_flag, editable)
VALUES
    ('zhyc-platform', 'platform.name', 'ZHYC 快速开发平台', 'string', 1, 1),
    ('zhyc-platform', 'security.login.maxRetryCount', '5', 'number', 1, 1),
    ('zhyc-platform', 'security.password.resetRequired', 'true', 'boolean', 1, 1);

INSERT IGNORE INTO sys_dict_type (tenant_id, dict_code, dict_name, system_flag, status)
VALUES
    ('zhyc-platform', 'common_status', '通用状态', 1, 'enabled'),
    ('zhyc-platform', 'audit_result', '审计结果', 1, 'enabled'),
    ('zhyc-platform', 'workflow_task_status', '流程任务状态', 1, 'enabled');

INSERT IGNORE INTO sys_dict_item (tenant_id, dict_code, item_label, item_value, item_color, sort_order, status)
VALUES
    ('zhyc-platform', 'common_status', '启用', 'enabled', 'green', 1, 'enabled'),
    ('zhyc-platform', 'common_status', '停用', 'disabled', 'red', 2, 'enabled'),
    ('zhyc-platform', 'audit_result', '成功', 'success', 'green', 1, 'enabled'),
    ('zhyc-platform', 'audit_result', '失败', 'failed', 'red', 2, 'enabled'),
    ('zhyc-platform', 'workflow_task_status', '待办', 'todo', 'blue', 1, 'enabled'),
    ('zhyc-platform', 'workflow_task_status', '已办', 'done', 'green', 2, 'enabled');

INSERT IGNORE INTO sys_module (id, module_code, module_name, version, module_type, enabled)
VALUES
    (1, 'system', '系统管理', '0.0.1', 'core', 1),
    (2, 'lowcode', '低代码中心', '0.0.1', 'core', 1),
    (3, 'workflow', '工作流中心', '0.0.1', 'core', 1),
    (4, 'openapi', '开放平台', '0.0.1', 'core', 1),
    (5, 'purchase', '采购样板', '0.0.1', 'sample', 1),
    (6, 'message', '消息中心', '0.0.1', 'extension', 1),
    (7, 'file', '文件中心', '0.0.1', 'extension', 1),
    (8, 'job', '在线作业', '0.0.1', 'extension', 1),
    (9, 'search', '全文检索', '0.0.1', 'extension', 1),
    (10, 'visual', '可视化大屏', '0.0.1', 'extension', 1),
    (11, 'i18n', '国际化', '0.0.1', 'extension', 1),
    (12, 'cms', '内容管理', '0.0.1', 'extension', 1),
    (13, 'ai', 'AI 能力中心', '0.0.1', 'core', 1);

INSERT IGNORE INTO sys_tenant_package_module (package_id, module_code, menu_code, permission)
SELECT 1, module_code, NULL, NULL
FROM sys_module
WHERE enabled = 1;
