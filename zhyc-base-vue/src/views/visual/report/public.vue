<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="public-report-page">
    <a-result
      v-if="errorMessage"
      status="404"
      title="报表不可访问"
      :sub-title="errorMessage"
    />
    <a-spin v-else :spinning="loading">
      <div class="public-report-shell">
        <header class="public-report-header">
          <div>
            <span>公开访问</span>
            <h1>{{ report?.reportName || '可视化报表' }}</h1>
          </div>
          <a-tag color="green">已发布</a-tag>
        </header>
        <main class="public-report-canvas">
          <a-empty v-if="components.length === 0" description="当前报表暂无组件" />
          <article
            v-for="component in components"
            :key="component.id"
            class="public-report-widget"
            :style="componentStyle(component)"
          >
            <h2>{{ component.title }}</h2>
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
                      <td>{{ component.fieldX || '维度' }}</td>
                      <td>{{ component.fieldY || component.fieldValue || '数值' }}</td>
                    </tr>
                  </tbody>
                </table>
              </template>
              <template v-else-if="component.type === 'text'">
                <p>{{ component.content || '报表说明' }}</p>
              </template>
              <template v-else>
                <div class="chart-preview" :class="component.type">
                  <i
                    v-for="height in componentChartBars(component)"
                    :key="height"
                    :style="{ height: `${height}%` }"
                  ></i>
                </div>
              </template>
            </div>
          </article>
        </main>
      </div>
    </a-spin>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';

import {
  getPublishedVisualReport,
  previewPublishedReportDataset,
  type VisualDatasetPreview,
  type VisualReport,
} from '@/api/visual/report';

type ReportComponentType = 'metric' | 'line' | 'bar' | 'pie' | 'table' | 'text';

