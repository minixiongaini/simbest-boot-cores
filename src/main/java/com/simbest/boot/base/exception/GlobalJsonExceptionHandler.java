/**
 * 版权所有 © 北京晟壁科技有限公司 2017-2027。保留一切权利!
 */
package com.simbest.boot.base.exception;


import com.simbest.boot.base.web.response.JsonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * 用途：全局异常处理
 * 作者: lishuyi
 * 时间: 2017/11/4  15:34
 */
@Slf4j
@RestControllerAdvice
public class GlobalJsonExceptionHandler {

    /**
     * @param req Http请求
     * @param e   JsonException
     * @return JsonResponse
     */
    @ExceptionHandler(value = Exception.class)
    public JsonResponse handleErrorException(HttpServletRequest req, Exception e) {
        return handleException(req, e);
    }

    /**
     * @param req Http请求
     * @param e   AccessDeniedException
     * @return JsonResponse
     */
    @ExceptionHandler(value = RuntimeException.class)
    public JsonResponse handleRuntimeException(HttpServletRequest req, RuntimeException e) {
        return handleException(req, e);
    }

    /**
     * @param req Http请求
     * @param e   Exception
     * @return JsonResponse
     */
    private JsonResponse handleException(HttpServletRequest req, Exception e) {
        log.warn(req.getRequestURL().toString());
        Exceptions.printException(e);
        return GlobalExceptionRegister.returnErrorResponse(e);
    }
}