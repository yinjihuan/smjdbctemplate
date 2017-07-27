package com.cxytiandi.jdbc.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * 标示此字段为主键，需要动态生成分布式主键ID<br>
 * 如果要用数据库默认的自增就不要加此注解<br>
 * 加了此注解，字段必须是String或者Long
 * @author yinjihuan
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE })
public @interface AutoId {
	
}
