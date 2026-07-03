<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="visual-page screen-page">
    <a-card title="可视化数据大屏" :bordered="false">
      <template #extra>
        <a-space>
          <a-select v-model:value="status" class="status-select" allow-clear placeholder="状态">
            <a-select-option value="draft">草稿</a-select-option>
            <a-select-option value="published">已发布</a-select-option>
            <a-select-option value="offline">offline</a-select-option>
          </a-select>
          <a-button :loading="loading" @click="loadScreens">刷新</a-button>
          <a-button type="primary" @click="openCreateForm">新增大屏</a-button>
        </a-space>
      </template>

      <a-alert v-if="errorMessage" class="state-alert" :message="errorMessage" type="error" show-icon />

      <a-table
        row-key="screenCode"
        size="small"
        :columns="columns"
        :data-source="screens"
        :loading="loading"
        :pagination="$tablePagination"
        :locale="{ emptyText: '暂无大屏。' }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'published' ? 'green' : 'default'">
              {{ $statusLabel(record.status) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'layoutJson'">
            <span class="config-summary">{{ layoutSummary(record.layoutJson) }}</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button size="small" @click="openEditForm(record)">设计</a-button>
              <a-button
                size="small"
                type="primary"
                :disabled="record.status === 'published'"
                @click="publishScreen(record)"
              >
                发布
              </a-button>
              <a-button size="small" @click="offlineScreen(record)">
                下线
              </a-button>
              <a-button
                size="small"
                :disabled="record.status !== 'published'"
                @click="openPublishedScreen(record)"
              >
                访问
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="formOpen"
      title="在线可视化大屏设计器"
      width="1320px"
      :confirm-loading="saving"
      @ok="submitForm"
    >
      <div class="designer-steps">
        <span>基础信息</span>
        <span>组件拖拽</span>
        <span>数据绑定</span>
        <span>预览发布</span>
      </div>

      <a-form layout="inline" class="base-form">
        <a-form-item label="大屏编码" required>
          <a-input v-model:value="formState.screenCode" placeholder="例如 ops_screen" />
        </a-form-item>
        <a-form-item label="大屏名称" required>
          <a-input v-model:value="formState.screenName" placeholder="请输入大屏名称" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="formState.status" class="status-select">
            <a-select-option value="draft">草稿</a-select-option>
            <a-select-option value="published">已发布</a-select-option>
            <a-select-option value="offline">offline</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>

      <div class="designer-shell">
        <aside class="palette-panel">
          <div class="panel-title">组件库</div>
          <button
            v-for="item in paletteWidgets"
            :key="item.type"
            class="palette-item"
            draggable="true"
            type="button"
            @dragstart="handlePaletteDragStart($event, item.type)"
          >
            <strong>{{ item.label }}</strong>
            <span>{{ item.description }}</span>
          </button>
        </aside>

        <main class="canvas-panel">
          <div class="canvas-header">
            <div>
              <strong>大屏画布</strong>
              <span>参考商业大屏流程：拖拽组件、绑定数据、预览后发布。</span>
            </div>
            <a-space>
              <a-button size="small" @click="alignSelectedWidget">对齐网格</a-button>
              <a-button size="small" danger :disabled="!selectedWidget" @click="removeSelectedWidget">删除组件</a-button>
            </a-space>
          </div>
          <div class="screen-canvas" @dragover.prevent @drop="handleCanvasDrop">
            <a-empty v-if="screenWidgets.length === 0" description="从左侧组件库拖拽组件开始设计大屏" />
            <div
              v-for="widget in screenWidgets"
              :key="widget.id"
              class="screen-widget"
              :class="{ active: widget.id === selectedWidgetId }"
              :style="widgetStyle(widget)"
              draggable="true"
              @click.stop="selectWidget(widget.id)"
              @dragstart="handleWidgetDragStart($event, widget.id)"
            >
              <div class="widget-title">{{ widget.title }}</div>
              <div class="widget-body">
                <template v-if="widget.type === 'stat'">
                  <strong>{{ widgetMetricValue(widget) }}</strong>
                  <span>{{ widget.datasetCode || '未绑定数据集' }}</span>
                </template>
                <template v-else-if="widget.type === 'table'">
                  <table>
                    <thead>
                      <tr>
                        <th v-for="column in widgetTableColumns(widget)" :key="column">{{ column }}</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="(row, rowIndex) in widgetPreviewRows(widget)" :key="rowIndex">
                        <td v-for="column in widgetTableColumns(widget)" :key="column">
                          {{ formatPreviewCell(row[column]) }}
                        </td>
                      </tr>
                      <tr v-if="widgetPreviewRows(widget).length === 0">
                        <td>{{ widget.fieldX || '区域' }}</td>
                        <td>{{ widget.fieldY || '数值' }}</td>
                      </tr>
                    </tbody>
                  </table>
                </template>
                <template v-else-if="widget.type === 'map'">
                  <div class="map-preview">
                    <span>地图热区</span>
                    <i></i>
                  </div>
                </template>
                <template v-else-if="widget.type === 'text'">
                  <p>{{ widget.content || '请输入大屏标题或说明' }}</p>
                </template>
                <template v-else>
                  <div class="chart-preview" :class="widget.type">
                    <i
                      v-for="(bar, index) in widgetChartBars(widget)"
                      :key="index"
                      :style="{ height: `${bar}%` }"
                    ></i>
                  </div>
                </template>
              </div>
            </div>
          </div>
        </main>

        <aside class="property-panel">
          <div class="panel-title">属性面板</div>
          <a-empty v-if="!selectedWidget" description="请选择大屏组件" />
          <a-form v-else layout="vertical">
            <a-form-item label="组件标题">
              <a-input v-model:value="selectedWidget.title" />
            </a-form-item>
            <a-form-item label="数据绑定">
              <a-select
                v-model:value="selectedWidget.datasetCode"
                allow-clear
                show-search
                option-filter-prop="label"
                :options="datasetOptions"
                placeholder="选择数据集"
                @change="handleSelectedDatasetChange"
              />
            </a-form-item>
            <a-form-item label="维度字段">
              <a-select
                v-model:value="selectedWidget.fieldX"
                allow-clear
                show-search
                option-filter-prop="label"
                :options="selectedDatasetFieldOptions"
                placeholder="选择维度字段"
              />
            </a-form-item>
            <a-form-item label="指标字段">
              <a-select
                v-model:value="selectedWidget.fieldY"
                allow-clear
                show-search
                option-filter-prop="label"
                :options="selectedDatasetFieldOptions"
                placeholder="选择指标字段"
              />
            </a-form-item>
            <a-form-item label="数值字段">
              <a-select
                v-model:value="selectedWidget.fieldValue"
                allow-clear
                show-search
                option-filter-prop="label"
                :options="selectedDatasetFieldOptions"
                placeholder="选择数值字段"
              />
            </a-form-item>
            <div class="field-preview">
              <span>字段预览</span>
              <a-empty v-if="selectedDatasetFieldOptions.length === 0" description="请选择数据集或检查 SQL 字段" />
              <div v-else class="field-tags">
                <a-tag v-for="field in selectedDatasetFieldOptions" :key="field.value" color="blue">
                  {{ field.label }}
                </a-tag>
              </div>
            </div>
            <div class="field-preview">
              <span>数据预览</span>
              <a-spin :spinning="datasetPreviewLoading">
                <a-empty v-if="!selectedDatasetPreview" description="请选择数据集后查看预览" />
                <template v-else>
                  <a-alert
                    :message="selectedDatasetPreview.message"
                    :type="selectedDatasetPreview.executable ? 'success' : 'info'"
                    show-icon
                  />
                  <a-table
                    v-if="selectedDatasetPreview.rows.length > 0"
                    class="preview-table"
                    size="small"
                    :pagination="$tablePagination"
                    :columns="selectedDatasetPreviewColumns"
                    :data-source="selectedDatasetPreview.rows"
                    row-key="__rowKey"
                  />
                </template>
              </a-spin>
            </div>
            <a-form-item v-if="selectedWidget.type === 'text'" label="文本内容">
              <a-textarea v-model:value="selectedWidget.content" :rows="3" />
            </a-form-item>
            <a-row :gutter="8">
              <a-col :span="12">
                <a-form-item label="宽度">
                  <a-input-number v-model:value="selectedWidget.w" :min="180" :max="900" class="full-input" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="高度">
                  <a-input-number v-model:value="selectedWidget.h" :min="120" :max="520" class="full-input" />
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>

          <div class="preview-card">
            <div class="panel-title">预览发布</div>
            <p>保存后写入布局 JSON，列表页可继续发布或下线。</p>
            <code>{{ screenWidgets.length }} 个大屏组件</code>
          </div>
        </aside>
      </div>

      <span class="permission-code">visual:screen:save visual:screen:publish</span>
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { Modal, message } from 'ant-design-vue';
import { computed, onMounted, reactive, ref } from 'vue';

import {
  changeVisualScreenStatus,
  listVisualDatasets,
  listVisualScreens,
  previewVisualDataset,
  saveVisualScreen,
  type VisualDataset,
  type VisualDatasetPreview,
  type VisualScreen,
  type VisualScreenSavePayload,
} from '@/api/visual/report';
import { requireAdminTenantId } from '@/utils/adminContext';

type ScreenWidgetType = 'stat' | 'line' | 'bar' | 'pie' | 'table' | 'map' | 'text';

interface PaletteWidget {
  /** 组件类型。 */
  type: ScreenWidgetType;
  /** 组件名称。 */
  label: string;
  /** 组件用途说明。 */
  description: string;
}

interface ScreenDesignerWidget {
  /** 大屏组件唯一标识。 */
  id: string;
  /** 组件类型。 */
  type: ScreenWidgetType;
  /** 组件标题。 */
  title: string;
  /** 组件横向位置。 */
  x: number;
  /** 组件纵向位置。 */
  y: number;
  /** 组件宽度。 */
  w: number;
  /** 组件高度。 */
  h: number;
  /** 绑定数据集编码。 */
  datasetCode: string;
  /** 维度字段。 */
  fieldX: string;
  /** 指标字段。 */
  fieldY: string;
  /** 数值字段。 */
  fieldValue: string;
  /** 文本内容。 */
  content: string;
}

interface ScreenLayoutConfig {
  /** 设计器配置版本。 */
  version: number;
  /** 大屏尺寸配置。 */
  viewport: { width: number; height: number };
  /** 大屏组件列表。 */
  widgets: ScreenDesignerWidget[];
}

interface DatasetFieldOption {
  /** 字段展示名称。 */
  label: string;
  /** 字段值。 */
  value: string;
}

/** 大屏列表。 */
const screens = ref<VisualScreen[]>([]);
/** 可绑定数据集。 */
const datasets = ref<VisualDataset[]>([]);
/** 数据集预览缓存。 */
const datasetPreviews = ref<Record<string, VisualDatasetPreview>>({});
/** 大屏状态筛选值。 */
const status = ref<string | undefined>();
/** 列表加载状态。 */
const loading = ref(false);
/** 保存状态。 */
const saving = ref(false);
/** 数据集预览加载状态。 */
const datasetPreviewLoading = ref(false);
/** 错误提示。 */
const errorMessage = ref('');
/** 大屏表单弹窗打开状态。 */
const formOpen = ref(false);
/** 当前选中组件主键。 */
const selectedWidgetId = ref('');
/** 大屏组件列表。 */
const screenWidgets = ref<ScreenDesignerWidget[]>([]);
/** 大屏表单状态。 */
const formState = reactive<VisualScreenSavePayload>({
  tenantId: '',
  screenCode: '',
  screenName: '',
  layoutJson: '',
  status: 'draft',
});

/** 可拖拽大屏组件。 */
const paletteWidgets: PaletteWidget[] = [
  { type: 'stat', label: '指标卡', description: '实时业务指标' },
  { type: 'line', label: '趋势折线', description: '按时间观察变化' },
  { type: 'bar', label: '排行柱图', description: '排行和对比' },
  { type: 'pie', label: '占比环图', description: '结构占比' },
  { type: 'table', label: '滚动列表', description: '明细或告警列表' },
  { type: 'map', label: '地图热区', description: '区域分布' },
  { type: 'text', label: '标题文本', description: '大屏标题说明' },
];

/** 大屏表格列。 */
const columns = [
  { title: '大屏编码', dataIndex: 'screenCode', key: 'screenCode', width: 160 },
  { title: '大屏名称', dataIndex: 'screenName', key: 'screenName', width: 170 },
  { title: '布局摘要', dataIndex: 'layoutJson', key: 'layoutJson' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 110 },
  { title: '操作', key: 'action', width: 210 },
];

/** 数据集下拉选项。 */
const datasetOptions = computed(() =>
  datasets.value.map((dataset) => ({
    label: `${dataset.datasetName}（${dataset.datasetCode}）`,
    value: dataset.datasetCode,
  })),
);

/** 当前选中大屏组件。 */
const selectedWidget = computed(() =>
  screenWidgets.value.find((widget) => widget.id === selectedWidgetId.value),
);

/** 当前组件可选字段。 */
const selectedDatasetFieldOptions = computed(() =>
  selectedWidget.value ? fieldSelectOptions(selectedWidget.value.datasetCode) : [],
);

/** 当前组件数据集预览。 */
const selectedDatasetPreview = computed(() => {
  const datasetCode = selectedWidget.value?.datasetCode;
  return datasetCode ? datasetPreviews.value[datasetCode] : undefined;
});

/** 当前组件数据预览表头。 */
const selectedDatasetPreviewColumns = computed(() =>
  selectedDatasetPreview.value?.columns.map((column) => ({
    title: column,
    dataIndex: column,
    key: column,
    width: 120,
  })) ?? [],
);

/**
 * 加载大屏列表。
 */
async function loadScreens(): Promise<void> {
  loading.value = true;
  errorMessage.value = '';
  try {
    screens.value = await listVisualScreens(status.value);
  } catch (error) {
    screens.value = [];
    errorMessage.value = error instanceof Error ? error.message : '大屏加载失败';
  } finally {
    loading.value = false;
  }
}

/**
 * 加载可绑定数据集。
 */
async function loadDatasets(): Promise<void> {
  try {
    datasets.value = await listVisualDatasets('enabled');
  } catch (error) {
    message.error(error instanceof Error ? error.message : '数据集加载失败');
  }
}

/**
 * 打开新增大屏表单。
 */
function openCreateForm(): void {
  resetForm();
  formOpen.value = true;
  void loadDatasets();
}

/**
 * 打开编辑大屏表单。
 *
 * @param screen 当前选中的大屏
 */
function openEditForm(screen: VisualScreen): void {
  formState.tenantId = screen.tenantId;
  formState.screenCode = screen.screenCode;
  formState.screenName = screen.screenName;
  formState.layoutJson = screen.layoutJson || '';
  formState.status = screen.status || 'draft';
  screenWidgets.value = parseScreenWidgets(screen.layoutJson);
  selectedWidgetId.value = screenWidgets.value[0]?.id ?? '';
  formOpen.value = true;
  void loadDatasets();
  void loadDatasetPreview(selectedWidget.value?.datasetCode);
}

/**
 * 提交大屏表单。
 */
async function submitForm(): Promise<void> {
  if (!validateForm()) {
    return;
  }

  saving.value = true;
  try {
    await saveVisualScreen({
      tenantId: requireAdminTenantId(),
      screenCode: formState.screenCode.trim(),
      screenName: formState.screenName.trim(),
      layoutJson: serializeScreenLayout(),
      status: formState.status || 'draft',
    });
    message.success('大屏已保存');
    formOpen.value = false;
    await loadScreens();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '大屏保存失败');
  } finally {
    saving.value = false;
  }
}

/**
 * 重置大屏表单。
 */
function resetForm(): void {
  formState.tenantId = '';
  formState.screenCode = '';
  formState.screenName = '';
  formState.layoutJson = '';
  formState.status = 'draft';
  screenWidgets.value = [];
  selectedWidgetId.value = '';
}

/**
 * 校验大屏表单。
 *
 * @returns 表单是否可以提交
 */
function validateForm(): boolean {
  if (!formState.screenCode.trim()) {
    message.warning('请输入大屏编码');
    return false;
  }
  if (!formState.screenName.trim()) {
    message.warning('请输入大屏名称');
    return false;
  }
  if (screenWidgets.value.length === 0) {
    message.warning('请先拖拽至少一个大屏组件');
    return false;
  }
  return true;
}

/**
 * 发布大屏。
 *
 * @param screen 大屏
 */
function publishScreen(screen: VisualScreen): void {
  Modal.confirm({
    title: '确认发布大屏',
    content: `发布后将对外展示：${screen.screenName}`,
    okText: '发布',
    cancelText: '取消',
    async onOk() {
      await changeVisualScreenStatus(screen.id, 'published');
      message.success('大屏已发布');
      await loadScreens();
      window.open(publishedScreenUrl(screen), '_blank', 'noopener,noreferrer');
    },
  });
}

/**
 * 下线大屏。
 *
 * @param screen 大屏
 */
function offlineScreen(screen: VisualScreen): void {
  Modal.confirm({
    title: '确认下线大屏',
    content: `下线后将停止展示：${screen.screenName}`,
    okText: '下线',
    cancelText: '取消',
    async onOk() {
      await changeVisualScreenStatus(screen.id, 'offline');
      message.success('大屏已下线');
      await loadScreens();
    },
  });
}

/**
 * 生成已发布大屏公开访问地址。
 *
 * @param screen 大屏
 * @returns 公开访问地址
 */
function publishedScreenUrl(screen: VisualScreen): string {
  return `${window.location.origin}/visual/public/screens/${encodeURIComponent(screen.tenantId)}/${encodeURIComponent(screen.screenCode)}`;
}

/**
 * 打开已发布大屏公开访问页面。
 *
 * @param screen 大屏
 */
function openPublishedScreen(screen: VisualScreen): void {
  if (screen.status !== 'published') {
    message.warning('请先发布大屏后再访问');
    return;
  }
  window.open(publishedScreenUrl(screen), '_blank', 'noopener,noreferrer');
}

/**
 * 组件库拖拽开始。
 *
 * @param event 拖拽事件
 * @param type 组件类型
 */
function handlePaletteDragStart(event: DragEvent, type: ScreenWidgetType): void {
  event.dataTransfer?.setData('widgetType', type);
}

/**
 * 画布组件拖拽开始。
 *
 * @param event 拖拽事件
 * @param widgetId 组件主键
 */
function handleWidgetDragStart(event: DragEvent, widgetId: string): void {
  selectWidget(widgetId);
  event.dataTransfer?.setData('widgetId', widgetId);
}

/**
 * 大屏组件拖入画布。
 *
 * @param event 拖拽事件
 */
function handleCanvasDrop(event: DragEvent): void {
  const widgetId = event.dataTransfer?.getData('widgetId');
  const widgetType = event.dataTransfer?.getData('widgetType') as ScreenWidgetType;
  const point = resolveCanvasPoint(event);
  if (widgetId) {
    const target = screenWidgets.value.find((widget) => widget.id === widgetId);
    if (target) {
      target.x = point.x;
      target.y = point.y;
    }
    return;
  }
  if (!widgetType) {
    return;
  }
  const widget = createScreenWidget(widgetType, point.x, point.y);
  screenWidgets.value.push(widget);
  selectedWidgetId.value = widget.id;
  void loadDatasetPreview(widget.datasetCode);
}

/**
 * 选中大屏组件。
 *
 * @param widgetId 组件主键
 */
function selectWidget(widgetId: string): void {
  selectedWidgetId.value = widgetId;
  void loadDatasetPreview(selectedWidget.value?.datasetCode);
}

/**
 * 删除当前选中组件。
 */
function removeSelectedWidget(): void {
  screenWidgets.value = screenWidgets.value.filter((widget) => widget.id !== selectedWidgetId.value);
  selectedWidgetId.value = screenWidgets.value[0]?.id ?? '';
}

/**
 * 当前组件对齐到 20 像素网格。
 */
function alignSelectedWidget(): void {
  if (!selectedWidget.value) {
    return;
  }
  selectedWidget.value.x = Math.round(selectedWidget.value.x / 20) * 20;
  selectedWidget.value.y = Math.round(selectedWidget.value.y / 20) * 20;
}

/**
 * 处理当前组件数据集变化。
 */
function handleSelectedDatasetChange(): void {
  if (!selectedWidget.value) {
    return;
  }
  selectedWidget.value.fieldX = '';
  selectedWidget.value.fieldY = '';
  selectedWidget.value.fieldValue = '';
  void loadDatasetPreview(selectedWidget.value.datasetCode);
}

/**
 * 创建大屏组件。
 *
 * @param type 组件类型
 * @param x 横向位置
 * @param y 纵向位置
 * @returns 大屏组件
 */
function createScreenWidget(type: ScreenWidgetType, x: number, y: number): ScreenDesignerWidget {
  const defaultTitle: Record<ScreenWidgetType, string> = {
    stat: '实时指标',
    line: '趋势监控',
    bar: '排行对比',
    pie: '占比结构',
    table: '告警列表',
    map: '区域分布',
    text: '大屏标题',
  };
  return {
    id: `screen_${Date.now()}_${Math.random().toString(16).slice(2)}`,
    type,
    title: defaultTitle[type],
    x,
    y,
    w: type === 'stat' ? 240 : 360,
    h: type === 'stat' ? 150 : 240,
    datasetCode: '',
    fieldX: '',
    fieldY: '',
    fieldValue: '',
    content: '',
  };
}

/**
 * 解析大屏布局组件。
 *
 * @param layoutJson 后端保存的布局 JSON
 * @returns 大屏组件
 */
function parseScreenWidgets(layoutJson: string): ScreenDesignerWidget[] {
  try {
    const parsed = JSON.parse(layoutJson || '{}') as Partial<ScreenLayoutConfig> | ScreenDesignerWidget[];
    if (Array.isArray(parsed)) {
      return parsed.map(normalizeWidget);
    }
    if (Array.isArray(parsed.widgets)) {
      return parsed.widgets.map(normalizeWidget);
    }
  } catch {
    // 兼容历史手写 JSON，解析失败时返回空画布。
  }
  return [];
}

/**
 * 规整历史大屏组件字段。
 *
 * @param widget 历史组件
 * @returns 大屏组件
 */
function normalizeWidget(widget: Partial<ScreenDesignerWidget>): ScreenDesignerWidget {
  return {
    id: widget.id || `screen_${Date.now()}_${Math.random().toString(16).slice(2)}`,
    type: widget.type || 'stat',
    title: widget.title || '大屏组件',
    x: Number(widget.x ?? 40),
    y: Number(widget.y ?? 40),
    w: Number(widget.w ?? 360),
    h: Number(widget.h ?? 240),
    datasetCode: widget.datasetCode || '',
    fieldX: widget.fieldX || '',
    fieldY: widget.fieldY || '',
    fieldValue: widget.fieldValue || '',
    content: widget.content || '',
  };
}

/**
 * 序列化大屏布局。
 *
 * @returns 布局 JSON 字符串
 */
function serializeScreenLayout(): string {
  return JSON.stringify({
    version: 1,
    viewport: { width: 1920, height: 1080 },
    widgets: screenWidgets.value,
  } satisfies ScreenLayoutConfig);
}

/**
 * 根据数据集编码生成字段下拉项。
 *
 * @param datasetCode 数据集编码
 * @returns 字段下拉项
 */
function fieldSelectOptions(datasetCode: string): DatasetFieldOption[] {
  const preview = datasetPreviews.value[datasetCode];
  if (preview?.columns.length) {
    return preview.columns.map((field) => ({ label: field, value: field }));
  }
  const dataset = datasets.value.find((item) => item.datasetCode === datasetCode);
  return dataset ? parseDatasetFields(dataset.sqlText) : [];
}

/**
 * 获取大屏组件绑定数据集的预览数据行。
 *
 * @param widget 大屏组件
 * @returns 预览数据行
 */
function widgetPreviewRows(widget: ScreenDesignerWidget): Record<string, unknown>[] {
  return datasetPreviews.value[widget.datasetCode]?.rows.slice(0, 3) ?? [];
}

/**
 * 获取大屏表格组件预览列。
 *
 * @param widget 大屏组件
 * @returns 表格列字段
 */
function widgetTableColumns(widget: ScreenDesignerWidget): string[] {
  const configuredColumns = [widget.fieldX, widget.fieldY, widget.fieldValue].filter(Boolean);
  if (configuredColumns.length > 0) {
    return [...new Set(configuredColumns)].slice(0, 3);
  }
  const previewColumns = datasetPreviews.value[widget.datasetCode]?.columns ?? [];
  return previewColumns.length > 0 ? previewColumns.slice(0, 3) : ['区域', '数值'];
}

/**
 * 获取大屏指标卡预览值。
 *
 * @param widget 大屏组件
 * @returns 指标展示值
 */
function widgetMetricValue(widget: ScreenDesignerWidget): string {
  const preview = datasetPreviews.value[widget.datasetCode];
  const field = widget.fieldValue || widget.fieldY || preview?.columns[0] || '';
  const value = field ? preview?.rows[0]?.[field] : undefined;
  return formatPreviewCell(value ?? widget.fieldValue ?? '实时指标');
}

/**
 * 生成大屏图表预览柱形高度。
 *
 * @param widget 大屏组件
 * @returns 百分比高度列表
 */
function widgetChartBars(widget: ScreenDesignerWidget): number[] {
  const preview = datasetPreviews.value[widget.datasetCode];
  const valueField = widget.fieldY || widget.fieldValue || findNumericField(preview);
  const values = valueField
    ? preview?.rows.map((row) => Number(row[valueField])).filter((value) => Number.isFinite(value)) ?? []
    : [];
  if (values.length === 0) {
    return [40, 72, 56, 88, 60, 76];
  }
  const max = Math.max(...values, 1);
  return values.slice(0, 8).map((value) => Math.max(18, Math.round((value / max) * 88)));
}

/**
 * 查找预览数据里的首个数值字段。
 *
 * @param preview 数据集预览
 * @returns 数值字段
 */
function findNumericField(preview?: VisualDatasetPreview): string {
  return preview?.columns.find((column) => preview.rows.some((row) => Number.isFinite(Number(row[column])))) ?? '';
}

/**
 * 格式化预览单元格展示值。
 *
 * @param value 原始预览值
 * @returns 展示文本
 */
function formatPreviewCell(value: unknown): string {
  if (value === null || value === undefined || value === '') {
    return '-';
  }
  return String(value);
}

/**
 * 加载数据集预览，失败时保留 SQL 解析兜底字段。
 *
 * @param datasetCode 数据集编码
 */
async function loadDatasetPreview(datasetCode?: string): Promise<void> {
  if (!datasetCode || datasetPreviews.value[datasetCode]) {
    return;
  }
  datasetPreviewLoading.value = true;
  try {
    const preview = await previewVisualDataset(datasetCode, 10);
    datasetPreviews.value = {
      ...datasetPreviews.value,
      [datasetCode]: {
        ...preview,
        rows: preview.rows.map((row, index) => ({ __rowKey: `${datasetCode}_${index}`, ...row })),
      },
    };
  } catch (error) {
    message.warning(error instanceof Error ? error.message : '数据集预览加载失败，已使用 SQL 字段兜底');
  } finally {
    datasetPreviewLoading.value = false;
  }
}

/**
 * 从 SQL 中解析 select 字段和别名。
 *
 * @param sqlText 数据集查询 SQL
 * @returns 字段下拉项
 */
function parseDatasetFields(sqlText: string): DatasetFieldOption[] {
  const selectMatch = sqlText.match(/select\s+([\s\S]+?)\s+from\s+/i);
  if (!selectMatch) {
    return [];
  }
  return splitSqlSelectFields(selectMatch[1])
    .map(resolveSqlFieldName)
    .filter((field): field is string => Boolean(field))
    .map((field) => ({ label: field, value: field }));
}

/**
 * 拆分 SQL select 字段，避免函数参数里的逗号被误拆。
 *
 * @param selectPart select 和 from 之间的 SQL 片段
 * @returns 字段片段
 */
function splitSqlSelectFields(selectPart: string): string[] {
  const fields: string[] = [];
  let depth = 0;
  let current = '';
  for (const char of selectPart) {
    if (char === '(') {
      depth += 1;
    }
    if (char === ')') {
      depth = Math.max(0, depth - 1);
    }
    if (char === ',' && depth === 0) {
      fields.push(current.trim());
      current = '';
      continue;
    }
    current += char;
  }
  if (current.trim()) {
    fields.push(current.trim());
  }
  return fields;
}

/**
 * 解析单个 SQL 字段片段的输出字段名。
 *
 * @param fieldExpression 字段表达式
 * @returns 输出字段名
 */
function resolveSqlFieldName(fieldExpression: string): string {
  const normalized = fieldExpression.replace(/[`"]/g, '').trim();
  const aliasMatch = normalized.match(/\s+as\s+([a-zA-Z_][\w]*)$/i) ?? normalized.match(/\s+([a-zA-Z_][\w]*)$/);
  if (aliasMatch) {
    return aliasMatch[1];
  }
  const parts = normalized.split('.');
  return parts[parts.length - 1]?.replace(/\W/g, '') ?? '';
}

/**
 * 计算组件样式。
 *
 * @param widget 大屏组件
 * @returns CSS 样式
 */
function widgetStyle(widget: ScreenDesignerWidget): Record<string, string> {
  return {
    left: `${widget.x}px`,
    top: `${widget.y}px`,
    width: `${widget.w}px`,
    height: `${widget.h}px`,
  };
}

/**
 * 计算拖拽落点。
 *
 * @param event 拖拽事件
 * @returns 画布坐标
 */
function resolveCanvasPoint(event: DragEvent): { x: number; y: number } {
  const target = event.currentTarget as HTMLElement;
  const rect = target.getBoundingClientRect();
  return {
    x: Math.max(0, Math.round(event.clientX - rect.left - 40)),
    y: Math.max(0, Math.round(event.clientY - rect.top - 30)),
  };
}

/**
 * 布局摘要。
 *
 * @param layoutJson 布局 JSON
 * @returns 摘要
 */
function layoutSummary(layoutJson: string): string {
  try {
    const parsed = JSON.parse(layoutJson || '{}') as Partial<ScreenLayoutConfig> | ScreenDesignerWidget[];
    if (Array.isArray(parsed)) {
      return `${parsed.length} 个大屏组件`;
    }
    return Array.isArray(parsed.widgets) ? `${parsed.widgets.length} 个大屏组件` : '兼容旧布局';
  } catch {
    return '布局待修复';
  }
}

onMounted(() => {
  void loadScreens();
});
</script>

<style scoped>
.visual-page {
  min-width: 0;
}

.state-alert {
  margin-bottom: 12px;
}

.status-select {
  width: 130px;
}

.full-input {
  width: 100%;
}

.config-summary {
  color: #64748b;
}

.designer-steps {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  margin-bottom: 14px;
}

.designer-steps span {
  padding: 8px 10px;
  border: 1px solid #bfdbfe;
  border-radius: 6px;
  background: #eff6ff;
  color: #1d4ed8;
  text-align: center;
  font-weight: 600;
}

.base-form {
  margin-bottom: 14px;
  padding-bottom: 14px;
  border-bottom: 1px solid #eef2f7;
  row-gap: 10px;
}

.designer-shell {
  display: grid;
  grid-template-columns: 190px minmax(0, 1fr) 280px;
  min-height: 660px;
  border: 1px solid #dbe3ef;
  border-radius: 8px;
  overflow: hidden;
}

.palette-panel,
.property-panel {
  min-width: 0;
  padding: 12px;
  background: #f8fafc;
}

.palette-panel {
  border-right: 1px solid #dbe3ef;
}

.property-panel {
  border-left: 1px solid #dbe3ef;
}

.panel-title {
  margin-bottom: 10px;
  color: #111827;
  font-size: 14px;
  font-weight: 700;
}

.palette-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  width: 100%;
  margin-bottom: 8px;
  padding: 10px;
  border: 1px solid #dbe3ef;
  border-radius: 6px;
  background: #fff;
  color: #111827;
  text-align: left;
  cursor: grab;
}

.palette-item span {
  color: #64748b;
  font-size: 12px;
}

.canvas-panel {
  min-width: 0;
  background: #fff;
}

.canvas-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px;
  border-bottom: 1px solid #dbe3ef;
}

.canvas-header div {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.canvas-header span {
  color: #64748b;
  font-size: 12px;
}

.screen-canvas {
  position: relative;
  min-height: 600px;
  overflow: auto;
  background:
    linear-gradient(135deg, rgb(15 23 42 / 5%) 25%, transparent 25%) -10px 0 / 20px 20px,
    linear-gradient(225deg, rgb(15 23 42 / 5%) 25%, transparent 25%) -10px 0 / 20px 20px,
    #eef4ff;
}

.screen-canvas :deep(.ant-empty) {
  padding-top: 200px;
}

.screen-widget {
  position: absolute;
  display: flex;
  flex-direction: column;
  border: 1px solid #bfdbfe;
  border-radius: 8px;
  background: rgb(255 255 255 / 94%);
  box-shadow: 0 12px 28px rgb(15 23 42 / 10%);
  cursor: move;
}

.screen-widget.active {
  border-color: #2563eb;
  box-shadow: 0 0 0 2px rgb(37 99 235 / 18%);
}

.widget-title {
  padding: 8px 10px;
  border-bottom: 1px solid #dbeafe;
  color: #111827;
  font-weight: 700;
}

.widget-body {
  flex: 1;
  min-height: 0;
  padding: 10px;
  color: #475569;
}

.widget-body strong {
  display: block;
  color: #1d4ed8;
  font-size: 28px;
}

.widget-body table {
  width: 100%;
  border-collapse: collapse;
}

.widget-body th,
.widget-body td {
  padding: 6px;
  border: 1px solid #dbeafe;
}

.widget-body th {
  background: #eff6ff;
  color: #334155;
  font-weight: 700;
}

.chart-preview {
  display: flex;
  align-items: end;
  gap: 8px;
  height: 100%;
  min-height: 110px;
}

.chart-preview i {
  flex: 1;
  border-radius: 5px 5px 0 0;
  background: linear-gradient(180deg, #38bdf8, #2563eb);
}

.chart-preview i:nth-child(1) { height: 40%; }
.chart-preview i:nth-child(2) { height: 72%; }
.chart-preview i:nth-child(3) { height: 56%; }
.chart-preview i:nth-child(4) { height: 88%; }
.chart-preview i:nth-child(5) { height: 60%; }
.chart-preview i:nth-child(6) { height: 76%; }

.chart-preview.line {
  align-items: center;
}

.chart-preview.line i {
  height: 3px;
  border-radius: 999px;
}

.chart-preview.pie {
  width: 130px;
  height: 130px;
  margin: 10px auto;
  border-radius: 50%;
  background: conic-gradient(#2563eb 0 40%, #06b6d4 40% 68%, #22c55e 68% 100%);
}

.chart-preview.pie i {
  display: none;
}

.map-preview {
  position: relative;
  display: grid;
  place-items: center;
  height: 100%;
  min-height: 140px;
  border-radius: 8px;
  background: radial-gradient(circle at 50% 50%, #93c5fd 0 16%, #dbeafe 17% 100%);
  color: #1d4ed8;
  font-weight: 700;
}

.map-preview i {
  position: absolute;
  right: 28%;
  top: 32%;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #ef4444;
  box-shadow: 0 0 0 8px rgb(239 68 68 / 15%);
}

.preview-card {
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid #dbe3ef;
  color: #64748b;
}

.field-preview {
  margin-bottom: 14px;
  padding: 10px;
  border: 1px dashed #bfdbfe;
  border-radius: 6px;
  background: #f8fbff;
}

.field-preview > span {
  display: block;
  margin-bottom: 8px;
  color: #475569;
  font-size: 13px;
  font-weight: 700;
}

.field-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.preview-card code {
  color: #2563eb;
}

.permission-code {
  display: none;
}
</style>
