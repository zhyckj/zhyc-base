<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="lowcode-page-model-page">
    <div class="page-model-workbench">
      <a-card :bordered="false" class="model-card">
        <template #title>
          <div class="card-title-block">
            <span class="card-title">页面模型</span>
            <span class="card-subtitle">按表模型组织页面生成配置</span>
          </div>
        </template>
        <template #extra>
          <a-button :loading="loading" @click="loadModels">刷新</a-button>
        </template>

        <a-input-search
          v-model:value="modelKeyword"
          allow-clear
          class="model-search"
          placeholder="搜索模型编码 / 名称 / 表名"
        />

        <div class="model-summary">
          <span>{{ filteredModels.length }} 个模型</span>
          <span>{{ pageModels.length }} 个页面</span>
          <span>{{ currentModel ? currentModel.tableName : '未选择' }}</span>
        </div>

        <a-table
          row-key="code"
          size="small"
          class="model-list-table"
          :columns="modelColumns"
          :data-source="filteredModels"
          :loading="loading"
          :pagination="false"
          :custom-row="buildModelRowProps"
          :scroll="{ y: 540 }"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'model'">
              <div class="model-cell">
                <strong>{{ record.name }}</strong>
                <span>{{ record.code }}</span>
              </div>
            </template>
            <template v-else-if="column.key === 'pageCount'">
              <a-badge :count="countModelPages(record.id)" :number-style="{ backgroundColor: '#2f7df6' }" />
            </template>
            <template v-else-if="column.key === 'status'">
              <a-tag :color="record.status === 'PUBLISHED' ? 'green' : 'default'">
                {{ $statusLabel(record.status) }}
              </a-tag>
            </template>
          </template>
        </a-table>
      </a-card>

      <a-card :bordered="false" class="config-card">
        <template #title>
          <div class="card-title-block">
            <span class="card-title">{{ currentTitle }}</span>
            <span class="card-subtitle">{{ currentModel ? currentModel.code : '请选择左侧模型后配置页面' }}</span>
          </div>
        </template>
        <template #extra>
          <a-space>
            <a-tag v-if="dirty" color="orange">未保存</a-tag>
            <a-button :loading="saving" type="primary" :disabled="!currentModel" @click="saveCurrentPageModel">
              {{ dirty ? '保存更改' : '保存页面模型' }}
            </a-button>
          </a-space>
        </template>

        <a-empty v-if="!currentModel" description="请选择左侧页面模型" />

        <template v-else>
          <div class="overview-grid">
            <div class="overview-item">
              <span>模型编码</span>
              <strong>{{ currentModel.code }}</strong>
            </div>
            <div class="overview-item">
              <span>字段数量</span>
              <strong>{{ fieldStats.total }}</strong>
            </div>
            <div class="overview-item">
              <span>已保存页面</span>
              <strong>{{ currentSavedPageModels.length }}</strong>
            </div>
            <div class="overview-item">
              <span>当前目标</span>
              <strong>{{ activePageTypeOption.target }}</strong>
            </div>
          </div>

          <div class="page-type-section">
            <div class="section-title-row">
              <div>
                <h3>页面类型</h3>
                <p>选择要生成或维护的端侧页面，已保存类型会自动标记。</p>
              </div>
            </div>

            <div class="page-type-groups">
              <div class="page-type-group">
                <span class="group-title">后台管理端</span>
                <div class="page-type-list">
                  <button
                    v-for="option in adminPageTypeOptions"
                    :key="option.type"
                    type="button"
                    class="page-type-card"
                    :class="{ active: activePageType === option.type, saved: isPageTypeSaved(option.type) }"
                    @click="setActivePageType(option.type)"
                  >
                    <span>{{ option.title }}</span>
                    <strong>{{ option.description }}</strong>
                    <em>{{ option.layout }}</em>
                  </button>
                </div>
              </div>

              <div class="page-type-group">
                <span class="group-title">移动端 uni-app</span>
                <div class="page-type-list">
                  <button
                    v-for="option in mobilePageTypeOptions"
                    :key="option.type"
                    type="button"
                    class="page-type-card"
                    :class="{ active: activePageType === option.type, saved: isPageTypeSaved(option.type) }"
                    @click="setActivePageType(option.type)"
                  >
                    <span>{{ option.title }}</span>
                    <strong>{{ option.description }}</strong>
                    <em>{{ option.layout }}</em>
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div class="config-section">
            <div class="section-title-row">
              <div>
                <h3>生成信息</h3>
                <p>路由、权限和组件路径由模型编码推导，保存后进入页面模型记录。</p>
              </div>
              <a-tag :color="isPageTypeSaved(activePageType) ? 'green' : 'blue'">
                {{ isPageTypeSaved(activePageType) ? '已保存' : '待保存' }}
              </a-tag>
            </div>

            <a-descriptions class="page-summary" size="small" :column="{ xxl: 2, xl: 2, lg: 1, md: 1, sm: 1, xs: 1 }" bordered>
              <a-descriptions-item label="页面类型">{{ currentPageDraft.pageName }}</a-descriptions-item>
              <a-descriptions-item label="生成目标">{{ currentPageDraft.target }}</a-descriptions-item>
              <a-descriptions-item label="路由路径">
                <span class="path-text">{{ currentPageDraft.routePath }}</span>
              </a-descriptions-item>
              <a-descriptions-item label="权限编码">
                <span class="path-text">{{ currentPageDraft.permission }}</span>
              </a-descriptions-item>
              <a-descriptions-item label="组件路径">
                <span class="path-text">{{ currentPageDraft.componentPath }}</span>
              </a-descriptions-item>
              <a-descriptions-item label="布局类型">{{ currentPageDraft.layoutType }}</a-descriptions-item>
            </a-descriptions>
          </div>

          <div class="config-section">
            <div class="section-title-row">
              <div>
                <h3>已保存页面</h3>
                <p>展示当前表模型已经落库的页面模型，便于核对生成覆盖范围。</p>
              </div>
            </div>
            <a-table
              class="saved-page-table"
              row-key="pageType"
              size="small"
              :columns="pageModelColumns"
              :data-source="currentSavedPageModels"
              :pagination="false"
              :scroll="{ x: 860 }"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'pageType'">
                  <a-tag color="blue">{{ pageTypeName(record.pageType) }}</a-tag>
                </template>
                <template v-else-if="column.key === 'routePath' || column.key === 'componentPath'">
                  <span class="path-text">{{ savedPagePath(record, column.key) }}</span>
                </template>
              </template>
            </a-table>
          </div>

          <div class="config-section field-section">
            <div class="section-title-row">
              <div>
                <h3>字段配置</h3>
                <p>控制列表、表单、查询和字典绑定，保存页面模型时同步写入表模型字段配置。</p>
              </div>
              <a-space class="field-stats" :size="6">
                <a-tag>列表 {{ fieldStats.listVisible }}</a-tag>
                <a-tag>表单 {{ fieldStats.formVisible }}</a-tag>
                <a-tag>查询 {{ fieldStats.queryable }}</a-tag>
                <a-tag>字典 {{ fieldStats.dictBound }}</a-tag>
              </a-space>
            </div>

            <a-table
              class="field-config-table"
              row-key="code"
              size="small"
              :columns="fieldColumns"
              :data-source="currentConfigFields"
              :pagination="currentConfigFields.length > 8 ? $tablePagination : false"
              :scroll="{ x: 1040 }"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'required'">
                  <a-tag :color="record.required ? 'red' : 'default'">{{ record.required ? '必填' : '可选' }}</a-tag>
                </template>
                <template v-else-if="column.key === 'listVisible'">
                  <a-switch v-model:checked="record.listVisible" checked-children="展示" un-checked-children="隐藏" @change="markDirty" />
                </template>
                <template v-else-if="column.key === 'formVisible'">
                  <a-switch v-model:checked="record.formVisible" checked-children="展示" un-checked-children="隐藏" @change="markDirty" />
                </template>
                <template v-else-if="column.key === 'queryable'">
                  <a-switch v-model:checked="record.queryable" checked-children="查询" un-checked-children="关闭" @change="markDirty" />
                </template>
                <template v-else-if="column.key === 'dictCode'">
                  <a-select
                    v-model:value="record.dictCode"
                    :loading="dictLoading"
                    :options="dictTypeOptions"
                    allow-clear
                    show-search
                    option-filter-prop="label"
                    placeholder="不绑定"
                    class="dict-code-select"
                    @change="markDirty"
                  />
                </template>
              </template>
            </a-table>
          </div>
        </template>
      </a-card>
    </div>
  </section>
