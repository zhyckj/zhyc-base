/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 可重复读取请求体的 HttpServletRequest 包装器。
 */
public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

  /** 缓存后的请求体字节。 */
  private final byte[] cachedBody;

  /**
   * 创建可重复读取请求体的请求包装器。
   *
   * @param request 原始 HTTP 请求
   * @throws IOException 读取原始请求体失败时抛出
   */
  public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
    super(request);
    this.cachedBody = request.getInputStream().readAllBytes();
  }

  /**
   * 返回 UTF-8 字符串请求体。
   *
   * @return 请求体字符串
   */
  public String getCachedBodyAsString() {
    return new String(cachedBody, resolveCharset());
  }

  /**
   * 获取可重复读取的请求体输入流。
   *
   * @return 基于缓存字节创建的新输入流
   */
  @Override
  public ServletInputStream getInputStream() {
    ByteArrayInputStream inputStream = new ByteArrayInputStream(cachedBody);
    return new ServletInputStream() {
      /**
       * 判断缓存请求体是否已读取完毕。
       *
       * @return 无剩余字节时返回 {@code true}
       */
      @Override
      public boolean isFinished() {
        return inputStream.available() == 0;
      }

      /**
       * 判断同步读取模式下输入流是否可读。
       *
       * @return 固定返回 {@code true}
       */
      @Override
      public boolean isReady() {
        return true;
      }

      /**
       * 设置异步读取监听器。
       *
       * @param readListener Servlet 异步读取监听器
       */
      @Override
      public void setReadListener(ReadListener readListener) {
        // 当前网关过滤器使用同步读取，请求体缓存不支持异步回调。
      }

      /**
       * 读取缓存请求体的下一个字节。
       *
       * @return 下一个字节，读取结束时返回 -1
       */
      @Override
      public int read() {
        return inputStream.read();
      }
    };
  }

  /**
   * 获取可重复读取的字符流。
   *
   * @return 使用请求字符集包装的 BufferedReader
   */
  @Override
  public BufferedReader getReader() {
    return new BufferedReader(new InputStreamReader(getInputStream(), resolveCharset()));
  }

  /**
   * 解析请求字符集，缺省使用 UTF-8。
   *
   * @return 请求字符集
   */
  private Charset resolveCharset() {
    String encoding = getCharacterEncoding();
    if (encoding == null || encoding.isBlank()) {
      return StandardCharsets.UTF_8;
    }
    return Charset.forName(encoding);
  }
}
