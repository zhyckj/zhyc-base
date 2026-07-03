/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import fs from 'node:fs';
import http from 'node:http';

const CDP_VERSION_URL = process.env.ADMIN_CDP_VERSION_URL || 'http://127.0.0.1:9333/json/version';
const ADMIN_BASE_URL = process.env.ADMIN_BASE_URL || 'http://127.0.0.1:5173';
const AUTH_LOGIN_URL = process.env.ADMIN_AUTH_LOGIN_URL || 'http://127.0.0.1:8090/mobile/auth/login';
const SCREENSHOT_DIR = process.env.ADMIN_SCREENSHOT_DIR || '/private/tmp';
const CDP_REQUEST_TIMEOUT_MS = Number(process.env.ADMIN_CDP_REQUEST_TIMEOUT_MS || 10_000);
const ADMIN_CONTEXT_STORAGE_KEY = 'ZHYC_ADMIN_RUNTIME_CONTEXT';
const VERIFY_USERNAME = process.env.ZHYC_ADMIN_VERIFY_USERNAME || process.env.ZHYC_MOBILE_VERIFY_USERNAME || '';
const VERIFY_PASSWORD = process.env.ZHYC_ADMIN_VERIFY_PASSWORD || process.env.ZHYC_MOBILE_VERIFY_PASSWORD || '';
const VERIFY_ACCOUNT_NAME = process.env.ZHYC_ADMIN_VERIFY_ACCOUNT_NAME || VERIFY_USERNAME;
const VERIFY_TENANT_ID = process.env.ZHYC_ADMIN_VERIFY_TENANT_ID || 'zhyc-platform';
const VERIFY_USER_ID = Number(process.env.ZHYC_ADMIN_VERIFY_USER_ID || 1);

let cdpSequence = 1;

/**
 * 创建“点击入口打开弹窗并检查字段”的后台交互验证场景。
 *
 * @param {Array<{code: string, title: string, path: string, pageText: string, buttonText: string, modalTitle: string, fields: string[]}>} configs 场景配置
 * @returns {Array<Record<string, unknown>>} CDP 验证场景
 */
function createModalFormScenarios(configs) {
  return configs.map((config) => {
    const fieldsLiteral = JSON.stringify(config.fields);
    const buttonTextLiteral = JSON.stringify(config.buttonText);
    return {
      code: config.code,
      title: config.title,
      path: config.path,
      authenticated: true,
      readyExpression: `(document.body?.innerText || '').includes(${JSON.stringify(config.pageText)}) && normalizeVerifyText(document.body?.innerText || '').includes(normalizeVerifyText(${buttonTextLiteral}))`,
      beforeVerifyExpression: `(() => {
        const target = Array.from(document.querySelectorAll('button'))
          .find((button) => normalizeVerifyText(button.innerText) === normalizeVerifyText(${buttonTextLiteral}));
        target?.click();
        return Boolean(target);
      })()`,
      verifyExpression: `(() => {
        const text = document.body.innerText;
        const normalizedText = normalizeVerifyText(text);
        const expectedFields = ${fieldsLiteral};
        return {
          modalOpen: Boolean(document.querySelector('.ant-modal')),
          hasTitle: text.includes(${JSON.stringify(config.modalTitle)}),
          missingFields: expectedFields.filter((field) => !normalizedText.includes(normalizeVerifyText(field))),
          text: text.slice(0, 900)
        };
      })()`,
      assert(result) {
        const failures = [];
        if (!result.modalOpen || !result.hasTitle) {
          failures.push(`点击${config.buttonText}后应打开${config.modalTitle}弹窗`);
        }
        if (Array.isArray(result.missingFields) && result.missingFields.length > 0) {
          failures.push(`${config.modalTitle}弹窗缺少字段：${result.missingFields.join('、')}`);
        }
        return failures;
      },
    };
  });
}

/**
 * 创建“页面内配置表单字段完整性”的后台交互验证场景。
 *
 * @param {Array<{code: string, title: string, path: string, pageText: string, actionText?: string, actionTextMustRemain?: boolean, fields: string[]}>} configs 场景配置
 * @returns {Array<Record<string, unknown>>} CDP 验证场景
 */
function createInlineFormScenarios(configs) {
  return configs.map((config) => {
    const fieldsLiteral = JSON.stringify(config.fields);
    const actionTextLiteral = JSON.stringify(config.actionText || '');
    const actionReadyExpression = config.actionText
      ? ` && normalizeVerifyText(document.body?.innerText || '').includes(normalizeVerifyText(${actionTextLiteral}))`
      : '';
    const beforeVerifyExpression = config.actionText
      ? `(() => {
        const target = Array.from(document.querySelectorAll('button'))
          .find((button) => normalizeVerifyText(button.innerText) === normalizeVerifyText(${actionTextLiteral}));
        target?.click();
        return Boolean(target);
      })()`
      : undefined;
    return {
      code: config.code,
      title: config.title,
      path: config.path,
      authenticated: true,
      readyExpression: `(document.body?.innerText || '').includes(${JSON.stringify(config.pageText)})${actionReadyExpression}`,
      ...(beforeVerifyExpression ? { beforeVerifyExpression } : {}),
      verifyExpression: `(() => {
        const text = document.body.innerText;
        const normalizedText = normalizeVerifyText(text);
        const expectedFields = ${fieldsLiteral};
        return {
          hasPageText: text.includes(${JSON.stringify(config.pageText)}),
          hasActionText: ${config.actionText && config.actionTextMustRemain !== false ? `normalizeVerifyText(text).includes(normalizeVerifyText(${actionTextLiteral}))` : 'true'},
          missingFields: expectedFields.filter((field) => !normalizedText.includes(normalizeVerifyText(field))),
          text: text.slice(0, 900)
        };
      })()`,
      assert(result) {
        const failures = [];
        if (!result.hasPageText) {
          failures.push(`${config.title}页面关键标题缺失：${config.pageText}`);
        }
        if (!result.hasActionText) {
          failures.push(`${config.title}页面缺少操作入口：${config.actionText}`);
        }
        if (Array.isArray(result.missingFields) && result.missingFields.length > 0) {
          failures.push(`${config.title}页面缺少字段：${result.missingFields.join('、')}`);
        }
        return failures;
      },
    };
  });
}

