<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="security-protection-page">
    <a-card :bordered="false" class="security-card">
      <template #title>
        <div class="card-title">
          <span>安全防护中心</span>
          <a-tag color="blue">防护策略 / 访问限制 / 安全事件</a-tag>
        </div>
      </template>
      <template #extra>
        <a-space>
          <a-input v-model:value="tenantId" class="tenant-id" readonly />
          <a-button :loading="loading || restrictionLoading" @click="loadAll">刷新</a-button>
        </a-space>
      </template>

      <a-alert v-if="status === 'error'" type="error" show-icon :message="errorMessage" class="state-alert" />

      <div class="metrics-grid">
        <div class="metric-item">
          <span class="metric-label">今日请求来源</span>
          <strong>{{ overview.todaySourceCount }}</strong>
          <small>统计日期 {{ formatStatDate(overview.statDate) }}</small>
        </div>
        <div class="metric-item">
          <span class="metric-label">最高 IP 请求</span>
          <strong>{{ overview.maxIpRequestCount }}</strong>
          <small>单个来源今日最高次数</small>
        </div>
        <div class="metric-item">
          <span class="metric-label">违规 IP</span>
          <strong>{{ overview.violationIpCount }}</strong>
          <small>触发拒绝、限流或高危事件</small>
        </div>
        <div class="metric-item">
          <span class="metric-label">封禁 IP</span>
          <strong>{{ overview.blockedIpCount }}</strong>
          <small>当前有效封禁规则</small>
        </div>
      </div>

      <div class="operation-bar">
        <div class="operation-copy">
          <strong>快速处置</strong>
          <span>手动封禁会同步到访问限制规则，解封后规则自动失效。</span>
        </div>
        <a-space wrap>
          <a-input v-model:value="blockForm.ipValue" class="ip-input" placeholder="输入 IP 或 CIDR" />
          <a-input v-model:value="blockForm.reason" class="reason-input" placeholder="封禁原因" />
          <a-input v-model:value="blockForm.endAt" class="time-input" type="datetime-local" />
          <a-popconfirm title="确认封禁该 IP 或 CIDR？" ok-text="确认" cancel-text="取消" @confirm="submitBlock">
            <a-button type="primary" :loading="blocking" v-permission="'system:security-protection:block'">
              封禁 IP
            </a-button>
          </a-popconfirm>
          <a-popconfirm title="确认解封该 IP 或 CIDR？" ok-text="确认" cancel-text="取消" @confirm="submitUnblock">
            <a-button :loading="unblocking" v-permission="'system:security-protection:unblock'">
              解封 IP
            </a-button>
          </a-popconfirm>
        </a-space>
      </div>

      <a-tabs v-model:active-key="activeTabKey" class="security-tabs">
        <a-tab-pane key="rules" tab="防护策略">
          <div class="content-grid rules-grid">
            <section class="content-section">
              <div class="section-title">
                <strong>核心阈值</strong>
                <span>命中阈值后按动作记录、校验或封禁</span>
              </div>
              <a-table
                row-key="policyCode"
                size="small"
                :columns="policyColumns"
                :data-source="policies"
                :loading="loading"
                :pagination="false"
              >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'policyName'">
                    <a-input v-model:value="record.policyName" />
                  </template>
                  <template v-if="column.key === 'scope'">
                    <a-tag color="blue">{{ scopeLabel(record.protectionScope) }}</a-tag>
                    <span class="target-pattern">{{ record.targetPattern }}</span>
                  </template>
                  <template v-if="column.key === 'threshold'">
                    <a-space compact>
                      <a-input-number v-model:value="record.thresholdLimit" :min="1" class="threshold-input" />
                      <a-input-number v-model:value="record.windowSeconds" :min="1" class="window-input" />
                    </a-space>
                  </template>
                  <template v-if="column.key === 'action'">
                    <a-select v-model:value="record.action" class="action-select">
                      <a-select-option value="observe">观察</a-select-option>
                      <a-select-option value="captcha">验证码</a-select-option>
                      <a-select-option value="block">封禁</a-select-option>
                    </a-select>
                  </template>
                  <template v-if="column.key === 'blockSeconds'">
                    <a-input-number v-model:value="record.blockSeconds" :min="0" class="block-seconds-input" />
                  </template>
                  <template v-if="column.key === 'status'">
                    <a-select v-model:value="record.status" class="status-select">
                      <a-select-option value="enabled">启用</a-select-option>
                      <a-select-option value="disabled">停用</a-select-option>
                    </a-select>
                  </template>
                  <template v-if="column.key === 'actionColumn'">
                    <a-button
                      size="small"
                      type="link"
                      :loading="savingPolicyCode === record.policyCode"
                      v-permission="'system:security-protection:save'"
                      @click="savePolicyRow(record)"
                    >
                      保存
                    </a-button>
                  </template>
                </template>
              </a-table>
            </section>

            <section class="content-section">
              <div class="section-title section-title-actions">
                <div>
                  <strong>访问限制规则</strong>
                  <span>统一管理 IP、账号、设备的允许或拒绝规则</span>
                </div>
                <a-space>
                  <a-select v-model:value="restrictionType" class="restriction-type-select" @change="loadRestrictions">
                    <a-select-option value="ip">IP</a-select-option>
                    <a-select-option value="account">账号</a-select-option>
                    <a-select-option value="device">设备</a-select-option>
                  </a-select>
                  <a-button :loading="restrictionLoading" @click="loadRestrictions">查询</a-button>
                  <a-button type="primary" v-permission="'system:access-restriction:save'" @click="openCreateRestrictionForm">
                    新增规则
                  </a-button>
                </a-space>
              </div>
              <a-alert
                v-if="restrictionErrorMessage"
                type="error"
                show-icon
                :message="restrictionErrorMessage"
                class="restriction-alert"
              />
              <a-table
                row-key="id"
                size="small"
                :columns="restrictionColumns"
                :data-source="restrictions"
                :loading="restrictionLoading"
                :pagination="$tablePagination"
              >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'restrictionType'">
                    <a-tag>{{ restrictionTypeLabel(record.restrictionType) }}</a-tag>
                  </template>
                  <template v-if="column.key === 'effect'">
                    <a-tag :color="record.effect === 'allow' ? 'green' : 'red'">
                      {{ record.effect === 'allow' ? '允许' : '拒绝' }}
                    </a-tag>
                  </template>
                  <template v-if="column.key === 'timeRange'">
                    {{ formatTimeRange(record.startAt, record.endAt) }}
                  </template>
                  <template v-if="column.key === 'action'">
                    <a-button
                      size="small"
                      type="link"
                      v-permission="'system:access-restriction:save'"
                      @click="openEditRestrictionForm(record)"
                    >
                      编辑
                    </a-button>
                  </template>
                </template>
              </a-table>
            </section>
          </div>
        </a-tab-pane>

        <a-tab-pane key="traffic" tab="访问态势">
          <div class="content-grid">
            <section class="content-section">
              <div class="section-title">
                <strong>请求来源排行</strong>
                <span>支持快速封禁异常来源</span>
              </div>
              <a-table
                row-key="name"
                size="small"
                :columns="ipRankColumns"
                :data-source="ipRanking"
                :loading="loading"
                :pagination="false"
              >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'action'">
                    <a-button size="small" type="link" @click="fillBlockIp(record.name)">封禁</a-button>
                  </template>
                </template>
              </a-table>
            </section>

            <section class="content-section">
              <div class="section-title">
                <strong>接口访问排行</strong>
                <span>用于识别高频接口和异常路径</span>
              </div>
              <a-table
                row-key="name"
                size="small"
                :columns="apiRankColumns"
                :data-source="apiRanking"
                :loading="loading"
                :pagination="false"
              />
            </section>
          </div>
        </a-tab-pane>

        <a-tab-pane key="events" tab="安全事件">
          <section class="content-section">
            <div class="section-title">
              <strong>最近安全事件</strong>
              <span>保留运行时处置轨迹</span>
            </div>
            <a-table
              row-key="id"
              size="small"
              :columns="eventColumns"
              :data-source="events"
              :loading="loading"
              :pagination="$tablePagination"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'eventLevel'">
                  <a-tag :color="eventLevelColor(record.eventLevel)">{{ eventLevelLabel(record.eventLevel) }}</a-tag>
                </template>
                <template v-if="column.key === 'result'">
                  <a-tag :color="resultColor(record.result)">{{ resultLabel(record.result) }}</a-tag>
                </template>
                <template v-if="column.key === 'occurredAt'">
                  {{ formatDateTime(record.occurredAt) }}
                </template>
              </template>
            </a-table>
          </section>
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <a-modal
      v-model:open="restrictionFormOpen"
      title="访问限制规则"
      :confirm-loading="restrictionSaving"
      @ok="submitRestrictionForm"
    >
      <a-form layout="vertical">
        <a-form-item label="租户编码">
          <a-input v-model:value="restrictionFormState.tenantId" readonly />
        </a-form-item>
        <a-form-item label="限制类型" required>
          <a-select v-model:value="restrictionFormState.restrictionType">
            <a-select-option value="ip">IP</a-select-option>
            <a-select-option value="account">账号</a-select-option>
            <a-select-option value="device">设备</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="规则值" required>
          <a-input v-model:value="restrictionFormState.ruleValue" placeholder="请输入 IP、账号或设备标识" />
        </a-form-item>
        <a-form-item label="生效动作" required>
          <a-select v-model:value="restrictionFormState.effect">
            <a-select-option value="allow">允许</a-select-option>
            <a-select-option value="deny">拒绝</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="生效开始时间">
          <a-input v-model:value="restrictionFormState.startAt" type="datetime-local" />
        </a-form-item>
        <a-form-item label="生效结束时间">
          <a-input v-model:value="restrictionFormState.endAt" type="datetime-local" />
        </a-form-item>
      </a-form>
      <span class="permission-code">system:access-restriction:save</span>
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { message } from 'ant-design-vue';

