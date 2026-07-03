<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <div class="logic-flow-designer">
    <aside class="designer-palette">
      <div class="panel-heading">
        <span class="panel-title">节点工具</span>
        <span class="panel-subtitle">4 类</span>
      </div>
      <div class="palette-actions">
        <button class="tool-card tool-card-start" type="button" @click="addWorkflowNode('start')">
          <span class="tool-icon"><FlagOutlined /></span>
          <span class="tool-content">
            <span class="tool-title">开始节点</span>
            <span class="tool-desc">入口</span>
          </span>
        </button>
        <button class="tool-card tool-card-approval" type="button" @click="addWorkflowNode('approval')">
          <span class="tool-icon"><UserOutlined /></span>
          <span class="tool-content">
            <span class="tool-title">审批节点</span>
            <span class="tool-desc">处理人</span>
          </span>
        </button>
        <button class="tool-card tool-card-condition" type="button" @click="addWorkflowNode('condition')">
          <span class="tool-icon"><BranchesOutlined /></span>
          <span class="tool-content">
            <span class="tool-title">条件节点</span>
            <span class="tool-desc">分支</span>
          </span>
        </button>
        <button class="tool-card tool-card-end" type="button" @click="addWorkflowNode('end')">
          <span class="tool-icon"><CheckCircleOutlined /></span>
          <span class="tool-content">
            <span class="tool-title">结束节点</span>
            <span class="tool-desc">出口</span>
          </span>
        </button>
      </div>
    </aside>

    <main class="designer-workbench">
      <div class="designer-toolbar">
        <div class="canvas-meta">
          <span class="canvas-title">流程画布</span>
          <span class="canvas-subtitle">{{ props.modelCode || '未命名模型' }}</span>
        </div>
        <a-space wrap size="small" class="toolbar-group">
          <a-button size="small" @click="resetCanvas">
            <template #icon><ReloadOutlined /></template>
            重置
          </a-button>
          <a-button size="small" @click="undo">
            <template #icon><UndoOutlined /></template>
            撤销
          </a-button>
          <a-button size="small" @click="redo">
            <template #icon><RedoOutlined /></template>
            重做
          </a-button>
          <a-button size="small" @click="zoomOut">
            <template #icon><ZoomOutOutlined /></template>
            缩小
          </a-button>
          <a-button size="small" @click="zoomIn">
            <template #icon><ZoomInOutlined /></template>
            放大
          </a-button>
          <a-button size="small" @click="fitCanvas">适应画布</a-button>
        </a-space>
        <div class="toolbar-actions">
          <a-button type="primary" size="small" @click="saveDesign">
            <template #icon><SaveOutlined /></template>
            保存设计稿
          </a-button>
        </div>
      </div>

      <div class="canvas-stage">
        <div ref="canvasRef" class="logic-flow-canvas" />
        <div class="canvas-hud">
          <span>{{ graphStats.nodeCount }} 个节点</span>
          <span>{{ graphStats.edgeCount }} 条连线</span>
          <span>{{ graphStats.zoomPercent }}%</span>
        </div>
        <div class="canvas-tip">
          {{ selectedElement ? `${selectedElement.elementType} · ${selectedElement.name || selectedElement.id}` : '拖动节点编排流程，点击元素配置属性' }}
        </div>
      </div>
    </main>

    <aside class="designer-properties">
      <div class="panel-heading">
        <span class="panel-title">属性配置</span>
        <span class="panel-subtitle">{{ selectedElement ? selectedElement.elementType : '未选择' }}</span>
      </div>
      <a-form v-if="selectedElement" layout="vertical" class="property-form">
        <a-tabs v-model:activeKey="propertyTab" size="small" class="property-tabs">
          <a-tab-pane key="basic" tab="基础信息">
            <section class="property-section">
              <div class="section-title">基础信息</div>
              <a-form-item label="名称">
                <a-input v-model:value="selectedElement.name" @blur="syncSelectedName" @press-enter="syncSelectedName" />
              </a-form-item>
              <a-form-item label="元素 ID">
                <a-input :value="selectedElement.id" disabled />
              </a-form-item>
            </section>
          </a-tab-pane>
          <a-tab-pane
            key="assignee"
            v-if="selectedElement.type === 'node' && selectedElement.nodeKind === 'approval'"
            tab="审批配置"
          >
            <section class="property-section">
              <div class="section-title">审批配置</div>
              <a-form-item label="处理人类型">
                <a-select v-model:value="selectedElement.assigneeType" @change="syncSelectedNodeProperties">
                  <a-select-option value="initiator">发起人</a-select-option>
                  <a-select-option value="user">指定用户</a-select-option>
                  <a-select-option value="role">角色</a-select-option>
                  <a-select-option value="post">岗位</a-select-option>
                  <a-select-option value="expression">表达式</a-select-option>
                </a-select>
              </a-form-item>
              <a-form-item v-if="selectedElement.assigneeType === 'user'" label="指定用户">
                <a-select
                  v-model:value="selectedElement.candidateUserIds"
                  mode="multiple"
                  show-search
                  allow-clear
                  :options="userOptions"
                  @change="syncSelectedNodeProperties"
                />
              </a-form-item>
              <a-form-item v-if="selectedElement.assigneeType === 'role'" label="角色">
                <a-select
                  v-model:value="selectedElement.candidateRoleCodes"
                  mode="multiple"
                  show-search
                  allow-clear
                  :options="roleOptions"
                  @change="syncSelectedNodeProperties"
                />
              </a-form-item>
              <a-form-item v-if="selectedElement.assigneeType === 'post'" label="岗位">
                <a-select
                  v-model:value="selectedElement.candidatePostCodes"
                  mode="multiple"
                  show-search
                  allow-clear
                  :options="postOptions"
                  @change="syncSelectedNodeProperties"
                />
              </a-form-item>
              <a-form-item v-if="selectedElement.assigneeType === 'expression'" label="处理人表达式">
                <a-input
                  v-model:value="selectedElement.assigneeExpression"
                  placeholder="${managerUserId}"
                  @blur="syncSelectedNodeProperties"
                  @press-enter="syncSelectedNodeProperties"
                />
              </a-form-item>
              <a-form-item label="节点表单">
                <a-select
                  v-model:value="selectedElement.formKey"
                  show-search
                  allow-clear
                  :options="formOptions"
                  @change="syncSelectedNodeProperties"
                />
              </a-form-item>
            </section>
          </a-tab-pane>
          <a-tab-pane
            key="policy"
            v-if="selectedElement.type === 'node' && selectedElement.nodeKind === 'approval'"
            tab="流程策略"
          >
            <section class="property-section">
              <div class="section-title">流程策略</div>
              <a-form-item label="审批方式">
                <a-select v-model:value="selectedElement.approvalType" @change="syncSelectedNodeProperties">
                  <a-select-option value="single">单人审批</a-select-option>
                  <a-select-option value="all">会签全部通过</a-select-option>
                  <a-select-option value="percent">按比例通过</a-select-option>
                </a-select>
              </a-form-item>
              <a-form-item label="通过条件">
                <a-input
                  v-model:value="selectedElement.passCondition"
                  placeholder="${approvedCount / totalCount >= 0.5}"
                  @blur="syncSelectedNodeProperties"
                  @press-enter="syncSelectedNodeProperties"
                />
              </a-form-item>
              <a-form-item label="超时小时">
                <a-input-number
                  v-model:value="selectedElement.timeoutHours"
                  :min="1"
                  :precision="0"
                  class="full-input"
                  @change="syncSelectedNodeProperties"
                />
              </a-form-item>
              <a-form-item label="抄送用户">
                <a-select
                  v-model:value="selectedElement.ccUserIds"
                  mode="multiple"
                  show-search
                  allow-clear
                  :options="userOptions"
                  @change="syncSelectedNodeProperties"
                />
              </a-form-item>
            </section>
          </a-tab-pane>
          <a-tab-pane v-if="selectedElement.type === 'edge'" key="condition" tab="流转条件">
            <section class="property-section">
              <div class="section-title">流转条件</div>
              <a-form-item label="条件表达式">
                <a-input
                  v-model:value="selectedElement.conditionExpression"
                  placeholder="${approved == true}"
                  @blur="syncSelectedCondition"
                  @press-enter="syncSelectedCondition"
                />
              </a-form-item>
            </section>
          </a-tab-pane>
        </a-tabs>
        <div class="property-footer">
          <a-button danger block @click="deleteSelected">
            <template #icon><DeleteOutlined /></template>
            删除
          </a-button>
        </div>
      </a-form>
      <a-empty v-else image="simple" description="未选择元素" />
    </aside>
  </div>