const INTERACTION_CASES = [
  {
    code: 'auth-guard',
    title: '未登录业务页保护',
    path: '/dashboard',
    authenticated: false,
    readyExpression: `location.pathname === '/login' && Boolean(document.querySelector('.auth-card'))`,
    verifyExpression: `(() => ({
      redirectedToLogin: location.pathname === '/login',
      exposesShell: Boolean(document.querySelector('.platform-layout, .platform-menu, .platform-page-tabs')),
      hasLoginCard: Boolean(document.querySelector('.auth-card')),
      text: document.body.innerText.slice(0, 300)
    }))()`,
    assert(result) {
      const failures = [];
      if (!result.redirectedToLogin) {
        failures.push('未登录访问后台业务页必须跳转登录页');
      }
      if (result.exposesShell) {
        failures.push('未登录状态不能渲染后台菜单壳层');
      }
      if (!result.hasLoginCard) {
        failures.push('登录页主体未显示');
      }
      return failures;
    },
  },
  {
    code: 'shell-interface-settings',
    title: '后台壳层界面设置弹窗',
    path: '/dashboard',
    authenticated: true,
    readyExpression: `Boolean(document.querySelector('.platform-layout')) && (document.body?.innerText || '').includes('个人工作台')`,
    beforeVerifyExpression: `(() => {
      const buttons = Array.from(document.querySelectorAll('button'));
      const target = buttons.find((button) => button.innerText.includes('界面'));
      target?.click();
      return Boolean(target);
    })()`,
    verifyExpression: `(() => ({
      modalOpen: Boolean(document.querySelector('.ant-modal')),
      hasThemeColor: document.body.innerText.includes('主题颜色'),
      hasMenuTheme: document.body.innerText.includes('菜单主题'),
      hasMenuTabs: document.body.innerText.includes('多菜单标签页'),
      text: document.body.innerText.slice(0, 500)
    }))()`,
    assert(result) {
      const failures = [];
      if (!result.modalOpen) {
        failures.push('点击界面按钮后应打开系统界面设置弹窗');
      }
      if (!result.hasThemeColor || !result.hasMenuTheme || !result.hasMenuTabs) {
        failures.push('界面设置弹窗缺少主题颜色、菜单主题或多菜单标签页控件');
      }
      return failures;
    },
  },
  {
    code: 'shell-menu-search',
    title: '顶部菜单搜索候选',
    path: '/dashboard',
    authenticated: true,
    readyExpression: `Boolean(document.querySelector('input[placeholder="搜索菜单"]'))`,
    beforeVerifyExpression: `(() => {
      const input = document.querySelector('input[placeholder="搜索菜单"]');
      if (!input) {
        return false;
      }
      input.focus();
      input.value = 'AI';
      input.dispatchEvent(new Event('input', { bubbles: true }));
      input.dispatchEvent(new KeyboardEvent('keyup', { key: 'I', bubbles: true }));
      return true;
    })()`,
    verifyExpression: `(() => ({
      hasSearchInput: Boolean(document.querySelector('input[placeholder="搜索菜单"]')),
      hasAiMenu: document.body.innerText.includes('AI 能力中心') || document.body.innerText.includes('供应商'),
      hasOptionPanel: Boolean(document.querySelector('.ant-select-dropdown, .menu-search-option')),
      text: document.body.innerText.slice(0, 500)
    }))()`,
    assert(result) {
      const failures = [];
      if (!result.hasSearchInput) {
        failures.push('顶部搜索框不存在');
      }
      if (!result.hasAiMenu) {
        failures.push('输入 AI 后未出现可匹配的 AI 菜单信息');
      }
      return failures;
    },
  },
  {
    code: 'lowcode-model-actions',
    title: '低代码数据表建模关键操作入口',
    path: '/lowcode/model',
    authenticated: true,
    readyExpression: `(document.body?.innerText || '').includes('数据表建模') && (document.body?.innerText || '').includes('字段配置')`,
    verifyExpression: `(() => ({
      titleVisible: document.body.innerText.includes('数据表建模'),
      hasCreateModel: document.body.innerText.includes('新建模型'),
      hasSaveModel: document.body.innerText.includes('保存模型'),
      hasPublishModel: document.body.innerText.includes('发布模型'),
      hasLoadTables: document.body.innerText.includes('加载') || document.body.innerText.includes('业务表'),
      hasFieldGrid: document.body.innerText.includes('字段编码') && document.body.innerText.includes('字段名称'),
      text: document.body.innerText.slice(0, 800)
    }))()`,
    assert(result) {
      const failures = [];
      if (!result.titleVisible) {
        failures.push('数据表建模页面标题缺失');
      }
      if (!result.hasCreateModel || !result.hasSaveModel || !result.hasPublishModel) {
        failures.push('数据表建模缺少新建、保存或发布入口');
      }
      if (!result.hasLoadTables || !result.hasFieldGrid) {
        failures.push('数据源表结构加载或字段配置区域缺失');
      }
      return failures;
    },
  },
  {
    code: 'ai-provider-actions',
    title: 'AI 供应商关键操作入口',
    path: '/ai/providers',
    authenticated: true,
    readyExpression: `(document.body?.innerText || '').includes('供应商') && (document.body?.innerText || '').includes('密钥引用')`,
    verifyExpression: `(() => ({
      titleVisible: document.body.innerText.includes('供应商'),
      hasSaveProvider: document.body.innerText.includes('保存供应商'),
      hasTestProvider: document.body.innerText.includes('测试供应商'),
      hasSecretRef: document.body.innerText.includes('密钥引用'),
      hasBaseUrl: document.body.innerText.includes('基础地址'),
      text: document.body.innerText.slice(0, 800)
    }))()`,
    assert(result) {
      const failures = [];
      if (!result.titleVisible) {
        failures.push('AI 供应商页面标题缺失');
      }
      if (!result.hasSaveProvider || !result.hasTestProvider) {
        failures.push('AI 供应商缺少保存或测试入口');
      }
      if (!result.hasSecretRef || !result.hasBaseUrl) {
        failures.push('AI 供应商缺少密钥引用或基础地址字段');
      }
      return failures;
    },
  },
  {
    code: 'workflow-model-designer',
    title: '流程模型设计器关键入口',
    path: '/workflow/models',
    authenticated: true,
    readyExpression: `(document.body?.innerText || '').includes('流程模型')`,
    verifyExpression: `(() => ({
      titleVisible: document.body.innerText.includes('流程模型'),
      hasCreate: document.body.innerText.includes('新增模型') || document.body.innerText.includes('新建模型'),
      hasDeploy: document.body.innerText.includes('部署') || document.body.innerText.includes('发布'),
      hasFlowable: document.body.innerText.includes('Flowable') || document.body.innerText.includes('流程'),
      text: document.body.innerText.slice(0, 800)
    }))()`,
    assert(result) {
      const failures = [];
      if (!result.titleVisible) {
        failures.push('流程模型页面标题缺失');
      }
      if (!result.hasCreate) {
        failures.push('流程模型缺少新增入口');
      }
      if (!result.hasDeploy && !result.hasFlowable) {
        failures.push('流程模型缺少部署/发布或 Flowable 相关入口');
      }
      return failures;
    },
  },
  {
    code: 'system-user-create-form',
    title: '用户管理新增表单入口',
    path: '/system/users',
    authenticated: true,
    readyExpression: `(document.body?.innerText || '').includes('用户管理') && (document.body?.innerText || '').includes('新增用户')`,
    beforeVerifyExpression: `(() => {
      const target = Array.from(document.querySelectorAll('button'))
        .find((button) => button.innerText.trim() === '新增用户');
      target?.click();
      return Boolean(target);
    })()`,
    verifyExpression: `(() => {
      const text = document.body.innerText;
      return {
        modalOpen: Boolean(document.querySelector('.ant-modal')),
        hasCreateTitle: text.includes('新增用户'),
        hasUsername: text.includes('登录账号'),
        hasNickname: text.includes('用户名称'),
        hasPassword: text.includes('初始密码'),
        hasBindingsTab: text.includes('角色与岗位'),
        bindingsTabDisabled: Boolean(Array.from(document.querySelectorAll('.ant-tabs-tab'))
          .find((tab) => tab.innerText.includes('角色与岗位'))?.classList.contains('ant-tabs-tab-disabled')),
        text: text.slice(0, 800)
      };
    })()`,
    assert(result) {
      const failures = [];
      if (!result.modalOpen || !result.hasCreateTitle) {
        failures.push('点击新增用户后应打开新增用户弹窗');
      }
      if (!result.hasUsername || !result.hasNickname || !result.hasPassword) {
        failures.push('新增用户弹窗缺少登录账号、用户名称或初始密码字段');
      }
      if (!result.hasBindingsTab || !result.bindingsTabDisabled) {
        failures.push('新增用户时角色与岗位页签必须存在且保持禁用，避免未保存用户直接绑定关系');
      }
      return failures;
    },
  },
  {
    code: 'system-menu-create-form',
    title: '菜单权限新增表单入口',
    path: '/system/menus',
    authenticated: true,
    readyExpression: `(document.body?.innerText || '').includes('菜单权限') && (document.body?.innerText || '').includes('新增菜单')`,
    beforeVerifyExpression: `(() => {
      const target = Array.from(document.querySelectorAll('button'))
        .find((button) => button.innerText.trim() === '新增菜单');
      target?.click();
      return Boolean(target);
    })()`,
    verifyExpression: `(() => {
      const text = document.body.innerText;
      return {
        modalOpen: Boolean(document.querySelector('.ant-modal')),
        hasCreateTitle: text.includes('新增菜单权限'),
        hasMenuCode: text.includes('菜单编码'),
        hasMenuName: text.includes('菜单名称'),
        hasMenuType: text.includes('菜单类型'),
        hasPermission: text.includes('权限标识'),
        text: text.slice(0, 800)
      };
    })()`,
    assert(result) {
      const failures = [];
      if (!result.modalOpen || !result.hasCreateTitle) {
        failures.push('点击新增菜单后应打开新增菜单权限弹窗');
      }
      if (!result.hasMenuCode || !result.hasMenuName || !result.hasMenuType || !result.hasPermission) {
        failures.push('新增菜单权限弹窗缺少编码、名称、类型或权限标识字段');
      }
      return failures;
    },
  },
  {
    code: 'system-secret-create-form',
    title: '密钥管理新增表单入口',
    path: '/system/secrets',
    authenticated: true,
    readyExpression: `(document.body?.innerText || '').includes('密钥管理') && (document.body?.innerText || '').includes('新增密钥')`,
    beforeVerifyExpression: `(() => {
      const target = Array.from(document.querySelectorAll('button'))
        .find((button) => button.innerText.trim() === '新增密钥');
      target?.click();
      return Boolean(target);
    })()`,
    verifyExpression: `(() => {
      const text = document.body.innerText;
      return {
        modalOpen: Boolean(document.querySelector('.ant-modal')),
        hasCreateTitle: text.includes('新增密钥'),
        hasSecretCode: text.includes('密钥编码'),
        hasSecretName: text.includes('密钥名称'),
        hasSecretKind: text.includes('密钥类型'),
        hasPlaintext: text.includes('密钥明文'),
        text: text.slice(0, 800)
      };
    })()`,
    assert(result) {
      const failures = [];
      if (!result.modalOpen || !result.hasCreateTitle) {
        failures.push('点击新增密钥后应打开新增密钥弹窗');
      }
      if (!result.hasSecretCode || !result.hasSecretName || !result.hasSecretKind || !result.hasPlaintext) {
        failures.push('新增密钥弹窗缺少编码、名称、类型或明文字段');
      }
      return failures;
    },
  },
  {
    code: 'search-index-create-form',
    title: '全文检索新增索引表单入口',
    path: '/search/index-configs',
    authenticated: true,
    readyExpression: `(document.body?.innerText || '').includes('全文检索') && (document.body?.innerText || '').includes('新增索引')`,
    beforeVerifyExpression: `(() => {
      const target = Array.from(document.querySelectorAll('button'))
        .find((button) => button.innerText.trim() === '新增索引');
      target?.click();
      return Boolean(target);
    })()`,
    verifyExpression: `(() => {
      const text = document.body.innerText;
      return {
        modalOpen: Boolean(document.querySelector('.ant-modal')),
        hasTitle: text.includes('全文检索索引'),
        hasIndexCode: text.includes('索引编码'),
        hasSourceTable: text.includes('来源表'),
        hasSearchFields: text.includes('检索字段'),
        hasStatus: text.includes('索引状态'),
        text: text.slice(0, 800)
      };
    })()`,
    assert(result) {
      const failures = [];
      if (!result.modalOpen || !result.hasTitle) {
        failures.push('点击新增索引后应打开全文检索索引弹窗');
      }
      if (!result.hasIndexCode || !result.hasSourceTable || !result.hasSearchFields || !result.hasStatus) {
        failures.push('全文检索索引弹窗缺少索引编码、来源表、检索字段或状态字段');
      }
      return failures;
    },
  },
  ...createModalFormScenarios([
    {
      code: 'system-tenant-create-form',
      title: '租户管理新增表单入口',
      path: '/system/tenants',
      pageText: '租户管理',
      buttonText: '新增租户',
      modalTitle: '新增租户',
      fields: ['租户编码', '租户名称', '租户套餐', '隔离模式', '状态', '联系人', '联系电话', '到期时间'],
    },
    {
      code: 'system-org-create-form',
      title: '组织机构新增表单入口',
      path: '/system/orgs',
      pageText: '组织机构',
      buttonText: '新增组织',
      modalTitle: '新增组织',
      fields: ['父级组织', '组织编码', '组织名称', '负责人', '排序', '状态'],
    },
    {
      code: 'system-post-create-form',
      title: '岗位管理新增表单入口',
      path: '/system/posts',
      pageText: '岗位管理',
      buttonText: '新增岗位',
      modalTitle: '新增岗位',
      fields: ['所属组织', '岗位编码', '岗位名称', '排序', '状态'],
    },
    {
      code: 'system-role-create-form',
      title: '角色管理新增表单入口',
      path: '/system/roles',
      pageText: '角色管理',
      buttonText: '新增角色',
      modalTitle: '新增角色',
      fields: ['基础信息', '角色编码', '角色名称', '数据权限', '状态', '数据权限范围'],
    },
    {
      code: 'system-param-create-form',
      title: '系统参数新增表单入口',
      path: '/system/params',
      pageText: '系统参数',
      buttonText: '新增参数',
      modalTitle: '系统参数',
      fields: ['租户编码', '参数键', '参数值', '值类型', '参数属性', '系统内置', '允许编辑'],
    },
    {
      code: 'message-template-create-form',
      title: '消息模板新增表单入口',
      path: '/message/templates',
      pageText: '消息模板',
      buttonText: '新增模板',
      modalTitle: '消息模板',
      fields: ['模板编码', '模板名称', '消息通道', '标题模板', '内容模板', '模板状态'],
    },
    {
      code: 'cms-channel-create-form',
      title: '内容栏目新增表单入口',
      path: '/cms/channels',
      pageText: '内容栏目',
      buttonText: '新增栏目',
      modalTitle: '内容栏目',
      fields: ['栏目编码', '栏目名称', '父栏目 ID', '排序号', '栏目状态'],
    },
    {
      code: 'job-task-create-form',
      title: '在线作业新增表单入口',
      path: '/job/tasks',
      pageText: '在线作业任务',
      buttonText: '新增任务',
      modalTitle: '作业任务',
      fields: ['任务编码', '任务名称', '执行周期', 'Cron 表达式', '处理器名称', '任务说明', '任务状态'],
    },
    {
      code: 'system-tenant-package-create-form',
      title: '租户套餐新增表单入口',
      path: '/system/tenant-packages',
      pageText: '租户套餐',
      buttonText: '新增套餐',
      modalTitle: '新增套餐',
      fields: ['套餐编码', '套餐名称', '套餐状态', '最大用户数', '存储容量 MB'],
    },
    {
      code: 'system-tenant-param-create-form',
      title: '租户参数新增表单入口',
      path: '/system/tenant-params',
      pageText: '租户参数',
      buttonText: '新增参数',
      modalTitle: '租户参数',
      fields: ['租户编码', '参数键', '参数值', '值类型', '租户可见'],
    },
    {
      code: 'system-access-restriction-create-form',
      title: '访问限制新增表单入口',
      path: '/system/security-protection',
      pageText: '安全防护中心',
      buttonText: '新增规则',
      modalTitle: '访问限制规则',
      fields: ['租户编码', '限制类型', '规则值', '生效动作', '生效开始时间', '生效结束时间'],
    },
    {
      code: 'system-dict-create-form',
      title: '字典管理新增表单入口',
      path: '/system/dicts',
      pageText: '字典管理',
      buttonText: '新增字典编码',
      modalTitle: '新增字典',
      fields: ['字典编码', '字典名称', '系统内置', '状态'],
    },
    {
      code: 'system-code-rule-create-form',
      title: '编码规则新增表单入口',
      path: '/system/code-rules',
      pageText: '编码规则',
      buttonText: '新增规则',
      modalTitle: '保存编码规则',
      fields: ['规则编码', '规则名称', '编码前缀', '日期格式', '序列号长度', '当前序列值', '启用状态'],
    },
    {
      code: 'workflow-category-create-form',
      title: '流程分类新增表单入口',
      path: '/workflow/categories',
      pageText: '流程分类',
      buttonText: '新增分类',
      modalTitle: '流程分类',
      fields: ['分类编码', '分类名称', '排序号', '状态', '备注'],
    },
    {
      code: 'workflow-binding-create-form',
      title: '流程表单绑定新增表单入口',
      path: '/workflow/form-bindings',
      pageText: '表单绑定',
      buttonText: '新增绑定',
      modalTitle: '表单绑定',
      fields: ['流程定义 key', '业务模块', '业务表名', '后台表单路由', '移动端表单路由', '状态', '备注'],
    },
    {
      code: 'file-storage-create-form',
      title: '存储配置新增表单入口',
      path: '/file/storage-configs',
      pageText: '存储配置',
      buttonText: '新增配置',
      modalTitle: '存储配置',
      fields: ['配置编码', '配置名称', '存储类型', '端点或根路径', '配置状态', '默认配置'],
    },
    {
      code: 'file-object-register-form',
      title: '文件对象登记表单入口',
      path: '/file/objects',
      pageText: '文件对象',
      buttonText: '登记文件',
      modalTitle: '登记文件',
      fields: ['存储配置编码', '原始文件名', '内容类型', '文件大小（字节）', '对象键或相对路径'],
    },
    {
      code: 'cms-content-create-form',
      title: '内容文章新增表单入口',
      path: '/cms/contents',
      pageText: '内容文章',
      buttonText: '新增文章',
      modalTitle: '内容文章',
      fields: ['栏目编码', '文章标题', '文章摘要', '文章正文', '文章状态'],
    },
    {
      code: 'i18n-message-create-form',
      title: '国际化词条新增表单入口',
      path: '/i18n/messages',
      pageText: '国际化词条',
      buttonText: '新增词条',
      modalTitle: '国际化词条',
      fields: ['语言标识', '词条键', '词条值', '词条状态'],
    },
    {
      code: 'message-inbox-send-form',
      title: '站内消息发送表单入口',
      path: '/message/inbox',
      pageText: '站内消息',
      buttonText: '发送消息',
      modalTitle: '发送站内消息',
      fields: ['接收人 ID', '接收人名称', '消息类型', '消息标题', '消息内容'],
    },
    {
      code: 'visual-dataset-create-form',
      title: '报表数据集新增表单入口',
      path: '/visual/datasets',
      pageText: '报表数据集',
      buttonText: '新增数据集',
      modalTitle: '报表数据集',
      fields: ['数据集编码', '数据集名称', '数据源编码', '查询 SQL', '状态'],
    },
    {
      code: 'visual-report-create-form',
      title: '报表设计器新增表单入口',
      path: '/visual/reports',
      pageText: '报表设计器',
      buttonText: '新增报表',
      modalTitle: '在线报表设计器',
      fields: ['基础信息', '拖拽布局', '数据绑定', '预览发布', '报表编码', '报表名称', '组件库', '设计画布', '属性面板'],
    },
    {
      code: 'visual-screen-create-form',
      title: '可视化大屏新增表单入口',
      path: '/visual/screens',
      pageText: '可视化数据大屏',
      buttonText: '新增大屏',
      modalTitle: '在线可视化大屏设计器',
      fields: ['基础信息', '组件拖拽', '数据绑定', '预览发布', '大屏编码', '大屏名称', '组件库', '大屏画布', '属性面板'],
    },
  ]),
  ...createInlineFormScenarios([
    {
      code: 'system-tenant-package-module-inline-form',
      title: '套餐授权绑定配置表单',
      path: '/system/tenant-package-modules',
      pageText: '套餐授权',
      actionText: '绑定授权',
      actionTextMustRemain: false,
      fields: ['当前套餐', '授权项', '模块', '授权资源', '绑定套餐授权', '租户套餐'],
    },
    {
      code: 'system-password-policy-inline-form',
      title: '密码策略配置表单',
      path: '/system/password-policies',
      pageText: '密码策略',
      fields: ['策略编码', '策略名称', '最小长度', '有效天数', '历史记忆次数', '最大失败次数', '锁定分钟数', '启用策略', '复杂度要求'],
    },
    {
      code: 'system-module-inline-actions',
      title: '模块管理列表操作入口',
      path: '/system/modules',
      pageText: '模块管理',
      fields: ['模块名称', '模块编码', '版本', '类型', '状态', '依赖模块', '资源数', '操作'],
    },
    {
      code: 'system-admin-scope-inline-form',
      title: '管理员范围配置入口',
      path: '/system/admin-scopes',
      pageText: '管理员范围配置',
      fields: ['当前租户', '刷新用户', '已配置范围', '租户用户', '当前配置用户', '租户用户列表', '配置范围'],
    },
    {
      code: 'lowcode-datasource-inline-form',
      title: '低代码数据源配置表单',
      path: '/lowcode/datasource',
      pageText: '数据源配置',
      actionText: '新建数据源',
      fields: ['数据源编码', '数据源名称', '数据库方言', '用户名', 'JDBC 地址', '口令密钥引用', '启用状态', '连接测试', '保存数据源'],
    },
    {
      code: 'lowcode-relation-inline-form',
      title: '低代码表关系配置表单',
      path: '/lowcode/relations',
      pageText: '表关系配置',
      fields: ['主表', '子表', '关系类型', '主表关联字段', '子表引用字段', '保存关系', '表关系列表'],
    },
    {
      code: 'lowcode-page-model-inline-form',
      title: '低代码页面模型配置表单',
      path: '/lowcode/pages',
      pageText: '页面模型',
      fields: ['保存页面模型', '列表页', '表单页', '详情页', '移动列表', '移动表单', '移动详情', '字段编码', '字段名称', '绑定字典'],
    },
    {
      code: 'lowcode-generator-inline-form',
      title: '低代码代码生成配置表单',
      path: '/lowcode/generator',
      pageText: '代码生成',
      fields: ['表模型编码', '生成目标端', '业务模块名', '业务实体名', '覆盖策略', '刷新模板', '生成校验', '生成预览', '执行生成'],
    },
    {
      code: 'lowcode-template-list-inline-form',
      title: '低代码生成模板清单',
      path: '/lowcode/generator',
      pageText: '模板清单',
      fields: ['代码生成', '模板编码', '模板名称', '目标端', '输出路径模式', '刷新模板'],
    },
    {
      code: 'lowcode-generation-record-inline-form',
      title: '低代码生成记录查询入口',
      path: '/lowcode/generator',
      pageText: '生成历史',
      fields: ['生成历史', '刷新历史', '记录 ID', '表模型', '目标端', '文件清单', '生成文件明细'],
    },
    {
      code: 'ai-model-inline-form',
      title: 'AI 模型配置表单',
      path: '/ai/models',
      pageText: '模型配置',
      actionText: '新建',
      fields: ['供应商', '模型编码', '模型名称', '模型类型', '上下文长度', '流式输出', '工具调用', '状态', '保存模型'],
    },
    {
      code: 'ai-app-inline-form',
      title: 'AI 应用接入配置表单',
      path: '/ai/apps',
      pageText: '应用接入',
      actionText: '新建',
      fields: ['应用编码', '应用名称', '默认模型', '每日令牌额度', '系统提示词', '状态', '保存应用', '测试调用', '变量 JSON'],
    },
    {
      code: 'ai-prompt-inline-form',
      title: 'AI 提示词配置表单',
      path: '/ai/prompts',
      pageText: '提示词',
      actionText: '新建',
      fields: ['提示词编码', '提示词名称', '版本', '变量清单', '模板内容', '状态', '保存提示词'],
    },
    {
      code: 'ai-audit-inline-list',
      title: 'AI 调用审计查询入口',
      path: '/ai/invocation-audits',
      pageText: '调用审计',
      fields: ['查询审计', 'Trace ID', '应用', '类型', '总令牌', '耗时', '状态'],
    },
    {
      code: 'openapi-api-key-inline-form',
      title: '开放 API Key 配置表单',
      path: '/openapi/api-keys',
      pageText: '密钥配置',
      actionText: '新建密钥',
      fields: ['应用编码', 'Access Key', 'Secret 密文', '状态', '过期时间', '轮换 Secret', '保存密钥'],
    },
    {
      code: 'openapi-catalog-inline-form',
      title: '开放 API 目录配置表单',
      path: '/openapi/catalogs',
      pageText: '目录配置',
      actionText: '新建 API',
      fields: ['API 编码', 'API 名称', '分组编码', 'HTTP 方法', '路径规则', '目录状态', '保存 API'],
    },
    {
      code: 'openapi-version-inline-form',
      title: '开放 API 版本配置表单',
      path: '/openapi/versions',
      pageText: '版本配置',
      actionText: '新建版本',
      fields: ['API 编码', '版本号', '后端路由', '请求 Schema', '响应 Schema', '发布状态', '发布版本'],
    },
    {
      code: 'openapi-api-permission-inline-form',
      title: '开放 API 授权配置表单',
      path: '/openapi/api-permissions',
      pageText: '授权配置',
      actionText: '新建授权',
      fields: ['应用编码', 'API 编码', 'API 名称', 'HTTP 方法', '路径规则', '授权状态', '保存授权'],
    },
    {
      code: 'openapi-oauth-client-inline-form',
      title: '开放 API OAuth2 客户端映射表单',
      path: '/openapi/oauth-clients',
      pageText: '客户端映射',
      actionText: '新建映射',
      fields: ['应用编码', '客户端 ID', '授权范围', '映射状态', '保存映射'],
    },
    {
      code: 'openapi-signature-policy-inline-form',
      title: '开放 API 签名策略配置表单',
      path: '/openapi/signature-policies',
      pageText: '策略配置',
      actionText: '重置',
      fields: ['应用编码', '签名算法', '时间戳窗口(秒)', 'nonce 有效期(秒)', '请求体摘要', '策略状态', '保存策略'],
    },
    {
      code: 'openapi-rate-limit-policy-inline-form',
      title: '开放 API 限流策略配置表单',
      path: '/openapi/rate-limit-policies',
      pageText: '策略配置',
      actionText: '新建策略',
      fields: ['应用编码', 'API 编码', '调用次数', '窗口秒数', '策略状态', '保存策略'],
    },
    {
      code: 'developer-portal-debug-inline-form',
      title: '开发者门户调试控制台',
      path: '/developer/portal',
      pageText: '调试控制台',
      fields: ['开发者首页', 'API Key 凭证', 'OAuth2 客户端', 'API 文档', '请求方法', '请求地址', '应用编码', '认证方式', '请求体', '生成调试快照', '调用调试代理'],
    },
  ]),
];