import {
  listSystemAccessRestrictions,
  saveSystemAccessRestriction,
  type SystemAccessRestriction,
  type SystemAccessRestrictionSavePayload,
} from '@/api/system/access-restriction';
import {
  blockSystemSecurityIp,
  getSystemSecurityOverview,
  listSystemSecurityApiRanking,
  listSystemSecurityEvents,
  listSystemSecurityIpRanking,
  listSystemSecurityPolicies,
  saveSystemSecurityPolicy,
  unblockSystemSecurityIp,
  type SystemSecurityEvent,
  type SystemSecurityOverview,
  type SystemSecurityPolicy,
  type SystemSecurityRank,
} from '@/api/system/security-protection';
import type { LoadStatus } from '@/types/platform';
import { getAdminRuntimeContext, requireAdminTenantId } from '@/utils/adminContext';

/** 当前租户业务编码。 */
const tenantId = ref(getAdminRuntimeContext().tenantId);
/** 当前页面标签。 */
const activeTabKey = ref('rules');
/** 页面加载状态。 */
const status = ref<LoadStatus>('idle');
/** 异常提示文案。 */
const errorMessage = ref('');
/** 正在保存的策略编码。 */
const savingPolicyCode = ref('');
/** 封禁按钮加载状态。 */
const blocking = ref(false);
/** 解封按钮加载状态。 */
const unblocking = ref(false);
/** 访问限制加载状态。 */
const restrictionLoading = ref(false);
/** 访问限制保存状态。 */
const restrictionSaving = ref(false);
/** 访问限制表单弹窗状态。 */
const restrictionFormOpen = ref(false);
/** 访问限制异常提示。 */
const restrictionErrorMessage = ref('');
/** 当前限制类型。 */
const restrictionType = ref('ip');
/** 安全防护总览。 */
const overview = ref<SystemSecurityOverview>({
  statDate: '',
  todaySourceCount: 0,
  maxIpRequestCount: 0,
  violationIpCount: 0,
  blockedIpCount: 0,
});
/** 安全防护策略列表。 */
const policies = ref<SystemSecurityPolicy[]>([]);
/** 来源 IP 请求排行。 */
const ipRanking = ref<SystemSecurityRank[]>([]);
/** 接口访问排行。 */
const apiRanking = ref<SystemSecurityRank[]>([]);
/** 最近安全事件。 */
const events = ref<SystemSecurityEvent[]>([]);
/** 访问限制规则列表。 */
const restrictions = ref<SystemAccessRestriction[]>([]);
/** IP 封禁表单。 */
const blockForm = reactive({
  ipValue: '',
  reason: '安全防护中心手动封禁',
  endAt: '',
});
/** 访问限制表单状态。 */
const restrictionFormState = reactive<SystemAccessRestrictionSavePayload>({
  tenantId: getAdminRuntimeContext().tenantId,
  restrictionType: 'ip',
  ruleValue: '',
  effect: 'deny',
  startAt: undefined,
  endAt: undefined,
});
/** 是否处于加载中。 */
const loading = computed(() => status.value === 'loading');