</template>

<script setup lang="ts">
import LogicFlow, { BaseNode, h, RectNodeModel } from '@logicflow/core';
import '@logicflow/core/dist/index.css';
import {
  DeleteOutlined,
  BranchesOutlined,
  CheckCircleOutlined,
  FlagOutlined,
  PlusOutlined,
  RedoOutlined,
  ReloadOutlined,
  SaveOutlined,
  UndoOutlined,
  UserOutlined,
  ZoomInOutlined,
  ZoomOutOutlined,
} from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue';

import { listSystemPosts, type SystemPost } from '@/api/system/post';
import { listSystemRoles, type SystemRole } from '@/api/system/role';
import { listSystemUsers, type SystemUser } from '@/api/system/user';
import { listWorkflowFormBindings, type WorkflowFormBinding } from '@/api/workflow/binding';
import { requireAdminTenantId } from '@/utils/adminContext';
import {
  convertLogicFlowToBpmnXml,
  createDefaultLogicFlowData,
  createLogicFlowDataFromBpmnXml,
} from './logicFlowBpmnConverter';

type WorkflowNodeKind = 'start' | 'approval' | 'condition' | 'end';
type AssigneeType = 'initiator' | 'user' | 'role' | 'post' | 'expression';
type ApprovalType = 'single' | 'all' | 'percent';