</template>

<script setup lang="ts">
import { message } from 'ant-design-vue';
import { computed, onMounted, ref } from 'vue';

import {
  listPageModels,
  listTableModels,
  savePageModel,
  saveTableModel,
  type LowcodeColumnModel,
  type LowcodePageModel,
  type LowcodeTableModelResponse,
} from '@/api/lowcode/model';
import { listSystemDictTypes, type SystemDictType } from '@/api/system/dict';
import { requireAdminTenantId } from '@/utils/adminContext';

/** 页面类型。 */
type PageType = 'LIST' | 'FORM' | 'DETAIL' | 'MOBILE' | 'MOBILE_FORM' | 'MOBILE_DETAIL';

/**
 * 页面类型选项。
 */
interface PageTypeOption {
  /** 页面类型编码；和后端页面模型类型保持一致。 */
  type: PageType;
  /** 页面类型名称；用于工作台卡片和列表展示。 */
  title: string;
  /** 生成目标端；用于区分后台管理端和移动端。 */
  target: string;
  /** 页面生成用途说明。 */
  description: string;
  /** 推荐布局类型。 */
  layout: string;
}

/**
 * 页面字段草案。
 */
interface PageFieldDraft {
  /** 字段编码。 */
  code: string;
  /** 字段名称。 */
  name: string;
  /** 字段类型。 */
  fieldType: string;
  /** 是否必填。 */
  required: boolean;
  /** 是否可查询。 */
  queryable: boolean;
  /** 是否列表展示。 */
  listVisible: boolean;
  /** 是否表单展示。 */
  formVisible: boolean;
  /** 绑定的系统字典编码。 */
  dictCode?: string;
}

