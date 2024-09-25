package com.sky.aspect;

import com.sky.anno.AutoFill;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Component
@Slf4j
@Aspect
public class AutoFillAspect {


    @Before("@annotation(com.sky.anno.AutoFill)")
    public void autoFill(JoinPoint joinPoint) {

        log.info("开始进行公共字段自动填充");
        //获取当前被拦截的方法上的注解对象

        AutoFill autoFill = joinPoint.getTarget().getClass().getAnnotation(AutoFill.class);
        OperationType value = autoFill.value();
        //获取当前被拦截的方法的参数即实体类对象
        Object entity = joinPoint.getArgs()[0];


        //无法直接调用set方法所以用反射
        if(value.equals(OperationType.INSERT)){
            try {

                Method setUpdateTime = entity.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod("setUpdateUser", Long.class);
                Method setCreateTime = entity.getClass().getDeclaredMethod("setCreateTime", LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod("setCreateUser", Long.class);

                setUpdateTime.invoke(entity, LocalDateTime.now());
                setUpdateUser.invoke(entity, BaseContext.getCurrentId());
                setCreateTime.invoke(entity, LocalDateTime.now());
                setCreateUser.invoke(entity, BaseContext.getCurrentId());

            } catch (Exception e) {
                throw new RuntimeException(e);
            }


        }else if(value.equals(OperationType.UPDATE)){

            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod("setUpdateUser", Long.class);
                setUpdateTime.invoke(entity, LocalDateTime.now());
                setUpdateUser.invoke(entity, BaseContext.getCurrentId());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }


        }




    }
}
