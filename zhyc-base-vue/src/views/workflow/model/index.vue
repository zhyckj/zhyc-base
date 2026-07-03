<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="model-page">
    <a-card title="流程模型" :bordered="false">
      <template #extra>
        <a-space>
          <a-button :loading="status === 'loading'" @click="loadModels">刷新</a-button>
          <a-button type="primary" @click="openCreateForm">新增模型</a-button>
        </a-space>
      </template>

      <a-alert v-if="status === 'error'" type="error" show-icon :message="errorMessage" class="state-alert" />

      <a-table
        row-key="modelCode"
        :columns="columns"
        :data-source="models"
        :loading="status === 'loading'"
        :pagination="$tablePagination"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'categoryName'">
            {{ formatCategoryName(record.categoryId) }}
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'enabled' ? 'green' : 'default'">
              {{ record.status === 'enabled' ? '启用' : '停用' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button size="small" type="primary" @click="openDesignerModal(record)">设计</a-button>
              <a-button size="small" @click="openEditForm(record)">编辑</a-button>
              <a-button size="small" type="link" @click="openDeployModal(record)">发布</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal v-model:open="formOpen" title="流程模型" :confirm-loading="saving" @ok="submitForm">
      <a-form layout="vertical">
        <a-form-item label="模型编码">
          <a-input v-model:value="formState.modelCode" :disabled="Boolean(formState.id)" />
        </a-form-item>
        <a-form-item label="模型名称">
          <a-input v-model:value="formState.modelName" />
        </a-form-item>
        <a-form-item label="流程分类">
          <a-select
            v-model:value="formState.categoryId"
            allow-clear
            show-search
            :filter-option="filterSelectOption"
            :loading="categoryLoading"
            placeholder="请选择流程分类"
          >
            <a-select-option v-for="category in enabledCategories" :key="category.id" :value="category.id">
              {{ category.categoryName }}（{{ category.categoryCode }}）
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="Flowable 模型 ID（可选）">
          <a-input
            v-model:value="formState.flowableModelId"
            placeholder="默认使用模型编码，可按需修改"
          />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="formState.status">
            <a-select-option value="enabled">启用</a-select-option>
            <a-select-option value="disabled">停用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="formState.remark" :rows="3" />
        </a-form-item>
      </a-form>
      <span class="permission-code">workflow:model:update</span>
    </a-modal>

    <a-modal
      v-model:open="deployOpen"
      title="发布流程模型"
      :confirm-loading="deploying"
      @ok="submitDeploy"
    >
      <a-form layout="vertical">
        <a-form-item label="模型编码">
          <a-input :value="deployModelCode" disabled />
        </a-form-item>
        <a-form-item label="BPMN XML">
          <a-textarea
            v-model:value="deployState.bpmnXml"
            :rows="8"
            placeholder="请输入流程设计器生成的 BPMN XML"
          />
        </a-form-item>
        <a-form-item label="发布备注">
          <a-textarea v-model:value="deployState.remark" :rows="3" />
        </a-form-item>
      </a-form>
      <span class="permission-code">workflow:model:deploy</span>
    </a-modal>

    <a-modal
      v-model:open="designerOpen"
      :title="`${deployModelCode || '流程'} - 可视化流程编排`"
      width="96vw"
      :footer="null"
      destroy-on-close
      wrap-class-name="workflow-designer-modal"
    >
      <LogicFlowDesigner
        v-if="designerOpen"
        :model-code="deployModelCode"
        :model-name="designerModelName"
        :initial-xml="designerInitialXml"
        @save="handleDesignerSave"
      />
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { message } from 'ant-design-vue';
import { computed, onMounted, reactive, ref, watch } from 'vue';
import type { DefaultOptionType } from 'ant-design-vue/es/select';

import { listWorkflowCategories, type WorkflowCategory } from '@/api/workflow/category';
import {
  deployWorkflowProcessModel,
  listWorkflowProcessModels,
  saveWorkflowProcessModel,
  type WorkflowProcessModel,
  type WorkflowProcessModelDeployPayload,
  type WorkflowProcessModelSavePayload,
} from '@/api/workflow/model';
import type { LoadStatus } from '@/types/platform';
import { requireAdminTenantId } from '@/utils/adminContext';
import LogicFlowDesigner from './components/LogicFlowDesigner.vue';

/** 页面加载状态。 */
const status = ref<LoadStatus>('idle');
/** 保存按钮加载状态。 */
const saving = ref(false);
/** 发布按钮加载状态。 */
const deploying = ref(false);
/** 分类下拉加载状态。 */
const categoryLoading = ref(false);
/** 表单弹窗打开状态。 */
const formOpen = ref(false);
/** 发布弹窗打开状态。 */
const deployOpen = ref(false);
/** 在线流程编排器打开状态。 */
const designerOpen = ref(false);
/** 异常提示文案。 */
const errorMessage = ref('');
/** 流程模型列表。 */
const models = ref<WorkflowProcessModel[]>([]);
/** 流程分类下拉数据。 */
const categories = ref<WorkflowCategory[]>([]);
/** 当前发布的流程模型主键。 */
const deployModelId = ref<number>();
/** 当前发布的流程模型编码。 */
const deployModelCode = ref('');
/** 当前设计的流程模型名称。 */
const designerModelName = ref('');
/** 当前正在设计的流程模型快照，用于保存 BPMN 草稿时保留模型基础字段。 */
const designerModel = ref<WorkflowProcessModel>();
/** 当前编排器初始 BPMN XML。 */
const designerInitialXml = ref('');
/** 页面内按流程模型缓存的 BPMN XML，避免切换模型时串用未发布设计。 */
const designerXmlCache = ref<Record<number, string>>({});
/** 流程模型表单状态。 */
const formState = reactive<WorkflowProcessModelSavePayload>({
  modelCode: '',
  modelName: '',
  categoryId: undefined,
  flowableModelId: '',
  bpmnXml: '',
  status: 'enabled',
  remark: '',
});
/** 流程模型发布表单状态。 */
const deployState = reactive<WorkflowProcessModelDeployPayload>({
  bpmnXml: '',
  remark: '',
});

/** 表格列定义。 */
const columns = [
  { title: '模型编码', dataIndex: 'modelCode', key: 'modelCode' },
  { title: '模型名称', dataIndex: 'modelName', key: 'modelName' },
  { title: '流程分类', key: 'categoryName', width: 180 },
  { title: 'Flowable 模型', dataIndex: 'flowableModelId', key: 'flowableModelId' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '备注', dataIndex: 'remark', key: 'remark' },
  { title: '操作', key: 'action', width: 220 },
];

/** 启用状态的流程分类下拉项。 */
const enabledCategories = computed(() => categories.value.filter((category) => category.status === 'enabled'));

/**
 * 按下拉展示文本过滤选项。
 *
 * @param input 用户输入的搜索关键字
 * @param option 下拉选项
 * @returns 是否匹配当前关键字
 */
function filterSelectOption(input: string, option?: DefaultOptionType): boolean {
  const label = String(option?.children ?? option?.label ?? option?.value ?? '');
  return label.toLowerCase().includes(input.trim().toLowerCase());
}

/**
 * 格式化流程分类名称。
 *
 * @param categoryId 流程分类主键
 * @returns 流程分类业务名称
 */
function formatCategoryName(categoryId?: number): string {
  if (!categoryId) {
    return '未分类';
  }
  const category = categories.value.find((item) => item.id === categoryId);
  return category ? `${category.categoryName}（${category.categoryCode}）` : '未知分类';
}

/**
 * 加载流程模型。
 */
async function loadModels(): Promise<void> {
  status.value = 'loading';
  try {
    requireAdminTenantId();
    models.value = await listWorkflowProcessModels();
    status.value = 'success';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '流程模型加载失败';
    status.value = 'error';
  }
}

/**
 * 加载流程分类下拉数据。
 */
async function loadCategories(): Promise<void> {
  categoryLoading.value = true;
  try {
    requireAdminTenantId();
    categories.value = await listWorkflowCategories();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '流程分类加载失败');
  } finally {
    categoryLoading.value = false;
  }
}

/**
 * 打开新增流程模型表单。
 */
function openCreateForm(): void {
  Object.assign(formState, {
    id: undefined,
    modelCode: '',
    modelName: '',
    categoryId: undefined,
    flowableModelId: '',
    bpmnXml: '',
    status: 'enabled',
    remark: '',
  });
  formOpen.value = true;
}

/**
 * 打开编辑流程模型表单。
 *
 * @param model 工作流流程模型
 */
function openEditForm(model: WorkflowProcessModel): void {
  Object.assign(formState, {
    id: model.id,
    modelCode: model.modelCode,
    modelName: model.modelName,
    categoryId: model.categoryId,
    flowableModelId: model.flowableModelId,
    bpmnXml: model.bpmnXml ?? '',
    status: model.status,
    remark: model.remark ?? '',
  });
  formOpen.value = true;
}

/**
 * 打开流程模型发布弹窗。
 *
 * @param model 工作流流程模型
 */
function openDeployModal(model: WorkflowProcessModel): void {
  prepareDeployContext(model);
  Object.assign(deployState, {
    bpmnXml: designerXmlCache.value[model.id] ?? model.bpmnXml ?? '',
    remark: model.remark ?? '',
  });
  deployOpen.value = true;
}

/**
 * 打开在线流程编排器。
 *
 * @param model 工作流流程模型
 */
function openDesignerModal(model: WorkflowProcessModel): void {
  prepareDeployContext(model);
  designerModel.value = model;
  designerInitialXml.value = designerXmlCache.value[model.id] ?? model.bpmnXml ?? '';
  designerOpen.value = true;
}

/**
 * 接收设计器生成的 BPMN XML，并带入发布弹窗。
 *
 * @param xml BPMN XML 文本
 */
async function handleDesignerSave(xml: string): Promise<void> {
  const activeModel = designerModel.value;
  if (!activeModel || !deployModelId.value) {
    message.error('流程模型上下文不能为空');
    return;
  }
  saving.value = true;
  try {
    requireAdminTenantId();
    await saveWorkflowProcessModel({
      id: activeModel.id,
      modelCode: activeModel.modelCode,
      modelName: activeModel.modelName,
      categoryId: activeModel.categoryId,
      flowableModelId: activeModel.flowableModelId,
      bpmnXml: xml,
      status: activeModel.status,
      remark: activeModel.remark,
    });
    designerXmlCache.value = {
      ...designerXmlCache.value,
      [activeModel.id]: xml,
    };
    message.success('BPMN 设计稿已保存');
    designerInitialXml.value = xml;
    designerOpen.value = false;
    await loadModels();
  } catch (error) {
    message.error(error instanceof Error ? error.message : 'BPMN 设计稿保存失败');
  } finally {
    saving.value = false;
  }
}

/**
 * 准备发布和设计上下文。
 *
 * @param model 工作流流程模型
 */
function prepareDeployContext(model: WorkflowProcessModel): void {
  deployModelId.value = model.id;
  deployModelCode.value = model.modelCode;
  designerModelName.value = model.modelName;
  designerModel.value = model;
  deployState.remark = model.remark ?? '';
}

/**
 * 提交流程模型表单。
 */
async function submitForm(): Promise<void> {
  saving.value = true;
  try {
    requireAdminTenantId();
    await saveWorkflowProcessModel({ ...formState });
    message.success('流程模型已保存');
    formOpen.value = false;
    await loadModels();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '流程模型保存失败');
  } finally {
    saving.value = false;
  }
}