/**
 * 读取 JSON 接口。
 *
 * @param {string} url 请求地址
 * @returns {Promise<unknown>} JSON 数据
 */
function readJson(url) {
  return new Promise((resolvePromise, rejectPromise) => {
    http
      .get(url, (response) => {
        let payload = '';
        response.on('data', (chunk) => {
          payload += chunk;
        });
        response.on('end', () => {
          try {
            resolvePromise(JSON.parse(payload));
          } catch (error) {
            rejectPromise(error);
          }
        });
      })
      .on('error', rejectPromise);
  });
}

/**
 * 连接 Chrome DevTools Protocol。
 *
 * @param {string} webSocketDebuggerUrl CDP WebSocket 地址
 * @returns {Promise<{send: Function, onEvent: Function, close: Function}>} CDP 客户端
 */
async function connectCdp(webSocketDebuggerUrl) {
  const socket = new WebSocket(webSocketDebuggerUrl);
  const pendingRequests = new Map();
  const eventListeners = new Set();

  socket.onmessage = (event) => {
    const message = JSON.parse(event.data);
    if (message.id && pendingRequests.has(message.id)) {
      const { resolve: resolvePromise, reject: rejectPromise } = pendingRequests.get(message.id);
      pendingRequests.delete(message.id);
      if (message.error) {
        rejectPromise(new Error(JSON.stringify(message.error)));
        return;
      }
      resolvePromise(message.result);
      return;
    }
    eventListeners.forEach((listener) => listener(message));
  };

  await new Promise((resolvePromise, rejectPromise) => {
    socket.onopen = resolvePromise;
    socket.onerror = rejectPromise;
  });

  return {
    send(method, params = {}, sessionId) {
      const id = cdpSequence++;
      const message = sessionId ? { id, method, params, sessionId } : { id, method, params };
      socket.send(JSON.stringify(message));
      return new Promise((resolvePromise, rejectPromise) => {
        const timeout = setTimeout(() => {
          pendingRequests.delete(id);
          rejectPromise(new Error(`CDP 请求超时：${method}`));
        }, CDP_REQUEST_TIMEOUT_MS);
        pendingRequests.set(id, {
          resolve(result) {
            clearTimeout(timeout);
            resolvePromise(result);
          },
          reject(error) {
            clearTimeout(timeout);
            rejectPromise(error);
          },
        });
      });
    },
    onEvent(listener) {
      eventListeners.add(listener);
      return () => eventListeners.delete(listener);
    },
    close() {
      socket.close();
    },
  };
}

