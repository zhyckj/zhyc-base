/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.runtime.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhyc.ai.runtime.spi.AiChatClient;
import com.zhyc.ai.runtime.spi.AiChatRequest;
import com.zhyc.ai.runtime.spi.AiChatResponse;
import com.zhyc.common.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * OpenAI Compatible 非流式对话适配器。
 *
 * <p>首期通过 JDK HttpClient 调用兼容 {@code /chat/completions} 的模型网关，避免业务模块引入具体厂商 SDK。</p>
 */
@Component
public class OpenAiCompatibleChatClient implements AiChatClient {

    private static final Set<String> SUPPORTED_PROVIDER_TYPES = Set.of(
            "openai_compatible", "dashscope", "volcengine", "deepseek", "zhipu", "local");
    private static final String ERROR_BASE_URL_REQUIRED = "ZHYC_AI_OPENAI_BASE_URL_REQUIRED";
    private static final String ERROR_API_KEY_REQUIRED = "ZHYC_AI_OPENAI_API_KEY_REQUIRED";
    private static final String ERROR_HTTP_FAILED = "ZHYC_AI_OPENAI_HTTP_FAILED";
    private static final String ERROR_RESPONSE_INVALID = "ZHYC_AI_OPENAI_RESPONSE_INVALID";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OpenAiCompatibleChatClient() {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build(), new ObjectMapper());
    }

    OpenAiCompatibleChatClient(HttpClient httpClient) {
        this(httpClient, new ObjectMapper());
    }

    OpenAiCompatibleChatClient(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = Objects.requireNonNull(httpClient, "HTTP 客户端不能为空");
        this.objectMapper = Objects.requireNonNull(objectMapper, "JSON 解析器不能为空");
    }

    @Override
    public boolean supports(String providerType) {
        return SUPPORTED_PROVIDER_TYPES.contains(providerType);
    }

    @Override
    public AiChatResponse chat(AiChatRequest request, String apiKey) {
        String endpoint = normalizeEndpoint(request.providerBaseUrl());
        String token = requireApiKey(apiKey);
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(endpoint))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(buildBody(request)))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BusinessException(ERROR_HTTP_FAILED, "AI 模型服务调用失败，HTTP 状态码: " + response.statusCode());
            }
            return parseResponse(response.body());
        } catch (IOException ex) {
            throw new BusinessException(ERROR_HTTP_FAILED, "AI 模型服务调用异常，请检查网络或供应商地址");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ERROR_HTTP_FAILED, "AI 模型服务调用被中断");
        }
    }

    private String normalizeEndpoint(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new BusinessException(ERROR_BASE_URL_REQUIRED, "AI 模型供应商基础地址不能为空");
        }
        String normalized = baseUrl.trim();
        if (normalized.endsWith("/chat/completions")) {
            return normalized;
        }
        String withoutSlash = normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
        return withoutSlash + "/chat/completions";
    }

    private String requireApiKey(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new BusinessException(ERROR_API_KEY_REQUIRED, "AI 模型供应商密钥解析结果不能为空");
        }
        String token = normalizeBearerToken(apiKey);
        if (token.isBlank()) {
            throw new BusinessException(ERROR_API_KEY_REQUIRED, "AI 模型供应商密钥解析结果不能为空");
        }
        return token;
    }

    private String normalizeBearerToken(String apiKey) {
        String token = apiKey.trim();
        if (token.toLowerCase(Locale.ROOT).startsWith("bearer ")) {
            return token.substring("Bearer ".length()).trim();
        }
        return token;
    }

    private String buildBody(AiChatRequest request) {
        return """
            {"model":"%s","stream":%s,"messages":[{"role":"system","content":"%s"},{"role":"user","content":"%s"}]}
            """.formatted(escapeJson(request.modelCode()), request.stream(),
                escapeJson(request.systemPrompt()), escapeJson(request.userPrompt())).trim();
    }

    private AiChatResponse parseResponse(String body) {
        try {
            JsonNode root = objectMapper.readTree(body == null ? "" : body);
            String content = findAssistantContent(root);
            if (content == null || content.isBlank()) {
                throw new BusinessException(ERROR_RESPONSE_INVALID, "AI 模型服务响应缺少输出内容");
            }
            JsonNode usage = root.path("usage");
            return new AiChatResponse(content, usage.path("prompt_tokens").asInt(0),
                    usage.path("completion_tokens").asInt(0), usage.path("total_tokens").asInt(0),
                    textOrNull(root.path("id")));
        } catch (JsonProcessingException ex) {
            throw new BusinessException(ERROR_RESPONSE_INVALID, "AI 模型服务响应不是有效 JSON");
        }
    }

    private String findAssistantContent(JsonNode root) {
        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            return null;
        }
        JsonNode firstChoice = choices.get(0);
        JsonNode messageContent = firstChoice.path("message").path("content");
        if (messageContent.isTextual()) {
            return messageContent.asText();
        }
        JsonNode textContent = firstChoice.path("text");
        return textOrNull(textContent);
    }

    private String textOrNull(JsonNode node) {
        return node != null && node.isTextual() ? node.asText() : null;
    }

    private String escapeJson(String value) {
        String safe = value == null ? "" : value;
        return safe.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

}
