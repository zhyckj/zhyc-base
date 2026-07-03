<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <a-config-provider :locale="zhCN" :theme="antDesignTheme">
    <router-view v-if="isStandalonePage" />
    <div v-else-if="!shouldRenderPlatformShell" class="platform-auth-redirect" aria-hidden="true"></div>
    <a-layout v-else class="platform-layout" :style="platformInterfaceCssVars">
      <a-layout-sider
        :class="['platform-sider', `platform-sider-${interfaceSettings.menuTheme}`]"
        :width="224"
        breakpoint="lg"
        collapsed-width="0"
      >
        <div class="platform-logo">ZHYC 快速开发平台</div>
        <a-menu
          class="platform-menu"
          :theme="interfaceSettings.menuTheme"
          mode="inline"
          :selected-keys="[route.path]"
          :open-keys="openMenuKeys"
          @click="handleMenuClick"
          @openChange="handleMenuOpenChange"
        >
          <template v-for="entry in navigationEntries" :key="entry.key">
            <a-menu-item v-if="entry.kind === 'item'" :key="entry.path">
              <span>{{ entry.title }}</span>
            </a-menu-item>
            <a-sub-menu v-else :key="entry.key">
              <template #title>
                <span>{{ entry.title }}</span>
              </template>
              <a-menu-item v-for="item in entry.items" :key="item.path">
                <span>{{ item.title }}</span>
              </a-menu-item>
            </a-sub-menu>
          </template>
        </a-menu>
      </a-layout-sider>
        <a-layout>
        <a-layout-header class="platform-header">
          <div class="platform-header-main">
            <div class="platform-title-row">
              <span class="platform-title">{{ currentTitle }}</span>
              <a-tag :color="isLoggedIn ? 'green' : 'orange'" class="platform-auth-tag">
                {{ isLoggedIn ? '已认证' : '未认证' }}
              </a-tag>
            </div>
            <div class="platform-header-context">
              <span class="platform-context-item">
                <ApartmentOutlined />
                {{ currentTenantText }}
              </span>
              <span class="platform-context-item">
                <BranchesOutlined />
                组织 {{ currentOrgText }}
              </span>
            </div>
          </div>
          <div class="platform-actions">
            <a-auto-complete
              v-model:value="menuSearchKeyword"
              class="platform-menu-search"
              :options="menuSearchOptions"
              :filter-option="filterMenuSearchOption"
              @select="handleMenuSearchSelect"
              @keyup.enter="handleMenuSearchEnter"
            >
              <a-input class="platform-menu-search-input" size="middle" allow-clear placeholder="搜索菜单">
                <template #prefix>
                  <SearchOutlined class="platform-menu-search-icon" />
                </template>
              </a-input>
              <template #option="{ label, groupTitle, path }">
                <div class="menu-search-option">
                  <span>{{ label }}</span>
                  <small>{{ groupTitle }} · {{ path }}</small>
                </div>
              </template>
            </a-auto-complete>
            <span class="platform-account-pill">
              <UserOutlined />
              {{ currentAccountText }}
            </span>
            <a-button size="small" type="primary" @click="goToLogin">
              <template #icon><LoginOutlined /></template>
              统一认证
            </a-button>
            <a-button size="small" danger aria-label="退出登录" @click="logoutAdmin">
              <template #icon><LogoutOutlined /></template>
              退出
            </a-button>
            <a-button size="small" @click="openInterfaceSettingsModal">
              <template #icon><BgColorsOutlined /></template>
              界面
            </a-button>
          </div>
        </a-layout-header>
        <div v-if="interfaceSettings.enableMenuTabs" class="platform-page-tabs">
          <div class="platform-page-tabs-track">
            <a-tabs
              type="editable-card"
              size="small"
              hide-add
              :active-key="activeMenuTabKey"
              @change="handlePageTabChange"
              @edit="handlePageTabEdit"
            >
              <a-tab-pane
                v-for="tab in openedMenuTabs"
                :key="tab.path"
                :closable="openedMenuTabs.length > 1"
              >
                <template #tab>
                  <span class="platform-page-tab-title" :title="tab.title">{{ tab.title }}</span>
                </template>
              </a-tab-pane>
            </a-tabs>
          </div>
        </div>
        <a-layout-content class="platform-content">
          <router-view />
        </a-layout-content>
      </a-layout>
    </a-layout>
    <a-modal
      v-if="shouldRenderPlatformShell"
      v-model:open="interfaceSettingsOpen"
      title="系统界面设置"
      ok-text="保存"
      cancel-text="取消"
      @ok="saveInterfaceSettings"
    >
      <a-form layout="vertical">
        <a-form-item label="主题颜色">
          <div class="style-color-control">
            <input v-model="interfaceSettingsForm.primaryColor" class="style-color-picker" type="color" />
            <a-input v-model:value="interfaceSettingsForm.primaryColor" placeholder="#2f7af7" />
          </div>
        </a-form-item>
        <a-form-item label="菜单主题">
          <a-segmented v-model:value="interfaceSettingsForm.menuTheme" :options="menuThemeOptions" block />
        </a-form-item>
        <a-form-item label="菜单自动收缩">
          <a-switch v-model:checked="interfaceSettingsForm.autoCollapse" checked-children="开启" un-checked-children="关闭" />
        </a-form-item>
        <a-form-item label="多菜单标签页">
          <a-switch v-model:checked="interfaceSettingsForm.enableMenuTabs" checked-children="开启" un-checked-children="关闭" />
        </a-form-item>
      </a-form>
    </a-modal>
  </a-config-provider>
