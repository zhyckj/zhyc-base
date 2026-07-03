/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.deployment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.workflow.definition.domain.WorkflowDefinition;
import com.zhyc.workflow.definition.repository.WorkflowDefinitionRepository;
import com.zhyc.workflow.model.domain.WorkflowProcessModel;
import com.zhyc.workflow.model.repository.WorkflowProcessModelRepository;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.DeploymentBuilder;
import org.junit.jupiter.api.Test;

/**
 * 工作流流程模型发布服务测试。
 */
class WorkflowModelDeploymentServiceTest {

  /**
   * 测试发布启用模型时应完成 Flowable 部署并保存平台流程定义版本。
   */
  @Test
  void shouldDeployEnabledModelAndSaveNextDefinitionVersion() {
    InMemoryModelRepository modelRepository = new InMemoryModelRepository();
    InMemoryDefinitionRepository definitionRepository = new InMemoryDefinitionRepository();
    CapturingDeploymentBuilder deploymentBuilder = new CapturingDeploymentBuilder();
    modelRepository.models.add(new WorkflowProcessModel(10L, "tenant-a", "purchase_request",
        "采购申请审批", 1L, "flowable-model-1", "<bpmn:definitions/>", "enabled",
        null, null, "采购首期流程"));
    definitionRepository.definitions.add(new WorkflowDefinition(1L, "tenant-a",
        "purchase_request", "采购申请审批", 1, "old-deployment", "active", null, null, null));
    WorkflowModelDeploymentService service = new DefaultWorkflowModelDeploymentService(
        modelRepository, definitionRepository, repositoryService(deploymentBuilder));

    WorkflowModelDeploymentResult result = service.deploy(new WorkflowModelDeploymentCommand(
        "tenant-a", 10L, "<definitions id=\"purchase_request\"/>", "发布采购审批流程"));

    assertEquals("deployment-100", result.getDeploymentId());
    assertEquals("purchase_request", result.getProcessKey());
    assertEquals(2, result.getVersion());
    assertEquals("tenant-a", deploymentBuilder.tenantId);
    assertEquals("purchase_request", deploymentBuilder.key);
    assertEquals("采购申请审批", deploymentBuilder.name);
    assertEquals("purchase_request.bpmn20.xml", deploymentBuilder.resourceName);
    assertEquals("<definitions id=\"purchase_request\"/>", deploymentBuilder.resourceText);
    WorkflowDefinition savedDefinition = definitionRepository.savedDefinition;
    assertEquals("tenant-a", savedDefinition.getTenantId());
    assertEquals("purchase_request", savedDefinition.getProcessKey());
    assertEquals("采购申请审批", savedDefinition.getProcessName());
    assertEquals(2, savedDefinition.getVersion());
    assertEquals("deployment-100", savedDefinition.getDeploymentId());
    assertEquals("active", savedDefinition.getStatus());
    assertEquals("发布采购审批流程", savedDefinition.getRemark());
  }

  /**
   * 测试发布非启用模型时应阻断部署。
   */
  @Test
  void shouldRejectDisabledModelDeployment() {
    InMemoryModelRepository modelRepository = new InMemoryModelRepository();
    InMemoryDefinitionRepository definitionRepository = new InMemoryDefinitionRepository();
    modelRepository.models.add(new WorkflowProcessModel(11L, "tenant-a", "disabled_flow",
        "停用流程", 1L, "flowable-model-2", "<bpmn:definitions/>", "disabled",
        null, null, null));
    WorkflowModelDeploymentService service = new DefaultWorkflowModelDeploymentService(
        modelRepository, definitionRepository, repositoryService(new CapturingDeploymentBuilder()));

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.deploy(new WorkflowModelDeploymentCommand("tenant-a", 11L,
            "<definitions/>", null)));

