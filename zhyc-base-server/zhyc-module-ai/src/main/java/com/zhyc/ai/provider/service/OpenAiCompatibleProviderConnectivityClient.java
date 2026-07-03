/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.provider.service;

import com.zhyc.common.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.Set;

/**
 * OpenAI 兼容供应商连通性测试客户端。
 */
@Component
public class OpenAiCompatibleProviderConnectivityClient implements AiProviderConnectivityClient {

    /** 基础地址缺失错误码。 */
    private static final String ERROR_BASE_URL_REQUIRED = "ZHYC_AI_PROVIDER_TEST_BASE_URL_REQUIRED";
    /** API Key 缺失错误码。 */
    private static final String ERROR_API_KEY_REQUIRED = "ZHYC_AI_PROVIDER_TEST_API_KEY_REQUIRED";
    /** API Key 格式错误码。 */
    private static final String ERROR_API_KEY_FORMAT = "ZHYC_AI_PROVIDER_TEST_API_KEY_FORMAT";
    /** HTTP 调用失败错误码。 */
    private static final String ERROR_HTTP_FAILED = "ZHYC_AI_PROVIDER_TEST_HTTP_FAILED";
    /** OpenAI 兼容供应商类型集合。 */
    private static final Set<String> SUPPORTED_PROVIDER_TYPES = Set.of(
            "openai_compatible", "dashscope", "volcengine", "deepseek", "zhipu", "local");

    /** HTTP 客户端。 */
    private final HttpClient httpClient;

    /**
     * 创建 OpenAI 兼容供应商连通性测试客户端。
     */
    public OpenAiCompatibleProviderConnectivityClient() {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build());
    }

    OpenAiCompatibleProviderConnectivityClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public boolean supports(String providerType) {
        return SUPPORTED_PROVIDER_TYPES.contains(providerType);
    }

    @Override
    public void test(String providerType, String providerCode, String secretRef, String baseUrl, String apiKey) {
        String endpoint = normalizeModelsEndpoint(baseUrl);
        String token = requireApiKey(apiKey);
        validateProviderApiKey(providerType, providerCode, secretRef, token);
        HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BusinessException(ERROR_HTTP_FAILED,
                        buildHttpFailureMessage(response.statusCode()));
            }
        } catch (IOException ex) {
            throw new BusinessException(ERROR_HTTP_FAILED, "供应商测试异常，请检查网络、基础地址或代理配置");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ERROR_HTTP_FAILED, "供应商测试被中断");
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ERROR_HTTP_FAILED, "供应商基础地址格式不正确");
        }
    }

    private String normalizeModelsEndpoint(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new BusinessException(ERROR_BASE_URL_REQUIRED, "供应商基础地址不能为空");
        }
        String normalized = baseUrl.trim();
        if (normalized.endsWith("/models")) {
            return normalized;
        }
        String withoutSlash = normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
        return withoutSlash + "/models";
    }

    private String requireApiKey(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new BusinessException(ERROR_API_KEY_REQUIRED, "供应商密钥解析结果不能为空");
        }
        String token = normalizeBearerToken(apiKey);
        if (token.isBlank()) {
            throw new BusinessException(ERROR_API_KEY_REQUIRED, "供应商密钥解析结果不能为空");
        }
        return token;
    }

    private String normalizeBearerToken(String apiKey) {
        String token = apiKey.trim();
        if (token.toLowerCase(Locale.ROOT).startsWith("authorization:")) {
            token = token.substring("Authorization:".length()).trim();
        }
        if (token.toLowerCase(Locale.ROOT).startsWith("bearer ")) {
            return token.substring("Bearer ".length()).trim();
        }
        return token;
    }

    private void validateProviderApiKey(String providerType, String providerCode, String secretRef, String token) {
        if ("deepseek".equals(providerType) && !token.startsWith("sk-")) {
            throw new BusinessException(ERROR_API_KEY_FORMAT,
                    "DeepSeek 密钥明文格式不正确：请在密钥管理中把 " + displaySecretRef(secretRef, providerCode)
                            + " 的明文更新为 DeepSeek 控制台生成的 sk- 开头 API Key，"
                            + "不要填写供应商编码、secret 引用或 Authorization/Bearer 请求头");
        }
    }

    private String displaySecretRef(String secretRef, String providerCode) {
        if (secretRef != null && !secretRef.isBlank()) {
            return secretRef.trim();
        }
        return "secret:" + providerCode;
    }

    private String buildHttpFailureMessage(int statusCode) {
        if (statusCode == 401 || statusCode == 403) {
            return "供应商测试失败，HTTP 状态码: " + statusCode
                    + "，请检查 API Key 是否正确、密钥明文是否只填写 Key 本身，"
                    + "不要填写 Authorization/Bearer 请求头，并确认账号余额和模型访问权限";
        }
        return "供应商测试失败，HTTP 状态码: " + statusCode;
    }
}
