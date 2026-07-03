/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.coderule.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.coderule.service.SysCodeRuleResponse;
import com.zhyc.system.coderule.service.SysCodeRuleSaveCommand;
import com.zhyc.system.coderule.service.SysCodeRuleService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * 系统编码规则管理接口。
 */
@RestController
@RequestMapping("/system/code-rules")
public class SysCodeRuleController {

    /** 系统编码规则保存请求缺失错误码。 */
    private static final String ERROR_SAVE_REQUEST_REQUIRED =
            "ZHYC_SYSTEM_CODE_RULE_SAVE_REQUEST_REQUIRED";
    /** 系统编码规则生成请求缺失错误码。 */
    private static final String ERROR_GENERATE_REQUEST_REQUIRED =
            "ZHYC_SYSTEM_CODE_RULE_GENERATE_REQUEST_REQUIRED";

    /** 系统编码规则业务服务。 */
    private final SysCodeRuleService codeRuleService;

    /**
     * 创建系统编码规则管理接口。
     *
     * @param codeRuleService 系统编码规则业务服务
     */
    public SysCodeRuleController(SysCodeRuleService codeRuleService) {
        this.codeRuleService = Objects.requireNonNull(codeRuleService, "系统编码规则业务服务不能为空");
    }

    /**
     * 查询租户编码规则列表。
     *
     * @param tenantId 租户业务编码
     * @return 编码规则列表
     */
    @RequiresPermissions("system:code-rule:query")
    @GetMapping
    public ApiResult<List<SysCodeRuleResponse>> listRules(@RequestParam("tenantId") String tenantId) {
        return ApiResult.ok(codeRuleService.listRules(tenantId));
    }

    /**
     * 保存或更新编码规则。
     *
     * @param request 编码规则保存请求
     * @return 空响应
     */
    @RequiresPermissions("system:code-rule:save")
    @PutMapping
    public ApiResult<Void> save(@RequestBody SysCodeRuleSaveRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_SAVE_REQUEST_REQUIRED, "系统编码规则保存请求不能为空");
        }
        codeRuleService.save(new SysCodeRuleSaveCommand(request.getTenantId(), request.getRuleCode(),
                request.getRuleName(), request.getPrefix(), request.getDatePattern(), request.getSequenceLength(),
                request.getCurrentValue(), request.isEnabled()));
        return ApiResult.ok(null);
    }

    /**
     * 生成下一个业务编码。
     *
     * @param request 编码规则生成请求
     * @return 下一个业务编码
     */
    @RequiresPermissions("system:code-rule:generate")
    @PostMapping("/next-code")
    public ApiResult<String> generateNextCode(@RequestBody SysCodeRuleNextRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_GENERATE_REQUEST_REQUIRED, "系统编码规则生成请求不能为空");
        }
        return ApiResult.ok(codeRuleService.generateNextCode(request.getTenantId(), request.getRuleCode(),
                request.getBusinessDate()));
    }
}
