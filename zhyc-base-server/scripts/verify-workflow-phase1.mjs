/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { existsSync, readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { resolve } from 'node:path';

const serverRoot = resolve(fileURLToPath(new URL('..', import.meta.url)));
const workspaceRoot = resolve(serverRoot, '..');

const requiredFiles = [
  'zhyc-base-server/zhyc-module-workflow/src/main/resources/db/V1__workflow_core.sql',
  'zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/category/controller/WorkflowCategoryController.java',
  'zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/model/controller/WorkflowProcessModelController.java',
  'zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/model/controller/WorkflowProcessModelDeployRequest.java',
  'zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/definition/controller/WorkflowDefinitionController.java',
  'zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/binding/controller/WorkflowFormBindingController.java',
  'zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/controller/WorkflowTaskController.java',
  'zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/service/WorkflowTaskService.java',
  'zhyc-base-server/zhyc-common/src/main/java/com/zhyc/common/workflow/WorkflowTaskActionHandler.java',
  'zhyc-base-server/zhyc-common/src/main/java/com/zhyc/common/workflow/WorkflowTaskActionContext.java',
  'zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/repository/WorkflowRuntimeRepository.java',
  'zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/FlowableWorkflowService.java',
  'zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/deployment/WorkflowModelDeploymentService.java',
  'zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/deployment/DefaultWorkflowModelDeploymentService.java',
  'zhyc-base-server/zhyc-module-workflow/src/test/java/com/zhyc/workflow/WorkflowTaskControllerContractTest.java',
  'zhyc-base-server/zhyc-module-workflow/src/test/java/com/zhyc/workflow/category/WorkflowCategoryControllerContractTest.java',
  'zhyc-base-server/zhyc-module-workflow/src/test/java/com/zhyc/workflow/model/WorkflowProcessModelControllerContractTest.java',
  'zhyc-base-server/zhyc-module-workflow/src/test/java/com/zhyc/workflow/definition/WorkflowDefinitionControllerContractTest.java',
  'zhyc-base-server/zhyc-module-workflow/src/test/java/com/zhyc/workflow/binding/WorkflowFormBindingControllerContractTest.java',
  'zhyc-base-vue/src/api/workflow/category.ts',
  'zhyc-base-vue/src/api/workflow/model.ts',
  'zhyc-base-vue/src/api/workflow/definition.ts',
  'zhyc-base-vue/src/api/workflow/binding.ts',
  'zhyc-base-vue/src/api/workflow/task.ts',
  'zhyc-base-vue/src/views/workflow/category/index.vue',
  'zhyc-base-vue/src/views/workflow/model/index.vue',
  'zhyc-base-vue/src/views/workflow/definition/index.vue',
  'zhyc-base-vue/src/views/workflow/binding/index.vue',
  'zhyc-base-vue/src/views/workflow/task/todo.vue',
  'zhyc-base-vue/src/views/workflow/task/started.vue',
  'zhyc-base-vue/src/views/workflow/task/cc.vue',
  'zhyc-base-vue/src/views/workflow/task/monitor.vue',
  'zhyc-base-uniapp/src/api/workflow.ts',
  'zhyc-base-uniapp/src/pages/workflow/todo.vue',
  'zhyc-base-uniapp/src/pages/workflow/done.vue',
  'zhyc-base-uniapp/src/pages/workflow/started.vue',
  'zhyc-base-uniapp/src/pages/workflow/cc.vue',
  'zhyc-base-uniapp/src/pages/workflow/detail.vue',
];

const requiredSnippets = [
  ['zhyc-base-server/zhyc-module-workflow/src/main/resources/db/V1__workflow_core.sql', 'CREATE TABLE IF NOT EXISTS wf_category'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/resources/db/V1__workflow_core.sql', 'CREATE TABLE IF NOT EXISTS wf_process_model'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/resources/db/V1__workflow_core.sql', 'CREATE TABLE IF NOT EXISTS wf_form_binding'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/resources/db/V1__workflow_core.sql', 'CREATE TABLE IF NOT EXISTS wf_process_definition'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/resources/db/V1__workflow_core.sql', 'CREATE TABLE IF NOT EXISTS wf_process_instance'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/resources/db/V1__workflow_core.sql', 'CREATE TABLE IF NOT EXISTS wf_task'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/resources/db/V1__workflow_core.sql', 'CREATE TABLE IF NOT EXISTS wf_approval_record'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/resources/db/V1__workflow_core.sql', 'CREATE TABLE IF NOT EXISTS wf_cc_record'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/resources/db/V1__workflow_core.sql', "receiver_id BIGINT NOT NULL COMMENT '抄送接收人用户 ID'"],
  ['zhyc-base-server/zhyc-module-workflow/src/main/resources/db/V1__workflow_core.sql', "read_flag TINYINT NOT NULL DEFAULT 0 COMMENT '阅读标识，0 未读，1 已读'"],
  ['zhyc-base-server/zhyc-module-workflow/src/main/resources/db/V1__workflow_core.sql', "tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码'"],
  ['zhyc-base-server/zhyc-module-workflow/src/main/resources/db/V1__workflow_core.sql', 'deleted TINYINT NOT NULL DEFAULT 0'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/category/controller/WorkflowCategoryController.java', '@RequestMapping("/workflow/categories")'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/model/controller/WorkflowProcessModelController.java', '@RequestMapping("/workflow/models")'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/model/controller/WorkflowProcessModelController.java', '@PostMapping("/{modelId}/deploy")'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/model/controller/WorkflowProcessModelController.java', '@RequiresPermissions("workflow:model:deploy")'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/definition/controller/WorkflowDefinitionController.java', '@RequestMapping("/workflow/definitions")'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/binding/controller/WorkflowFormBindingController.java', '@RequestMapping("/workflow/form-bindings")'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/controller/WorkflowTaskController.java', '@RequestMapping("/workflow/tasks")'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/service/WorkflowTaskService.java', 'listTodoTasks'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/service/WorkflowTaskService.java', 'listDoneTasks'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/service/WorkflowTaskService.java', 'listStartedProcesses'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/service/WorkflowTaskService.java', 'listCcTasks'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/service/WorkflowTaskService.java', 'listMonitoredProcesses'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/service/WorkflowTaskService.java', 'approve'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/service/WorkflowTaskService.java', 'reject'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/service/WorkflowTaskService.java', 'revoke'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/constant/WorkflowRuntimeStatus.java', 'RUNNING("RUNNING", "流程实例运行中")'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/constant/WorkflowRuntimeStatus.java', 'TODO("TODO", "任务待处理")'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/constant/WorkflowRuntimeStatus.java', 'APPROVED("APPROVED", "任务已审批通过")'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/constant/WorkflowRuntimeStatus.java', 'REJECTED("REJECTED", "任务已驳回")'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/constant/WorkflowRuntimeStatus.java', 'REVOKED("REVOKED", "流程或任务已撤回")'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/constant/WorkflowRuntimeStatus.java', 'public String getDescription()'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/constant/WorkflowRuntimeStatus.java', 'public static WorkflowRuntimeStatus fromCode(String code)'],
  ['zhyc-base-server/zhyc-module-workflow/src/test/java/com/zhyc/workflow/WorkflowRuntimeStatusTest.java', 'shouldExposeRuntimeStatusDescriptions'],
  ['zhyc-base-server/zhyc-module-workflow/src/test/java/com/zhyc/workflow/WorkflowRuntimeStatusTest.java', 'shouldParseRuntimeStatusFromCode'],
  ['zhyc-base-server/zhyc-common/src/main/java/com/zhyc/common/workflow/WorkflowTaskActionHandler.java', 'WorkflowTaskActionContext'],
  ['zhyc-base-server/zhyc-common/src/main/java/com/zhyc/common/workflow/WorkflowTaskActionContext.java', '业务模块只能依赖该公共契约'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/service/DefaultWorkflowTaskService.java', 'com.zhyc.common.workflow.WorkflowTaskActionHandler'],
  ['zhyc-base-server/pom.xml', '<flowable.version>8.0.0</flowable.version>'],
  ['zhyc-base-server/zhyc-module-workflow/pom.xml', '<artifactId>flowable-engine</artifactId>'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/FlowableWorkflowService.java', 'RuntimeService'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/FlowableWorkflowService.java', 'TaskService'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/FlowableWorkflowService.java', '@ConditionalOnBean'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/FlowableWorkflowService.java', 'WorkflowRuntimeRepository'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/FlowableWorkflowService.java', 'syncRuntimeRepository'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/FlowableWorkflowService.java', 'taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult()'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/FlowableWorkflowService.java', 'resolveFirstTaskAssigneeUserId'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/FlowableWorkflowService.java', '流程变量 tenantId 不能为空'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/repository/WorkflowRuntimeRepository.java', 'firstTaskAssigneeUserId'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/repository/MyBatisWorkflowRuntimeRepository.java', 'firstTaskAssigneeUserId'],
  ['zhyc-base-server/zhyc-module-workflow/src/test/java/com/zhyc/workflow/FlowableWorkflowServiceTest.java', 'shouldStartProcessThroughFlowableRuntimeService'],
  ['zhyc-base-server/zhyc-module-workflow/src/test/java/com/zhyc/workflow/FlowableWorkflowServiceTest.java', 'shouldSyncRuntimeRepositoryAfterStartingFlowableProcess'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/deployment/DefaultWorkflowModelDeploymentService.java', 'RepositoryService'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/deployment/DefaultWorkflowModelDeploymentService.java', '.addString(processKey + BPMN_RESOURCE_SUFFIX, bpmnXml)'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/deployment/DefaultWorkflowModelDeploymentService.java', 'definitionRepository.save'],
  ['zhyc-base-server/zhyc-module-workflow/src/test/java/com/zhyc/workflow/deployment/WorkflowModelDeploymentServiceTest.java', 'shouldDeployEnabledModelAndSaveNextDefinitionVersion'],
  ['zhyc-base-server/zhyc-module-workflow/src/test/java/com/zhyc/workflow/WorkflowTaskControllerContractTest.java', 'shouldMergeNullApproveCommandWithPathAndHeaders'],
  ['zhyc-base-server/zhyc-module-workflow/src/test/java/com/zhyc/workflow/WorkflowTaskControllerContractTest.java', 'shouldExposeStartedAndCcTaskContracts'],
  ['zhyc-base-server/zhyc-module-workflow/src/test/java/com/zhyc/workflow/WorkflowTaskControllerContractTest.java', 'shouldExposeProcessMonitorContract'],
  ['zhyc-base-server/zhyc-module-workflow/src/test/java/com/zhyc/workflow/WorkflowTaskControllerContractTest.java', 'shouldMergeNullRejectCommandWithPathAndHeaders'],
  ['zhyc-base-server/zhyc-module-workflow/src/test/java/com/zhyc/workflow/WorkflowTaskControllerContractTest.java', 'shouldMergeNullRevokeCommandWithPathAndHeaders'],
  ['zhyc-base-server/zhyc-module-workflow/src/test/java/com/zhyc/workflow/category/WorkflowCategoryControllerContractTest.java', 'shouldConvertNullCategoryRequestToCommandWithTenant'],
  ['zhyc-base-server/zhyc-module-workflow/src/test/java/com/zhyc/workflow/model/WorkflowProcessModelControllerContractTest.java', 'shouldConvertNullModelRequestToCommandWithTenant'],
  ['zhyc-base-server/zhyc-module-workflow/src/test/java/com/zhyc/workflow/model/WorkflowProcessModelControllerContractTest.java', 'shouldConvertNullDeployRequestToCommandWithTenantAndModel'],
  ['zhyc-base-server/zhyc-module-workflow/src/test/java/com/zhyc/workflow/model/WorkflowProcessModelControllerContractTest.java', 'shouldRejectDeployWhenDeploymentServiceMissingWithBusinessException'],
  ['zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/model/controller/WorkflowProcessModelController.java', 'WorkflowServiceValidation.businessFailure("Flowable 发布服务未配置，不能发布流程模型")'],
  ['zhyc-base-server/zhyc-module-workflow/src/test/java/com/zhyc/workflow/definition/WorkflowDefinitionControllerContractTest.java', 'shouldConvertNullDefinitionRequestToCommandWithTenant'],
  ['zhyc-base-server/zhyc-module-workflow/src/test/java/com/zhyc/workflow/binding/WorkflowFormBindingControllerContractTest.java', 'shouldConvertNullBindingRequestToCommandWithTenant'],
  ['zhyc-base-vue/src/api/workflow/task.ts', '/workflow/tasks'],
  ['zhyc-base-vue/src/api/workflow/task.ts', '/workflow/tasks/started'],
  ['zhyc-base-vue/src/api/workflow/task.ts', '/workflow/tasks/cc'],
  ['zhyc-base-vue/src/api/workflow/task.ts', '/workflow/tasks/monitor'],
  ['zhyc-base-vue/src/api/workflow/task.ts', 'listStartedProcesses'],
  ['zhyc-base-vue/src/api/workflow/task.ts', 'listCcTasks'],
  ['zhyc-base-vue/src/api/workflow/task.ts', 'listMonitoredProcesses'],
  ['zhyc-base-vue/src/api/workflow/task.ts', '/approve'],
  ['zhyc-base-vue/src/api/workflow/task.ts', '/reject'],
  ['zhyc-base-vue/src/api/workflow/model.ts', 'deployWorkflowProcessModel'],
  ['zhyc-base-vue/src/api/workflow/model.ts', '/deploy'],
  ['zhyc-base-vue/src/views/workflow/category/index.vue', "message.success('流程分类已保存')"],
  ['zhyc-base-vue/src/views/workflow/model/index.vue', "message.success('流程模型已保存')"],
  ['zhyc-base-vue/src/views/workflow/model/index.vue', 'openDeployModal'],
  ['zhyc-base-vue/src/views/workflow/model/index.vue', 'deployWorkflowProcessModel'],
  ['zhyc-base-vue/src/views/workflow/model/index.vue', "message.success('流程模型已发布')"],
  ['zhyc-base-vue/src/views/workflow/model/index.vue', 'workflow:model:deploy'],
  ['zhyc-base-vue/src/views/workflow/definition/index.vue', "message.success('流程定义已保存')"],
  ['zhyc-base-vue/src/views/workflow/binding/index.vue', "message.success('表单绑定已保存')"],
  ['zhyc-base-vue/src/views/workflow/task/todo.vue', 'Modal.confirm'],
  ['zhyc-base-vue/src/views/workflow/task/todo.vue', "message.error(error instanceof Error ? error.message : '任务处理失败')"],
  ['zhyc-base-vue/src/views/workflow/task/started.vue', '我发起的'],
  ['zhyc-base-vue/src/views/workflow/task/started.vue', 'listStartedProcesses'],
  ['zhyc-base-vue/src/views/workflow/task/started.vue', 'workflow:task:started'],
  ['zhyc-base-vue/src/views/workflow/task/cc.vue', '抄送我的'],
  ['zhyc-base-vue/src/views/workflow/task/cc.vue', 'listCcTasks'],
  ['zhyc-base-vue/src/views/workflow/task/cc.vue', 'workflow:task:cc'],
  ['zhyc-base-vue/src/views/workflow/task/monitor.vue', '流程监控'],
  ['zhyc-base-vue/src/views/workflow/task/monitor.vue', 'listMonitoredProcesses'],
  ['zhyc-base-vue/src/views/workflow/task/monitor.vue', 'workflow:task:monitor'],
  ['zhyc-base-uniapp/src/api/workflow.ts', '/workflow/tasks'],
  ['zhyc-base-uniapp/src/api/workflow.ts', '/workflow/tasks/started'],
  ['zhyc-base-uniapp/src/api/workflow.ts', '/workflow/tasks/cc'],
  ['zhyc-base-uniapp/src/api/workflow.ts', 'listMobileStartedProcesses'],
  ['zhyc-base-uniapp/src/api/workflow.ts', 'listMobileCcTasks'],
  ['zhyc-base-uniapp/src/api/workflow.ts', 'approveTask'],
  ['zhyc-base-uniapp/src/api/workflow.ts', 'rejectTask'],
  ['zhyc-base-uniapp/src/pages/workflow/todo.vue', '暂无待办任务'],
  ['zhyc-base-uniapp/src/pages/workflow/done.vue', '暂无已办任务'],
  ['zhyc-base-uniapp/src/pages/workflow/started.vue', '我发起的'],
  ['zhyc-base-uniapp/src/pages/workflow/started.vue', 'listMobileStartedProcesses'],
  ['zhyc-base-uniapp/src/pages/workflow/started.vue', 'requireMobileTenantId'],
  ['zhyc-base-uniapp/src/pages/workflow/cc.vue', '抄送我的'],
  ['zhyc-base-uniapp/src/pages/workflow/cc.vue', 'listMobileCcTasks'],
  ['zhyc-base-uniapp/src/pages/workflow/cc.vue', 'requireMobileTenantId'],
  ['zhyc-base-uniapp/src/pages/workflow/detail.vue', '处理失败'],
];

const errors = [];

for (const file of requiredFiles) {
  const absolutePath = resolve(workspaceRoot, file);
  if (!existsSync(absolutePath)) {
    errors.push(`缺少文件：${file}`);
  }
}

for (const [file, snippet] of requiredSnippets) {
  const absolutePath = resolve(workspaceRoot, file);
  if (!existsSync(absolutePath)) {
    continue;
  }
  const content = readFileSync(absolutePath, 'utf8');
  if (!content.includes(snippet)) {
    errors.push(`缺少关键内容：${file} -> ${snippet}`);
  }
}

if (errors.length > 0) {
  console.error('工作流首期契约校验失败。');
  for (const error of errors) {
    console.error(`- ${error}`);
  }
  process.exit(1);
}

console.log('工作流首期契约校验通过。');
