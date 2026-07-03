/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.menu;

import com.zhyc.system.menu.mapper.SysMenuMapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统菜单 Mapper 契约测试。
 */
class SysMenuMapperContractTest {

    /**
     * 验证菜单查询显式映射状态字段，避免菜单树响应丢失启停状态。
     *
     * @throws Exception 反射读取 Mapper 方法失败时抛出
     */
    @Test
    void shouldMapMenuStatusColumnInTreeQueries() throws Exception {
        Method enabledQuery = SysMenuMapper.class.getMethod("selectEnabledByTenantId", String.class);
        Results results = enabledQuery.getAnnotation(Results.class);

        assertNotNull(results, "启用菜单查询必须声明结果映射");
        assertTrue(Arrays.stream(results.value()).anyMatch(SysMenuMapperContractTest::isStatusResult),
                "菜单查询结果映射必须包含 status -> status");

        Method allQuery = SysMenuMapper.class.getMethod("selectByTenantId", String.class);
        ResultMap resultMap = allQuery.getAnnotation(ResultMap.class);
        assertNotNull(resultMap, "全部菜单查询必须复用菜单结果映射");
        assertEquals("SysMenuResultMap", resultMap.value()[0]);
    }

    private static boolean isStatusResult(Result result) {
        return "status".equals(result.column()) && "status".equals(result.property());
    }
}
