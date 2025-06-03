package com.admin.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.admin.common.result.ResultCode;
import com.admin.common.result.ResultVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

@Slf4j
public class ResponseUtil {


    /**
     * 异常消息返回
     * @param response
     * @param resultCode
     * @throws IOException
     */
    public static void writeErrorResponse(HttpServletResponse response, ResultCode resultCode) throws IOException {
        int code = switch (resultCode) {
            case TOKEN_INVALID, REFRESH_TOKEN_INVALID -> HttpStatus.UNAUTHORIZED.value();
            default -> HttpStatus.BAD_REQUEST.value();
        };

        response.setStatus(code);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        try (PrintWriter writer = response.getWriter()) {
            ResultVO resultVO = ResultVO.error(resultCode);
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResponse = objectMapper.writeValueAsString(resultVO);
            writer.write(jsonResponse);
            writer.flush();
        } catch (IOException e) {
            log.error("响应异常处理失败", e);
        }
    }
}
