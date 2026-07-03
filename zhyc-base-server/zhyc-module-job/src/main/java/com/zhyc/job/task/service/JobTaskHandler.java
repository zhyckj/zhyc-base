/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.job.task.service;

/**
 * 在线作业任务处理器。
 *
 * <p>业务模块通过实现该接口注册可执行作业，调度模块只按处理器名称调用，不直接依赖业务实现。</p>
 */
public interface JobTaskHandler {

  /**
   * 返回处理器名称。
   *
   * @return 处理器名称，需与作业任务配置的 handlerName 一致
   */
  String getHandlerName();

  /**
   * 执行作业任务。
   *
   * @param context 作业任务执行上下文
   */
  void handle(JobTaskExecutionContext context);
}