/**
 * 页面模型草案。
 */
interface PageModelDraft {
  /** 页面名称。 */
  pageName: string;
  /** 生成目标。 */
  target: string;
  /** 路由路径。 */
  routePath: string;
  /** 权限编码。 */
  permission: string;
  /** 组件文件路径。 */
  componentPath: string;
  /** 页面布局类型。 */
  layoutType: string;
  /** 页面字段。 */
  fields: PageFieldDraft[];
}

/** 页面类型配置；集中维护端侧分组、文案和布局提示。 */
const pageTypeOptions: PageTypeOption[] = [
  { type: 'LIST', title: '列表页', target: '后台管理端', description: '表格检索与批量操作入口', layout: 'TABLE' },
  { type: 'FORM', title: '表单页', target: '后台管理端', description: '新增编辑表单承载页', layout: 'FORM' },
  { type: 'DETAIL', title: '详情页', target: '后台管理端', description: '字段信息只读查看页', layout: 'DESCRIPTIONS' },
  { type: 'MOBILE', title: '移动列表', target: 'uni-app', description: '移动端列表与检索入口', layout: 'UNIAPP_PAGE' },
  { type: 'MOBILE_FORM', title: '移动表单', target: 'uni-app', description: '移动端新增编辑页面', layout: 'UNIAPP_PAGE' },
  { type: 'MOBILE_DETAIL', title: '移动详情', target: 'uni-app', description: '移动端详情查看页面', layout: 'UNIAPP_PAGE' },
];

/** 表模型列表。 */
const models = ref<LowcodeTableModelResponse[]>([]);
/** 已保存页面模型列表。 */
const pageModels = ref<LowcodePageModel[]>([]);
/** 当前表模型。 */
const currentModel = ref<LowcodeTableModelResponse>();
/** 表模型搜索关键字。 */
const modelKeyword = ref('');
/** 当前页面类型。 */
const activePageType = ref<PageType>('LIST');
/** 字段配置是否存在未保存改动。 */
const dirty = ref(false);
/** 加载状态。 */
const loading = ref(false);
/** 保存状态。 */
const saving = ref(false);
/** 字典类型加载状态。 */
const dictLoading = ref(false);
/** 当前租户可绑定的系统字典类型。 */
const dictTypes = ref<SystemDictType[]>([]);

