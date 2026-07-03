/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.oauthclient.mapper;

import com.zhyc.openapi.oauthclient.domain.OpenApiOauthClient;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

/**
 * 开放平台 OAuth2 客户端映射 MyBatis Mapper。
 */
@Mapper
public interface OpenApiOauthClientMapper {

    /**
     * 查询租户指定应用的 OAuth2 客户端映射列表。
     *
     * @param tenantId 租户业务编码
     * @param appCode 开发者应用编码
     * @return OAuth2 客户端映射列表
     */
    @SelectProvider(type = OpenApiOauthClientSqlProvider.class, method = "selectByTenantIdAndAppCode")
    List<OpenApiOauthClient> selectByTenantIdAndAppCode(@Param("tenantId") String tenantId,
                                                         @Param("appCode") String appCode);

    /**
     * 保存或更新 OAuth2 客户端映射。
     *
     * @param client OAuth2 客户端映射领域对象
     */
    @InsertProvider(type = OpenApiOauthClientSqlProvider.class, method = "upsert")
    void upsert(OpenApiOauthClient client);
}
