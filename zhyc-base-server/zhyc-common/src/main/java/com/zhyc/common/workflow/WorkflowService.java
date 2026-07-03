/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.common.workflow;

import java.util.Map;

/**
 * 工作流能力门面。
 *
 * <p>业务模块依赖该接口发起和处理流程，具体工作流引擎由实现模块适配。</p>
 */
public interface WorkflowService {

    /**
     * 启动流程实例。
     *
     * @param processKey 流程定义 key
     * @param businessKey 业务对象唯一标识
     * @param variables 流程变量；传入 {@code null} 时等价于空变量集合，由实现方规范化处理
     * @return 新启动的流程实例 ID
     */
    String startProcess(String processKey, String businessKey, Map<String, Object> variables);

    /**
     * 审批通过任务。
     *
     * @param taskId 待审批任务 ID
     * @param comment 审批意见
     * @param variables 审批时提交的流程变量；传入 {@code null} 时等价于空变量集合，由实现方规范化处理
     */
    void approve(String taskId, String comment, Map<String, Object> variables);

    /**
     * 驳回任务。
     *
     * @param taskId 待驳回任务 ID
     * @param comment 驳回意见
     */
    void reject(String taskId, String comment);

    /**
     * 撤回流程实例。
     *
     * @param processInstanceId 流程实例 ID
     * @param reason 撤回原因
     */
    void revoke(String processInstanceId, String reason);
}