/**
 * 等待指定时长。
 *
 * @param {number} milliseconds 等待毫秒数
 * @returns {Promise<void>} 等待完成
 */
function wait(milliseconds) {
  return new Promise((resolvePromise) => {
    setTimeout(resolvePromise, milliseconds);
  });
}

/**
 * 验证一个后台交互场景。
 *
 * @param {ReturnType<typeof connectCdp>} rootClient 根 CDP 客户端
 * @param {Record<string, unknown> | null} authContext 后台登录上下文
 * @param {typeof INTERACTION_CASES[number]} scenario 验证场景
 * @returns {Promise<Record<string, unknown>>} 验证结果
 */
async function verifyInteraction(rootClient, authContext, scenario) {
  const target = await rootClient.send('Target.createTarget', {
    url: new URL('/login', ADMIN_BASE_URL).toString(),
  });
  const attached = await rootClient.send('Target.attachToTarget', {
    targetId: target.targetId,
    flatten: true,
  });
  const sessionId = attached.sessionId;
  const send = (method, params = {}) => rootClient.send(method, params, sessionId);
  const runtimeErrors = [];
  const unsubscribe = rootClient.onEvent((message) => {
    if (message.sessionId !== sessionId) {
      return;
    }
    if (message.method === 'Runtime.exceptionThrown') {
      runtimeErrors.push(message.params?.exceptionDetails?.text || 'Runtime.exceptionThrown');
    }
    if (message.method === 'Log.entryAdded' && message.params?.entry?.level === 'error') {
      const errorText = message.params.entry.text || 'Log.entryAdded';
      if (!isIgnorableBrowserLog(errorText)) {
        runtimeErrors.push(errorText);
      }
    }
  });

  try {
    await preparePage(send);
    await send('Page.navigate', { url: new URL('/', ADMIN_BASE_URL).toString() });
    await wait(600);
    await waitForExpression(send, `document.readyState !== 'loading'`);
    if (scenario.authenticated) {
      await writeAdminContext(send, authContext);
    } else {
      await clearAdminContext(send);
    }
    await send('Page.navigate', { url: new URL(scenario.path, ADMIN_BASE_URL).toString() });
    await wait(600);
    await waitForExpression(send, `document.readyState === 'complete' || document.readyState === 'interactive'`);
    await waitForScenarioReady(send, scenario);
    if (scenario.beforeVerifyExpression) {
      await send('Runtime.evaluate', {
        awaitPromise: true,
        returnByValue: true,
        expression: scenario.beforeVerifyExpression,
      });
      await wait(800);
    }
    const evaluation = await send('Runtime.evaluate', {
      returnByValue: true,
      expression: scenario.verifyExpression,
    });
    const details = evaluation.result.value ?? {};
    const failures = scenario.assert(details);
    const blockingRuntimeErrors = runtimeErrors.filter((errorText) => !isIgnorableRuntimeError(errorText));
    if (blockingRuntimeErrors.length > 0) {
      failures.push(`运行时异常：${blockingRuntimeErrors.slice(0, 2).join('；')}`);
    }
    const result = {
      code: scenario.code,
      title: scenario.title,
      path: scenario.path,
      passed: failures.length === 0,
      failures,
      details,
    };
    if (!result.passed) {
      result.screenshot = await capturePageScreenshot(send, scenario.code);
    }
    return result;
  } finally {
    unsubscribe();
    try {
      await rootClient.send('Target.closeTarget', { targetId: target.targetId });
    } catch (error) {
      console.warn(`关闭 CDP 页面失败：${error.message}`);
    }
  }
}

