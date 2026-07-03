<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="monitor-page">
    <a-card title="服务监控" :bordered="false">
      <template #extra>
        <a-button :loading="loading" @click="loadServices">刷新</a-button>
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
          <span>在线服务</span>
          <strong>{{ onlineServiceCount }}/{{ services.length }}</strong>
        </div>
        <div class="summary-item">
          <span>平均响应</span>
          <strong>{{ averageResponseTimeMs }} ms</strong>
        </div>
        <div class="summary-item">
          <span>最慢响应</span>
          <strong>{{ maxResponseTimeMs }} ms</strong>
        </div>
        <div class="summary-item">
          <span>健康状态</span>
          <strong>{{ unhealthyServiceCount === 0 ? '正常' : `${unhealthyServiceCount} 个异常` }}</strong>
        </div>
      </div>

      <a-table
        row-key="serviceName"
        size="small"
        :columns="columns"
        :data-source="services"
        :loading="loading"
        :locale="{ emptyText: '暂无服务运行状态。' }"
        :pagination="$tablePagination"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'UP' ? 'green' : 'red'">{{ $statusLabel(record.status) }}</a-tag>
          </template>
          <template v-if="column.key === 'responseTimeMs'">
            <a-tag :color="serviceLatencyColor(record.responseTimeMs)">{{ record.responseTimeMs }} ms</a-tag>
          </template>
        </template>
      </a-table>
    </a-card>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';

import { listRuntimeServiceStatus, type RuntimeServiceStatus } from '@/api/monitor/runtime';

/** 服务运行状态列表。 */
const services = ref<RuntimeServiceStatus[]>([]);
/** 加载状态。 */
const loading = ref(false);
/** 服务状态加载错误提示。 */
const errorMessage = ref('');

/** 服务监控表格列。 */
const columns = [
  { title: '服务名称', dataIndex: 'serviceName', key: 'serviceName' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '响应耗时(ms)', dataIndex: 'responseTimeMs', key: 'responseTimeMs', width: 140 },
  { title: '版本', dataIndex: 'version', key: 'version', width: 140 },
  { title: '最近心跳', dataIndex: 'heartbeatAt', key: 'heartbeatAt', width: 190 },
];

/** 在线服务数量。 */
const onlineServiceCount = computed(() => services.value.filter((service) => service.status === 'UP').length);
/** 异常服务数量。 */
const unhealthyServiceCount = computed(() => services.value.length - onlineServiceCount.value);
/** 平均响应耗时。 */
const averageResponseTimeMs = computed(() => averageLatency(services.value.map((service) => service.responseTimeMs)));
/** 最慢响应耗时。 */
const maxResponseTimeMs = computed(() => Math.max(0, ...services.value.map((service) => service.responseTimeMs ?? 0)));

/**
 * 加载服务运行状态。
 */
async function loadServices(): Promise<void> {
  loading.value = true;
  errorMessage.value = '';
  try {
    services.value = await listRuntimeServiceStatus();
  } catch (error) {
    services.value = [];
    errorMessage.value = error instanceof Error ? error.message : '服务运行状态加载失败';
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  void loadServices();
});

/**
 * 获取服务响应耗时标签颜色。
 *
 * @param responseTimeMs 响应耗时，单位毫秒
 * @returns Ant Design 标签颜色
 */
function serviceLatencyColor(responseTimeMs: number): string {
  if (responseTimeMs >= 1000) {
    return 'red';
  }
  if (responseTimeMs >= 300) {
    return 'orange';
  }
  return 'green';
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