/** 策略表格列定义。 */
const policyColumns = [
  { title: '策略编码', dataIndex: 'policyCode', key: 'policyCode', width: 150 },
  { title: '策略名称', dataIndex: 'policyName', key: 'policyName', width: 170 },
  { title: '范围 / 目标', key: 'scope', width: 180 },
  { title: '阈值 / 秒', key: 'threshold', width: 160 },
  { title: '动作', key: 'action', width: 110 },
  { title: '封禁秒数', key: 'blockSeconds', width: 110 },
  { title: '状态', key: 'status', width: 100 },
  { title: '操作', key: 'actionColumn', width: 80 },
];
/** 访问限制表格列定义。 */
const restrictionColumns = [
  { title: '类型', dataIndex: 'restrictionType', key: 'restrictionType', width: 90 },
  { title: '规则值', dataIndex: 'ruleValue', key: 'ruleValue' },
  { title: '动作', dataIndex: 'effect', key: 'effect', width: 90 },
  { title: '生效时间', key: 'timeRange', width: 220 },
  { title: '操作', key: 'action', width: 80 },
];
/** 来源 IP 排行列定义。 */
const ipRankColumns = [
  { title: 'IP', dataIndex: 'name', key: 'name' },
  { title: '请求数', dataIndex: 'requestCount', key: 'requestCount', width: 100 },
  { title: '操作', key: 'action', width: 90 },
];
/** 接口访问排行列定义。 */
const apiRankColumns = [
  { title: '接口', dataIndex: 'name', key: 'name' },
  { title: '请求数', dataIndex: 'requestCount', key: 'requestCount', width: 100 },
];
/** 最近事件列定义。 */
const eventColumns = [
  { title: '等级', key: 'eventLevel', width: 90 },
  { title: '来源 IP', dataIndex: 'sourceIp', key: 'sourceIp', width: 150 },
  { title: '路径', dataIndex: 'requestPath', key: 'requestPath' },
  { title: '结果', key: 'result', width: 100 },
  { title: '时间', key: 'occurredAt', width: 170 },
];

