/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.version.repository;

import com.zhyc.openapi.version.domain.OpenApiVersion;
import com.zhyc.openapi.version.mapper.OpenApiVersionMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * 基于 MyBatis 的开放 API 版本发布仓储实现。
 */
@Repository
public class MyBatisOpenApiVersionRepository implements OpenApiVersionRepository {

    /** API 版本 Mapper。 */
    private final OpenApiVersionMapper versionMapper;

    /**
     * 创建开放 API 版本发布仓储实现。
     *
     * @param versionMapper API 版本 Mapper
     */
    public MyBatisOpenApiVersionRepository(OpenApiVersionMapper versionMapper) {
        this.versionMapper = Objects.requireNonNull(versionMapper, "API 版本 Mapper 不能为空");
    }

    @Override
    public List<OpenApiVersion> findByApiCode(String apiCode) {
        return versionMapper.selectByApiCode(apiCode);
    }

    @Override
    public void save(OpenApiVersion version) {
        versionMapper.upsert(version);
    }
}