/** 表模型列。 */
const modelColumns = [
  { title: '表模型', dataIndex: 'code', key: 'model' },
  { title: '页面', key: 'pageCount', width: 76 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 92 },
];

/** 页面字段列。 */
const fieldColumns = [
  { title: '字段编码', dataIndex: 'code', key: 'code', width: 160 },
  { title: '字段名称', dataIndex: 'name', key: 'name', width: 160 },
  { title: '字段类型', dataIndex: 'fieldType', key: 'fieldType', width: 130 },
  { title: '必填', dataIndex: 'required', key: 'required', width: 90 },
  { title: '列表展示', dataIndex: 'listVisible', key: 'listVisible', width: 120 },
  { title: '表单展示', dataIndex: 'formVisible', key: 'formVisible', width: 120 },
  { title: '查询字段', dataIndex: 'queryable', key: 'queryable', width: 120 },
  { title: '绑定字典', dataIndex: 'dictCode', key: 'dictCode', width: 220 },
];

/** 已保存页面模型列。 */
const pageModelColumns = [
  { title: '页面类型', dataIndex: 'pageType', key: 'pageType', width: 120 },
  { title: '路由路径', dataIndex: 'routePath', key: 'routePath' },
  { title: '组件路径', dataIndex: 'componentPath', key: 'componentPath' },
  { title: '布局类型', dataIndex: 'layoutType', key: 'layoutType', width: 140 },
];

/** 过滤后的表模型列表。 */
const filteredModels = computed(() => {
  const keyword = modelKeyword.value.trim().toLowerCase();
  if (!keyword) {
    return models.value;
  }
  return models.value.filter((model) => [
    model.code,
    model.name,
    model.tableName,
  ].some((field) => field?.toLowerCase().includes(keyword)));
});

/** 后台管理端页面类型。 */
const adminPageTypeOptions = computed(() => pageTypeOptions.filter((option) => option.target === '后台管理端'));

/** 移动端页面类型。 */
const mobilePageTypeOptions = computed(() => pageTypeOptions.filter((option) => option.target === 'uni-app'));

/** 当前激活的页面类型配置。 */
const activePageTypeOption = computed(() => findPageTypeOption(activePageType.value) ?? pageTypeOptions[0]);

/** 当前页面标题。 */
const currentTitle = computed(() => {
  if (!currentModel.value) {
    return '页面模型配置';
  }
  return `${currentModel.value.name}页面模型`;
});

/** 当前页面模型草案。 */
const currentPageDraft = computed<PageModelDraft>(() => {
  const model = currentModel.value;
  if (!model) {
    return {
      pageName: '未选择',
      target: '-',
      routePath: '-',
      permission: '-',
      componentPath: '-',
      layoutType: '-',
      fields: [],
    };
  }
  return buildPageDraft(model, activePageType.value);
});

/** 当前表模型下已保存的页面模型。 */
const currentSavedPageModels = computed(() => {
  const model = currentModel.value;
  if (!model) {
    return [];
  }
  return pageModels.value.filter((pageModel) => pageModel.tableModelId === model.id);
});

/** 当前可在线配置的字段列表。 */
const currentConfigFields = computed(() => currentModel.value?.columns ?? []);

/** 当前字段配置统计。 */
const fieldStats = computed(() => {
  const fields = currentConfigFields.value;
  return {
    total: fields.length,
    listVisible: fields.filter((field) => field.listVisible).length,
    formVisible: fields.filter((field) => field.formVisible).length,
    queryable: fields.filter((field) => field.queryable).length,
    dictBound: fields.filter((field) => Boolean(field.dictCode)).length,
  };
});