    assertEquals("ZHYC_WORKFLOW_ARGUMENT_INVALID", exception.getCode());
    assertEquals("流程模型未启用，不能发布", exception.getMessage());
  }

  /**
   * 创建记录部署调用的 Flowable 仓储服务代理。
   *
   * @param deploymentBuilder 部署构建器记录器
   * @return Flowable 仓储服务代理
   */
  private RepositoryService repositoryService(CapturingDeploymentBuilder deploymentBuilder) {
    return (RepositoryService) Proxy.newProxyInstance(RepositoryService.class.getClassLoader(),
        new Class<?>[] {RepositoryService.class}, (proxy, method, args) -> {
          if ("createDeployment".equals(method.getName())) {
            return deploymentBuilder.proxy();
          }
          throw new UnsupportedOperationException("未支持的 RepositoryService 方法: "
              + method.getName());
        });
  }

  /**
   * 内存流程模型仓储，用于验证租户内模型读取。
   */
  private static class InMemoryModelRepository implements WorkflowProcessModelRepository {

    /** 流程模型列表。 */
    private final List<WorkflowProcessModel> models = new ArrayList<>();

    @Override
    public List<WorkflowProcessModel> findByTenantId(String tenantId) {
      return models.stream()
          .filter(model -> tenantId.equals(model.getTenantId()))
          .toList();
    }

    @Override
    public void save(WorkflowProcessModel model) {
      models.add(model);
    }
  }

  /**
   * 内存流程定义仓储，用于验证新定义版本保存。
   */
  private static class InMemoryDefinitionRepository implements WorkflowDefinitionRepository {

    /** 已存在或已保存的流程定义列表。 */
    private final List<WorkflowDefinition> definitions = new ArrayList<>();
    /** 最近一次保存的流程定义。 */
    private WorkflowDefinition savedDefinition;

    @Override
    public List<WorkflowDefinition> findByTenantId(String tenantId) {
      return definitions.stream()
          .filter(definition -> tenantId.equals(definition.getTenantId()))
          .toList();
    }

    @Override
    public void save(WorkflowDefinition definition) {
      savedDefinition = definition;
      definitions.add(definition);
    }
  }

  /**
   * 记录 Flowable 部署构建器调用参数。
   */
  private static class CapturingDeploymentBuilder {

    /** 部署所属租户业务编码。 */
    private String tenantId;
    /** Flowable 部署 key。 */
    private String key;
    /** Flowable 部署名称。 */
    private String name;
    /** BPMN 资源文件名。 */
    private String resourceName;
    /** BPMN XML 文本。 */
    private String resourceText;

    /**
     * 创建 Flowable 部署构建器代理。
     *
     * @return 部署构建器代理
     */
    private DeploymentBuilder proxy() {
      return (DeploymentBuilder) Proxy.newProxyInstance(DeploymentBuilder.class.getClassLoader(),
          new Class<?>[] {DeploymentBuilder.class}, (proxy, method, args) -> {
            String methodName = method.getName();
            if ("tenantId".equals(methodName)) {
              tenantId = (String) args[0];
              return proxy;
            }
            if ("key".equals(methodName)) {
              key = (String) args[0];
              return proxy;
            }
            if ("name".equals(methodName)) {
              name = (String) args[0];
              return proxy;
            }
            if ("addString".equals(methodName)) {
              resourceName = (String) args[0];
              resourceText = (String) args[1];
              return proxy;
            }
            if ("deploy".equals(methodName)) {
              return deployment();
            }
            throw new UnsupportedOperationException("未支持的 DeploymentBuilder 方法: "
                + methodName);
          });
    }

    /**
     * 创建 Flowable 部署结果代理。
     *
     * @return 部署结果代理
     */
    private Deployment deployment() {
      return (Deployment) Proxy.newProxyInstance(Deployment.class.getClassLoader(),
          new Class<?>[] {Deployment.class}, (proxy, method, args) -> {
            if ("getId".equals(method.getName())) {
              return "deployment-100";
            }
            throw new UnsupportedOperationException("未支持的 Deployment 方法: "
                + method.getName());
          });
    }
  }
}