/**
 * 判断浏览器日志是否为验证无关噪声。
 *
 * @param {string} errorText 浏览器错误日志
 * @returns {boolean} 无需阻断验证时返回 true
 */
function isIgnorableBrowserLog(errorText) {
  return /Failed to load resource/i.test(errorText) && /(404|500|net::ERR_ABORTED|favicon)/i.test(errorText);
}

/**
 * 判断运行时异常是否为当前入口验证可忽略的接口加载噪声。
 *
 * @param {string} errorText 运行时异常文本
 * @returns {boolean} 无需阻断验证时返回 true
 */
function isIgnorableRuntimeError(errorText) {
  return /Uncaught \(in promise\)/i.test(errorText);
}

/**
 * 准备页面 CDP 能力和桌面视口。
 *
 * @param {Function} send 当前 CDP 会话发送函数
 * @returns {Promise<void>} 准备完成
 */
async function preparePage(send) {
  await send('Page.enable');
  await send('Runtime.enable');
  await send('Log.enable');
  await send('Emulation.setDeviceMetricsOverride', {
    width: 1440,
    height: 900,
    deviceScaleFactor: 1,
    mobile: false,
    screenWidth: 1440,
    screenHeight: 900,
  });
  await send('Page.addScriptToEvaluateOnNewDocument', {
    source: `
      globalThis.normalizeVerifyText = function normalizeVerifyText(value) {
        return String(value || '').replace(/\\s+/g, '');
      };
    `,
  });
  await wait(500);
}

