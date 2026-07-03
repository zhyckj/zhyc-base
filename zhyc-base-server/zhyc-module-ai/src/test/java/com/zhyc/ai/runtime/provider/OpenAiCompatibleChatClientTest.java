/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.runtime.provider;

import com.sun.net.httpserver.HttpServer;
import com.zhyc.ai.runtime.spi.AiChatRequest;
import com.zhyc.ai.runtime.spi.AiChatResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * OpenAI 兼容运行时对话适配器测试。
 */
class OpenAiCompatibleChatClientTest {

    @Test
    void shouldSupportDeepSeekProviderType() {
        OpenAiCompatibleChatClient client = new OpenAiCompatibleChatClient();

        assertTrue(client.supports("openai_compatible"));
        assertTrue(client.supports("deepseek"));
        assertFalse(client.supports("unsupported"));
    }

    @Test
    void shouldReadAssistantMessageContentFromStructuredResponse() throws IOException {
        String fieldJson = """
                [{"code":"id","name":"主键","fieldType":"LONG","length":20,"scale":0,
                "required":true,"primaryKey":true,"autoIncrement":true,"listVisible":false,
                "formVisible":false,"queryable":false,"comment":"主键"}]
                """.trim();
        String responseBody = """
                {"id":"trace-lowcode","meta":{"content":"metadata-content"},
                "choices":[{"message":{"role":"assistant","content":"%s"}}],
                "usage":{"prompt_tokens":11,"completion_tokens":22,"total_tokens":33}}
                """.formatted(escapeJson(fieldJson)).trim();
        HttpServer server = startJsonServer(responseBody);
        try {
            OpenAiCompatibleChatClient client = new OpenAiCompatibleChatClient();
            AiChatResponse response = client.chat(new AiChatRequest("tenant-a", "lowcode-model-assistant",
                    "deepseek", "http://127.0.0.1:" + server.getAddress().getPort(), "deepseek-chat",
                    "只输出 JSON", "生成字段", false), "sk-test");

            assertEquals(fieldJson, response.content());
            assertEquals(11, response.promptTokens());
            assertEquals(22, response.completionTokens());
            assertEquals(33, response.totalTokens());
            assertEquals("trace-lowcode", response.traceId());
        } finally {
            server.stop(0);
        }
    }

    private static HttpServer startJsonServer(String responseBody) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/chat/completions", exchange -> {
            byte[] body = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(body);
            }
        });
        server.start();
        return server;
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
