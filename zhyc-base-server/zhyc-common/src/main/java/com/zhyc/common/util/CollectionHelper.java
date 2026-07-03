/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 集合处理帮助类。
 *
 * <p>统一处理集合空值判断和只读兜底，减少业务模块中重复的 null 与空集合判断。</p>
 */
public final class CollectionHelper {

  private CollectionHelper() {
  }

  /**
   * 判断集合是否为空。
   *
   * @param values 待判断集合
   * @return null 或空集合返回 {@code true}
   */
  public static boolean isEmpty(Collection<?> values) {
    return values == null || values.isEmpty();
  }

  /**
   * 判断集合是否不为空。
   *
   * @param values 待判断集合
   * @return 包含元素返回 {@code true}
   */
  public static boolean isNotEmpty(Collection<?> values) {
    return !isEmpty(values);
  }

  /**
   * 判断映射是否为空。
   *
   * @param values 待判断映射
   * @return null 或空映射返回 {@code true}
   */
  public static boolean isEmpty(Map<?, ?> values) {
    return values == null || values.isEmpty();
  }

  /**
   * 判断映射是否不为空。
   *
   * @param values 待判断映射
   * @return 包含键值对返回 {@code true}
   */
  public static boolean isNotEmpty(Map<?, ?> values) {
    return !isEmpty(values);
  }

  /**
   * 将可能为空的集合转为不可变列表。
   *
   * @param values 原始集合
   * @param <T> 元素类型
   * @return 不可变列表；原始集合为 null 时返回空列表
   */
  public static <T> List<T> nullToEmptyList(Collection<T> values) {
    if (values == null || values.isEmpty()) {
      return List.of();
    }
    return Collections.unmodifiableList(new ArrayList<>(values));
  }
}