const WORKFLOW_NODE_TYPES: Record<WorkflowNodeKind, string> = {
  start: 'workflow-start',
  approval: 'workflow-approval',
  condition: 'workflow-condition',
  end: 'workflow-end',
};

interface ApprovalNodeProperties {
  [key: string]: unknown;
  kind?: WorkflowNodeKind;
  width?: number;
  height?: number;
  radius?: number;
  assigneeType?: AssigneeType;
  assigneeExpression?: string;
  candidateUserIds?: string[];
  candidateRoleCodes?: string[];
  candidatePostCodes?: string[];
  formKey?: string;
  approvalType?: ApprovalType;
  passCondition?: string;
  timeoutHours?: number;
  ccUserIds?: string[];
}

interface DesignerProps {
  modelCode: string;
  modelName: string;
  initialXml?: string;
}

interface SelectedElement {
  type: 'node' | 'edge';
  id: string;
  elementType: string;
  name: string;
  nodeKind?: WorkflowNodeKind;
  assigneeType?: AssigneeType;
  assigneeExpression?: string;
  candidateUserIds?: string[];
  candidateRoleCodes?: string[];
  candidatePostCodes?: string[];
  formKey?: string;
  approvalType?: ApprovalType;
  passCondition?: string;
  timeoutHours?: number;
  ccUserIds?: string[];
  conditionExpression?: string;
}

interface WorkflowNodeVisualConfig {
  title: string;
  desc: string;
  badge: string;
  fill: string;
  stroke: string;
  accent: string;
  icon: string;
  iconBg: string;
  iconPath: string;
  radius: number;
  filter: string;
}

const props = defineProps<DesignerProps>();
const emit = defineEmits<{
  save: [xml: string];
}>();

const canvasRef = ref<HTMLDivElement>();
const logicFlow = ref<LogicFlow>();
const selectedElement = ref<SelectedElement | null>(null);
const propertyTab = ref('basic');
const graphStats = ref({
  nodeCount: 0,
  edgeCount: 0,
  zoomPercent: 100,
});
const nodeSequence = ref(1);
const users = ref<SystemUser[]>([]);
const roles = ref<SystemRole[]>([]);
const posts = ref<SystemPost[]>([]);
const formBindings = ref<WorkflowFormBinding[]>([]);

/** 可选用户，统一使用用户主键作为 Flowable 处理人 ID。 */
const userOptions = computed(() =>
  users.value
    .filter((user) => user.status === 'enabled')
    .map((user) => ({
      label: `${user.nickname}（${user.username}）`,
      value: String(user.id),
    })),
);
/** 可选角色，写入 Flowable candidateGroups 时加 role 前缀。 */
const roleOptions = computed(() =>
  roles.value
    .filter((role) => role.status === 'enabled')
    .map((role) => ({
      label: `${role.name}（${role.roleCode}）`,
      value: role.roleCode,
    })),
);
/** 可选岗位，写入 Flowable candidateGroups 时加 post 前缀。 */
const postOptions = computed(() =>
  posts.value
    .filter((post) => post.status === 'enabled')
    .map((post) => ({
      label: `${post.postName}（${post.postCode}）`,
      value: post.postCode,
    })),
);
/** 可选表单，优先绑定后台表单路由作为节点 formKey。 */
const formOptions = computed(() =>
  formBindings.value
    .filter((binding) => binding.status === 'enabled')
    .map((binding) => ({
      label: `${binding.businessModule} / ${binding.businessTable}（${binding.formRoute}）`,
      value: binding.formRoute,
    })),
);

class WorkflowNodeModel extends RectNodeModel<ApprovalNodeProperties> {
  setAttributes(): void {
    const nodeSize = resolveWorkflowNodeSize(this.properties.kind);
    this.width = Number(this.properties.width) || nodeSize.width;
    this.height = Number(this.properties.height) || nodeSize.height;
    this.radius = Number(this.properties.radius) || nodeSize.radius;
  }

  getTextStyle(): LogicFlow.TextNodeTheme {
    return {
      ...super.getTextStyle(),
      color: 'transparent',
      fontSize: 1,
    };
  }
}

