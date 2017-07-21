package com.cxytiandi.jdbc.annotation;

import java.lang.annotation.*;
/**
 * 表名
 * @author yinjihuan
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TableName {
	
	/**
	 * 表名
	 * @return
	 */
	String value();
	
	/**
	 * 表描述
	 * @return
	 */
	String desc();
	
	/**
	 * 作者
	 * @return
	 */
	String author();
}
