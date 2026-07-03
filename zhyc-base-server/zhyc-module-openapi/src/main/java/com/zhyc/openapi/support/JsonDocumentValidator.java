/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.support;

import java.util.ArrayList;
import java.util.List;

/**
 * 开放 API 首期轻量 JSON 文档校验器。
 *
 * <p>当前 OpenAPI 模块不额外引入 JSON 依赖，本校验器仅用于入库前的 JSON 语法兜底，
 * 不承担 JSON Schema、IP 地址或业务语义校验。</p>
 */
public final class JsonDocumentValidator {

    /**
     * 隐藏工具类构造方法。
     */
    private JsonDocumentValidator() {
    }

    /**
     * 判断文本是否为合法 JSON 文档。
     *
     * @param value JSON 文本
     * @return 合法 JSON 对象或数组返回 {@code true}
     */
    public static boolean isJsonDocument(String value) {
        return new JsonDocumentParser(value).parseDocument();
    }

    /**
     * 判断文本是否为合法 JSON 对象文档。
     *
     * @param value JSON 文本
     * @return 合法 JSON 对象返回 {@code true}
     */
    public static boolean isJsonObject(String value) {
        return value != null && value.trim().startsWith("{") && isJsonDocument(value);
    }

    /**
     * 判断文本是否为合法 JSON 数组文档。
     *
     * @param value JSON 文本
     * @return 合法 JSON 数组返回 {@code true}
     */
    public static boolean isJsonArray(String value) {
        return value != null && value.trim().startsWith("[") && isJsonDocument(value);
    }

    /**
     * 判断文本是否为合法 JSON 字符串数组文档。
     *
     * @param value JSON 文本
     * @return 合法 JSON 字符串数组返回 {@code true}
     */
    public static boolean isJsonStringArray(String value) {
        return value != null && new JsonDocumentParser(value).parseStringArrayDocument();
    }

    /**
     * 判断文本是否为合法非空 JSON 字符串数组文档。
     *
     * @param value JSON 文本
     * @return 合法且所有元素均为非空白字符串返回 {@code true}
     */
    public static boolean isNonBlankJsonStringArray(String value) {
        return value != null && new JsonDocumentParser(value).parseNonBlankStringArrayDocument();
    }

    /**
     * 读取合法 JSON 字符串数组中的字符串元素。
     *
     * @param value JSON 文本
     * @return 合法 JSON 字符串数组返回元素列表；非法文本返回 {@code null}
     */
    public static List<String> readJsonStringArray(String value) {
        if (value == null) {
            return null;
        }
        return new JsonDocumentParser(value).parseStringArrayValuesDocument();
    }

    /**
     * 首期轻量 JSON 文档解析器。
     *
     * <p>解析对象、数组、字符串、数字、布尔值和 null 的基本 JSON 语法。</p>
     */
    private static final class JsonDocumentParser {

        /** 待解析 JSON 文档。 */
        private final String value;
        /** 当前解析位置。 */
        private int position;

        /**
         * 创建 JSON 文档解析器。
         *
         * @param value 待解析 JSON 文档
         */
        private JsonDocumentParser(String value) {
            this.value = value == null ? "" : value;
        }

        /**
         * 解析顶层 JSON 文档。
         *
         * @return 文档是合法 JSON 返回 {@code true}
         */
        private boolean parseDocument() {
            skipWhitespace();
            if (!peek('{') && !peek('[')) {
                return false;
            }
            if (!parseValue()) {
                return false;
            }
            skipWhitespace();
            return position == value.length();
        }

        /**
         * 解析顶层 JSON 字符串数组文档。
         *
         * @return 文档是合法 JSON 字符串数组返回 {@code true}
         */
        private boolean parseStringArrayDocument() {
            skipWhitespace();
            if (!consume('[')) {
                return false;
            }
            skipWhitespace();
            if (consume(']')) {
                skipWhitespace();
                return position == value.length();
            }
            while (position < value.length()) {
                if (parseStringContent() == null) {
                    return false;
                }
                skipWhitespace();
                if (consume(']')) {
                    skipWhitespace();
                    return position == value.length();
                }
                if (!consume(',')) {
                    return false;
                }
                skipWhitespace();
            }
            return false;
        }

        /**
         * 解析顶层 JSON 非空字符串数组文档。
         *
         * @return 文档是合法非空 JSON 字符串数组返回 {@code true}
         */
        private boolean parseNonBlankStringArrayDocument() {
            skipWhitespace();
            if (!consume('[')) {
                return false;
            }
            skipWhitespace();
            if (consume(']')) {
                skipWhitespace();
                return position == value.length();
            }
            while (position < value.length()) {
                String item = parseStringContent();
                if (item == null || item.trim().isEmpty()) {
                    return false;
                }
                skipWhitespace();
                if (consume(']')) {
                    skipWhitespace();
                    return position == value.length();
                }
                if (!consume(',')) {
                    return false;
                }
                skipWhitespace();
            }
            return false;
        }

