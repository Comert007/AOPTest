package com.yw.android.aoptest.aop;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.yw.android.aoptest.BaseApplication;
import com.yw.android.aoptest.LoginActivity;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Created by feng on 2017/10/9.
 */

@Aspect
public class CheckLoginAspectJ {
    private static final String TAG = "CheckLogin";

    /**
     * 找到处理的切点
     * * *(..)  可以处理CheckLogin这个类所有的方法
     */
    @Pointcut("execution(@com.yw.android.aoptest.aop.CheckLogin  * *(..))")
    public void executionCheckLogin() {

    }

    /**
     * 处理切面
     *
     * @param joinPoint
     * @return
     */
    @Around("executionCheckLogin()")
    public Object checkLogin(ProceedingJoinPoint joinPoint) throws Throwable {
        Log.i(TAG, "checkLogin: ");
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        CheckLogin checkLogin = signature.getMethod().getAnnotation(CheckLogin.class);
        if (checkLogin != null) {
            Context context = (Context) joinPoint.getThis();
            if (BaseApplication.isLogin) {
                Log.i(TAG, "checkLogin: 登录成功 ");
                return joinPoint.proceed();
            } else {
                Log.i(TAG, "checkLogin: 请登录");
                Toast.makeText(context, "请登录", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, LoginActivity.class);
                context.startActivity(intent);
                return null;
            }
        }
        return joinPoint.proceed();
    }


}
