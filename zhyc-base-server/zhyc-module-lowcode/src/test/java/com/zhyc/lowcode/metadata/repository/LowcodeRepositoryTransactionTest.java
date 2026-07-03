/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.repository;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 低代码仓储事务边界测试。
 */
class LowcodeRepositoryTransactionTest {

  /**
   * 验证表模型保存具备事务边界，避免表模型已保存但字段重建失败造成元数据不一致。
   *
   * @throws NoSuchMethodException 方法签名缺失时抛出
   */
  @Test
  void shouldMarkTableModelSaveAsTransactional() throws NoSuchMethodException {
    Method save = MyBatisLowcodeTableModelRepository.class.getMethod(
        "save", com.zhyc.lowcode.metadata.domain.LowcodeTableModel.class);

    assertNotNull(save.getAnnotation(Transactional.class));
  }
}
