/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.service;

import com.zhyc.lowcode.metadata.domain.LowcodeDataSource;
import com.zhyc.lowcode.metadata.domain.LowcodePageModel;
import com.zhyc.lowcode.metadata.domain.LowcodePhysicalTable;
import com.zhyc.lowcode.metadata.domain.LowcodeTableRelation;
import com.zhyc.lowcode.metadata.domain.LowcodeTableModel;

import java.util.List;

/**
 * 低代码元数据服务接口。
 */
public interface LowcodeMetadataService {

  /**
   * 注册或更新数据源定义。
   *
   * @param dataSource 数据源定义
   * @return 保存后的数据源定义
   */
  LowcodeDataSource saveDataSource(LowcodeDataSource dataSource);

  /**
   * 查询数据源定义。
   *
   * @param tenantId 租户业务编码
   * @param code 数据源编码
   * @return 数据源定义
   */
  LowcodeDataSource getDataSource(String tenantId, String code);

  /**
   * 查询租户内数据源列表。
   *
   * @param tenantId 租户业务编码
   * @return 数据源列表
   */
  List<LowcodeDataSource> listDataSources(String tenantId);

  /**
   * 查询租户内数据源的物理表清单。
   *
   * @param tenantId 租户业务编码
   * @param dataSourceId 数据源主键
   * @return 物理表清单
   */
  List<LowcodePhysicalTable> listPhysicalTables(String tenantId, Long dataSourceId);

  /**
   * 测试数据源连接配置。
   *
   * <p>首期执行轻量配置预检查，不解析或传输数据库口令明文；后续可替换为真实连接探测器。</p>
   *
   * @param tenantId 租户业务编码
   * @param code 数据源编码
   * @return 数据源连接测试结果
   */
  LowcodeDataSourceConnectionTestResult testDataSourceConnection(String tenantId, String code);

  /**
   * 保存表模型。
   *
   * @param tableModel 表模型
   * @return 保存后的表模型
   */
  LowcodeTableModel saveTableModel(LowcodeTableModel tableModel);

  /**
   * 查询表模型。
   *
   * @param tenantId 租户业务编码
   * @param code 模型编码
   * @return 表模型
   */
  LowcodeTableModel getTableModel(String tenantId, String code);

  /**
   * 从数据源物理表导入低代码表模型。
   *
   * @param tenantId 租户业务编码
   * @param dataSourceId 数据源主键
   * @param tableName 物理表名
   * @param modelCode 模型编码
   * @param modelName 模型名称
   * @return 保存后的表模型
   */
  LowcodeTableModel importTableModel(String tenantId, Long dataSourceId, String tableName,
                                     String modelCode, String modelName);

  /**
   * 查询租户内表模型列表。
   *
   * @param tenantId 租户业务编码
   * @return 表模型列表
   */
  List<LowcodeTableModel> listTableModels(String tenantId);

  /**
   * 发布表模型。
   *
   * @param tenantId 租户业务编码
   * @param code 模型编码
   * @return 发布后的表模型
   */
  LowcodeTableModel publishTableModel(String tenantId, String code);

  /**
   * 保存表关系模型。
   *
   * @param relation 表关系模型
   * @return 保存后的表关系模型
   */
  LowcodeTableRelation saveTableRelation(LowcodeTableRelation relation);

  /**
   * 查询租户内表关系模型。
   *
   * @param tenantId 租户业务编码
   * @return 表关系模型列表
   */
  List<LowcodeTableRelation> listTableRelations(String tenantId);

  /**
   * 保存页面模型。
   *
   * @param pageModel 页面模型
   * @return 保存后的页面模型
   */
  LowcodePageModel savePageModel(LowcodePageModel pageModel);

  /**
   * 查询租户内页面模型。
   *
   * @param tenantId 租户业务编码
   * @return 页面模型列表
   */
  List<LowcodePageModel> listPageModels(String tenantId);
}