/**
 * 等待表达式在页面上下文中成立。
 *
 * @param {Function} send 当前 CDP 会话发送函数
 * @param {string} expression 页面表达式
 * @param {number} timeoutMs 最大等待毫秒数
 * @returns {Promise<boolean>} 表达式是否成立
 */
async function waitForExpression(send, expression, timeoutMs = 8_000) {
  const startedAt = Date.now();
  while (Date.now() - startedAt <= timeoutMs) {
    const evaluation = await send('Runtime.evaluate', {
      awaitPromise: true,
      returnByValue: true,
      expression: `(() => Boolean(${expression}))()`,
    });
    if (evaluation.result.value) {
      return true;
    }
    await wait(200);
  }
  return false;
}

/**
 * 等待当前验证场景关键内容渲染。
 *
 * @param {Function} send 当前 CDP 会话发送函数
 * @param {typeof INTERACTION_CASES[number]} scenario 验证场景
 * @returns {Promise<void>} 等待完成
 */
async function waitForScenarioReady(send, scenario) {
  if (scenario.readyExpression) {
    await waitForExpression(send, scenario.readyExpression);
    return;
  }
  if (!scenario.authenticated) {
    await waitForExpression(
      send,
      `location.pathname === '/login' && ((document.body?.innerText || '').includes('统一认证登录') || Boolean(document.querySelector('input')))`,
    );
    return;
  }
  await waitForExpression(
    send,
    `Boolean(document.querySelector('.platform-layout, .platform-menu, .platform-page-tabs')) && (document.body?.innerText || '').trim().length > 0`,
  );
}

