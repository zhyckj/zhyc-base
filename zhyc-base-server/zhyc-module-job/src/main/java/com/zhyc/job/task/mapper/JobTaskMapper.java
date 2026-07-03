/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.job.task.mapper;

import com.zhyc.job.task.domain.JobTask;
import com.zhyc.job.task.service.JobTaskLogResponse;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

/**
 * 在线作业任务 MyBatis Mapper。
 */
@Mapper
public interface JobTaskMapper {

  /**
   * 查询作业任务列表。
   *
   * @param tenantId 租户业务编码
   * @param status 作业状态
   * @return 作业任务列表
   */
  @SelectProvider(type = JobTaskSqlProvider.class, method = "selectTasks")
  List<JobTask> selectTasks(@Param("tenantId") String tenantId, @Param("status") String status);

  /**
   * 按租户和主键查询作业任务。
   *
   * @param tenantId 租户业务编码
   * @param id 作业任务主键
   * @return 作业任务，未找到时为空
   */
  @SelectProvider(type = JobTaskSqlProvider.class, method = "selectByTenantAndId")
  Optional<JobTask> selectByTenantAndId(@Param("tenantId") String tenantId, @Param("id") Long id);

  /**
   * 保存或更新作业任务。
   *
   * @param task 作业任务
   */
  @InsertProvider(type = JobTaskSqlProvider.class, method = "upsertTask")
  void upsert(JobTask task);

  /**
   * 更新作业任务状态。
   *
   * @param tenantId 租户业务编码
   * @param id 作业任务主键
   * @param status 作业状态
   */
  @UpdateProvider(type = JobTaskSqlProvider.class, method = "updateStatus")
  void updateStatus(@Param("tenantId") String tenantId, @Param("id") Long id,
      @Param("status") String status);

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
  @InsertProvider(type = JobTaskSqlProvider.class, method = "insertLog")
  void insertLog(@Param("tenantId") String tenantId, @Param("jobId") Long jobId,
      @Param("triggerType") String triggerType, @Param("result") String result,
      @Param("errorMessage") String errorMessage, @Param("operatorId") Long operatorId);

  /**
   * 查询作业执行日志。
   *
   * @param tenantId 租户业务编码
   * @param jobId 作业任务主键
   * @return 作业执行日志列表
   */
  @SelectProvider(type = JobTaskSqlProvider.class, method = "selectLogs")
  List<JobTaskLogResponse> selectLogs(@Param("tenantId") String tenantId, @Param("jobId") Long jobId);
}