interface ReportDesignerComponent {
  /** 报表组件唯一标识。 */
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
  /** 绑定数据集编码。 */
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

/** 当前路由。 */
const route = useRoute();
/** 已发布报表。 */
const report = ref<VisualReport>();
/** 页面加载状态。 */
const loading = ref(false);
/** 错误提示。 */
const errorMessage = ref('');
/** 公开访问数据集预览缓存。 */
const datasetPreviews = ref<Record<string, VisualDatasetPreview>>({});

/** 报表组件列表。 */
const components = computed(() => parseReportComponents(report.value));

/**
 * 加载公开访问报表。
 */
async function loadPublishedReport(): Promise<void> {
  loading.value = true;
  errorMessage.value = '';
  try {
    const tenantId = String(route.params.tenantId || '');
    const reportCode = String(route.params.reportCode || '');
    report.value = await getPublishedVisualReport(tenantId, reportCode);
    await loadPublicDatasetPreviews(tenantId, reportCode);
  } catch (error) {
    report.value = undefined;
    errorMessage.value = error instanceof Error ? error.message : '报表未发布或不存在';
  } finally {
    loading.value = false;
  }
}

/**
 * 加载公开报表组件引用的数据集预览。
 *
 * @param tenantId 租户业务编码
 * @param reportCode 报表编码
 */
async function loadPublicDatasetPreviews(tenantId: string, reportCode: string): Promise<void> {
  const datasetCodes = [...new Set(components.value.map((component) => component.datasetCode).filter(Boolean))];
  const previews = await Promise.allSettled(
    datasetCodes.map(async (datasetCode) => ({
      datasetCode,
      preview: await previewPublishedReportDataset(tenantId, reportCode, datasetCode, 10),
    })),
  );
  datasetPreviews.value = previews.reduce<Record<string, VisualDatasetPreview>>((result, item) => {
    if (item.status === 'fulfilled') {
      result[item.value.datasetCode] = item.value.preview;
    }
    return result;
  }, {});
}

/**
 * 解析公开报表组件配置。
 *
 * @param currentReport 已发布报表
 * @returns 报表组件列表
 */
function parseReportComponents(currentReport?: VisualReport): ReportDesignerComponent[] {
  if (!currentReport) {
    return [];
  }
  try {
    const parsed = JSON.parse(currentReport.configJson || '{}') as Partial<ReportDesignerConfig>;
    if (Array.isArray(parsed.components)) {
      return parsed.components.map((component) => normalizeComponent(component, currentReport));
    }
  } catch {
    // 兼容历史手写 JSON，解析失败时回退为单组件。
  }
  return [normalizeComponent({}, currentReport)];
}

/**
 * 规整历史组件字段。
 *
 * @param component 历史组件
 * @param currentReport 已发布报表
 * @returns 公开报表组件
 */
function normalizeComponent(
  component: Partial<ReportDesignerComponent>,
  currentReport: VisualReport,
): ReportDesignerComponent {
  return {
    id: component.id || `public_report_${currentReport.reportCode}`,
    type: component.type || ((currentReport.chartType as ReportComponentType) ?? 'table'),
    title: component.title || currentReport.reportName,
    x: Number(component.x ?? 40),
    y: Number(component.y ?? 40),
    w: Number(component.w ?? 320),
    h: Number(component.h ?? 220),
    datasetCode: component.datasetCode || currentReport.datasetCode,
    fieldX: component.fieldX || '',
    fieldY: component.fieldY || '',
    fieldValue: component.fieldValue || '',
    content: component.content || '',
  };
}

/**
 * 计算公开报表组件样式。
 *
 * @param component 报表组件
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
 * 获取公开组件绑定数据集的预览数据行。
 *
 * @param component 报表组件
 * @returns 预览数据行
 */
function componentPreviewRows(component: ReportDesignerComponent): Record<string, unknown>[] {
  return datasetPreviews.value[component.datasetCode]?.rows.slice(0, 3) ?? [];
}

/**
 * 获取公开表格组件预览列。
 *
 * @param component 报表组件
 * @returns 表格列字段
 */
function componentTableColumns(component: ReportDesignerComponent): string[] {
  const configuredColumns = [component.fieldX, component.fieldY, component.fieldValue].filter(Boolean);
  if (configuredColumns.length > 0) {
    return [...new Set(configuredColumns)].slice(0, 3);
  }
  const previewColumns = datasetPreviews.value[component.datasetCode]?.columns ?? [];
  return previewColumns.length > 0 ? previewColumns.slice(0, 3) : ['维度', '数值'];
}

/**
 * 获取公开指标卡预览值。
 *
 * @param component 报表组件
 * @returns 指标展示值
 */
function componentMetricValue(component: ReportDesignerComponent): string {
  const preview = datasetPreviews.value[component.datasetCode];
  const field = component.fieldValue || component.fieldY || preview?.columns[0] || '';
  const value = field ? preview?.rows[0]?.[field] : undefined;
  return formatPreviewCell(value ?? component.fieldValue ?? '指标值');
}

/**
 * 生成公开图表预览柱形高度。
 *
 * @param component 报表组件
 * @returns 百分比高度列表
 */
function componentChartBars(component: ReportDesignerComponent): number[] {
  if (component.type === 'pie') {
    return [];
  }
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

onMounted(() => {
  void loadPublishedReport();
});
</script>

<style scoped>
.public-report-page {
  min-height: 100vh;
  background: #f3f7fb;
}

.public-report-shell {
  min-height: 100vh;
  padding: 20px;
}

.public-report-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.public-report-header span {
  color: #2563eb;
  font-size: 13px;
  font-weight: 700;
}

.public-report-header h1 {
  margin: 4px 0 0;
  color: #0f172a;
  font-size: 24px;
  letter-spacing: 0;
}

.public-report-canvas {
  position: relative;
  min-height: calc(100vh - 92px);
  overflow: auto;
  border: 1px solid #dbeafe;
  border-radius: 8px;
  background: #ffffff;
}

.public-report-canvas :deep(.ant-empty) {
  padding-top: 180px;
}

.public-report-widget {
  position: absolute;
  display: flex;
  flex-direction: column;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 10px 24px rgb(15 23 42 / 8%);
}

.public-report-widget h2 {
  margin: 0;
  padding: 9px 12px;
  border-bottom: 1px solid #eef2f7;
  color: #111827;
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 0;
}

.widget-body {
  flex: 1;
  min-height: 0;
  padding: 12px;
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
  gap: 8px;
  height: 100%;
  min-height: 110px;
}

.chart-preview i {
  flex: 1;
  border-radius: 5px 5px 0 0;
  background: linear-gradient(180deg, #60a5fa, #2563eb);
}

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
  background: conic-gradient(#2563eb 0 45%, #06b6d4 45% 72%, #22c55e 72% 100%);
}

.chart-preview.pie i {
  display: none;
}
</style>