/**
 * 清理后台登录上下文。
 *
 * @param {Function} send 当前 CDP 会话发送函数
 * @returns {Promise<void>} 清理完成
 */
async function clearAdminContext(send) {
  await send('Runtime.evaluate', {
    awaitPromise: true,
    returnByValue: true,
    expression: `(() => {
      localStorage.removeItem(${JSON.stringify(ADMIN_CONTEXT_STORAGE_KEY)});
      return true;
    })()`,
  });
}

/**
 * 写入后台登录上下文。
 *
 * @param {Function} send 当前 CDP 会话发送函数
 * @param {Record<string, unknown>} authContext 后台登录上下文
 * @returns {Promise<void>} 写入完成
 */
async function writeAdminContext(send, authContext) {
  await send('Runtime.evaluate', {
    awaitPromise: true,
    returnByValue: true,
    expression: `(() => {
      localStorage.setItem(${JSON.stringify(ADMIN_CONTEXT_STORAGE_KEY)}, ${JSON.stringify(JSON.stringify(authContext))});
      return true;
    })()`,
  });
}

/**
 * 捕获失败页面截图。
 *
 * @param {Function} send 当前 CDP 会话发送函数
 * @param {string} scenarioCode 场景编码
 * @returns {Promise<string>} 截图路径
 */
