/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import java.util.ArrayList;
import java.util.List;

/**
 * 开放 API 客户端 IP 白名单匹配器。
 *
 * <p>首期支持 IPv4 精确匹配和 IPv4 CIDR，输入数据来自已校验的开发者应用白名单。</p>
 */
public class OpenApiClientIpMatcher {

  /**
   * 判断客户端 IP 是否命中白名单 JSON。
   *
   * @param ipWhitelist IP 白名单 JSON 字符串数组
   * @param clientIp 客户端 IP
   * @return 命中白名单返回 {@code true}
   */
  public boolean matches(String ipWhitelist, String clientIp) {
    Long clientIpValue = parseIpv4(clientIp);
    if (clientIpValue == null) {
      return false;
    }
    List<String> items = readJsonStringArray(ipWhitelist);
    for (String item : items) {
      if (matchesItem(item.trim(), clientIpValue)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 判断客户端 IP 是否命中单个白名单项。
   *
   * @param item 白名单项
   * @param clientIpValue 客户端 IPv4 数值
   * @return 命中返回 {@code true}
   */
  private boolean matchesItem(String item, long clientIpValue) {
    String[] parts = item.split("/", -1);
    Long baseIp = parseIpv4(parts[0]);
    if (baseIp == null) {
      return false;
    }
    if (parts.length == 1) {
      return baseIp == clientIpValue;
    }
    if (parts.length != 2) {
      return false;
    }
    Integer prefixLength = parsePrefixLength(parts[1]);
    if (prefixLength == null) {
      return false;
    }
    long mask = prefixLength == 0 ? 0L : 0xffffffffL << (32 - prefixLength) & 0xffffffffL;
    return (baseIp & mask) == (clientIpValue & mask);
  }

  /**
   * 解析 IPv4 地址为无符号 32 位整数。
   *
   * @param value IPv4 地址
   * @return 解析成功返回数值，否则返回 {@code null}
   */
  private Long parseIpv4(String value) {
    if (value == null) {
      return null;
    }
    String[] octets = value.trim().split("\\.", -1);
    if (octets.length != 4) {
      return null;
    }
    long result = 0;
    for (String octet : octets) {
      Integer number = parseIntegerInRange(octet, 0, 255);
      if (number == null) {
        return null;
      }
      result = (result << 8) + number;
    }
    return result;
  }

  /**
   * 解析 CIDR 前缀长度。
   *
   * @param value 前缀长度文本
   * @return 合法前缀长度返回整数，否则返回 {@code null}
   */
  private Integer parsePrefixLength(String value) {
    return parseIntegerInRange(value, 0, 32);
  }

  /**
   * 解析指定范围内的整数。
   *
   * @param value 文本
   * @param min 最小值
   * @param max 最大值
   * @return 合法整数返回数值，否则返回 {@code null}
   */
  private Integer parseIntegerInRange(String value, int min, int max) {
    if (value == null || value.isEmpty()
        || value.chars().anyMatch(character -> !Character.isDigit(character))) {
      return null;
    }
    int number;
    try {
      number = Integer.parseInt(value);
    } catch (NumberFormatException ex) {
      return null;
    }
    return number >= min && number <= max ? number : null;
  }

  /**
   * 读取 JSON 字符串数组。
   *
   * @param value JSON 字符串数组
   * @return 字符串元素列表；非法内容返回空列表
   */
  private List<String> readJsonStringArray(String value) {
    List<String> items = new ArrayList<>();
    if (value == null) {
      return items;
    }
    String trimmed = value.trim();
    if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
      return items;
    }
    int position = 1;
    while (position < trimmed.length() - 1) {
      while (position < trimmed.length() - 1 && Character.isWhitespace(trimmed.charAt(position))) {
        position++;
      }
      if (position >= trimmed.length() - 1) {
        break;
      }
      if (trimmed.charAt(position) != '"') {
        return List.of();
      }
      StringBuilder item = new StringBuilder();
      position++;
      while (position < trimmed.length() - 1) {
        char current = trimmed.charAt(position++);
        if (current == '"') {
          items.add(item.toString());
          break;
        }
        if (current == '\\' && position < trimmed.length() - 1) {
          Character escaped = parseEscapedJsonCharacter(trimmed, position);
          if (escaped == null) {
            return List.of();
          }
          item.append(escaped);
          position += escapedCharacterLength(trimmed, position);
          continue;
        }
        item.append(current);
      }
      while (position < trimmed.length() - 1 && Character.isWhitespace(trimmed.charAt(position))) {
        position++;
      }
      if (position < trimmed.length() - 1) {
        if (trimmed.charAt(position) != ',') {
          return List.of();
        }
        position++;
      }
    }
    return items;
  }

  /**
   * 解析 JSON 字符串转义字符。
   *
   * @param value JSON 文本
   * @param position 反斜杠后的转义起始位置
   * @return 解析成功返回字符，否则返回 {@code null}
   */
  private Character parseEscapedJsonCharacter(String value, int position) {
    if (position >= value.length()) {
      return null;
    }
    char escaped = value.charAt(position);
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
    if (escaped != 'u' || position + 4 >= value.length()) {
      return null;
    }
    int codePoint = 0;
    for (int index = 1; index <= 4; index++) {
      char hex = value.charAt(position + index);
      Integer hexValue = parseHexValue(hex);
      if (hexValue == null) {
        return null;
      }
      codePoint = codePoint * 16 + hexValue;
    }
    return (char) codePoint;
  }

  /**
   * 返回 JSON 转义片段长度。
   *
   * @param value JSON 文本
   * @param position 反斜杠后的转义起始位置
   * @return 转义片段长度
   */
  private int escapedCharacterLength(String value, int position) {
    return position < value.length() && value.charAt(position) == 'u' ? 5 : 1;
  }

  /**
   * 解析十六进制字符。
   *
   * @param value 十六进制字符
   * @return 解析成功返回数值，否则返回 {@code null}
   */
  private Integer parseHexValue(char value) {
    if (value >= '0' && value <= '9') {
      return value - '0';
    }
    if (value >= 'a' && value <= 'f') {
      return value - 'a' + 10;
    }
    if (value >= 'A' && value <= 'F') {
      return value - 'A' + 10;
    }
    return null;
  }
}
