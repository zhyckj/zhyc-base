/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.version.mapper;

import com.zhyc.openapi.version.domain.OpenApiVersion;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

/**
 * 开放 API 版本发布 MyBatis Mapper。
 */
@Mapper
public interface OpenApiVersionMapper {

    /**
     * 按 API 编码查询版本列表。
     *
     * @param apiCode API 业务编码
     * @return API 版本列表
     */
    @SelectProvider(type = OpenApiVersionSqlProvider.class, method = "selectByApiCode")
    List<OpenApiVersion> selectByApiCode(@Param("apiCode") String apiCode);

    /**
     * 发布或更新 API 版本。
     *
     * @param version API 版本发布领域对象
     */
    @InsertProvider(type = OpenApiVersionSqlProvider.class, method = "upsert")
    void upsert(OpenApiVersion version);
}
