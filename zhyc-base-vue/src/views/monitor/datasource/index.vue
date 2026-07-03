<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="monitor-page">
    <a-card title="数据源监控" :bordered="false">
      <template #extra>
        <a-button :loading="loading" @click="loadDataSources">刷新</a-button>
      </template>

      <a-alert
        v-if="errorMessage"
        class="state-alert"
        :message="errorMessage"
        type="error"
        show-icon
      />

      <div class="monitor-summary">
        <div class="summary-item">
          <span>连接正常</span>
          <strong>{{ connectedDataSourceCount }}/{{ dataSources.length }}</strong>
        </div>
        <div class="summary-item">
          <span>连接异常</span>
          <strong>{{ disconnectedDataSourceCount }}</strong>
        </div>
        <div class="summary-item">
          <span>平均耗时</span>
          <strong>{{ averageCostMs }} ms</strong>
        </div>
        <div class="summary-item">
          <span>慢连接</span>
          <strong>{{ slowDataSourceCount }}</strong>
        </div>
      </div>

      <a-table
        row-key="sourceCode"
        size="small"
        :columns="columns"
        :data-source="dataSources"
        :loading="loading"
        :locale="{ emptyText: '暂无数据源运行状态。' }"
        :pagination="$tablePagination"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'CONNECTED' ? 'green' : 'red'">{{ $statusLabel(record.status) }}</a-tag>
          </template>
          <template v-if="column.key === 'costMs'">
            <a-tag :color="dataSourceLatencyColor(record.costMs)">{{ record.costMs }} ms</a-tag>
          </template>
          <template v-if="column.key === 'quality'">
            <span class="quality-cell">
              <a-tag :color="dataSourceLatencyColor(record.costMs)">{{ dataSourceQualityLabel(record.costMs) }}</a-tag>
              <small>{{ record.status === 'CONNECTED' ? '连接检测完成' : '连接不可用' }}</small>
            </span>
          </template>
        </template>
      </a-table>
    </a-card>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';

import { listRuntimeDataSourceStatus, type RuntimeDataSourceStatus } from '@/api/monitor/runtime';

/** 数据源运行状态列表。 */
const dataSources = ref<RuntimeDataSourceStatus[]>([]);
/** 加载状态。 */
const loading = ref(false);
/** 数据源状态加载错误提示。 */
const errorMessage = ref('');

/** 数据源监控表格列。 */
const columns = [
  { title: '数据源编码', dataIndex: 'sourceCode', key: 'sourceCode' },
  { title: '数据源名称', dataIndex: 'sourceName', key: 'sourceName' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '耗时(ms)', dataIndex: 'costMs', key: 'costMs', width: 110 },
  { title: '连接质量', key: 'quality', width: 150 },
  { title: '检测时间', dataIndex: 'checkedAt', key: 'checkedAt', width: 190 },
];

/** 连接正常的数据源数量。 */
const connectedDataSourceCount = computed(() => dataSources.value.filter((source) => source.status === 'CONNECTED').length);
/** 连接异常的数据源数量。 */
const disconnectedDataSourceCount = computed(() => dataSources.value.length - connectedDataSourceCount.value);
/** 平均检测耗时。 */
const averageCostMs = computed(() => averageLatency(dataSources.value.map((source) => source.costMs)));
/** 慢连接数据源数量。 */
const slowDataSourceCount = computed(() => dataSources.value.filter((source) => source.costMs >= 300).length);

/**
 * 加载数据源运行状态。
 */
async function loadDataSources(): Promise<void> {
  loading.value = true;
  errorMessage.value = '';
  try {
    dataSources.value = await listRuntimeDataSourceStatus();
  } catch (error) {
    dataSources.value = [];
    errorMessage.value = error instanceof Error ? error.message : '数据源运行状态加载失败';
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  void loadDataSources();
});

/**
 * 获取数据源检测耗时标签颜色。
 *
 * @param costMs 检测耗时，单位毫秒
 * @returns Ant Design 标签颜色
 */
function dataSourceLatencyColor(costMs: number): string {
  if (costMs >= 1000) {
    return 'red';
  }
  if (costMs >= 300) {
    return 'orange';
  }
  return 'green';
}

/**
 * 获取数据源连接质量说明。
 *
 * @param costMs 检测耗时，单位毫秒
 * @returns 连接质量说明
 */
function dataSourceQualityLabel(costMs: number): string {
  if (costMs >= 1000) {
    return '严重偏慢';
  }
  if (costMs >= 300) {
    return '偏慢';
  }
  return '良好';
}

/**
 * 计算平均耗时。
 *
 * @param latencies 耗时列表
 * @returns 平均耗时，单位毫秒
 */
function averageLatency(latencies: number[]): number {
  if (latencies.length === 0) {
    return 0;
  }
  const total = latencies.reduce((sum, latency) => sum + (Number.isFinite(latency) ? latency : 0), 0);
  return Math.round(total / latencies.length);
}
</script>

<style scoped>
.monitor-page {
  min-width: 0;
}

.state-alert {
  margin-bottom: 12px;
}

.monitor-summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 14px;
}

.summary-item {
  padding: 12px 14px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #f8fafc;
}

.summary-item span {
  display: block;
  margin-bottom: 6px;
  color: #64748b;
  font-size: 12px;
}

.summary-item strong {
  color: #111827;
  font-size: 18px;
}

.quality-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.quality-cell small {
  color: #64748b;
}

@media (max-width: 920px) {
  .monitor-summary {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 560px) {
  .monitor-summary {
    grid-template-columns: 1fr;
  }
}
</style>