        /**
         * 读取顶层 JSON 字符串数组文档元素。
         *
         * @return 文档合法时返回字符串元素列表，否则返回 {@code null}
         */
        private List<String> parseStringArrayValuesDocument() {
            List<String> items = new ArrayList<>();
            skipWhitespace();
            if (!consume('[')) {
                return null;
            }
            skipWhitespace();
            if (consume(']')) {
                skipWhitespace();
                return position == value.length() ? items : null;
            }
            while (position < value.length()) {
                String item = parseStringContent();
                if (item == null) {
                    return null;
                }
                items.add(item);
                skipWhitespace();
                if (consume(']')) {
                    skipWhitespace();
                    return position == value.length() ? items : null;
                }
                if (!consume(',')) {
                    return null;
                }
                skipWhitespace();
            }
            return null;
        }

        private boolean parseValue() {
            skipWhitespace();
            if (position >= value.length()) {
                return false;
            }
            char current = value.charAt(position);
            if (current == '{') {
                return parseObject();
            }
            if (current == '[') {
                return parseArray();
            }
            if (current == '"') {
                return parseString();
            }
            if (current == '-' || Character.isDigit(current)) {
                return parseNumber();
            }
            return parseLiteral("true") || parseLiteral("false") || parseLiteral("null");
        }

        private boolean parseObject() {
            position++;
            skipWhitespace();
            if (consume('}')) {
                return true;
            }
            while (position < value.length()) {
                if (!parseString()) {
                    return false;
                }
                skipWhitespace();
                if (!consume(':')) {
                    return false;
                }
                if (!parseValue()) {
                    return false;
                }
                skipWhitespace();
                if (consume('}')) {
                    return true;
                }
                if (!consume(',')) {
                    return false;
                }
                skipWhitespace();
            }
            return false;
        }

        private boolean parseArray() {
            position++;
            skipWhitespace();
            if (consume(']')) {
                return true;
            }
            while (position < value.length()) {
                if (!parseValue()) {
                    return false;
                }
                skipWhitespace();
                if (consume(']')) {
                    return true;
                }
                if (!consume(',')) {
                    return false;
                }
            }
            return false;
        }

        private boolean parseString() {
            return parseStringContent() != null;
        }

        private String parseStringContent() {
            if (!consume('"')) {
                return null;
            }
            StringBuilder content = new StringBuilder();
            while (position < value.length()) {
                char current = value.charAt(position++);
                if (current == '"') {
                    return content.toString();
                }
                if (current == '\\') {
                    Character escaped = parseEscapedCharacter();
                    if (escaped == null) {
                        return null;
                    }
                    content.append(escaped);
                    continue;
                }
                if (current < 0x20) {
                    return null;
                }
                content.append(current);
            }
            return null;
        }

        private Character parseEscapedCharacter() {
            if (position >= value.length()) {
                return null;
            }
            char escaped = value.charAt(position++);
            if (escaped == '"' || escaped == '\\' || escaped == '/') {
                return escaped;
            }
            if (escaped == 'b') {
                return '\b';
            }
            if (escaped == 'f') {
                return '\f';
            }
            if (escaped == 'n') {
                return '\n';
            }
            if (escaped == 'r') {
                return '\r';
            }
            if (escaped == 't') {
                return '\t';
            }
            if (escaped != 'u' || position + 4 > value.length()) {
                return null;
            }
            int codePoint = 0;
            for (int index = 0; index < 4; index++) {
                char hex = value.charAt(position++);
                if (!isHexCharacter(hex)) {
                    return null;
                }
                codePoint = codePoint * 16 + hexValue(hex);
            }
            return (char) codePoint;
        }

        private boolean parseNumber() {
            if (consume('-') && position >= value.length()) {
                return false;
            }
            if (consume('0')) {
                if (position < value.length() && Character.isDigit(value.charAt(position))) {
                    return false;
                }
            } else if (!parseDigits()) {
                return false;
            }
            if (consume('.')) {
                if (!parseDigits()) {
                    return false;
                }
            }
            if (consume('e') || consume('E')) {
                consume('+');
                consume('-');
                return parseDigits();
            }
            return true;
        }

        private boolean parseDigits() {
            int start = position;
            while (position < value.length() && Character.isDigit(value.charAt(position))) {
                position++;
            }
            return position > start;
        }

        private boolean parseLiteral(String literal) {
            if (value.startsWith(literal, position)) {
                position += literal.length();
                return true;
            }
            return false;
        }

        private boolean consume(char expected) {
            if (peek(expected)) {
                position++;
                return true;
            }
            return false;
        }

        private boolean peek(char expected) {
            return position < value.length() && value.charAt(position) == expected;
        }

        private void skipWhitespace() {
            while (position < value.length() && Character.isWhitespace(value.charAt(position))) {
                position++;
            }
        }

        private boolean isHexCharacter(char value) {
            return (value >= '0' && value <= '9')
                    || (value >= 'a' && value <= 'f')
                    || (value >= 'A' && value <= 'F');
        }

        private int hexValue(char value) {
            if (value >= '0' && value <= '9') {
                return value - '0';
            }
            if (value >= 'a' && value <= 'f') {
                return value - 'a' + 10;
            }
            return value - 'A' + 10;
        }
    }
}