</template>

<script setup lang="ts">
import {
  ApartmentOutlined,
  BgColorsOutlined,
  BranchesOutlined,
  LoginOutlined,
  LogoutOutlined,
  SearchOutlined,
  UserOutlined,
} from '@ant-design/icons-vue';
import zhCN from 'ant-design-vue/es/locale/zh_CN';
import type { MenuInfo } from 'ant-design-vue/es/menu/src/interface';
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { adminRoutes } from './router/routes';
import {
  clearAdminRuntimeContext,
  getAdminRuntimeContext,
  hasAuthenticatedAdminContext,
  subscribeAdminRuntimeContextChange,
  type AdminRuntimeContext,
} from './utils/adminContext';
import { buildAdminOAuthLogoutConfig, submitAdminOAuthLogout } from './utils/adminOAuth';
import { refreshAdminPermissions } from './utils/permission';

/** 菜单主题类型。 */
type PlatformMenuTheme = 'light' | 'dark';

/**
 * 后台系统界面设置。
 */
interface PlatformInterfaceSettings {
  /** 主题主色，用于按钮、菜单选中态等全局组件 token。 */
  primaryColor: string;
  /** 左侧菜单主题。 */
  menuTheme: PlatformMenuTheme;
  /** 是否自动收缩其他已展开菜单分组。 */
  autoCollapse: boolean;
  /** 是否启用多菜单标签页。 */
  enableMenuTabs: boolean;
}

/**
 * 后台菜单导航项。
 */
interface NavigationItem {
  /** 路由访问路径，对应菜单项选中值。 */
  path: string;
  /** 菜单展示名称。 */
  title: string;
  /** 页面访问权限编码，用于和后端 Shiro 菜单权限保持一致。 */
  permission: string;
}

/**
 * 后台一级菜单目录。
 */
interface NavigationDirectory {
  /** 菜单节点类型。 */
  kind: 'directory';
  /** 目录唯一编码。 */
  key: string;
  /** 目录展示名称。 */
  title: string;
  /** 目录下的页面入口。 */
  items: NavigationItem[];
}

/**
 * 后台一级直达菜单。
 */
interface NavigationDirectItem extends NavigationItem {
  /** 菜单节点类型。 */
  kind: 'item';
  /** 直达菜单唯一编码。 */
  key: string;
}

/** 后台一级菜单节点。 */
type NavigationEntry = NavigationDirectory | NavigationDirectItem;

/**
 * 菜单搜索候选项。
 */
interface MenuSearchOption {
  /** AutoComplete 选中值，使用路由路径直接跳转。 */
  value: string;
  /** 页面名称。 */
  label: string;
  /** 路由路径。 */
  path: string;
  /** 所属一级菜单。 */
  groupTitle: string;
  /** 搜索关键字集合。 */
  keywords: string;
}

/**
 * 已打开菜单标签页。
 */
interface OpenedMenuTab {
  /** 菜单路由路径，用于去重和跳转。 */
  path: string;
  /** 菜单展示名称。 */
  title: string;
}

/** 后台界面设置本地存储键。 */
const ZHYC_ADMIN_INTERFACE_STORAGE_KEY = 'zhyc-admin-interface-settings';

/** 旧版后台界面配置本地存储键，用于平滑迁移历史配置。 */
const ZHYC_ADMIN_LEGACY_STYLE_STORAGE_KEY = 'zhyc-admin-style-settings';

/** 默认后台界面设置。 */
const DEFAULT_PLATFORM_INTERFACE_SETTINGS: PlatformInterfaceSettings = {
  primaryColor: '#2f7af7',
  menuTheme: 'light',
  autoCollapse: true,
  enableMenuTabs: true,
};

