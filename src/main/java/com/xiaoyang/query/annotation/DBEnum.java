package com.xiaoyang.query.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注册数据库中的字段为枚举类型
 * 
 * @author dongjiangzheng
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DBEnum {
	/**
	 * 对应数据库中的表名
	 * 
	 * @return
	 */
	String[] tableNames();

	/**
	 * 与table属性对应
	 * 
	 * @return
	 */
	String[] columns();
}
