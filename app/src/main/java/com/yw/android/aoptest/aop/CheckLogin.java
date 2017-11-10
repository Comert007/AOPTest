package com.yw.android.aoptest.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by feng on 2017/10/9.
 */

@Retention(RetentionPolicy.RUNTIME) //保留到源码中，同时也保留到class中，最后加载到虚拟机中
@Target({ElementType.METHOD,ElementType.CONSTRUCTOR}) //可以注解在方法或构造上
public @interface CheckLogin {
}
