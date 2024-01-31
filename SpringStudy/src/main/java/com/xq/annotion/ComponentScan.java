package com.xq.annotion;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.TYPE)
public @interface ComponentScan {
    String[] value() default {};
}