/**
 * 加载安全防护中心全部数据。
 */
async function loadAll(): Promise<void> {
  status.value = 'loading';
  errorMessage.value = '';
  const currentTenantId = syncTenantContext();
  try {
    const [overviewData, policyList, ipRankList, apiRankList, eventList] = await Promise.all([
      getSystemSecurityOverview(currentTenantId),
      listSystemSecurityPolicies(currentTenantId),
      listSystemSecurityIpRanking(currentTenantId),
      listSystemSecurityApiRanking(currentTenantId),
      listSystemSecurityEvents(currentTenantId),
    ]);
    overview.value = overviewData;
    policies.value = policyList;
    ipRanking.value = ipRankList;
    apiRanking.value = apiRankList;
    events.value = eventList;
    status.value = 'success';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '安全防护中心数据加载失败';
    status.value = 'error';
  }
  await loadRestrictions();
}

/**
 * 加载访问限制规则。
 */
async function loadRestrictions(): Promise<void> {
  restrictionLoading.value = true;
  restrictionErrorMessage.value = '';
  try {
    restrictions.value = await listSystemAccessRestrictions(syncTenantContext(), restrictionType.value);
  } catch (error) {
    restrictionErrorMessage.value = error instanceof Error ? error.message : '访问限制规则加载失败';
  } finally {
    restrictionLoading.value = false;
  }
}

/**
 * 保存单条安全防护策略。
 *
 * @param policy 安全防护策略
 */
async function savePolicyRow(policy: SystemSecurityPolicy): Promise<void> {
  if (!policy.thresholdLimit || !policy.windowSeconds) {
    message.error('阈值和时间窗口必须大于 0');
    return;
  }
  savingPolicyCode.value = policy.policyCode;
  try {
    await saveSystemSecurityPolicy({
      ...policy,
      tenantId: syncTenantContext(),
      blockSeconds: policy.blockSeconds || undefined,
    });
    message.success('策略已保存');
    await loadAll();
  } finally {
    savingPolicyCode.value = '';
  }
}

/**
 * 打开新增访问限制规则表单。
 */
function openCreateRestrictionForm(): void {
  const currentTenantId = syncTenantContext();
  Object.assign(restrictionFormState, {
    tenantId: currentTenantId,
    restrictionType: restrictionType.value,
    ruleValue: '',
    effect: 'deny',
    startAt: undefined,
    endAt: undefined,
  });
  restrictionFormOpen.value = true;
}

/**
 * 打开编辑访问限制规则表单。
 *
 * @param restriction 访问限制规则
 */
