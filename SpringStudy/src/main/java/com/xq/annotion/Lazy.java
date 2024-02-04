package com.xq.annotion;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.TYPE)
public @interface Lazy {
    boolean value() default false;
}