class WorkflowNodeView extends BaseNode {
  getShape(): h.JSX.Element {
    const { model } = this.props;
    const properties = model.properties as ApprovalNodeProperties;
    const nodeKind = properties.kind ?? 'approval';
    const config = getWorkflowNodeVisualConfig(nodeKind);
    const x = model.x - model.width / 2;
    const y = model.y - model.height / 2;
    const iconX = x + 31;
    const iconY = y + model.height / 2;
    const titleX = x + 58;
    const titleY = y + 26;
    const descY = y + 45;
    const title = readElementText(model.text as LogicFlow.TextConfig) || config.title;

    return h('g', { className: `workflow-node workflow-node-${nodeKind}` }, [
      model.isSelected
        ? h('rect', {
          x: x - 5,
          y: y - 5,
          width: model.width + 10,
          height: model.height + 10,
          rx: config.radius + 5,
          ry: config.radius + 5,
          fill: 'rgba(47, 109, 246, 0.08)',
          stroke: 'rgba(47, 109, 246, 0.34)',
          strokeWidth: 1.2,
        })
        : null,
      h('rect', {
        className: 'workflow-node-shell',
        x,
        y,
        width: model.width,
        height: model.height,
        rx: config.radius,
        ry: config.radius,
        fill: config.fill,
        stroke: config.stroke,
        strokeWidth: model.isSelected ? 1.6 : 1,
        filter: config.filter,
      }),
      h('path', {
        d: `M${x + config.radius} ${y}H${x + model.width - config.radius}Q${x + model.width} ${y} ${x + model.width} ${y + config.radius}V${y + model.height - config.radius}Q${x + model.width} ${y + model.height} ${x + model.width - config.radius} ${y + model.height}H${x + config.radius}Q${x} ${y + model.height} ${x} ${y + model.height - config.radius}V${y + config.radius}Q${x} ${y} ${x + config.radius} ${y}Z`,
        fill: 'none',
        stroke: model.isSelected ? config.accent : 'transparent',
        strokeWidth: 1.4,
      }),
      h('rect', {
        x: x + 12,
        y: iconY - 17,
        width: 34,
        height: 34,
        rx: 10,
        ry: 10,
        fill: config.iconBg,
      }),
      h('path', {
        d: config.iconPath,
        transform: `translate(${iconX - 21}, ${iconY - 21})`,
        fill: 'none',
        stroke: config.icon,
        strokeWidth: 1.9,
        strokeLinecap: 'round',
        strokeLinejoin: 'round',
      }),
      h('text', {
        x: titleX,
        y: titleY,
        fill: '#1f2937',
        fontSize: 14,
        fontWeight: 700,
        dominantBaseline: 'middle',
      }, title),
      h('text', {
        x: x + model.width - 16,
        y: titleY,
        fill: config.accent,
        fontSize: 10,
        fontWeight: 600,
        textAnchor: 'end',
        dominantBaseline: 'middle',
      }, config.badge),
      h('text', {
        x: titleX,
        y: descY,
        fill: '#7b8798',
        fontSize: 11,
        dominantBaseline: 'middle',
      }, config.desc),
    ]);
  }

  getText(): h.JSX.Element | null {
    return null;
  }
}

function registerWorkflowNodes(instance: LogicFlow): void {
  Object.values(WORKFLOW_NODE_TYPES).forEach((type) => {
    instance.register({
      type,
      view: WorkflowNodeView,
      model: WorkflowNodeModel,
    });
  });
}

function resolveWorkflowNodeSize(kind: WorkflowNodeKind = 'approval'): { width: number; height: number; radius: number } {
  if (kind === 'start' || kind === 'end') {
    return { width: 156, height: 56, radius: 14 };
  }
  if (kind === 'condition') {
    return { width: 172, height: 60, radius: 14 };
  }
  return { width: 196, height: 64, radius: 14 };
}

function getWorkflowNodeVisualConfig(kind: WorkflowNodeKind): WorkflowNodeVisualConfig {
  const configs: Record<WorkflowNodeKind, WorkflowNodeVisualConfig> = {
    start: {
      title: '开始节点',
      desc: '流程入口',
      badge: '开始',
      fill: '#ffffff',
      stroke: '#dbe7e4',
      accent: '#14b8a6',
      icon: '#0f766e',
      iconBg: '#e7faf5',
      iconPath: 'M16 12h10M16 12v14M16 13c3-4 7 3 10-1v10c-3 4-7-3-10 1',
      radius: 14,
      filter: 'drop-shadow(0 10px 18px rgba(15, 23, 42, 0.08))',
    },
    approval: {
      title: '审批节点',
      desc: '处理人 / 表单 / 策略',
      badge: '审批',
      fill: '#ffffff',
      stroke: '#d8e2f2',
      accent: '#3b82f6',
      icon: '#2563eb',
      iconBg: '#eef5ff',
      iconPath: 'M21 13a4 4 0 1 0 0 8a4 4 0 0 0 0-8M13 29c1.5-4 14.5-4 16 0',
      radius: 14,
      filter: 'drop-shadow(0 12px 22px rgba(15, 23, 42, 0.1))',
    },
    condition: {
      title: '条件节点',
      desc: '条件分支',
      badge: '分支',
      fill: '#ffffff',
      stroke: '#f0dfcf',
      accent: '#fb923c',
      icon: '#ea580c',
      iconBg: '#fff3e6',
      iconPath: 'M21 12v5M21 17c-5 0-7 3-7 7M21 17c5 0 7 3 7 7M14 24h-3M28 24h3',
      radius: 14,
      filter: 'drop-shadow(0 12px 22px rgba(15, 23, 42, 0.08))',
    },
    end: {
      title: '结束节点',
      desc: '流程出口',
      badge: '结束',
      fill: '#ffffff',
      stroke: '#dbe7e4',
      accent: '#14b8a6',
      icon: '#0f766e',
      iconBg: '#e7faf5',
      iconPath: 'M21 12a9 9 0 1 0 0 18a9 9 0 0 0 0-18M17 21l3 3l6-7',
      radius: 14,
      filter: 'drop-shadow(0 10px 18px rgba(15, 23, 42, 0.08))',
    },
  };
  return configs[kind];
}