/** 菜单主题选项。 */
const menuThemeOptions = [
  { label: '浅色菜单', value: 'light' },
  { label: '深色菜单', value: 'dark' },
];

/**
 * 菜单分组定义。
 *
 * <p>侧边栏顶层名称和顺序对齐后端 `sys_menu` 种子数据，避免菜单权限页和运行时导航显示不一致。</p>
 */
const menuGroupDefinitions = [
  {
    kind: 'item',
    key: 'dashboard',
    title: '个人工作台',
    path: '/dashboard',
  },
  {
    kind: 'directory',
    key: 'system',
    title: '系统管理',
    paths: [
      '/system/tenants',
      '/system/tenant-packages',
      '/system/tenant-package-modules',
      '/system/tenant-params',
      '/system/admin-scopes',
      '/system/orgs',
      '/system/posts',
      '/system/users',
      '/system/roles',
      '/system/menus',
      '/system/permission-audits',
      '/system/security-protection',
      '/system/password-policies',
      '/system/modules',
      '/system/code-rules',
      '/system/params',
      '/system/dicts',
      '/system/audit-logs',
      '/system/login-logs',
      '/system/exception-logs',
      '/system/secrets',
    ],
  },
  {
    kind: 'directory',
    key: 'lowcode',
    title: '低代码中心',
    paths: ['/lowcode/datasource', '/lowcode/model', '/lowcode/relations', '/lowcode/pages', '/lowcode/generator'],
  },
  {
    kind: 'directory',
    key: 'ai',
    title: 'AI 能力中心',
    paths: ['/ai/providers', '/ai/models', '/ai/apps', '/ai/prompts', '/ai/invocation-audits'],
  },
  {
    kind: 'directory',
    key: 'workflow',
    title: '工作流中心',
    paths: ['/workflow/tasks/todo', '/workflow/tasks/done', '/workflow/tasks/started', '/workflow/tasks/cc', '/workflow/tasks/monitor', '/workflow/categories', '/workflow/models', '/workflow/form-bindings', '/workflow/definitions'],
  },
  {
    kind: 'directory',
    key: 'openapi',
    title: '开放平台',
    paths: ['/developer/portal', '/openapi/apps', '/openapi/api-keys', '/openapi/oauth-clients', '/openapi/catalogs', '/openapi/versions', '/openapi/api-permissions', '/openapi/signature-policies', '/openapi/rate-limit-policies', '/openapi/call-audits', '/openapi/error-logs'],
  },
  { kind: 'directory', key: 'purchase', title: '采购样板', paths: ['/purchase/requests', '/purchase/orders', '/purchase/approvals'] },
  { kind: 'directory', key: 'message', title: '消息中心', paths: ['/message/inbox', '/message/templates'] },
  { kind: 'directory', key: 'file', title: '文件中心', paths: ['/file/storage-configs', '/file/objects', '/file/preview-logs'] },
  { kind: 'directory', key: 'cms', title: '内容管理', paths: ['/cms/channels', '/cms/contents'] },
  { kind: 'directory', key: 'visual', title: '报表大屏', paths: ['/visual/datasets', '/visual/reports', '/visual/screens'] },
  { kind: 'directory', key: 'job', title: '在线作业', paths: ['/job/tasks'] },
  { kind: 'directory', key: 'i18n', title: '国际化', paths: ['/i18n/messages'] },
  { kind: 'directory', key: 'search', title: '全文检索', paths: ['/search/index-configs'] },
  { kind: 'directory', key: 'monitor', title: '系统监控', paths: ['/monitor/services', '/monitor/data-sources', '/monitor/sql'] },
] as const;

const route = useRoute();
const router = useRouter();
const adminContext = reactive<AdminRuntimeContext>(getAdminRuntimeContext());
const interfaceSettingsOpen = ref(false);
const openMenuKeys = ref<string[]>([]);
const menuSearchKeyword = ref('');
const interfaceSettings = reactive<PlatformInterfaceSettings>(loadPlatformInterfaceSettings());
const interfaceSettingsForm = reactive<PlatformInterfaceSettings>({ ...interfaceSettings });
const openedMenuTabs = ref<OpenedMenuTab[]>([]);
let unsubscribeAdminContextChange: (() => void) | null = null;

/** 当前页面是否使用独立认证布局。 */
const isStandalonePage = computed(() => Boolean(route.meta?.standalone));

/** 后台导航项，首期对应已确认的核心菜单。 */
const navigationItems: NavigationItem[] = adminRoutes
  .filter((item) => item.meta?.permission)
  .map((item) => ({
    path: item.path,
    title: String(item.meta?.title ?? item.name ?? item.path),
    permission: String(item.meta?.permission),
  }));