/** 字典类型下拉选项。 */
const dictTypeOptions = computed(() => dictTypes.value
  .filter((dictType) => dictType.status === 'enabled')
  .map((dictType) => ({
    label: `${dictType.dictName} / ${dictType.dictCode}`,
    value: dictType.dictCode,
  })));

/**
 * 加载低代码表模型。
 */
async function loadModels(): Promise<void> {
  loading.value = true;
  try {
    const tenantId = syncTenantContext();
    const [loadedModels, loadedPageModels] = await Promise.all([
      listTableModels(tenantId),
      listPageModels(tenantId),
    ]);
    const previousModelId = currentModel.value?.id;
    models.value = loadedModels;
    pageModels.value = loadedPageModels;
    currentModel.value = loadedModels.find((model) => model.id === previousModelId) ?? loadedModels[0];
    dirty.value = false;
    void loadDictTypes(tenantId);
  } finally {
    loading.value = false;
  }
}

/**
 * 加载可用于页面字段绑定的系统字典类型。
 *
 * @param tenantId 当前租户编码
 */
async function loadDictTypes(tenantId = syncTenantContext()): Promise<void> {
  dictLoading.value = true;
  try {
    dictTypes.value = await listSystemDictTypes(tenantId);
  } catch {
    dictTypes.value = [];
    message.warning('系统字典加载失败，字段字典绑定下拉已置空');
  } finally {
    dictLoading.value = false;
  }
}

/**
 * 保存当前页面模型。
 */
async function saveCurrentPageModel(): Promise<void> {
  const model = currentModel.value;
  if (!model) {
    message.warning('请选择表模型');
    return;
  }

  saving.value = true;
  try {
    const draft = currentPageDraft.value;
    const tenantId = syncTenantContext();
    const savedTableModel = await saveTableModel({
      tenantId,
      dataSourceId: model.dataSourceId,
      code: model.code,
      name: model.name,
      tableName: model.tableName,
      status: model.status,
      columns: model.columns,
    });
    await savePageModel({
      tenantId,
      tableModelId: savedTableModel.id,
      pageType: activePageType.value,
      routePath: draft.routePath,
      componentPath: draft.componentPath,
      layoutType: draft.layoutType,
    });
    currentModel.value = savedTableModel;
    dirty.value = false;
    message.success('页面模型和字段配置已保存');
    pageModels.value = await listPageModels(tenantId);
    models.value = models.value.map((item) => (item.id === savedTableModel.id ? savedTableModel : item));
  } finally {
    saving.value = false;
  }
}

/**
 * 读取当前后台租户编码。
 *
 * @returns 当前租户编码
 */
function syncTenantContext(): string {
  return requireAdminTenantId();
}

/**
 * 选择当前表模型。
 *
 * @param record 表模型
 */
function selectModel(record: LowcodeTableModelResponse): void {
  currentModel.value = record;
  dirty.value = false;
}

/**
 * 构建表模型行点击属性。
 *
 * @param record 表模型
 * @returns 行点击属性
 */
function buildModelRowProps(record: LowcodeTableModelResponse): { onClick: () => void; class?: string } {
  return {
    class: record.id === currentModel.value?.id ? 'is-selected' : undefined,
    onClick: () => selectModel(record),
  };
}

/**
 * 设置当前页面类型。
 *
 * @param pageType 页面类型
 */
function setActivePageType(pageType: PageType): void {
  activePageType.value = pageType;
}

/**
 * 标记字段配置存在未保存改动。
 */
function markDirty(): void {
  dirty.value = true;
}

/**
 * 统计指定表模型已保存的页面数量。
 *
 * @param tableModelId 表模型主键
 * @returns 页面数量
 */
function countModelPages(tableModelId: number): number {
  return pageModels.value.filter((pageModel) => pageModel.tableModelId === tableModelId).length;
}

/**
 * 判断页面类型是否已保存。
 *
 * @param pageType 页面类型
 * @returns 是否已保存
 */
function isPageTypeSaved(pageType: PageType): boolean {
  return currentSavedPageModels.value.some((pageModel) => pageModel.pageType === pageType);
}

