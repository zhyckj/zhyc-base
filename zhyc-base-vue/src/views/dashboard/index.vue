<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="dashboard-page">
    <div class="dashboard-hero">
      <div class="dashboard-hero-main">
        <a-tag color="blue" class="dashboard-hero-tag">企业级快速开发平台</a-tag>
        <h1>个人工作台</h1>
        <p>
          {{ accountName }}，当前租户为 {{ tenantName }}。这里聚合待办、低代码、开放平台和业务样板的关键入口。
        </p>
      </div>
      <div class="dashboard-hero-actions">
        <a-button type="primary" @click="go('/workflow/tasks/todo')">
          <template #icon><ClockCircleOutlined /></template>
          处理待办
        </a-button>
        <a-button @click="go('/lowcode/model')">
          <template #icon><DatabaseOutlined /></template>
          数据建模
        </a-button>
        <a-button :loading="loading" @click="loadOverview">
          <template #icon><ReloadOutlined /></template>
          刷新
        </a-button>
      </div>
    </div>

    <a-alert
      v-if="loadError"
      :message="loadError"
      type="error"
      show-icon
      closable
      @close="loadError = ''"
    />
    <a-alert
      v-if="warningMessage"
      :message="warningMessage"
      type="warning"
      show-icon
      closable
      @close="warnings = []"
    />

    <a-row :gutter="[16, 16]" class="metric-row">
      <a-col :xs="24" :md="12" :xl="6" v-for="metric in metrics" :key="metric.title">
        <a-card :bordered="false" :loading="loading" class="metric-card">
          <div class="metric-card-body">
            <span class="metric-icon" :class="getMetricTone(metric.title)">
              <component :is="getMetricIcon(metric.title)" />
            </span>
            <a-statistic :title="metric.title" :value="metric.value" :suffix="metric.suffix" />
          </div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :xl="16">
        <a-card class="section-card" :bordered="false">
          <template #title>快捷入口</template>
          <div class="shortcut-grid">
            <button
              v-for="item in shortcuts"
              :key="item.path"
              type="button"
              class="shortcut-card"
              @click="go(item.path)"
            >
              <span class="shortcut-icon" :class="item.tone">
                <component :is="item.icon" />
              </span>
              <span class="shortcut-content">
                <strong>{{ item.title }}</strong>
                <small>{{ item.description }}</small>
              </span>
              <RightOutlined class="shortcut-arrow" />
            </button>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :xl="8">
        <a-card class="section-card" :bordered="false">
          <template #title>运行概览</template>
          <div class="health-list">
            <div v-for="item in healthItems" :key="item.title" class="health-item">
              <span class="health-icon" :class="item.tone">
                <component :is="item.icon" />
              </span>
              <span class="health-content">
                <strong>{{ item.title }}</strong>
                <small>{{ item.description }}</small>
              </span>
              <a-tag :color="item.statusColor">{{ item.statusText }}</a-tag>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :lg="12">
        <a-card class="section-card" :bordered="false">
          <template #title>流程与业务</template>
          <a-list :data-source="businessActions" :split="false">
            <template #renderItem="{ item }">
              <a-list-item class="action-list-item" @click="go(item.path)">
                <a-list-item-meta :title="item.title" :description="item.description">
                  <template #avatar>
                    <span class="action-avatar" :class="item.tone">
                      <component :is="item.icon" />
                    </span>
                  </template>
                </a-list-item-meta>
                <RightOutlined />
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>
      <a-col :xs="24" :lg="12">
        <a-card class="section-card" :bordered="false">
          <template #title>平台能力</template>
          <div class="capability-grid">
            <div v-for="item in capabilityItems" :key="item.title" class="capability-item">
              <strong>{{ item.title }}</strong>
              <span>{{ item.description }}</span>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>
  </section>
</template>

<script setup lang="ts">
import {
  ApiOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  CloudServerOutlined,
  CodeOutlined,
  DatabaseOutlined,
  FileTextOutlined,
  FormOutlined,
  ReloadOutlined,
  RightOutlined,
  SafetyCertificateOutlined,
  ThunderboltOutlined,
} from '@ant-design/icons-vue';
import type { Component } from 'vue';
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

import { getDashboardOverview, type DashboardMetricItem } from '@/api/dashboard/overview';
import { getAdminRuntimeContext, requireAdminTenantId, requireAdminUserId } from '@/utils/adminContext';

const router = useRouter();

/**
 * 工作台入口定义。
 */
interface DashboardActionItem {
  /** 入口标题。 */
  title: string;
  /** 入口说明。 */
  description: string;
  /** 目标路由。 */
  path: string;
  /** 展示图标组件。 */
  icon: Component;
  /** 色彩风格类名。 */
  tone: string;
}

/**
 * 工作台状态项。
 */
