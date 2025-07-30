package com.hienao.openlist2strm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一API响应格式 所有接口都应该返回这种格式的响应
 *
 * @param <T> 数据类型，根据具体接口的返回数据定义
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

  /** 响应状态码 200: 成功 其他: 失败 */
  private int code;

  /** 响应消息 */
  private String message;

  /** 响应数据，类型根据具体接口定义 */
  private T data;

  /**
   * 成功响应
   *
   * @param data 响应数据
   * @param <T> 数据类型
   * @return ApiResponse
   */
  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(200, "success", data);
  }

  /**
   * 成功响应（带自定义消息）
   *
   * @param data 响应数据
   * @param message 自定义消息
   * @param <T> 数据类型
   * @return ApiResponse
   */
  public static <T> ApiResponse<T> success(T data, String message) {
    return new ApiResponse<>(200, message, data);
  }

  /**
   * 失败响应
   *
   * @param code 错误码
   * @param message 错误消息
   * @param <T> 数据类型
   * @return ApiResponse
   */
  public static <T> ApiResponse<T> error(int code, String message) {
    return new ApiResponse<>(code, message, null);
  }

  /**
   * 失败响应（默认500错误码）
   *
   * @param message 错误消息
   * @param <T> 数据类型
   * @return ApiResponse
   */
  public static <T> ApiResponse<T> error(String message) {
    return new ApiResponse<>(500, message, null);
  }
}
