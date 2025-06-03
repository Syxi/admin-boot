package com.admin.common.exception;

import com.admin.common.result.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
    public ResultVO customHandlerException(CustomException e) {
        log.error("本地调用异常：", e.getCode() == null ? 500 : e.getCode(), e.getMessage(), e);
        return ResultVO.error(e.getMessage());
    }

}