interface DashboardStatusItem {
  /** 状态标题。 */
  title: string;
  /** 状态说明。 */
  description: string;
  /** 展示图标组件。 */
  icon: Component;
  /** 图标色彩风格类名。 */
  tone: string;
  /** 状态标签文本。 */
  statusText: string;
  /** 状态标签颜色。 */
  statusColor: string;
}

const initialContext = getAdminRuntimeContext();

/** 当前账号展示名称。 */
const accountName = ref(initialContext.accountName || '未登录账号');

/** 当前租户展示名称。 */
const tenantName = ref(initialContext.tenantId || '未选择租户');

/** 工作台指标。 */
const metrics = ref<DashboardMetricItem[]>([
  { title: '待办任务', value: 0, suffix: '条' },
  { title: '我的申请', value: 0, suffix: '条' },
  { title: '开放 API 调用', value: 0, suffix: '次' },
  { title: '生成记录', value: 0, suffix: '次' },
]);

/** 工作台加载状态。 */
const loading = ref(false);

/** 工作台整体加载失败提示。 */
const loadError = ref('');

/** 工作台局部加载失败提示。 */
const warnings = ref<string[]>([]);

/** 局部加载失败合并提示。 */
const warningMessage = computed(() => warnings.value.join('；'));

/** 首期核心业务入口。 */
const shortcuts: DashboardActionItem[] = [
  { title: '流程待办', description: '处理当前审批任务', path: '/workflow/tasks/todo', icon: ClockCircleOutlined, tone: 'tone-blue' },
  { title: '采购申请', description: '发起和跟踪采购流程', path: '/purchase/requests', icon: FormOutlined, tone: 'tone-green' },
  { title: '数据表建模', description: '维护低代码数据模型', path: '/lowcode/model', icon: DatabaseOutlined, tone: 'tone-cyan' },
  { title: '代码生成', description: '生成前后端模块代码', path: '/lowcode/generator', icon: CodeOutlined, tone: 'tone-purple' },
  { title: '开发者应用', description: '管理开放 API 调用方', path: '/openapi/apps', icon: ApiOutlined, tone: 'tone-orange' },
  { title: '系统参数', description: '维护平台运行参数', path: '/system/params', icon: SafetyCertificateOutlined, tone: 'tone-slate' },
];

/** 流程和业务快捷动作。 */
const businessActions: DashboardActionItem[] = [
  { title: '流程已办', description: '查看已审批、已处理的流程记录', path: '/workflow/tasks/done', icon: CheckCircleOutlined, tone: 'tone-green' },
  { title: '我发起的', description: '跟踪本人发起的流程实例状态', path: '/workflow/tasks/started', icon: FileTextOutlined, tone: 'tone-blue' },
  { title: '采购订单', description: '查看采购执行和订单流转情况', path: '/purchase/orders', icon: FormOutlined, tone: 'tone-orange' },
];

/** 平台运行状态首期概览。 */
const healthItems: DashboardStatusItem[] = [
  { title: '核心平台', description: '模块化单体运行中', icon: CloudServerOutlined, tone: 'tone-blue', statusText: '正常', statusColor: 'green' },
  { title: '统一认证', description: 'OAuth2/OIDC 已接入', icon: SafetyCertificateOutlined, tone: 'tone-green', statusText: '已启用', statusColor: 'blue' },
  { title: '开放 API', description: 'API Key 与 OAuth2 双模式', icon: ApiOutlined, tone: 'tone-orange', statusText: '首期', statusColor: 'orange' },
  { title: '低代码生成', description: '模型、模板、生成记录可用', icon: ThunderboltOutlined, tone: 'tone-purple', statusText: '可用', statusColor: 'purple' },
];

/** 平台核心能力概览。 */
const capabilityItems = [
  { title: 'SaaS 租户隔离', description: '共享库共享表，通过 tenant_id 做首期数据隔离。' },
  { title: '权限与审计', description: '菜单、角色、数据权限和访问审计统一管理。' },
  { title: '工作流门面', description: '业务模块通过平台门面接入 Flowable。' },
  { title: '开放生态', description: '为第三方系统集成和开发者门户预留扩展。' },
];

/**
 * 跳转到快捷入口。
 *
 * @param path 目标路由
 */
function go(path: string): void {
  void router.push(path);
}

/**
 * 加载工作台概览指标。
 */
async function loadOverview(): Promise<void> {
  loading.value = true;
  loadError.value = '';
  try {
    const adminContext = getAdminRuntimeContext();
    accountName.value = adminContext.accountName || '未登录账号';
    tenantName.value = adminContext.tenantId || '未选择租户';
    const tenantId = requireAdminTenantId(adminContext);
    const currentUserId = requireAdminUserId(adminContext);
    const overview = await getDashboardOverview({ tenantId, userId: currentUserId });
    metrics.value = overview.metrics;
    warnings.value = overview.warnings;
  } catch (error) {
    loadError.value = error instanceof Error ? error.message : '工作台指标加载失败';
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  void loadOverview();
});