onMounted(() => {
  void initCanvas();
  void loadDesignerOptions();
});

onBeforeUnmount(() => {
  logicFlow.value?.destroy();
  logicFlow.value = undefined;
});

async function initCanvas(): Promise<void> {
  await nextTick();
  if (!canvasRef.value) {
    return;
  }
  const instance = new LogicFlow({
    container: canvasRef.value,
    grid: {
      size: 20,
      visible: true,
      type: 'dot',
      config: {
        color: '#d3deee',
        thickness: 1,
      },
      majorBold: {
        opacity: 0.22,
        boldIndices: [5],
        dashArrayConfig: { pattern: [1, 0] },
        customBoldWidth: 1.2,
      },
    },
    keyboard: { enabled: true },
    edgeType: 'polyline',
    snapline: true,
    history: true,
    textEdit: true,
    stopScrollGraph: true,
    stopZoomGraph: false,
    style: {
      rect: {
        radius: 12,
        fill: '#f0f6ff',
        stroke: '#2563eb',
        strokeWidth: 2,
      },
      circle: {
        fill: '#effcf8',
        stroke: '#0f766e',
        strokeWidth: 2,
      },
      diamond: {
        fill: '#fff7ed',
        stroke: '#ea580c',
        strokeWidth: 2,
      },
      polyline: {
        stroke: '#64748b',
        strokeWidth: 2,
        radius: 8,
      },
      anchor: {
        r: 4.5,
        fill: '#2563eb',
        stroke: '#ffffff',
        strokeWidth: 2,
      },
      text: {
        color: '#182234',
        fontSize: 14,
        fontWeight: 600,
      },
      outline: {
        stroke: '#2563eb',
        strokeWidth: 1,
        strokeDasharray: '4 4',
        fill: 'transparent',
      },
    },
  });
  logicFlow.value = instance;
  registerWorkflowNodes(instance);
  bindCanvasEvents(instance);
  instance.render(createLogicFlowDataFromBpmnXml(props.initialXml, props.modelCode, props.modelName));
  fitCanvas();
  updateCanvasState();
}

function bindCanvasEvents(instance: LogicFlow): void {
  instance.on('node:click', (payload: { data: LogicFlow.NodeData }) => {
    const properties = normalizeApprovalNodeProperties(payload.data.properties as ApprovalNodeProperties | undefined);
    propertyTab.value = 'basic';
    selectedElement.value = {
      type: 'node',
      id: payload.data.id,
      elementType: formatNodeKind(payload.data),
      name: readElementText(payload.data.text),
      nodeKind: properties.kind,
      assigneeType: properties.assigneeType,
      assigneeExpression: properties.assigneeExpression,
      candidateUserIds: properties.candidateUserIds,
      candidateRoleCodes: properties.candidateRoleCodes,
      candidatePostCodes: properties.candidatePostCodes,
      formKey: properties.formKey,
      approvalType: properties.approvalType,
      passCondition: properties.passCondition,
      timeoutHours: properties.timeoutHours,
      ccUserIds: properties.ccUserIds,
    };
  });
  instance.on('edge:click', (payload: { data: LogicFlow.EdgeData }) => {
    const properties = payload.data.properties as { conditionExpression?: string } | undefined;
    propertyTab.value = 'condition';
    selectedElement.value = {
      type: 'edge',
      id: payload.data.id,
      elementType: '连线',
      name: readElementText(payload.data.text),
      conditionExpression: properties?.conditionExpression ?? '',
    };
  });
  instance.on('blank:click', () => {
    propertyTab.value = 'basic';
    selectedElement.value = null;
    updateCanvasState();
  });
  ['node:add', 'node:delete', 'edge:add', 'edge:delete', 'edge:adjust'].forEach((eventName) => {
    instance.on(eventName, updateCanvasState);
  });
}

function addWorkflowNode(kind: WorkflowNodeKind): void {
  const instance = logicFlow.value;
  if (!instance) {
    return;
  }
  const offset = nodeSequence.value * 36;
  nodeSequence.value += 1;
  const nodeConfig = createNodeConfig(kind, 220 + offset, 140 + (offset % 180));
  instance.addNode(nodeConfig);
  updateCanvasState();
}