async function capturePageScreenshot(send, scenarioCode) {
  const screenshot = await send('Page.captureScreenshot', {
    format: 'png',
    fromSurface: true,
  });
  const file = `${SCREENSHOT_DIR}/zhyc-admin-interaction-${scenarioCode}.png`;
  fs.writeFileSync(file, Buffer.from(screenshot.data, 'base64'));
  return file;
}

/**
 * 调用认证中心登录接口初始化后台验证上下文。
 *
 * @returns {Promise<Record<string, unknown>>} 后台登录上下文
 */
async function seedAuthenticatedContext() {
  if (!VERIFY_USERNAME || !VERIFY_PASSWORD) {
    throw new Error('请提供 ZHYC_ADMIN_VERIFY_USERNAME / ZHYC_ADMIN_VERIFY_PASSWORD 后再执行后台交互验证');
  }
  const response = await fetch(AUTH_LOGIN_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      username: VERIFY_USERNAME,
      password: VERIFY_PASSWORD,
    }),
  });
  if (!response.ok) {
    throw new Error(`后台验证登录接口 HTTP 状态码：${response.status}`);
  }
  const result = await response.json();
  const loginResult = result?.data?.accessToken || result?.data?.access_token ? result.data : result?.data?.data;
  const accessToken = loginResult?.accessToken || loginResult?.access_token || loginResult?.token;
  if (!result?.success || !accessToken) {
    throw new Error(result?.message || result?.code || '后台验证登录接口未返回访问令牌');
  }
  const claims = parseJwtClaims(accessToken);
  const expiresIn = Number(loginResult.expiresIn || loginResult.expires_in || claims.exp || 0);
  const tenantId = String(loginResult.tenantId || loginResult.tenant_id || claims.tenantId || claims.tenant_id || VERIFY_TENANT_ID);
  const userId = Number(loginResult.userId || loginResult.user_id || claims.userId || claims.user_id || claims.sub || VERIFY_USER_ID);
  const accountName = String(
    loginResult.accountName || loginResult.account_name || claims.accountName || claims.preferred_username || VERIFY_ACCOUNT_NAME,
  );
  const adminContext = {
    tenantId,
    userId,
    orgId: null,
    accountName,
    accessToken,
    accessTokenExpiresAt: calculateExpiresAt(expiresIn),
  };
  if (!adminContext.userId || !adminContext.tenantId || !adminContext.accountName) {
    throw new Error('后台交互验证登录接口响应缺少用户、租户或账号上下文');
  }
  return adminContext;
}

/**
 * 解析 JWT 载荷。
 *
 * @param {string} accessToken 访问令牌
 * @returns {Record<string, unknown>} JWT 声明
 */
function parseJwtClaims(accessToken) {
  try {
    const payload = accessToken.split('.')[1];
    return JSON.parse(Buffer.from(payload.replace(/-/g, '+').replace(/_/g, '/'), 'base64').toString('utf8'));
  } catch {
    return {};
  }
}

/**
 * 计算访问令牌过期时间。
 *
 * @param {number} expiresIn 过期秒数或 JWT exp
 * @returns {number | null} 过期时间戳
 */
function calculateExpiresAt(expiresIn) {
  if (!Number.isFinite(expiresIn) || expiresIn <= 0) {
    return null;
  }
  return expiresIn > 4_000_000_000 ? expiresIn * 1000 : Date.now() + expiresIn * 1000;
}

/**
 * 主流程。
 */
async function main() {
  const browserVersion = await readJson(CDP_VERSION_URL);
  const rootClient = await connectCdp(browserVersion.webSocketDebuggerUrl);
  const results = [];
  try {
    const authContext = await seedAuthenticatedContext();
    console.log(`后台交互验证上下文初始化完成：${authContext.accountName} / ${authContext.tenantId}`);
    for (const scenario of INTERACTION_CASES) {
      console.log(`验证后台交互：${scenario.title}`);
      results.push(await verifyInteraction(rootClient, authContext, scenario));
    }
  } finally {
    rootClient.close();
  }

  const failedResults = results.filter((item) => !item.passed);
  console.log(JSON.stringify({
    total: results.length,
    passed: results.length - failedResults.length,
    failed: failedResults.length,
    failures: failedResults.map((item) => ({
      code: item.code,
      title: item.title,
      failures: item.failures,
      screenshot: item.screenshot,
      details: item.details,
    })),
  }, null, 2));
  if (failedResults.length > 0) {
    process.exit(1);
  }
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