/**
 * 获取指标卡图标组件。
 *
 * @param title 指标标题
 * @returns 指标对应图标组件
 */
function getMetricIcon(title: string): Component {
  const iconMap: Record<string, Component> = {
    待办任务: ClockCircleOutlined,
    我的申请: FileTextOutlined,
    '开放 API 调用': ApiOutlined,
    生成记录: CodeOutlined,
  };
  return iconMap[title] ?? ThunderboltOutlined;
}

/**
 * 获取指标卡色彩风格。
 *
 * @param title 指标标题
 * @returns 色彩风格类名
 */
function getMetricTone(title: string): string {
  const toneMap: Record<string, string> = {
    待办任务: 'tone-blue',
    我的申请: 'tone-green',
    '开放 API 调用': 'tone-orange',
    生成记录: 'tone-purple',
  };
  return toneMap[title] ?? 'tone-slate';
}
</script>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.dashboard-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 22px 24px;
  border: 1px solid #dbeafe;
  border-radius: 8px;
  background:
    linear-gradient(135deg, rgb(239 246 255 / 96%), rgb(255 255 255 / 96%)),
    #ffffff;
  box-shadow: 0 8px 24px rgb(15 23 42 / 6%);
}

.dashboard-hero-main {
  min-width: 0;
}

.dashboard-hero-tag {
  margin-bottom: 10px;
  margin-inline-end: 0;
}

.dashboard-hero h1 {
  margin: 0;
  color: #0f172a;
  font-size: 24px;
  line-height: 1.25;
}

.dashboard-hero p {
  max-width: 760px;
  margin: 8px 0 0;
  color: #475569;
  line-height: 1.7;
}

.dashboard-hero-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 8px;
}

.metric-row {
  align-items: stretch;
}

.metric-card {
  height: 100%;
  border-radius: 8px;
  box-shadow: 0 4px 14px rgb(15 23 42 / 5%);
}

.metric-card-body {
  display: flex;
  align-items: center;
  gap: 14px;
}

.metric-icon,
.shortcut-icon,
.health-icon,
.action-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
}

.metric-icon {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  font-size: 22px;
}

.section-card {
  height: 100%;
  border-radius: 8px;
  box-shadow: 0 4px 14px rgb(15 23 42 / 5%);
}

.shortcut-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.shortcut-card {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  min-height: 76px;
  padding: 14px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #ffffff;
  color: inherit;
  text-align: left;
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.shortcut-card:hover {
  border-color: #60a5fa;
  box-shadow: 0 8px 20px rgb(37 99 235 / 10%);
  transform: translateY(-1px);
}

.shortcut-icon {
  width: 38px;
  height: 38px;
  border-radius: 8px;
  font-size: 20px;
}

.shortcut-content,
.health-content {
  display: flex;
  flex: 1;
  min-width: 0;
  flex-direction: column;
  gap: 4px;
}

.shortcut-content strong,
.health-content strong,
.capability-item strong {
  color: #0f172a;
  font-size: 14px;
}

.shortcut-content small,
.health-content small,
.capability-item span {
  color: #64748b;
  font-size: 12px;
  line-height: 1.5;
}

.shortcut-arrow {
  color: #94a3b8;
}

.health-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.health-item {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 54px;
}

.health-icon,
.action-avatar {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  font-size: 18px;
}

.action-list-item {
  margin-bottom: 8px;
  padding: 12px !important;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  cursor: pointer;
  transition: border-color 0.2s ease, background 0.2s ease;
}

.action-list-item:hover {
  border-color: #93c5fd;
  background: #f8fbff;
}

.capability-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.capability-item {
  display: flex;
  min-height: 86px;
  flex-direction: column;
  gap: 8px;
  padding: 14px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #fbfdff;
}

.tone-blue {
  background: #eff6ff;
  color: #2563eb;
}

.tone-green {
  background: #ecfdf5;
  color: #059669;
}

.tone-cyan {
  background: #ecfeff;
  color: #0891b2;
}

.tone-purple {
  background: #f5f3ff;
  color: #7c3aed;
}

.tone-orange {
  background: #fff7ed;
  color: #ea580c;
}

.tone-slate {
  background: #f1f5f9;
  color: #475569;
}

@media (max-width: 1200px) {
  .shortcut-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .dashboard-hero {
    align-items: flex-start;
    flex-direction: column;
  }

  .dashboard-hero-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .shortcut-grid,
  .capability-grid {
    grid-template-columns: 1fr;
  }
}
</style>