/**
 * 获取页面类型名称。
 *
 * @param pageType 页面类型编码
 * @returns 页面类型名称
 */
function pageTypeName(pageType: string): string {
  return findPageTypeOption(pageType)?.title ?? pageType;
}

/**
 * 获取已保存页面路径类字段。
 *
 * @param pageModel 已保存页面模型
 * @param columnKey 列编码
 * @returns 路由路径或组件路径
 */
function savedPagePath(pageModel: LowcodePageModel, columnKey: string): string {
  if (columnKey === 'routePath') {
    return pageModel.routePath;
  }
  if (columnKey === 'componentPath') {
    return pageModel.componentPath;
  }
  return '';
}

/**
 * 查找页面类型配置。
 *
 * @param pageType 页面类型编码
 * @returns 页面类型配置
 */
function findPageTypeOption(pageType: string): PageTypeOption | undefined {
  return pageTypeOptions.find((option) => option.type === pageType);
}

/**
 * 构建页面模型草案。
 *
 * @param model 表模型
 * @param pageType 页面类型
 * @returns 页面模型草案
 */
function buildPageDraft(model: LowcodeTableModelResponse, pageType: PageType): PageModelDraft {
  const moduleCode = resolveModuleCode(model.code);
  const entityCode = resolveEntityCode(model.code);
  const fields = resolvePageFields(model.columns, pageType);
  const pageNames: Record<PageType, string> = {
    LIST: '列表页',
    FORM: '表单页',
    DETAIL: '详情页',
    MOBILE: '移动列表页',
    MOBILE_FORM: '移动表单页',
    MOBILE_DETAIL: '移动详情页',
  };
  const targets: Record<PageType, string> = {
    LIST: '后台管理端',
    FORM: '后台管理端',
    DETAIL: '后台管理端',
    MOBILE: 'uni-app',
    MOBILE_FORM: 'uni-app',
    MOBILE_DETAIL: 'uni-app',
  };
  return {
    pageName: pageNames[pageType],
    target: targets[pageType],
    routePath: resolveRoutePath(moduleCode, entityCode, pageType),
    permission: `${moduleCode}:${entityCode}:query`,
    componentPath: resolveComponentPath(moduleCode, entityCode, pageType),
    layoutType: resolveLayoutType(pageType),
    fields,
  };
}

/**
 * 按页面类型筛选字段。
 *
 * @param columns 表模型字段
 * @param pageType 页面类型
 * @returns 页面字段草案
 */
function resolvePageFields(columns: LowcodeColumnModel[], pageType: PageType): PageFieldDraft[] {
  return columns
    .filter((column) => {
      if (pageType === 'LIST' || pageType === 'MOBILE') {
        return column.listVisible;
      }
      if (pageType === 'FORM' || pageType === 'MOBILE_FORM') {
        return column.formVisible;
      }
      return !column.primaryKey || pageType === 'DETAIL' || pageType === 'MOBILE_DETAIL';
    })
    .map((column) => ({
      code: column.code,
      name: column.name,
      fieldType: column.fieldType,
      required: column.required,
      queryable: column.queryable,
      listVisible: column.listVisible,
      formVisible: column.formVisible,
      dictCode: column.dictCode,
    }));
}

/**
 * 解析页面路由。
 *
 * @param moduleCode 模块编码
 * @param entityCode 实体编码
 * @param pageType 页面类型
 * @returns 页面路由
 */
function resolveRoutePath(moduleCode: string, entityCode: string, pageType: PageType): string {
  const mobileRouteSuffixes: Partial<Record<PageType, string>> = {
    MOBILE: 'list',
    MOBILE_FORM: 'form',
    MOBILE_DETAIL: 'detail',
  };
  const mobileRouteSuffix = mobileRouteSuffixes[pageType];
  if (mobileRouteSuffix) {
    return `/pages/${moduleCode}/${entityCode}/${mobileRouteSuffix}`;
  }
  return `/${moduleCode}/${entityCode}`;
}

/**
 * 从模型编码解析模块编码。
 *
 * @param modelCode 模型编码
 * @returns 模块编码
 */
