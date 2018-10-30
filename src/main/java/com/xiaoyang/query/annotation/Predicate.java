package com.xiaoyang.query.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.xiaoyang.query.enums.PredicateBorder;
import com.xiaoyang.query.enums.PredicateType;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Predicate {

	/**
	 * 对应的字段名称
	 * 
	 * @return
	 */
	String field() default "";

	/**
	 * 对比的策略
	 * 
	 * @return
	 */
	PredicateType type();

	/**
	 * 条件边界
	 * 
	 * @return
	 */
	PredicateBorder border() default PredicateBorder.DEFAULT;

}
