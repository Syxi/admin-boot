package com.admin.common.result;


import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 统一请求返回结果
 * @author suYan
 * @date 2023/4/1 15:52
 */
public class ResultVO<T> {

    /**
     * 请求状态码
     */
    @Schema(description = "请求状态码，200-正确，其它-错误")
    private String code;

    /**
     * 请求状态描述
     */
    @Schema(description = "请求状态描述")
    private String msg;

    /**
     * 响应数据，可以为空
     * "响应数据：成功时返回需要的数据，失败时返回详细原因或为null"
     */
    private T data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }


    public ResultVO(String code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
    /**
     * 请求成功，返回ResultVO，但data为空
     *
     * @param <T> 预期类型
     * @return 成功的空
     */
    public static <T> ResultVO<T> success() {
        return new ResultVO<>(ResultCode.SUCCESS.getCode(), "请求成功", null);
    }

    /**
     * 请求成功，返回ResultVO，有data字段
     *
     * @param <T> 数据类型
     * @param t   数据
     * @return 成功的数据
     */
    public static <T> ResultVO<T> success(T t) {
        return new ResultVO<>(ResultCode.SUCCESS.getCode(), "请求成功", t);
    }

    /**
     * 根据业务方需要，追加的请求成功，返回message，有data字段
     *
     * @param message 成功消息
     * @param <T>     预期类型
     * @return 成功的空
     */
    public static <T> ResultVO<T> success(String message, T data) {
        return new ResultVO<>(ResultCode.SUCCESS.getCode(), message, data);
    }


    /**
     * 自定义错误
     *
     * @param <T>  预期类型
     * @param code 错误代码
     * @param msg  错误详情
     * @return 错误的详情
     */
    public static <T> ResultVO<T> error(String code, String msg) {
        return new ResultVO<>(code, msg, null);
    }

    public static <T> ResultVO<T> error(String msg) {
        return new ResultVO<>(ResultCode.ERROR.getCode(), msg, null);
    }

    public static <T> ResultVO<T> error() {
        return new ResultVO<>(ResultCode.ERROR.getCode(), "操作失败", null);
    }

    public static <T> ResultVO<T> error(ResultCode resultCode) {
        return  new ResultVO<>(resultCode.getCode(), resultCode.getMsg(), null);
    }

    /**
     * boolean类型返回结果
     * @param result
     * @return
     * @param <T>
     */
    public static <T> ResultVO<T> judge(boolean result) {
        if (result) {
            return success();
        } else {
            return error();
        }
    }




}