function resolveModuleCode(modelCode: string): string {
  return modelCode.split('_').filter(Boolean)[0] || 'business';
}

/**
 * 从模型编码解析实体编码，和代码生成器保持一致。
 *
 * @param modelCode 模型编码
 * @returns 小驼峰实体编码
 */
function resolveEntityCode(modelCode: string): string {
  const parts = modelCode.split('_').filter(Boolean);
  if (parts.length <= 1) {
    return toCamelEntityCode(parts);
  }
  return toCamelEntityCode(parts.slice(1));
}

/**
 * 将模型编码片段转换为小驼峰实体编码。
 *
 * @param parts 模型编码片段
 * @returns 小驼峰实体编码
 */
function toCamelEntityCode(parts: string[]): string {
  if (parts.length === 0) {
    return 'business';
  }
  return parts
    .map((part, index) => {
      const normalized = part.trim();
      if (index === 0) {
        return normalized;
      }
      return normalized.charAt(0).toUpperCase() + normalized.slice(1);
    })
    .join('');
}

/**
 * 解析生成后组件路径。
 *
 * @param moduleCode 模块编码
 * @param entityCode 实体编码
 * @param pageType 页面类型
 * @returns 组件文件路径
 */
function resolveComponentPath(moduleCode: string, entityCode: string, pageType: PageType): string {
  if (pageType === 'MOBILE') {
    return `zhyc-base-uniapp/src/pages/${moduleCode}/${entityCode}/list.vue`;
  }
  if (pageType === 'MOBILE_FORM') {
    return `zhyc-base-uniapp/src/pages/${moduleCode}/${entityCode}/form.vue`;
  }
  if (pageType === 'MOBILE_DETAIL') {
    return `zhyc-base-uniapp/src/pages/${moduleCode}/${entityCode}/detail.vue`;
  }
  if (pageType === 'FORM') {
    return `zhyc-base-vue/src/views/${moduleCode}/${entityCode}/form.vue`;
  }
  if (pageType === 'DETAIL') {
    return `zhyc-base-vue/src/views/${moduleCode}/${entityCode}/detail.vue`;
  }
  return `zhyc-base-vue/src/views/${moduleCode}/${entityCode}/index.vue`;
}

/**
 * 解析页面布局类型。
 *
 * @param pageType 页面类型
 * @returns 页面布局类型
 */
function resolveLayoutType(pageType: PageType): string {
  const layoutTypes: Record<PageType, string> = {
    LIST: 'TABLE',
    FORM: 'FORM',
    DETAIL: 'DESCRIPTIONS',
    MOBILE: 'UNIAPP_PAGE',
    MOBILE_FORM: 'UNIAPP_PAGE',
    MOBILE_DETAIL: 'UNIAPP_PAGE',
  };
  return layoutTypes[pageType];
}

onMounted(() => {
  void loadModels();
});
</script>

<style scoped>
.lowcode-page-model-page {
  min-width: 0;
}

.page-model-workbench {
  display: grid;
  grid-template-columns: minmax(330px, 0.36fr) minmax(0, 1fr);
  gap: 16px;
  align-items: start;
}

.model-card,
.config-card {
  border-radius: 10px;
  box-shadow: 0 8px 24px rgb(24 39 75 / 6%);
}