function openEditRestrictionForm(restriction: SystemAccessRestriction): void {
  Object.assign(restrictionFormState, {
    tenantId: restriction.tenantId,
    restrictionType: restriction.restrictionType,
    ruleValue: restriction.ruleValue,
    effect: restriction.effect,
    startAt: normalizeDateTime(restriction.startAt),
    endAt: normalizeDateTime(restriction.endAt),
  });
  restrictionFormOpen.value = true;
}

/**
 * 提交访问限制规则表单。
 */
async function submitRestrictionForm(): Promise<void> {
  if (!restrictionFormState.ruleValue.trim()) {
    message.error('请输入访问限制规则值');
    return;
  }
  restrictionSaving.value = true;
  try {
    const currentTenantId = syncTenantContext();
    await saveSystemAccessRestriction({
      ...restrictionFormState,
      tenantId: currentTenantId,
      ruleValue: restrictionFormState.ruleValue.trim(),
      startAt: normalizeDateTime(restrictionFormState.startAt),
      endAt: normalizeDateTime(restrictionFormState.endAt),
    });
    restrictionFormOpen.value = false;
    restrictionType.value = restrictionFormState.restrictionType;
    message.success('访问限制规则已保存');
    await loadRestrictions();
  } finally {
    restrictionSaving.value = false;
  }
}

/**
 * 提交 IP 封禁。
 */
async function submitBlock(): Promise<void> {
  if (!blockForm.ipValue.trim()) {
    message.error('请输入 IP 或 CIDR');
    return;
  }
  blocking.value = true;
  try {
    await blockSystemSecurityIp({
      tenantId: syncTenantContext(),
      ipValue: blockForm.ipValue.trim(),
      blockType: 'manual',
      reason: blockForm.reason.trim() || '安全防护中心手动封禁',
      endAt: normalizeDateTime(blockForm.endAt),
    });
    message.success('IP 已封禁');
    await loadAll();
  } finally {
    blocking.value = false;
  }
}

/**
 * 提交 IP 解封。
 */
async function submitUnblock(): Promise<void> {
  if (!blockForm.ipValue.trim()) {
    message.error('请输入需要解封的 IP 或 CIDR');
    return;
  }
  unblocking.value = true;
  try {
    await unblockSystemSecurityIp(syncTenantContext(), blockForm.ipValue.trim());
    message.success('IP 已解封');
    await loadAll();
  } finally {
    unblocking.value = false;
  }
}

/**
 * 从排行中带入封禁 IP。
 *
 * @param ipValue 来源 IP
 */
function fillBlockIp(ipValue: string): void {
  blockForm.ipValue = ipValue;
}

/**
 * 同步后台运行时租户上下文。
 *
 * @returns 当前后台租户编码
 */
function syncTenantContext(): string {
  const currentTenantId = requireAdminTenantId();
  tenantId.value = currentTenantId;
  return currentTenantId;
}

/**
 * 转换防护范围显示文案。
 *
 * @param scope 防护范围
 * @returns 防护范围文案
 */
function scopeLabel(scope: string): string {
  const labels: Record<string, string> = {
    admin_api: '后台接口',
    login: '登录',
    openapi: '开放 API',
    ai: 'AI 调用',
  };
  return labels[scope] || scope;
}

/**
 * 转换访问限制类型文案。
 *
 * @param value 访问限制类型
 * @returns 访问限制类型文案
 */
function restrictionTypeLabel(value: string): string {
  const labels: Record<string, string> = {
    ip: 'IP',
    account: '账号',
    device: '设备',
  };
  return labels[value] || value;
}

/**
 * 转换安全等级颜色。
 *
 * @param level 安全等级
 * @returns 标签颜色
 */
function eventLevelColor(level: string): string {
  const colors: Record<string, string> = {
    low: 'green',
    medium: 'orange',
    high: 'red',
    critical: 'purple',
  };
  return colors[level] || 'default';
}

/**
 * 转换安全等级文案。
 *
 * @param level 安全等级
 * @returns 安全等级文案
 */
function eventLevelLabel(level: string): string {
  const labels: Record<string, string> = {
    low: '低',
    medium: '中',
    high: '高',
    critical: '严重',
  };
  return labels[level] || level;
}

/**
 * 转换事件结果颜色。
 *
 * @param result 事件结果
 * @returns 标签颜色
 */
function resultColor(result: string): string {
  if (['blocked', 'denied', 'failed', 'limited'].includes(result)) {
    return 'red';
  }
  if (result === 'allowed') {
    return 'green';
  }
  return 'blue';
}

