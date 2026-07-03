<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="public-screen-page">
    <a-result
      v-if="errorMessage"
      status="404"
      title="大屏不可访问"
      :sub-title="errorMessage"
    />
    <a-spin v-else :spinning="loading">
      <div class="public-screen-shell">
        <header class="public-screen-header">
          <div>
            <span>公开访问</span>
            <h1>{{ screen?.screenName || '可视化数据大屏' }}</h1>
          </div>
          <a-tag color="green">已发布</a-tag>
        </header>
        <main class="public-screen-canvas">
          <a-empty v-if="widgets.length === 0" description="当前大屏暂无组件" />
          <div
            v-for="widget in widgets"
            :key="widget.id"
            class="public-screen-widget"
            :style="widgetStyle(widget)"
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
                      <td>{{ widget.fieldX || '维度' }}</td>
                      <td>{{ widget.fieldY || widget.fieldValue || '数值' }}</td>
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
                <p>{{ widget.content || '大屏说明' }}</p>
              </template>
              <template v-else>
                <div class="chart-preview" :class="widget.type">
                  <i v-for="height in widgetChartBars(widget)" :key="height" :style="{ height: `${height}%` }"></i>
                </div>
              </template>
            </div>
          </div>
        </main>
      </div>
    </a-spin>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';

import {
  getPublishedVisualScreen,
  previewPublishedScreenDataset,
  type VisualDatasetPreview,
  type VisualScreen,
} from '@/api/visual/report';

type ScreenWidgetType = 'stat' | 'line' | 'bar' | 'pie' | 'table' | 'map' | 'text';

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
  viewport?: { width: number; height: number };
  /** 大屏组件列表。 */
  widgets: ScreenDesignerWidget[];
}

/** 当前路由。 */
const route = useRoute();
/** 已发布大屏。 */
const screen = ref<VisualScreen>();
/** 加载状态。 */
const loading = ref(false);
/** 错误提示。 */
const errorMessage = ref('');
/** 公开访问数据集预览缓存。 */
const datasetPreviews = ref<Record<string, VisualDatasetPreview>>({});
/** 图表占位高度。 */
const chartHeights = [42, 70, 54, 86, 62, 76];

/** 大屏组件列表。 */
const widgets = computed(() => parseScreenWidgets(screen.value?.layoutJson ?? ''));

/**
 * 加载公开访问大屏。
 */
async function loadPublishedScreen(): Promise<void> {
  loading.value = true;
  errorMessage.value = '';
  try {
    const tenantId = String(route.params.tenantId || '');
    const screenCode = String(route.params.screenCode || '');
    screen.value = await getPublishedVisualScreen(tenantId, screenCode);
    await loadPublicDatasetPreviews(tenantId, screenCode);
  } catch (error) {
    screen.value = undefined;
    errorMessage.value = error instanceof Error ? error.message : '大屏未发布或不存在';
  } finally {
    loading.value = false;
  }
}

/**
 * 加载公开大屏组件引用的数据集预览。
 *
 * @param tenantId 租户业务编码
 * @param screenCode 大屏编码
 */
async function loadPublicDatasetPreviews(tenantId: string, screenCode: string): Promise<void> {
  const datasetCodes = [...new Set(widgets.value.map((widget) => widget.datasetCode).filter(Boolean))];
  const previews = await Promise.allSettled(
    datasetCodes.map(async (datasetCode) => ({
      datasetCode,
      preview: await previewPublishedScreenDataset(tenantId, screenCode, datasetCode, 10),
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
    return [];
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
    id: widget.id || `public_screen_${Math.random().toString(16).slice(2)}`,
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
 * 计算公开大屏组件样式。
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
 * 获取公开组件绑定数据集的预览数据行。
 *
 * @param widget 大屏组件
 * @returns 预览数据行
 */
function widgetPreviewRows(widget: ScreenDesignerWidget): Record<string, unknown>[] {
  return datasetPreviews.value[widget.datasetCode]?.rows.slice(0, 3) ?? [];
}

/**
 * 获取公开表格组件预览列。
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
  return previewColumns.length > 0 ? previewColumns.slice(0, 3) : ['维度', '数值'];
}

/**
 * 获取公开指标卡预览值。
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
 * 生成公开图表预览柱形高度。
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
    return chartHeights;
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
  void loadPublishedScreen();
});
</script>

<style scoped>
.public-screen-page {
  min-height: 100vh;
  background: #edf4ff;
}

.public-screen-shell {
  min-height: 100vh;
  padding: 20px;
}

.public-screen-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.public-screen-header span {
  color: #2563eb;
  font-size: 13px;
  font-weight: 700;
}

.public-screen-header h1 {
  margin: 4px 0 0;
  color: #0f172a;
  font-size: 24px;
  letter-spacing: 0;
}

.public-screen-canvas {
  position: relative;
  min-height: calc(100vh - 92px);
  overflow: auto;
  border: 1px solid #bfdbfe;
  border-radius: 8px;
  background:
    linear-gradient(135deg, rgb(15 23 42 / 5%) 25%, transparent 25%) -10px 0 / 20px 20px,
    linear-gradient(225deg, rgb(15 23 42 / 5%) 25%, transparent 25%) -10px 0 / 20px 20px,
    #f8fbff;
}

.public-screen-canvas :deep(.ant-empty) {
  padding-top: 180px;
}

.public-screen-widget {
  position: absolute;
  display: flex;
  flex-direction: column;
  border: 1px solid #bfdbfe;
  border-radius: 8px;
  background: rgb(255 255 255 / 96%);
  box-shadow: 0 12px 28px rgb(15 23 42 / 10%);
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
</style>
