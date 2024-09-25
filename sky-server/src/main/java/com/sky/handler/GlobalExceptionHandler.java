package com.sky.handler;

import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex) {
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());

    }
    @ExceptionHandler
    public Result exceptionHandler(Exception ex) {
        //Duplicate entry 'lisi' for key 'employee.idx_username'
        if (ex.getMessage().contains("Duplicate entry")) {
            String msg = ex.getMessage().split(" ")[2];
            return Result.error(msg + "已存在");
        }
        else return Result.error("未知错误");
    }


    }
