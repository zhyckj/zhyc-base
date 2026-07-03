/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.job.task.repository;

import com.zhyc.job.task.domain.JobTask;
import com.zhyc.job.task.service.JobTaskLogResponse;
import java.util.List;
import java.util.Optional;

/**
 * 在线作业任务仓储。
 */
public interface JobTaskRepository {

  /**
   * 按租户和状态查询作业任务。
   *
   * @param tenantId 租户业务编码
   * @param status 作业状态
   * @return 作业任务列表
   */
  List<JobTask> findByTenantAndStatus(String tenantId, String status);

  /**
   * 按租户和主键查询作业任务。
   *
   * @param tenantId 租户业务编码
   * @param id 作业任务主键
   * @return 作业任务，未找到时为空
   */
  Optional<JobTask> findByTenantAndId(String tenantId, Long id);

  /**
   * 保存或更新作业任务。
   *
   * @param task 作业任务
   */
  void save(JobTask task);

  /**
   * 更新作业任务状态。
   *
   * @param tenantId 租户业务编码
   * @param id 作业任务主键
   * @param status 作业状态
   */
  void updateStatus(String tenantId, Long id, String status);

  /**
   * 写入作业执行日志。
   *
   * @param tenantId 租户业务编码
   * @param jobId 作业任务主键
   * @param triggerType 触发类型
   * @param result 执行结果
   * @param errorMessage 错误信息
   * @param operatorId 操作人用户主键
   */
  void insertLog(String tenantId, Long jobId, String triggerType, String result,
      String errorMessage, Long operatorId);

  /**
   * 查询作业执行日志。
   *
   * @param tenantId 租户业务编码
   * @param jobId 作业任务主键
   * @return 作业执行日志列表
   */
  List<JobTaskLogResponse> findLogs(String tenantId, Long jobId);
}