/** 按路由路径索引导航项，便于菜单分组声明保持简洁。 */
const navigationItemMap = new Map(navigationItems.map((item) => [item.path, item]));

/** 后台菜单树。 */
const navigationEntries: NavigationEntry[] = menuGroupDefinitions
  .map((definition): NavigationEntry | null => {
    if (definition.kind === 'item') {
      const item = navigationItemMap.get(definition.path);
      if (!item) {
        return null;
      }
      return {
        ...item,
        kind: 'item',
        key: definition.key,
        title: definition.title,
      };
    }
    return {
      kind: 'directory',
      key: definition.key,
      title: definition.title,
      items: definition.paths.map((path) => navigationItemMap.get(path)).filter((item): item is NavigationItem => Boolean(item)),
    };
  })
  .filter((entry): entry is NavigationEntry => {
    if (!entry) {
      return false;
    }
    if (entry.kind === 'item') {
      return true;
    }
    return entry.items.length > 0;
  });

/** 菜单搜索平铺候选项。 */
const menuSearchSourceItems = computed<MenuSearchOption[]>(() =>
  navigationEntries.flatMap((entry) => {
    if (entry.kind === 'item') {
      return [{
        value: entry.path,
        label: entry.title,
        path: entry.path,
        groupTitle: '首页',
        keywords: `${entry.title} ${entry.path} ${entry.permission}`.toLowerCase(),
      }];
    }
    return entry.items.map((item) => ({
      value: item.path,
      label: item.title,
      path: item.path,
      groupTitle: entry.title,
      keywords: `${entry.title} ${item.title} ${item.path} ${item.permission}`.toLowerCase(),
    }));
  }),
);

/** 菜单搜索下拉选项。 */
const menuSearchOptions = computed(() => menuSearchSourceItems.value.map((item) => ({
  value: item.value,
  label: item.label,
  path: item.path,
  groupTitle: item.groupTitle,
  keywords: item.keywords,
})));

/** Ant Design Vue 全局主题 token。 */
const antDesignTheme = computed(() => ({
  token: {
    colorPrimary: interfaceSettings.primaryColor,
  },
}));

/** 后台根布局 CSS 变量。 */
const platformInterfaceCssVars = computed(() => ({
  '--platform-primary-color': interfaceSettings.primaryColor,
}));

/** 当前激活菜单标签页。 */
const activeMenuTabKey = computed(() => route.path);

/** 当前页面标题，用于头部展示。 */
const currentTitle = computed(() => {
  const matched = navigationItems.find((item) => item.path === route.path);
  return matched?.title ?? String(route.meta?.title ?? '个人工作台');
});

/** 当前路由所属一级菜单编码。 */
const currentMenuGroupKey = computed(() =>
  navigationEntries.find((entry) => entry.kind === 'directory' && entry.items.some((item) => item.path === route.path))?.key,
);

/** 当前租户展示文本。 */
const currentTenantText = computed(() => adminContext.tenantId || '未选择租户');

/** 当前账号展示文本。 */
const currentAccountText = computed(() => adminContext.accountName || '未登录');

/** 当前组织展示文本；未限定具体组织时代表按全部组织范围查看。 */
const currentOrgText = computed(() => adminContext.orgId?.toString() || '全部组织');

/** 当前是否已有后台认证上下文。 */
const isLoggedIn = computed(() => hasAuthenticatedAdminContext(adminContext));

/** 是否允许渲染后台管理壳层。 */
const shouldRenderPlatformShell = computed(() => !isStandalonePage.value && isLoggedIn.value);

/**
 * 切换后台菜单。
 *
 * @param info 菜单点击事件，key 对应路由路径
 */
function handleMenuClick(info: MenuInfo): void {
  syncOpenMenuKeysAfterNavigation(String(info.key));
  void router.push(String(info.key));
}

/**
 * 同步展开的一级菜单。
 *
 * @param keys 当前 Ant Design Vue 菜单展开编码列表
 */
function handleMenuOpenChange(keys: string[]): void {
  openMenuKeys.value = interfaceSettings.autoCollapse ? keys.slice(-1) : keys;
}

/**
 * 过滤菜单搜索候选项。
 *
 * @param inputValue 用户输入关键字
 * @param option 菜单候选项
 * @returns 是否展示该候选项
 */
function filterMenuSearchOption(inputValue: string, option: unknown): boolean {
  const keywords = typeof option === 'object' && option !== null && 'keywords' in option
    ? String((option as MenuSearchOption).keywords)
    : '';
  return keywords.includes(inputValue.trim().toLowerCase());
}