function createNodeConfig(kind: WorkflowNodeKind, x: number, y: number): LogicFlow.NodeConfig {
  const id = `${props.modelCode || 'workflow'}_${kind}_${Date.now().toString(36)}_${nodeSequence.value}`;
  if (kind === 'start' || kind === 'end') {
    const size = resolveWorkflowNodeSize(kind);
    return {
      id,
      type: WORKFLOW_NODE_TYPES[kind],
      x,
      y,
      text: kind === 'start' ? '开始节点' : '结束节点',
      properties: { kind, width: size.width, height: size.height, radius: size.radius },
    };
  }
  if (kind === 'condition') {
    const size = resolveWorkflowNodeSize(kind);
    return {
      id,
      type: WORKFLOW_NODE_TYPES.condition,
      x,
      y,
      text: '条件节点',
      properties: { kind, width: size.width, height: size.height, radius: size.radius },
    };
  }
  const size = resolveWorkflowNodeSize(kind);
  return {
    id,
    type: WORKFLOW_NODE_TYPES.approval,
    x,
    y,
    text: '审批节点',
    properties: {
      kind,
      width: size.width,
      height: size.height,
      radius: size.radius,
      assigneeType: 'initiator',
      approvalType: 'single',
      candidateUserIds: [],
      candidateRoleCodes: [],
      candidatePostCodes: [],
      ccUserIds: [],
    },
    width: size.width,
    height: size.height,
  };
}

async function loadDesignerOptions(): Promise<void> {
  try {
    const tenantId = requireAdminTenantId();
    const [loadedUsers, loadedRoles, loadedPosts, loadedFormBindings] = await Promise.all([
      listSystemUsers(tenantId),
      listSystemRoles(tenantId),
      listSystemPosts(tenantId),
      listWorkflowFormBindings(),
    ]);
    users.value = loadedUsers;
    roles.value = loadedRoles;
    posts.value = loadedPosts;
    formBindings.value = loadedFormBindings;
  } catch (error) {
    message.error(error instanceof Error ? error.message : '流程编排选项加载失败');
  }
}

function resetCanvas(): void {
  selectedElement.value = null;
  logicFlow.value?.render(createDefaultLogicFlowData(props.modelCode, props.modelName));
  fitCanvas();
  updateCanvasState();
}

function undo(): void {
  logicFlow.value?.undo();
  updateCanvasState();
}

function redo(): void {
  logicFlow.value?.redo();
  updateCanvasState();
}

function zoomIn(): void {
  logicFlow.value?.zoom(true);
  updateCanvasState();
}

function zoomOut(): void {
  logicFlow.value?.zoom(false);
  updateCanvasState();
}

function fitCanvas(): void {
  logicFlow.value?.fitView(24, 24);
  updateCanvasState();
}

function syncSelectedName(): void {
  const current = selectedElement.value;
  if (!current) {
    return;
  }
  logicFlow.value?.updateText(current.id, current.name.trim());
}

function syncSelectedCondition(): void {
  const current = selectedElement.value;
  if (!current || current.type !== 'edge') {
    return;
  }
  logicFlow.value?.setProperties(current.id, {
    conditionExpression: current.conditionExpression?.trim() || undefined,
  });
}

function syncSelectedNodeProperties(): void {
  const current = selectedElement.value;
  if (!current || current.type !== 'node') {
    return;
  }
  logicFlow.value?.setProperties(current.id, {
    kind: current.nodeKind,
    assigneeType: current.assigneeType,
    assigneeExpression: current.assigneeExpression?.trim() || undefined,
    candidateUserIds: normalizeStringList(current.candidateUserIds),
    candidateRoleCodes: normalizeStringList(current.candidateRoleCodes),
    candidatePostCodes: normalizeStringList(current.candidatePostCodes),
    formKey: current.formKey?.trim() || undefined,
    approvalType: current.approvalType,
    passCondition: current.passCondition?.trim() || undefined,
    timeoutHours: current.timeoutHours,
    ccUserIds: normalizeStringList(current.ccUserIds),
  });
}

function deleteSelected(): void {
  const current = selectedElement.value;
  if (!current) {
    return;
  }
  logicFlow.value?.deleteElement(current.id);
  selectedElement.value = null;
  updateCanvasState();
}

function saveDesign(): void {
  const graphData = logicFlow.value?.getGraphRawData();
  if (!graphData) {
    message.error('流程画布未初始化');
    return;
  }
  const nodeKinds = new Set(
    graphData.nodes.map((node) => {
      const properties = node.properties as { kind?: WorkflowNodeKind } | undefined;
      return properties?.kind;
    }),
  );
  if (!nodeKinds.has('start') || !nodeKinds.has('end')) {
    message.error('流程必须包含开始节点和结束节点');
    return;
  }
  emit('save', convertLogicFlowToBpmnXml(graphData, props.modelCode, props.modelName));
}

function formatNodeKind(node: LogicFlow.NodeData): string {
  const properties = node.properties as { kind?: WorkflowNodeKind } | undefined;
  const labels: Record<WorkflowNodeKind, string> = {
    start: '开始节点',
    approval: '审批节点',
    condition: '条件节点',
    end: '结束节点',
  };
  return properties?.kind ? labels[properties.kind] : node.type;
}

