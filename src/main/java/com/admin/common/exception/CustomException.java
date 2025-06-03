package com.admin.common.exception;

import com.admin.common.result.ResultCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * 自定义异常
 * @author suYan
 * @date 2023/4/1 14:40
 */
@Slf4j
@Getter
public class CustomException extends RuntimeException {

    public ResultCode code;


    public CustomException(String message) {
        super(message);
    }



    public CustomException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode;
    }



    @Override
    public String getMessage() {
        String message = super.getMessage();
        log.error(message);
        return StringUtils.isBlank(message) ? "服务器出错" : super.getMessage();
    }


}
