package com.admin.common.exception;

import com.admin.common.result.ResultVO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理
 * @author suYan
 * @date 2023/4/1 16:05
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义异常
     */
    @ExceptionHandler(CustomException.class)
    public ResultVO<?> customHandlerException(CustomException e) {
        log.error("自定义异常: code={}, message={}", e.getCode() == null ? 500 : e.getCode(), e.getMessage(), e);
        return ResultVO.error(e.getMessage());
    }

    /**
     * 处理参数校验异常 - @Valid 注解校验失败
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultVO<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", message);
        return ResultVO.error(message);
    }

    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultVO<?> handleBindException(BindException e) {
        String message = e.getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数绑定异常: {}", message);
        return ResultVO.error(message);
    }

    /**
     * 处理约束违反异常 - @Validated 注解校验失败
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultVO<?> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("约束校验失败: {}", message);
        return ResultVO.error(message);
    }

    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultVO<?> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String message = String.format("参数类型不匹配: %s 应该是 %s 类型", 
                e.getName(), e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "未知");
        log.warn(message);
        return ResultVO.error(message);
    }

    /**
     * 处理认证异常
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResultVO<?> handleAuthenticationException(AuthenticationException e) {
        log.warn("认证失败: {}", e.getMessage());
        return ResultVO.error("认证失败: " + e.getMessage());
    }

    /**
     * 处理错误凭证异常
     */
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResultVO<?> handleBadCredentialsException(BadCredentialsException e) {
        log.warn("用户名或密码错误");
        return ResultVO.error("用户名或密码错误");
    }

    /**
     * 处理访问拒绝异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResultVO<?> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("访问被拒绝: {}", e.getMessage());
        return ResultVO.error("您没有权限访问该资源");
    }

    /**
     * 处理请求方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ResultVO<?> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方法不支持: {}", e.getMessage());
        return ResultVO.error("不支持的请求方法: " + e.getMethod());
    }

    /**
     * 处理404异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResultVO<?> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn("请求路径不存在: {}", e.getRequestURL());
        return ResultVO.error("请求的资源不存在");
    }

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResultVO<?> handleNullPointerException(NullPointerException e) {
        log.error("空指针异常", e);
        return ResultVO.error("系统内部错误，请联系管理员");
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultVO<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("非法参数: {}", e.getMessage());
        return ResultVO.error(e.getMessage());
    }

    /**
     * 处理通用异常 - 兜底处理
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResultVO<?> handleException(Exception e) {
        log.error("系统异常", e);
        return ResultVO.error("系统繁忙，请稍后再试");
    }
}
