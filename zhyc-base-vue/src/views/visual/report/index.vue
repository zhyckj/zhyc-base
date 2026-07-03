<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="visual-page">
    <a-card title="报表设计器" :bordered="false">
      <template #extra>
        <a-space>
          <a-select v-model:value="status" class="status-select" allow-clear placeholder="状态">
            <a-select-option value="draft">草稿</a-select-option>
            <a-select-option value="published">已发布</a-select-option>
            <a-select-option value="offline">已下线</a-select-option>
            <a-select-option value="enabled">启用</a-select-option>
            <a-select-option value="disabled">停用</a-select-option>
          </a-select>
          <a-button :loading="loading" @click="loadReports">刷新</a-button>
          <a-button type="primary" @click="openCreateForm">新增报表</a-button>
        </a-space>
      </template>

      <a-alert v-if="errorMessage" class="state-alert" :message="errorMessage" type="error" show-icon />

      <a-table
        row-key="reportCode"
        size="small"
        :columns="columns"
        :data-source="reports"
        :loading="loading"
        :pagination="$tablePagination"
        :locale="{ emptyText: '暂无报表。' }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="reportStatusColor(record.status)">
              {{ reportStatusLabel(record.status) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'chartType'">
            <a-tag color="blue">{{ chartTypeLabel(record.chartType) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'configJson'">
            <span class="config-summary">{{ configSummary(record.configJson) }}</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button size="small" @click="openEditForm(record)">设计</a-button>
              <a-button
                size="small"
                type="primary"
                :disabled="record.status === 'published'"
                @click="publishReport(record)"
              >
                发布
              </a-button>
              <a-button size="small" @click="offlineReport(record)">
                下线
              </a-button>
              <a-button
                size="small"
                :disabled="record.status !== 'published'"
                @click="openPublishedReport(record)"
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
      title="在线报表设计器"
      width="1280px"
      :confirm-loading="saving"
      @ok="submitForm"
    >
      <div class="designer-steps">
        <span>基础信息</span>
        <span>拖拽布局</span>
        <span>数据绑定</span>
        <span>预览发布</span>
      </div>

      <a-form layout="inline" class="base-form">
        <a-form-item label="报表编码" required>
          <a-input v-model:value="formState.reportCode" placeholder="例如 order_chart" />
        </a-form-item>
        <a-form-item label="报表名称" required>
          <a-input v-model:value="formState.reportName" placeholder="请输入报表名称" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="formState.status" class="status-select">
            <a-select-option value="draft">草稿</a-select-option>
            <a-select-option value="published">已发布</a-select-option>
            <a-select-option value="offline">已下线</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>

      <div class="designer-shell">
        <aside class="palette-panel">
          <div class="panel-title">组件库</div>
          <button
            v-for="item in paletteComponents"
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
              <strong>设计画布</strong>
              <span>拖拽组件到画布，选中后在右侧配置数据绑定。</span>
            </div>
            <a-space>
              <a-button size="small" @click="alignSelectedComponent">对齐网格</a-button>
              <a-button size="small" danger :disabled="!selectedComponent" @click="removeSelectedComponent">删除组件</a-button>
            </a-space>
          </div>
          <div class="report-canvas" @dragover.prevent @drop="handleCanvasDrop">
            <a-empty v-if="designerComponents.length === 0" description="从左侧组件库拖拽组件开始设计报表" />
            <div
              v-for="component in designerComponents"
              :key="component.id"
              class="report-widget"
              :class="{ active: component.id === selectedComponentId }"
              :style="componentStyle(component)"
              draggable="true"
              @click.stop="selectComponent(component.id)"
              @dragstart="handleWidgetDragStart($event, component.id)"
            >
              <div class="widget-title">{{ component.title }}</div>
              <div class="widget-body">
                <template v-if="component.type === 'metric'">
                  <strong>{{ componentMetricValue(component) }}</strong>
                  <span>{{ component.datasetCode || '未绑定数据集' }}</span>
                </template>
                <template v-else-if="component.type === 'table'">
                  <table>
                    <thead>
                      <tr>
                        <th v-for="column in componentTableColumns(component)" :key="column">{{ column }}</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="(row, rowIndex) in componentPreviewRows(component)" :key="rowIndex">
                        <td v-for="column in componentTableColumns(component)" :key="column">
                          {{ formatPreviewCell(row[column]) }}
                        </td>
                      </tr>
                      <tr v-if="componentPreviewRows(component).length === 0">
                        <td>{{ component.fieldX || '字段A' }}</td>
                        <td>{{ component.fieldY || '字段B' }}</td>
                      </tr>
                    </tbody>
                  </table>
                </template>
                <template v-else-if="component.type === 'pie'">
                  <div class="pie-preview"></div>
                </template>
                <template v-else-if="component.type === 'text'">
                  <p>{{ component.content || '请输入说明文字' }}</p>
                </template>
                <template v-else>
                  <div class="chart-preview" :class="component.type">
                    <i
                      v-for="(bar, index) in componentChartBars(component)"
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
          <a-empty v-if="!selectedComponent" description="请选择画布组件" />
          <a-form v-else layout="vertical">
            <a-form-item label="组件标题">
              <a-input v-model:value="selectedComponent.title" />
            </a-form-item>
            <a-form-item label="数据绑定">
              <a-select
                v-model:value="selectedComponent.datasetCode"
                allow-clear
                show-search
                option-filter-prop="label"
                :options="datasetOptions"
                placeholder="选择报表数据集"
                @change="handleSelectedDatasetChange"
              />
            </a-form-item>
            <a-form-item label="维度字段">
              <a-select
                v-model:value="selectedComponent.fieldX"
                allow-clear
                show-search
                option-filter-prop="label"
                :options="selectedDatasetFieldOptions"
                placeholder="选择维度字段"
              />
            </a-form-item>
            <a-form-item label="指标字段">
              <a-select
                v-model:value="selectedComponent.fieldY"
                allow-clear
                show-search
                option-filter-prop="label"
                :options="selectedDatasetFieldOptions"
                placeholder="选择指标字段"
              />
            </a-form-item>
            <a-form-item label="展示值字段">
              <a-select
                v-model:value="selectedComponent.fieldValue"
                allow-clear
                show-search
                option-filter-prop="label"
                :options="selectedDatasetFieldOptions"
                placeholder="选择展示值字段"
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
            <a-form-item v-if="selectedComponent.type === 'text'" label="文本内容">
              <a-textarea v-model:value="selectedComponent.content" :rows="3" />
            </a-form-item>
            <a-row :gutter="8">
              <a-col :span="12">
                <a-form-item label="宽度">
                  <a-input-number v-model:value="selectedComponent.w" :min="160" :max="760" class="full-input" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="高度">
                  <a-input-number v-model:value="selectedComponent.h" :min="120" :max="420" class="full-input" />
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>

          <div class="preview-card">
            <div class="panel-title">预览发布</div>
            <p>保存后会把画布组件、数据集和字段映射写入配置 JSON。</p>
            <code>{{ designerComponents.length }} 个组件</code>
          </div>
        </aside>
      </div>

      <span class="permission-code">visual:report:save visual:report:publish</span>
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { Modal, message } from 'ant-design-vue';
import { computed, onMounted, reactive, ref } from 'vue';

import {
  changeVisualReportStatus,
  listVisualDatasets,
  listVisualReports,
  previewVisualDataset,
  saveVisualReport,
  type VisualDataset,
  type VisualDatasetPreview,
  type VisualReport,
  type VisualReportSavePayload,
} from '@/api/visual/report';
import { requireAdminTenantId } from '@/utils/adminContext';

type ReportComponentType = 'metric' | 'line' | 'bar' | 'pie' | 'table' | 'text';

interface PaletteComponent {
  /** 组件类型。 */
  type: ReportComponentType;
  /** 组件名称。 */
  label: string;
  /** 组件用途说明。 */
  description: string;
}

interface ReportDesignerComponent {
  /** 画布组件唯一标识。 */
  id: string;
  /** 组件类型。 */
  type: ReportComponentType;
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
  /** 绑定的数据集编码。 */
  datasetCode: string;
  /** 维度字段。 */
  fieldX: string;
  /** 指标字段。 */
  fieldY: string;
  /** 展示值字段。 */
  fieldValue: string;
  /** 文本组件内容。 */
  content: string;
}

interface ReportDesignerConfig {
  /** 设计器配置版本。 */
  version: number;
  /** 报表组件列表。 */
  components: ReportDesignerComponent[];
}

interface DatasetFieldOption {
  /** 字段展示名称。 */
  label: string;
  /** 字段值。 */
  value: string;
}

/** 报表列表。 */
const reports = ref<VisualReport[]>([]);
/** 可绑定数据集列表。 */
const datasets = ref<VisualDataset[]>([]);
/** 数据集预览缓存。 */
const datasetPreviews = ref<Record<string, VisualDatasetPreview>>({});
/** 报表状态筛选值。 */
const status = ref<string | undefined>();
/** 列表加载状态。 */
const loading = ref(false);
/** 保存状态。 */
const saving = ref(false);
/** 数据集预览加载状态。 */
const datasetPreviewLoading = ref(false);
/** 错误提示。 */
const errorMessage = ref('');
/** 报表表单弹窗打开状态。 */
const formOpen = ref(false);
/** 当前选中组件主键。 */
const selectedComponentId = ref('');
/** 报表设计器组件。 */
const designerComponents = ref<ReportDesignerComponent[]>([]);
/** 报表表单状态。 */
const formState = reactive<VisualReportSavePayload>({
  tenantId: '',
  reportCode: '',
  reportName: '',
  datasetCode: '',
  chartType: 'line',
  configJson: '',
  status: 'draft',
});

/** 可拖拽组件库。 */
const paletteComponents: PaletteComponent[] = [
  { type: 'metric', label: '指标卡', description: '展示核心 KPI' },
  { type: 'line', label: '折线图', description: '趋势分析' },
  { type: 'bar', label: '柱状图', description: '分类对比' },
  { type: 'pie', label: '饼图', description: '占比结构' },
  { type: 'table', label: '明细表格', description: '字段列表' },
  { type: 'text', label: '文本说明', description: '标题或备注' },
];

/** 报表表格列。 */
const columns = [
  { title: '报表编码', dataIndex: 'reportCode', key: 'reportCode', width: 160 },
  { title: '报表名称', dataIndex: 'reportName', key: 'reportName', width: 160 },
  { title: '数据集', dataIndex: 'datasetCode', key: 'datasetCode', width: 150 },
  { title: '图表类型', dataIndex: 'chartType', key: 'chartType', width: 110 },
  { title: '配置摘要', dataIndex: 'configJson', key: 'configJson' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 210 },
];

/** 数据集下拉选项。 */
const datasetOptions = computed(() =>
  datasets.value.map((dataset) => ({
    label: `${dataset.datasetName}（${dataset.datasetCode}）`,
    value: dataset.datasetCode,
  })),
);

/** 当前选中的设计器组件。 */
const selectedComponent = computed(() =>
  designerComponents.value.find((component) => component.id === selectedComponentId.value),
);

/** 当前组件可选字段。 */
const selectedDatasetFieldOptions = computed(() =>
  selectedComponent.value ? fieldSelectOptions(selectedComponent.value.datasetCode) : [],
);

/** 当前组件数据集预览。 */
const selectedDatasetPreview = computed(() => {
  const datasetCode = selectedComponent.value?.datasetCode;
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
 * 加载报表列表。
 */
async function loadReports(): Promise<void> {
  loading.value = true;
  errorMessage.value = '';
  try {
    reports.value = await listVisualReports(status.value);
  } catch (error) {
    reports.value = [];
    errorMessage.value = error instanceof Error ? error.message : '报表加载失败';
  } finally {
    loading.value = false;
  }
}

/**
 * 加载设计器可绑定数据集。
 */
async function loadDatasets(): Promise<void> {
  try {
    datasets.value = await listVisualDatasets('enabled');
  } catch (error) {
    message.error(error instanceof Error ? error.message : '数据集加载失败');
  }
}

/**
 * 打开新增报表表单。
 */
function openCreateForm(): void {
  resetForm();
  formOpen.value = true;
  void loadDatasets();
}

/**
 * 打开编辑报表表单。
 *
 * @param report 当前选中的报表
 */
function openEditForm(report: VisualReport): void {
  formState.tenantId = report.tenantId;
  formState.reportCode = report.reportCode;
  formState.reportName = report.reportName;
  formState.datasetCode = report.datasetCode;
  formState.chartType = report.chartType || 'line';
  formState.configJson = report.configJson || '';
  formState.status = normalizeReportStatus(report.status);
  designerComponents.value = parseDesignerComponents(report.configJson, report);
  selectedComponentId.value = designerComponents.value[0]?.id ?? '';
  formOpen.value = true;
  void loadDatasets();
  void loadDatasetPreview(selectedComponent.value?.datasetCode);
}

/**
 * 提交报表表单。
 */
async function submitForm(): Promise<void> {
  if (!validateForm()) {
    return;
  }

  saving.value = true;
  try {
    const primaryComponent = designerComponents.value[0];
    const primaryDatasetCode = primaryComponent?.datasetCode || formState.datasetCode.trim();
    await saveVisualReport({
      tenantId: requireAdminTenantId(),
      reportCode: formState.reportCode.trim(),
      reportName: formState.reportName.trim(),
      datasetCode: primaryDatasetCode,
      chartType: primaryComponent?.type || formState.chartType || 'table',
      configJson: serializeDesignerConfig(),
      status: formState.status || 'draft',
    });
    message.success('报表已保存');
    formOpen.value = false;
    await loadReports();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '报表保存失败');
  } finally {
    saving.value = false;
  }
}

/**
 * 重置报表表单。
 */
function resetForm(): void {
  formState.tenantId = '';
  formState.reportCode = '';
  formState.reportName = '';
  formState.datasetCode = '';
  formState.chartType = 'line';
  formState.configJson = '';
  formState.status = 'draft';
  designerComponents.value = [];
  selectedComponentId.value = '';
}

/**
 * 校验报表表单。
 *
 * @returns 表单是否可以提交
 */
function validateForm(): boolean {
  if (!formState.reportCode.trim()) {
    message.warning('请输入报表编码');
    return false;
  }
  if (!formState.reportName.trim()) {
    message.warning('请输入报表名称');
    return false;
  }
  if (designerComponents.value.length === 0) {
    message.warning('请先拖拽至少一个报表组件');
    return false;
  }
  if (!designerComponents.value.some((component) => component.datasetCode)) {
    message.warning('请至少为一个组件绑定数据集');
    return false;
  }
  return true;
}

/**
 * 发布报表。
 *
 * @param report 报表
 */
function publishReport(report: VisualReport): void {
  Modal.confirm({
    title: '确认发布报表',
    content: `发布后将开放访问：${report.reportName}`,
    okText: '发布',
    cancelText: '取消',
    async onOk() {
      await changeVisualReportStatus(report.id, 'published');
      message.success('报表已发布');
      await loadReports();
      window.open(publishedReportUrl(report), '_blank', 'noopener,noreferrer');
    },
  });
}

/**
 * 下线报表。
 *
 * @param report 报表
 */
function offlineReport(report: VisualReport): void {
  Modal.confirm({
    title: '确认下线报表',
    content: `下线后将停止公开访问：${report.reportName}`,
    okText: '下线',
    cancelText: '取消',
    async onOk() {
      await changeVisualReportStatus(report.id, 'offline');
      message.success('报表已下线');
      await loadReports();
    },
  });
}

/**
 * 生成已发布报表公开访问地址。
 *
 * @param report 报表
 * @returns 公开访问地址
 */
function publishedReportUrl(report: VisualReport): string {
  return `${window.location.origin}/visual/public/reports/${encodeURIComponent(report.tenantId)}/${encodeURIComponent(report.reportCode)}`;
}

/**
 * 打开已发布报表公开访问页面。
 *
 * @param report 报表
 */
function openPublishedReport(report: VisualReport): void {
  if (report.status !== 'published') {
    message.warning('请先发布报表后再访问');
    return;
  }
  window.open(publishedReportUrl(report), '_blank', 'noopener,noreferrer');
}

/**
 * 组件库拖拽开始。
 *
 * @param event 拖拽事件
 * @param type 组件类型
 */
function handlePaletteDragStart(event: DragEvent, type: ReportComponentType): void {
  event.dataTransfer?.setData('componentType', type);
}

/**
 * 画布组件拖拽开始。
 *
 * @param event 拖拽事件
 * @param componentId 组件主键
 */
function handleWidgetDragStart(event: DragEvent, componentId: string): void {
  selectComponent(componentId);
  event.dataTransfer?.setData('componentId', componentId);
}

/**
 * 组件拖入画布。
 *
 * @param event 拖拽事件
 */
function handleCanvasDrop(event: DragEvent): void {
  const componentId = event.dataTransfer?.getData('componentId');
  const componentType = event.dataTransfer?.getData('componentType') as ReportComponentType;
  const point = resolveCanvasPoint(event);
  if (componentId) {
    const target = designerComponents.value.find((component) => component.id === componentId);
    if (target) {
      target.x = point.x;
      target.y = point.y;
    }
    return;
  }
  if (!componentType) {
    return;
  }
  const component = createDesignerComponent(componentType, point.x, point.y);
  designerComponents.value.push(component);
  selectedComponentId.value = component.id;
  syncPrimaryDataset();
  void loadDatasetPreview(component.datasetCode);
}

/**
 * 选中画布组件。
 *
 * @param componentId 组件主键
 */
function selectComponent(componentId: string): void {
  selectedComponentId.value = componentId;
  void loadDatasetPreview(selectedComponent.value?.datasetCode);
}

/**
 * 删除当前选中组件。
 */
function removeSelectedComponent(): void {
  designerComponents.value = designerComponents.value.filter((component) => component.id !== selectedComponentId.value);
  selectedComponentId.value = designerComponents.value[0]?.id ?? '';
  syncPrimaryDataset();
}

/**
 * 将当前组件位置对齐到 20 像素网格。
 */
function alignSelectedComponent(): void {
  if (!selectedComponent.value) {
    return;
  }
  selectedComponent.value.x = Math.round(selectedComponent.value.x / 20) * 20;
  selectedComponent.value.y = Math.round(selectedComponent.value.y / 20) * 20;
}

/**
 * 同步主数据集到报表基础字段。
 */
function syncPrimaryDataset(): void {
  formState.datasetCode = designerComponents.value.find((component) => component.datasetCode)?.datasetCode ?? '';
}

/**
 * 处理当前组件数据集变化。
 */
function handleSelectedDatasetChange(): void {
  if (!selectedComponent.value) {
    return;
  }
  selectedComponent.value.fieldX = '';
  selectedComponent.value.fieldY = '';
  selectedComponent.value.fieldValue = '';
  syncPrimaryDataset();
  void loadDatasetPreview(selectedComponent.value.datasetCode);
}

/**
 * 创建设计器组件。
 *
 * @param type 组件类型
 * @param x 横向位置
 * @param y 纵向位置
 * @returns 设计器组件
 */
function createDesignerComponent(type: ReportComponentType, x: number, y: number): ReportDesignerComponent {
  const defaultTitle: Record<ReportComponentType, string> = {
    metric: '核心指标',
    line: '趋势图',
    bar: '分类对比',
    pie: '占比分析',
    table: '明细表格',
    text: '说明文本',
  };
  return {
    id: `report_${Date.now()}_${Math.random().toString(16).slice(2)}`,
    type,
    title: defaultTitle[type],
    x,
    y,
    w: type === 'metric' ? 220 : 320,
    h: type === 'metric' ? 140 : 220,
    datasetCode: formState.datasetCode,
    fieldX: '',
    fieldY: '',
    fieldValue: '',
    content: '',
  };
}

/**
 * 解析报表设计器配置。
 *
 * @param configJson 后端保存的配置 JSON
 * @param report 报表基础信息
 * @returns 设计器组件
 */
function parseDesignerComponents(configJson: string, report: VisualReport): ReportDesignerComponent[] {
  try {
    const parsed = JSON.parse(configJson || '{}') as Partial<ReportDesignerConfig>;
    if (Array.isArray(parsed.components)) {
      return parsed.components.map((component) => normalizeComponent(component, report));
    }
  } catch {
    // 兼容历史手写 JSON，解析失败时回退为单组件。
  }
  return [
    createDesignerComponent((report.chartType as ReportComponentType) || 'table', 40, 40),
  ].map((component) => ({
    ...component,
    title: report.reportName,
    datasetCode: report.datasetCode,
  }));
}

/**
 * 规整历史组件字段，避免脏配置破坏设计器。
 *
 * @param component 历史组件
 * @param report 报表基础信息
 * @returns 设计器组件
 */
function normalizeComponent(component: Partial<ReportDesignerComponent>, report: VisualReport): ReportDesignerComponent {
  return {
    id: component.id || `report_${Date.now()}_${Math.random().toString(16).slice(2)}`,
    type: component.type || ((report.chartType as ReportComponentType) ?? 'table'),
    title: component.title || report.reportName,
    x: Number(component.x ?? 40),
    y: Number(component.y ?? 40),
    w: Number(component.w ?? 320),
    h: Number(component.h ?? 220),
    datasetCode: component.datasetCode || report.datasetCode,
    fieldX: component.fieldX || '',
    fieldY: component.fieldY || '',
    fieldValue: component.fieldValue || '',
    content: component.content || '',
  };
}

/**
 * 序列化设计器配置。
 *
 * @returns 配置 JSON 字符串
 */
function serializeDesignerConfig(): string {
  return JSON.stringify({
    version: 1,
    components: designerComponents.value,
  } satisfies ReportDesignerConfig);
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
 * 获取组件绑定数据集的预览数据行。
 *
 * @param component 设计器组件
 * @returns 预览数据行
 */
function componentPreviewRows(component: ReportDesignerComponent): Record<string, unknown>[] {
  return datasetPreviews.value[component.datasetCode]?.rows.slice(0, 3) ?? [];
}

/**
 * 获取组件表格预览列。
 *
 * @param component 设计器组件
 * @returns 表格列字段
 */
function componentTableColumns(component: ReportDesignerComponent): string[] {
  const configuredColumns = [component.fieldX, component.fieldY, component.fieldValue].filter(Boolean);
  if (configuredColumns.length > 0) {
    return [...new Set(configuredColumns)].slice(0, 3);
  }
  const previewColumns = datasetPreviews.value[component.datasetCode]?.columns ?? [];
  return previewColumns.length > 0 ? previewColumns.slice(0, 3) : ['字段A', '字段B'];
}

/**
 * 获取指标卡预览值。
 *
 * @param component 设计器组件
 * @returns 指标展示值
 */
function componentMetricValue(component: ReportDesignerComponent): string {
  const preview = datasetPreviews.value[component.datasetCode];
  const field = component.fieldValue || component.fieldY || preview?.columns[0] || '';
  const value = field ? preview?.rows[0]?.[field] : undefined;
  return formatPreviewCell(value ?? component.fieldValue ?? '指标值');
}

/**
 * 生成组件图表预览柱形高度。
 *
 * @param component 设计器组件
 * @returns 百分比高度列表
 */
function componentChartBars(component: ReportDesignerComponent): number[] {
  const preview = datasetPreviews.value[component.datasetCode];
  const valueField = component.fieldY || component.fieldValue || findNumericField(preview);
  const values = valueField
    ? preview?.rows.map((row) => Number(row[valueField])).filter((value) => Number.isFinite(value)) ?? []
    : [];
  if (values.length === 0) {
    return [38, 66, 50, 82, 58];
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
 * @param component 设计器组件
 * @returns CSS 样式
 */
function componentStyle(component: ReportDesignerComponent): Record<string, string> {
  return {
    left: `${component.x}px`,
    top: `${component.y}px`,
    width: `${component.w}px`,
    height: `${component.h}px`,
  };
}

/**
 * 计算拖拽落点。
 *
 * @param event 拖拽事件
 * @returns 画布内坐标
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
 * 规整报表状态，兼容早期启停状态。
 *
 * @param value 后端返回或表单携带的状态
 * @returns 可展示的报表状态
 */
function normalizeReportStatus(value?: string): string {
  if (value === 'enabled') {
    return 'published';
  }
  if (value === 'disabled') {
    return 'offline';
  }
  return value || 'draft';
}

/**
 * 获取报表状态中文名称。
 *
 * @param value 报表状态
 * @returns 中文状态名称
 */
function reportStatusLabel(value: string): string {
  const labels: Record<string, string> = {
    draft: '草稿',
    published: '已发布',
    offline: '已下线',
    enabled: '启用',
    disabled: '停用',
  };
  return labels[value] ?? value;
}

/**
 * 获取报表状态标签颜色。
 *
 * @param value 报表状态
 * @returns Ant Design 标签颜色
 */
function reportStatusColor(value: string): string {
  if (value === 'published' || value === 'enabled') {
    return 'green';
  }
  if (value === 'draft') {
    return 'blue';
  }
  return 'default';
}

/**
 * 图表类型中文名称。
 *
 * @param chartType 图表类型
 * @returns 中文名称
 */
function chartTypeLabel(chartType: string): string {
  const labels: Record<string, string> = {
    metric: '指标卡',
    line: '折线图',
    bar: '柱状图',
    pie: '饼图',
    table: '表格',
    text: '文本',
  };
  return labels[chartType] ?? chartType;
}

/**
 * 配置摘要。
 *
 * @param configJson 配置 JSON
 * @returns 摘要
 */
function configSummary(configJson: string): string {
  try {
    const parsed = JSON.parse(configJson || '{}') as Partial<ReportDesignerConfig>;
    return Array.isArray(parsed.components) ? `${parsed.components.length} 个设计组件` : '兼容旧配置';
  } catch {
    return '配置待修复';
  }
}

onMounted(() => {
  void loadReports();
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
  border: 1px solid #dbeafe;
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
  min-height: 620px;
  border: 1px solid #e5e7eb;
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
  border-right: 1px solid #e5e7eb;
}

.property-panel {
  border-left: 1px solid #e5e7eb;
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
  border-bottom: 1px solid #e5e7eb;
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

.report-canvas {
  position: relative;
  min-height: 560px;
  overflow: auto;
  background-color: #f8fafc;
  background-image:
    linear-gradient(#e5e7eb 1px, transparent 1px),
    linear-gradient(90deg, #e5e7eb 1px, transparent 1px);
  background-size: 20px 20px;
}

.report-canvas :deep(.ant-empty) {
  padding-top: 180px;
}

.report-widget {
  position: absolute;
  display: flex;
  flex-direction: column;
  border: 1px solid #dbe3ef;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 10px 24px rgb(15 23 42 / 8%);
  cursor: move;
}

.report-widget.active {
  border-color: #2563eb;
  box-shadow: 0 0 0 2px rgb(37 99 235 / 16%);
}

.widget-title {
  padding: 8px 10px;
  border-bottom: 1px solid #eef2f7;
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
  font-size: 26px;
}

.widget-body table {
  width: 100%;
  border-collapse: collapse;
}

.widget-body th,
.widget-body td {
  padding: 6px;
  border: 1px solid #e5e7eb;
}

.widget-body th {
  background: #f8fafc;
  color: #334155;
  font-weight: 700;
}

.chart-preview {
  display: flex;
  align-items: end;
  gap: 10px;
  height: 100%;
  min-height: 100px;
}

.chart-preview i {
  flex: 1;
  border-radius: 5px 5px 0 0;
  background: #60a5fa;
}

.chart-preview i:nth-child(1) { height: 38%; }
.chart-preview i:nth-child(2) { height: 66%; }
.chart-preview i:nth-child(3) { height: 50%; }
.chart-preview i:nth-child(4) { height: 82%; }
.chart-preview i:nth-child(5) { height: 58%; }

.chart-preview.line {
  align-items: center;
}

.chart-preview.line i {
  height: 3px;
  border-radius: 999px;
}

.pie-preview {
  width: 120px;
  height: 120px;
  margin: 12px auto;
  border-radius: 50%;
  background: conic-gradient(#2563eb 0 44%, #22c55e 44% 72%, #f59e0b 72% 100%);
}

.preview-card {
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid #e5e7eb;
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
  display: inline-block;
  margin-top: 4px;
  color: #2563eb;
}

.permission-code {
  display: none;
}
</style>