/**
 * 搜索选择菜单后直接跳转。
 *
 * @param path 菜单路由路径
 */
function handleMenuSearchSelect(path: string): void {
  const target = menuSearchSourceItems.value.find((item) => item.path === path);
  if (!target) {
    return;
  }
  menuSearchKeyword.value = '';
  syncOpenMenuKeysAfterNavigation(target.path);
  void router.push(target.path);
}

/**
 * 搜索框回车时跳转到第一个匹配菜单。
 */
function handleMenuSearchEnter(): void {
  const keyword = menuSearchKeyword.value.trim().toLowerCase();
  if (!keyword) {
    return;
  }
  const target = menuSearchSourceItems.value.find((item) => item.keywords.includes(keyword));
  if (target) {
    handleMenuSearchSelect(target.path);
  }
}

/**
 * 切换已打开菜单标签页。
 *
 * @param path 标签页路由路径
 */
function handlePageTabChange(path: string): void {
  if (!path || path === route.path) {
    return;
  }
  syncOpenMenuKeysAfterNavigation(path);
  void router.push(path);
}

/**
 * 编辑菜单标签页。
 *
 * @param targetKey 目标标签页路径
 * @param action 编辑动作
 */
function handlePageTabEdit(targetKey: string | MouseEvent, action: 'add' | 'remove'): void {
  if (action !== 'remove' || typeof targetKey !== 'string') {
    return;
  }
  closeMenuTab(targetKey);
}

/**
 * 确保指定菜单路由已打开为标签页。
 *
 * @param path 菜单路由路径
 */
function ensureMenuTab(path: string): void {
  const target = navigationItems.find((item) => item.path === path);
  if (!target || openedMenuTabs.value.some((tab) => tab.path === target.path)) {
    return;
  }
  openedMenuTabs.value = [...openedMenuTabs.value, { path: target.path, title: target.title }];
}

/**
 * 关闭指定菜单标签页。
 *
 * @param path 菜单路由路径
 */
function closeMenuTab(path: string): void {
  const currentTabs = openedMenuTabs.value;
  if (currentTabs.length <= 1) {
    return;
  }
  const closeIndex = currentTabs.findIndex((tab) => tab.path === path);
  if (closeIndex < 0) {
    return;
  }
  const nextTabs = currentTabs.filter((tab) => tab.path !== path);
  openedMenuTabs.value = nextTabs;
  if (path !== route.path) {
    return;
  }
  const nextActiveTab = nextTabs[Math.max(0, closeIndex - 1)] ?? nextTabs[0];
  if (nextActiveTab) {
    syncOpenMenuKeysAfterNavigation(nextActiveTab.path);
    void router.push(nextActiveTab.path);
  }
}

/**
 * 打开系统界面设置弹窗。
 */
function openInterfaceSettingsModal(): void {
  Object.assign(interfaceSettingsForm, interfaceSettings);
  interfaceSettingsOpen.value = true;
}

/**
 * 保存系统界面设置。
 */
function saveInterfaceSettings(): void {
  const nextSettings = normalizePlatformInterfaceSettings(interfaceSettingsForm);
  Object.assign(interfaceSettings, nextSettings);
  persistPlatformInterfaceSettings(nextSettings);
  syncOpenMenuKeysAfterNavigation(route.path);
  if (nextSettings.enableMenuTabs) {
    ensureMenuTab(route.path);
  }
  interfaceSettingsOpen.value = false;
}

/**
 * 进入后台统一认证登录页。
 */
function goToLogin(): void {
  void router.push({ path: '/login', query: { returnTo: route.fullPath } });
}

/**
 * 退出后台登录态。
 */
function logoutAdmin(): void {
  clearAdminRuntimeContext();
  Object.assign(adminContext, getAdminRuntimeContext());
  void refreshAdminPermissions();
  try {
    submitAdminOAuthLogout(buildAdminOAuthLogoutConfig());
  } catch {
    void router.push({ path: '/login', query: { loggedOut: '1' } });
  }
}

/**
 * 同步后台运行时上下文到页面头部展示状态。
 *
 * <p>统一认证回调页和 HTTP 静默刷新会直接更新本地上下文，根组件需要主动刷新响应式快照，
 * 否则顶栏会继续显示旧的未登录状态。</p>
 *
 * @param nextContext 最新后台运行时上下文
 */
function syncAdminContext(nextContext: AdminRuntimeContext = getAdminRuntimeContext()): void {
  Object.assign(adminContext, nextContext);
}

