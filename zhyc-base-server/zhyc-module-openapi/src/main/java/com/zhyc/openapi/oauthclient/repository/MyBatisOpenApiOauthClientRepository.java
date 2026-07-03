/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.oauthclient.repository;

import com.zhyc.openapi.oauthclient.domain.OpenApiOauthClient;
import com.zhyc.openapi.oauthclient.mapper.OpenApiOauthClientMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * 基于 MyBatis 的开放平台 OAuth2 客户端映射仓储实现。
 */
@Repository
public class MyBatisOpenApiOauthClientRepository implements OpenApiOauthClientRepository {

    /** OAuth2 客户端映射 Mapper。 */
    private final OpenApiOauthClientMapper oauthClientMapper;

    /**
     * 创建 OAuth2 客户端映射仓储实现。
     *
     * @param oauthClientMapper OAuth2 客户端映射 Mapper
     */
    public MyBatisOpenApiOauthClientRepository(OpenApiOauthClientMapper oauthClientMapper) {
        this.oauthClientMapper = Objects.requireNonNull(oauthClientMapper, "OAuth2 客户端映射 Mapper 不能为空");
    }

    @Override
    public List<OpenApiOauthClient> findByTenantIdAndAppCode(String tenantId, String appCode) {
        return oauthClientMapper.selectByTenantIdAndAppCode(tenantId, appCode);
    }

    @Override
    public void save(OpenApiOauthClient client) {
        oauthClientMapper.upsert(client);
    }
}