/**
 * 转换事件结果文案。
 *
 * @param result 事件结果
 * @returns 事件结果文案
 */
function resultLabel(result: string): string {
  const labels: Record<string, string> = {
    allowed: '允许',
    blocked: '封禁',
    denied: '拒绝',
    failed: '失败',
    limited: '限流',
    recorded: '记录',
  };
  return labels[result] || result;
}

/**
 * 格式化统计日期。
 *
 * @param value 统计日期
 * @returns 日期文案
 */
function formatStatDate(value: string): string {
  if (!value || value.length !== 8) {
    return '-';
  }
  return `${value.slice(0, 4)}-${value.slice(4, 6)}-${value.slice(6, 8)}`;
}

/**
 * 格式化日期时间。
 *
 * @param value 日期时间
 * @returns 日期时间文案
 */
function formatDateTime(value?: string): string {
  if (!value) {
    return '-';
  }
  return value.replace('T', ' ').slice(0, 19);
}

/**
 * 格式化生效时间范围。
 *
 * @param startAt 生效开始时间
 * @param endAt 生效结束时间
 * @returns 生效时间范围文案
 */
function formatTimeRange(startAt?: string, endAt?: string): string {
  return `${startAt ? formatDateTime(startAt) : '立即'} 至 ${endAt ? formatDateTime(endAt) : '长期'}`;
}

/**
 * 规范化日期时间输入。
 *
 * @param value 日期时间值
 * @returns 后端 LocalDateTime 可解析的日期时间值
 */
function normalizeDateTime(value?: string): string | undefined {
  if (!value) {
    return undefined;
  }
  return value.length === 16 ? `${value}:00` : value;
}

onMounted(() => {
  void loadAll();
});
</script>

<style scoped>
.security-protection-page {
  min-width: 0;
}

.security-card :deep(.ant-card-head) {
  border-bottom-color: #edf1f7;
}

.card-title,
.operation-bar,
.section-title,
.section-title-actions {
  display: flex;
  align-items: center;
}

.card-title {
  gap: 10px;
}

.tenant-id {
  width: 180px;
}

.state-alert,
.restriction-alert {
  margin-bottom: 16px;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.metric-item {
  min-width: 0;
  padding: 14px 16px;
  border: 1px solid #edf1f7;
  border-radius: 8px;
  background: #fbfcff;
}

.metric-label,
.metric-item small,
.operation-copy span,
.section-title span {
  color: #667085;
}

.metric-label,
.metric-item small {
  display: block;
}

.metric-item strong {
  display: block;
  margin: 6px 0;
  color: #111827;
  font-size: 26px;
  line-height: 32px;
}

.operation-bar {
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
  padding: 14px 16px;
  border: 1px solid #edf1f7;
  border-radius: 8px;
  background: #f8fbff;
}

.operation-copy {
  display: flex;
  min-width: 220px;
  flex-direction: column;
  gap: 4px;
}

.operation-copy strong,
.section-title strong {
  color: #111827;
}

.ip-input {
  width: 190px;
}

.reason-input {
  width: 260px;
}

.time-input {
  width: 210px;
}

.security-tabs :deep(.ant-tabs-nav) {
  margin-bottom: 14px;
}

.content-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(360px, 0.85fr);
  gap: 18px;
}

.rules-grid {
  grid-template-columns: minmax(520px, 1.05fr) minmax(480px, 0.95fr);
}

.content-section {
  min-width: 0;
}

.section-title {
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.section-title-actions {
  align-items: flex-start;
}

.section-title-actions > div {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 4px;
}

.section-title strong {
  font-size: 16px;
}

.section-title span {
  font-size: 13px;
}

.target-pattern {
  margin-left: 6px;
  color: #667085;
  font-size: 12px;
}

.threshold-input,
.window-input {
  width: 76px;
}

.action-select {
  width: 96px;
}

.status-select {
  width: 86px;
}

.block-seconds-input {
  width: 96px;
}

.restriction-type-select {
  width: 96px;
}

.permission-code {
  display: none;
}

@media (max-width: 1400px) {
  .operation-bar {
    align-items: flex-start;
    flex-direction: column;
  }

  .content-grid,
  .rules-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 1200px) {
  .metrics-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .metrics-grid {
    grid-template-columns: 1fr;
  }

  .ip-input,
  .reason-input,
  .time-input,
  .tenant-id {
    width: 100%;
  }
}
</style>