/**
 * 未认证访问业务页时强制跳转登录页。
 *
 * <p>路由守卫负责阻止正常导航，根组件兜底阻止首次挂载、刷新和异常缓存场景先露出后台菜单。</p>
 */
function redirectToLoginIfNeeded(): void {
  if (isStandalonePage.value || isLoggedIn.value) {
    return;
  }
  void router.replace({
    path: '/login',
    query: {
      returnTo: route.fullPath,
    },
  });
}

/**
 * 从本地存储读取后台界面设置。
 *
 * @returns 已校验的后台界面设置
 */
function loadPlatformInterfaceSettings(): PlatformInterfaceSettings {
  if (typeof window === 'undefined') {
    return { ...DEFAULT_PLATFORM_INTERFACE_SETTINGS };
  }
  try {
    const rawValue = window.localStorage.getItem(ZHYC_ADMIN_INTERFACE_STORAGE_KEY)
      ?? window.localStorage.getItem(ZHYC_ADMIN_LEGACY_STYLE_STORAGE_KEY);
    return normalizePlatformInterfaceSettings(rawValue ? JSON.parse(rawValue) : DEFAULT_PLATFORM_INTERFACE_SETTINGS);
  } catch {
    return { ...DEFAULT_PLATFORM_INTERFACE_SETTINGS };
  }
}

/**
 * 规范化后台界面设置，避免异常本地数据破坏页面渲染。
 *
 * @param value 待校验界面设置
 * @returns 可安全使用的界面设置
 */
function normalizePlatformInterfaceSettings(value: Partial<PlatformInterfaceSettings> | null | undefined): PlatformInterfaceSettings {
  const primaryColor = typeof value?.primaryColor === 'string' && /^#[0-9a-fA-F]{6}$/.test(value.primaryColor.trim())
    ? value.primaryColor.trim()
    : DEFAULT_PLATFORM_INTERFACE_SETTINGS.primaryColor;
  const menuTheme = value?.menuTheme === 'dark' ? 'dark' : 'light';
  const autoCollapse = typeof value?.autoCollapse === 'boolean'
    ? value.autoCollapse
    : DEFAULT_PLATFORM_INTERFACE_SETTINGS.autoCollapse;
  const enableMenuTabs = typeof value?.enableMenuTabs === 'boolean'
    ? value.enableMenuTabs
    : DEFAULT_PLATFORM_INTERFACE_SETTINGS.enableMenuTabs;
  return {
    primaryColor,
    menuTheme,
    autoCollapse,
    enableMenuTabs,
  };
}

/**
 * 持久化后台界面设置。
 *
 * @param settings 已校验界面设置
 */
function persistPlatformInterfaceSettings(settings: PlatformInterfaceSettings): void {
  if (typeof window === 'undefined') {
    return;
  }
  try {
    window.localStorage.setItem(ZHYC_ADMIN_INTERFACE_STORAGE_KEY, JSON.stringify(settings));
  } catch {
    // 本地存储不可用时只保留本次页面会话的界面设置。
  }
}

/**
 * 按路由路径定位所属一级菜单。
 *
 * @param path 路由路径
 * @returns 一级菜单编码
 */
function findMenuGroupKeyByPath(path: string): string | undefined {
  return navigationEntries.find((entry) => entry.kind === 'directory' && entry.items.some((item) => item.path === path))?.key;
}

/**
 * 跳转后同步菜单展开状态。
 *
 * @param path 当前或目标路由路径
 */
function syncOpenMenuKeysAfterNavigation(path: string): void {
  const groupKey = findMenuGroupKeyByPath(path);
  if (!groupKey) {
    if (interfaceSettings.autoCollapse) {
      openMenuKeys.value = [];
    }
    return;
  }
  openMenuKeys.value = interfaceSettings.autoCollapse
    ? [groupKey]
    : Array.from(new Set([...openMenuKeys.value, groupKey]));
}

onMounted(() => {
  unsubscribeAdminContextChange = subscribeAdminRuntimeContextChange(syncAdminContext);
  syncAdminContext();
  void refreshAdminPermissions();
});

onUnmounted(() => {
  unsubscribeAdminContextChange?.();
  unsubscribeAdminContextChange = null;
});

watch(
  currentMenuGroupKey,
  (groupKey) => {
    if (groupKey && shouldRenderPlatformShell.value) {
      syncOpenMenuKeysAfterNavigation(route.path);
    }
  },
  { immediate: true },
);

watch(
  () => route.path,
  (path) => {
    if (shouldRenderPlatformShell.value) {
      ensureMenuTab(path);
    }
  },
  { immediate: true },
);

