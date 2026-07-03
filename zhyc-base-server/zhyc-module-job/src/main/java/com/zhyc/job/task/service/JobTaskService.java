/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.job.task.service;

import java.util.List;

/**
 * 在线作业任务业务服务。
 */
public interface JobTaskService {

  /**
   * 查询作业任务列表。
   *
   * @param tenantId 租户业务编码
   * @param status 作业状态
   * @return 作业任务列表
   */
  List<JobTaskResponse> listTasks(String tenantId, String status);

  /**
   * 保存作业任务。
   *
   * @param command 作业任务保存命令
   */
  void save(JobTaskSaveCommand command);

  /**
   * 更新作业任务状态。
   *
   * @param tenantId 租户业务编码
   * @param id 作业任务主键
   * @param status 作业状态
   */
  void updateStatus(String tenantId, Long id, String status);

  /**
   * 手动触发作业任务并写入执行日志。
   *
   * @param tenantId 租户业务编码
   * @param id 作业任务主键
   * @param operatorId 操作人用户主键
   */
  void trigger(String tenantId, Long id, Long operatorId);

  /**
   * 查询作业执行日志。
   *
   * @param tenantId 租户业务编码
   * @param jobId 作业任务主键
   * @return 作业执行日志列表
   */
  List<JobTaskLogResponse> listLogs(String tenantId, Long jobId);
}