function normalizeApprovalNodeProperties(properties?: ApprovalNodeProperties): ApprovalNodeProperties {
  return {
    kind: properties?.kind ?? 'approval',
    assigneeType: properties?.assigneeType ?? 'initiator',
    assigneeExpression: properties?.assigneeExpression ?? '',
    candidateUserIds: normalizeStringList(properties?.candidateUserIds),
    candidateRoleCodes: normalizeStringList(properties?.candidateRoleCodes),
    candidatePostCodes: normalizeStringList(properties?.candidatePostCodes),
    formKey: properties?.formKey ?? '',
    approvalType: properties?.approvalType ?? 'single',
    passCondition: properties?.passCondition ?? '',
    timeoutHours: properties?.timeoutHours ?? undefined,
    ccUserIds: normalizeStringList(properties?.ccUserIds),
  };
}

function normalizeStringList(value?: string[]): string[] {
  return Array.isArray(value) ? value.map((item) => String(item).trim()).filter(Boolean) : [];
}

function readElementText(text?: string | LogicFlow.TextConfig): string {
  if (!text) {
    return '';
  }
  if (typeof text === 'string') {
    return text.trim();
  }
  const legacyText = text as LogicFlow.TextConfig & { content?: string };
  return String(legacyText.value ?? legacyText.content ?? '').trim();
}

function updateCanvasState(): void {
  const instance = logicFlow.value;
  if (!instance) {
    return;
  }
  const graphData = instance.getGraphRawData();
  const transform = instance.getTransform();
  graphStats.value = {
    nodeCount: graphData.nodes?.length ?? 0,
    edgeCount: graphData.edges?.length ?? 0,
    zoomPercent: Math.round((transform.SCALE_X || 1) * 100),
  };
}
</script>

<style scoped>
.logic-flow-designer {
  display: grid;
  grid-template-columns: 176px minmax(0, 1fr) 320px;
  gap: 10px;
  min-height: 676px;
  color: #1f2937;
  background: #f7f9fc;
}

.designer-palette,
.designer-properties,
.designer-workbench {
  min-width: 0;
  border: 1px solid #dfe6f0;
  background: #ffffff;
}

.designer-palette,
.designer-properties {
  padding: 14px;
  overflow: hidden;
}

.designer-properties {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-heading {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 12px;
}

.panel-title {
  font-size: 14px;
  font-weight: 600;
}

.panel-subtitle {
  overflow: hidden;
  max-width: 120px;
  color: #8b95a5;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.palette-actions {
  display: grid;
  gap: 10px;
}

.tool-card {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  min-height: 58px;
  padding: 10px;
  border: 1px solid #d9e3f2;
  border-radius: 8px;
  background: #ffffff;
  color: #243044;
  cursor: pointer;
  transition: border-color 0.16s ease, box-shadow 0.16s ease, transform 0.16s ease;
}

.tool-card:hover {
  border-color: #7aa7ff;
  box-shadow: 0 8px 20px rgba(47, 109, 246, 0.12);
  transform: translateY(-1px);
}

.tool-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: #eef4ff;
  color: #2f6df6;
  font-size: 16px;
}

.tool-card-start .tool-icon,
.tool-card-end .tool-icon {
  background: #ecfdf7;
  color: #0f766e;
}

.tool-card-condition .tool-icon {
  background: #fff7ed;
  color: #ea580c;
}

.tool-content {
  display: grid;
  min-width: 0;
  text-align: left;
}

.tool-title {
  font-size: 13px;
  font-weight: 600;
  line-height: 18px;
}