watch(
  [isStandalonePage, isLoggedIn, () => route.fullPath],
  () => redirectToLoginIfNeeded(),
  { immediate: true },
);
</script>

<style scoped>
.platform-auth-redirect {
  min-height: 100vh;
  background: #f3f6fc;
}

.platform-layout {
  min-height: 100vh;
}

.platform-sider {
  min-height: 100vh;
  background: #ffffff;
  border-right: 1px solid #e2e8f0;
  box-shadow: 4px 0 18px rgb(15 23 42 / 4%);
}

.platform-sider :deep(.ant-layout-sider-children) {
  background: #ffffff;
}

.platform-sider-dark {
  background: #0f172a;
  border-right-color: #1e293b;
  box-shadow: 4px 0 18px rgb(15 23 42 / 12%);
}

.platform-sider-dark :deep(.ant-layout-sider-children) {
  background: #0f172a;
}

.platform-logo {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  color: #0f172a;
  font-size: 15px;
  font-weight: 600;
  border-bottom: 1px solid #e2e8f0;
  background: linear-gradient(135deg, #ffffff, #f8fbff);
}

.platform-sider-dark .platform-logo {
  color: #f8fafc;
  border-bottom-color: #1e293b;
  background: linear-gradient(135deg, #0f172a, #111827);
}

.platform-menu {
  border-inline-end: 0;
  background: #ffffff;
}

.platform-menu :deep(.ant-menu-item),
.platform-menu :deep(.ant-menu-submenu-title) {
  color: #475569;
}

.platform-menu :deep(.ant-menu-item-selected) {
  color: var(--platform-primary-color);
  background: color-mix(in srgb, var(--platform-primary-color) 12%, #ffffff);
}

.platform-menu :deep(.ant-menu-item-selected::after) {
  border-inline-end-color: var(--platform-primary-color);
}

.platform-menu :deep(.ant-menu-submenu-selected > .ant-menu-submenu-title) {
  color: var(--platform-primary-color);
}

.platform-menu :deep(.ant-menu-sub) {
  background: #f8fbff;
}

.platform-sider-dark .platform-menu {
  background: #0f172a;
}

.platform-sider-dark .platform-menu :deep(.ant-menu-item),
.platform-sider-dark .platform-menu :deep(.ant-menu-submenu-title) {
  color: #cbd5e1;
}

.platform-sider-dark .platform-menu :deep(.ant-menu-item-selected) {
  color: #ffffff;
  background: color-mix(in srgb, var(--platform-primary-color) 58%, #0f172a);
}

.platform-sider-dark .platform-menu :deep(.ant-menu-sub) {
  background: #111827;
}

.platform-header {
  height: auto;
  min-height: 64px;
  padding: 10px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  line-height: normal;
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
  box-shadow: 0 1px 2px rgb(15 23 42 / 4%);
}

.platform-header-main {
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-width: 0;
}

.platform-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.platform-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  white-space: nowrap;
}

.platform-auth-tag {
  margin-inline-end: 0;
}

.platform-header-context {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
}

.platform-context-item,
.platform-account-pill {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  min-width: 0;
}

.platform-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  min-width: 0;
  flex-wrap: wrap;
}

.platform-menu-search {
  flex: 0 1 280px;
  width: 280px;
  min-width: 220px;
}

.platform-menu-search :deep(.platform-menu-search-input.ant-input-affix-wrapper) {
  height: 36px;
  padding: 0 10px;
  border-color: #cbd5e1;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: none;
  transition: border-color 0.18s ease, box-shadow 0.18s ease;
}

.platform-menu-search :deep(.platform-menu-search-input.ant-input-affix-wrapper:hover) {
  border-color: #93c5fd;
}

.platform-menu-search :deep(.platform-menu-search-input.ant-input-affix-wrapper-focused) {
  border-color: var(--platform-primary-color);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--platform-primary-color) 14%, transparent);
}

.platform-menu-search :deep(.ant-input-prefix) {
  margin-inline-end: 8px;
  color: #64748b;
  line-height: 1;
}

.platform-menu-search :deep(.platform-menu-search-icon) {
  display: block;
  font-size: 15px;
}

.platform-menu-search :deep(.ant-input) {
  min-width: 0;
  color: #0f172a;
  font-size: 14px;
}

.platform-menu-search :deep(.ant-input::placeholder) {
  color: #94a3b8;
}