.card-title-block {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.card-title {
  color: #1f2937;
  font-size: 16px;
  font-weight: 700;
  line-height: 24px;
}

.card-subtitle {
  overflow: hidden;
  color: #667085;
  font-size: 12px;
  font-weight: 400;
  line-height: 18px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.model-search {
  margin-bottom: 12px;
}

.model-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.model-summary span {
  max-width: 100%;
  padding: 3px 8px;
  overflow: hidden;
  color: #475467;
  font-size: 12px;
  line-height: 18px;
  text-overflow: ellipsis;
  white-space: nowrap;
  background: #f6f8fb;
  border: 1px solid #e8edf5;
  border-radius: 999px;
}

.model-cell {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.model-cell strong,
.model-cell span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.model-cell strong {
  color: #1f2937;
  font-size: 13px;
  line-height: 20px;
}

.model-cell span {
  color: #667085;
  font-size: 12px;
  line-height: 18px;
}

.model-list-table :deep(.ant-table-row) {
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.model-list-table :deep(.ant-table-row:hover > td),
.model-list-table :deep(.is-selected > td) {
  background: #eef5ff;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 18px;
}

.overview-item {
  min-width: 0;
  padding: 14px 16px;
  background: linear-gradient(180deg, #f8fbff 0%, #fff 100%);
  border: 1px solid #e8eef8;
  border-radius: 8px;
}

.overview-item span {
  display: block;
  margin-bottom: 6px;
  color: #667085;
  font-size: 12px;
  line-height: 18px;
}

.overview-item strong {
  display: block;
  overflow: hidden;
  color: #1f2937;
  font-size: 18px;
  line-height: 26px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.page-type-section,
.config-section {
  padding-top: 16px;
  margin-top: 16px;
  border-top: 1px solid #edf1f7;
}

.page-type-section {
  margin-top: 0;
}

.section-title-row {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 12px;
}

.section-title-row h3 {
  margin: 0;
  color: #1f2937;
  font-size: 15px;
  font-weight: 700;
  line-height: 22px;
}

.section-title-row p {
  margin: 2px 0 0;
  color: #667085;
  font-size: 12px;
  line-height: 18px;
}

.page-type-groups {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.page-type-group {
  min-width: 0;
}

.group-title {
  display: block;
  margin-bottom: 8px;
  color: #344054;
  font-size: 13px;
  font-weight: 700;
  line-height: 20px;
}

.page-type-list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.page-type-card {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
  min-height: 96px;
  padding: 12px;
  text-align: left;
  cursor: pointer;
  background: #fff;
  border: 1px solid #e4eaf3;
  border-radius: 8px;
  transition: border-color 0.18s ease, box-shadow 0.18s ease, transform 0.18s ease;
}

.page-type-card:hover,
.page-type-card.active {
  border-color: #2f7df6;
  box-shadow: 0 8px 20px rgb(47 125 246 / 12%);
  transform: translateY(-1px);
}

.page-type-card.active {
  background: linear-gradient(180deg, #f4f8ff 0%, #fff 100%);
}

.page-type-card.saved::after {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 7px;
  height: 7px;
  content: '';
  background: #22c55e;
  border-radius: 999px;
  box-shadow: 0 0 0 3px rgb(34 197 94 / 14%);
}

.page-type-card span {
  padding-right: 12px;
  color: #1f2937;
  font-size: 14px;
  font-weight: 700;
  line-height: 20px;
}

.page-type-card strong {
  color: #475467;
  font-size: 12px;
  font-weight: 400;
  line-height: 18px;
}

.page-type-card em {
  margin-top: auto;
  color: #2f7df6;
  font-size: 11px;
  font-style: normal;
  line-height: 16px;
}

.page-summary {
  margin-bottom: 16px;
}

.saved-page-table {
  margin-bottom: 16px;
}

.field-config-table {
  margin-top: 16px;
}

.dict-code-select {
  width: 200px;
}

.path-text {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  color: #344054;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', monospace;
  font-size: 12px;
  text-overflow: ellipsis;
  vertical-align: bottom;
  white-space: nowrap;
}

.field-stats {
  flex-wrap: wrap;
  justify-content: flex-end;
}

.config-card :deep(.ant-card-head),
.model-card :deep(.ant-card-head) {
  min-height: 64px;
}

.config-card :deep(.ant-card-body),
.model-card :deep(.ant-card-body) {
  padding-top: 18px;
}

@media (max-width: 1280px) {
  .page-model-workbench {
    grid-template-columns: 1fr;
  }

  .model-list-table {
    max-height: none;
  }
}

@media (max-width: 960px) {
  .overview-grid,
  .page-type-groups {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .section-title-row {
    flex-direction: column;
  }

  .overview-grid,
  .page-type-list {
    grid-template-columns: 1fr;
  }

  .config-card :deep(.ant-card-extra),
  .model-card :deep(.ant-card-extra) {
    margin-left: 0;
  }
}
</style>