/**
 * 提交流程模型发布。
 */
async function submitDeploy(): Promise<void> {
  if (!deployModelId.value) {
    message.error('流程模型主键不能为空');
    return;
  }
  deploying.value = true;
  try {
    requireAdminTenantId();
    await deployWorkflowProcessModel(deployModelId.value, { ...deployState });
    message.success('流程模型已发布');
    deployOpen.value = false;
    await loadModels();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '流程模型发布失败');
  } finally {
    deploying.value = false;
  }
}

onMounted(() => {
  void loadModels();
  void loadCategories();
});

watch(
  () => formState.modelCode,
  (nextModelCode, previousModelCode) => {
    if (formState.id) {
      return;
    }
    const currentFlowableModelId = formState.flowableModelId?.trim();
    const previousCode = previousModelCode?.trim();
    if (!currentFlowableModelId || currentFlowableModelId === previousCode) {
      formState.flowableModelId = nextModelCode.trim();
    }
  },
);
</script>

<style scoped>
.model-page {
  min-width: 0;
}

.state-alert {
  margin-bottom: 12px;
}

.full-field {
  width: 100%;
}

:global(.workflow-designer-modal .ant-modal) {
  max-width: calc(100vw - 24px);
  top: 16px;
}

:global(.workflow-designer-modal .ant-modal-body) {
  max-height: calc(100vh - 120px);
  overflow: auto;
}

.permission-code {
  display: none;
}
</style>
