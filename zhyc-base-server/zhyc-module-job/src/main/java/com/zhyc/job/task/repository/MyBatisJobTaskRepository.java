/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.job.task.repository;

import com.zhyc.job.task.domain.JobTask;
import com.zhyc.job.task.mapper.JobTaskMapper;
import com.zhyc.job.task.service.JobTaskLogResponse;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * 基于 MyBatis 的在线作业任务仓储实现。
 */
@Repository
public class MyBatisJobTaskRepository implements JobTaskRepository {

  /** 作业任务 Mapper。 */
  private final JobTaskMapper taskMapper;

  /**
   * 创建作业任务仓储实现。
   *
   * @param taskMapper 作业任务 Mapper
   */
  public MyBatisJobTaskRepository(JobTaskMapper taskMapper) {
    this.taskMapper = Objects.requireNonNull(taskMapper, "作业任务 Mapper 不能为空");
  }

  @Override
  public List<JobTask> findByTenantAndStatus(String tenantId, String status) {
    return taskMapper.selectTasks(tenantId, status);
  }

  @Override
  public Optional<JobTask> findByTenantAndId(String tenantId, Long id) {
    return taskMapper.selectByTenantAndId(tenantId, id);
  }

  @Override
  public void save(JobTask task) {
    taskMapper.upsert(task);
  }

  @Override
  public void updateStatus(String tenantId, Long id, String status) {
    taskMapper.updateStatus(tenantId, id, status);
  }

  @Override
  public void insertLog(String tenantId, Long jobId, String triggerType, String result,
      String errorMessage, Long operatorId) {
    taskMapper.insertLog(tenantId, jobId, triggerType, result, errorMessage, operatorId);
  }

  @Override
  public List<JobTaskLogResponse> findLogs(String tenantId, Long jobId) {
    return taskMapper.selectLogs(tenantId, jobId);
  }
}