.menu-search-option {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.menu-search-option span {
  color: #0f172a;
  font-weight: 500;
}

.menu-search-option small {
  color: #64748b;
  font-size: 12px;
}

.platform-account-pill {
  max-width: 180px;
  height: 28px;
  padding: 0 10px;
  border: 1px solid #dbeafe;
  border-radius: 999px;
  background: #eff6ff;
  color: #1e3a8a;
  font-size: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.platform-page-tabs {
  padding: 8px 18px 6px;
  background: #f3f6fb;
  border-bottom: 1px solid #e5e7eb;
}

.platform-page-tabs-track {
  display: flex;
  align-items: center;
  min-height: 34px;
  padding: 0;
  overflow: hidden;
  border: 0;
  border-radius: 0;
  background: transparent;
}

.platform-page-tabs-track > :deep(.ant-tabs) {
  min-width: 0;
  width: 100%;
}

.platform-page-tabs :deep(.ant-tabs-nav) {
  margin: 0;
  min-height: 36px;
}

.platform-page-tabs :deep(.ant-tabs-nav::before) {
  border-bottom: 0;
}

.platform-page-tabs :deep(.ant-tabs-nav-wrap) {
  position: relative;
}

.platform-page-tabs :deep(.ant-tabs-nav-wrap::after) {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  width: 26px;
  pointer-events: none;
  content: "";
  background: linear-gradient(90deg, rgb(243 246 251 / 0%), #f3f6fb 76%);
}

.platform-page-tabs :deep(.ant-tabs-nav-list) {
  gap: 6px;
}

.platform-page-tabs :deep(.ant-tabs-tab) {
  position: relative;
  height: 32px;
  min-width: 0;
  max-width: none;
  margin: 0;
  padding: 0;
  overflow: visible;
  border: 1px solid #d6e0ef;
  border-radius: 7px;
  background: #ffffff;
  color: #64748b;
  box-shadow: 0 1px 2px rgb(15 23 42 / 4%);
  transition: border-color 0.18s ease, color 0.18s ease, box-shadow 0.18s ease, background 0.18s ease;
}

.platform-page-tabs :deep(.ant-tabs-tab-with-remove) {
  display: inline-flex;
  align-items: center;
  width: fit-content;
}

.platform-page-tabs :deep(.ant-tabs-tab:hover) {
  border-color: #b9d4ff;
  background: #f8fbff;
  color: #334155;
}

.platform-page-tabs :deep(.ant-tabs-tab-active) {
  border-color: var(--platform-primary-color);
  background: #ffffff;
  box-shadow: 0 4px 12px rgb(47 122 247 / 12%);
}

.platform-page-tabs :deep(.ant-tabs-tab-active::after) {
  position: absolute;
  right: 14px;
  bottom: 0;
  left: 14px;
  height: 2px;
  content: "";
  border-radius: 999px 999px 0 0;
  background: var(--platform-primary-color);
}

.platform-page-tabs :deep(.ant-tabs-tab-active .ant-tabs-tab-btn) {
  color: var(--platform-primary-color);
  font-weight: 600;
}

.platform-page-tabs :deep(.ant-tabs-tab-btn) {
  min-width: 0;
  flex: 0 0 auto;
  overflow: visible;
  padding: 0 6px 0 10px;
  font-size: 13px;
  line-height: 30px;
  text-overflow: clip;
  white-space: nowrap;
}

.platform-page-tab-title {
  display: block;
  min-width: 0;
  max-width: none;
  overflow: visible;
  text-overflow: clip;
  white-space: nowrap;
}

.platform-page-tabs :deep(.ant-tabs-tab-remove) {
  position: static;
  flex: 0 0 18px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 26px;
  margin: 0 3px 0 0;
  border-radius: 6px;
  color: #94a3b8;
  transition: background 0.18s ease, color 0.18s ease;
}

.platform-page-tabs :deep(.ant-tabs-tab-remove:hover) {
  background: #eff6ff;
  color: var(--platform-primary-color);
}

.platform-content {
  min-width: 0;
  min-height: calc(100vh - 64px);
  padding: 18px;
  overflow-x: hidden;
  background: #f3f6fb;
}

.platform-content :deep(.ant-card),
.platform-content :deep(.ant-table-wrapper) {
  min-width: 0;
  max-width: 100%;
}

.platform-content :deep(.ant-table-content) {
  max-width: 100%;
  overflow-x: auto;
}

.style-color-control {
  display: grid;
  grid-template-columns: 48px minmax(0, 1fr);
  gap: 10px;
  align-items: center;
}

.style-color-picker {
  width: 48px;
  height: 32px;
  padding: 0;
  overflow: hidden;
  border: 1px solid #d9e1ea;
  border-radius: 6px;
  background: #ffffff;
  cursor: pointer;
}

@media (max-width: 1024px) {
  .platform-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .platform-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .platform-menu-search {
    width: min(100%, 320px);
  }
}
</style>