.tool-desc {
  overflow: hidden;
  color: #8b95a5;
  font-size: 12px;
  line-height: 18px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.designer-workbench {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.designer-toolbar {
  display: grid;
  grid-template-columns: minmax(120px, 180px) minmax(0, 1fr) auto;
  align-items: center;
  gap: 10px;
  min-height: 54px;
  padding: 9px 12px;
  border-bottom: 1px solid #dfe6f0;
  background: linear-gradient(180deg, #ffffff 0%, #fbfcff 100%);
}

.canvas-meta {
  display: grid;
  min-width: 0;
}

.canvas-title {
  color: #111827;
  font-size: 13px;
  font-weight: 600;
  line-height: 18px;
}

.canvas-subtitle {
  overflow: hidden;
  color: #8b95a5;
  font-size: 12px;
  line-height: 16px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.toolbar-group {
  justify-content: center;
  min-width: 0;
}

.toolbar-actions {
  display: flex;
  justify-content: flex-end;
}

.logic-flow-canvas {
  width: 100%;
  height: 100%;
  min-height: 622px;
  border-radius: 10px;
  background:
    radial-gradient(circle at 20% 18%, rgba(47, 109, 246, 0.08), transparent 26%),
    radial-gradient(circle at 80% 72%, rgba(15, 118, 110, 0.07), transparent 28%),
    linear-gradient(180deg, #fbfdff 0%, #f5f8fd 100%);
}

.canvas-stage {
  position: relative;
  flex: 1;
  min-height: 622px;
  padding: 12px;
  overflow: hidden;
  background:
    linear-gradient(90deg, rgba(226, 232, 240, 0.35) 1px, transparent 1px),
    linear-gradient(180deg, rgba(226, 232, 240, 0.35) 1px, transparent 1px),
    #f4f7fb;
  background-size: 40px 40px;
}

.canvas-stage::before {
  position: absolute;
  inset: 12px;
  z-index: 0;
  border: 1px solid rgba(210, 220, 236, 0.74);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.56);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.8);
  content: '';
  pointer-events: none;
}

.canvas-stage > .logic-flow-canvas {
  position: relative;
  z-index: 1;
}

.canvas-hud,
.canvas-tip {
  position: absolute;
  z-index: 3;
  display: inline-flex;
  align-items: center;
  border: 1px solid rgba(210, 220, 236, 0.88);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 10px 28px rgba(31, 41, 55, 0.08);
  backdrop-filter: blur(8px);
}

.canvas-hud {
  right: 24px;
  bottom: 24px;
  gap: 8px;
  padding: 6px 10px;
  color: #475569;
  font-size: 12px;
}

.canvas-hud span + span {
  padding-left: 8px;
  border-left: 1px solid #e2e8f0;
}

.canvas-tip {
  left: 24px;
  bottom: 24px;
  max-width: min(520px, calc(100% - 220px));
  padding: 6px 12px;
  overflow: hidden;
  color: #64748b;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.property-form {
  display: flex;
  flex: 1;
  min-height: 0;
  flex-direction: column;
}

.property-tabs {
  flex: 1;
  min-height: 0;
}

.property-tabs :deep(.ant-tabs-nav) {
  margin-bottom: 10px;
}

.property-tabs :deep(.ant-tabs-tab) {
  padding: 6px 0;
  font-size: 12px;
}

.property-tabs :deep(.ant-tabs-content-holder),
.property-tabs :deep(.ant-tabs-content),
.property-tabs :deep(.ant-tabs-tabpane) {
  min-height: 0;
}

.property-tabs :deep(.ant-tabs-tabpane) {
  overflow-y: auto;
  padding-right: 2px;
}

.property-section {
  padding: 12px;
  border: 1px solid #e4e9f2;
  border-radius: 8px;
  background: linear-gradient(180deg, #fbfcff 0%, #ffffff 100%);
}

.section-title {
  margin-bottom: 10px;
  color: #111827;
  font-size: 13px;
  font-weight: 600;
}

.property-section :deep(.ant-form-item) {
  margin-bottom: 10px;
}

.property-section :deep(.ant-form-item:last-child) {
  margin-bottom: 0;
}

.property-section :deep(.ant-form-item-label) {
  padding-bottom: 4px;
}

.property-section :deep(.ant-form-item-label > label) {
  color: #4b5563;
  font-size: 12px;
}

.property-section :deep(.ant-input),
.property-section :deep(.ant-input-number),
.property-section :deep(.ant-select-selector) {
  border-radius: 6px;
}

.full-input {
  width: 100%;
}

.property-footer {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e8edf5;
}

:deep(.lf-node-text),
:deep(.lf-edge-text) {
  letter-spacing: 0;
}

:deep(.lf-node-text) {
  font-weight: 500;
}

:deep(.lf-base) {
  background: transparent;
}

:deep(.lf-graph) {
  background: transparent;
}

:deep(.lf-grid) {
  opacity: 0.72;
}

:deep(.lf-basic-shape) {
  filter: drop-shadow(0 8px 12px rgba(37, 99, 235, 0.08));
  transition: filter 0.16s ease, stroke-width 0.16s ease;
}

:deep(.lf-node:hover .lf-basic-shape) {
  filter: drop-shadow(0 12px 18px rgba(37, 99, 235, 0.14));
}

:deep(.lf-node-selected .lf-basic-shape) {
  filter: drop-shadow(0 14px 24px rgba(37, 99, 235, 0.18));
}

:deep(.workflow-node) {
  cursor: move;
}

:deep(.workflow-node text) {
  user-select: none;
  letter-spacing: 0;
}

:deep(.workflow-node-shell) {
  transition: stroke-width 0.16s ease, filter 0.16s ease;
}

:deep(.lf-node:hover .workflow-node-shell) {
  stroke-width: 1.4;
}

:deep(.lf-edge path),
:deep(.lf-edge polyline) {
  filter: drop-shadow(0 2px 3px rgba(100, 116, 139, 0.16));
}

:deep(.lf-edge-selected path),
:deep(.lf-edge-selected polyline) {
  filter: drop-shadow(0 3px 6px rgba(37, 99, 235, 0.2));
}

:deep(.lf-node-anchor) {
  filter: drop-shadow(0 2px 4px rgba(37, 99, 235, 0.28));
}

:deep(.lf-node-anchor-hover) {
  fill: rgba(37, 99, 235, 0.12);
  stroke: rgba(37, 99, 235, 0.26);
}

:deep(.lf-node-select-decorate) {
  display: none;
}

:deep(.lf-line-text) {
  color: #475569;
  font-size: 12px;
}

:deep(.lf-snapline line) {
  stroke: rgba(47, 109, 246, 0.28);
  stroke-dasharray: 3 5;
}

@media (max-width: 1180px) {
  .logic-flow-designer {
    grid-template-columns: 160px minmax(0, 1fr);
  }

  .designer-properties {
    grid-column: 1 / -1;
  }
}
</style>
