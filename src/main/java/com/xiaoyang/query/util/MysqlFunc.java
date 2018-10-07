package com.xiaoyang.query.util;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.DateTemplate;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.DateTimeTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.core.types.dsl.SimpleTemplate;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringTemplate;

/**
 * MYSQL函数
 * 
 * @author ZengFC
 *
 */
public class MysqlFunc {
	/**
	 * BIT_OR
	 * 
	 * @param column
	 * @return
	 */
	public static <T extends Number & Comparable<?>> NumberTemplate<T> bitOr(NumberExpression<T> column) {
		return Expressions.numberTemplate(column.getType(), "BIT_OR({0})", column);
	}

	/**
	 * BIT_OR
	 * 
	 * @param column
	 * @return
	 */
	public static <T extends Number & Comparable<?>> NumberTemplate<T> bitOr(NumberPath<T> column) {
		return Expressions.numberTemplate(column.getType(), "BIT_OR({0})", column);
	}

	/**
	 * and
	 * 
	 * @param column
	 * @param number
	 * @return
	 */
	public static <T extends Number & Comparable<?>> NumberTemplate<T> and(NumberExpression<T> column, T number) {
		return Expressions.numberTemplate(column.getType(), "({0} & {1})", column, number);
	}

	/**
	 * and
	 * 
	 * @param column
	 * @param number
	 * @return
	 */
	public static <T extends Number & Comparable<?>> NumberTemplate<T> and(NumberPath<T> column, T number) {
		return Expressions.numberTemplate(column.getType(), "({0} & {1})", column, number);
	}

	/**
	 * ROUND
	 * 
	 * @param column
	 * @param decimal
	 * @return
	 */
	public static <T extends Number & Comparable<?>> NumberTemplate<T> round(NumberExpression<T> column, int decimal) {
		return Expressions.numberTemplate(column.getType(), "ROUND({0},{1})", column, decimal);
	}

	/**
	 * ROUND
	 * 
	 * @param column
	 * @return
	 */
	public static <T extends Number & Comparable<?>> NumberTemplate<T> round(NumberExpression<T> column) {
		return Expressions.numberTemplate(column.getType(), "ROUND({0})", column);
	}

	/**
	 * IFNULL(v1, v2)
	 * 
	 * @param column
	 * @param valueIfNull
	 * @return
	 */
	public static <T extends Number & Comparable<?>> NumberTemplate<T> ifNull(NumberExpression<T> column,
			T valueIfNull) {
		return Expressions.numberTemplate(column.getType(), "IFNULL({0},{1})", column, valueIfNull);
	}
	
	public static <T extends Number & Comparable<?>> NumberTemplate<T> ifNull(NumberExpression<T> column,
			NumberExpression<T>  valueIfNull) {
		return Expressions.numberTemplate(column.getType(), "IFNULL({0},{1})", column, valueIfNull);
	}

	/**
	 * IFNULL(v1, v2)
	 * 
	 * @param column
	 * @param valueIfNull
	 * @return
	 */
	public static <T extends Number & Comparable<?>> NumberTemplate<T> ifNull(NumberPath<T> column, T valueIfNull) {
		return Expressions.numberTemplate(column.getType(), "IFNULL({0},{1})", column, valueIfNull);
	}

	

	/**
	 * IFNULL(v1, v2)
	 * 
	 * @param column
	 * @param valueIfNull
	 * @return
	 */
	public static StringTemplate ifnull(StringExpression column, String valueIfNull) {
		return Expressions.stringTemplate("IFNULL({0},{1})", column, valueIfNull);
	}

	

	/**
	 * IFNULL(v1, v2)
	 * 
	 * @param column
	 * @param valueIfNull
	 * @return
	 */
	public static <T extends Comparable<?>> DateTimeTemplate<T> ifnull(DateTimeExpression<T> column,
			String valueIfNull) {
		return Expressions.dateTimeTemplate(column.getType(), "IFNULL({0},{1})", column, valueIfNull);
	}

	/**
	 * IFNULL(v1, v2)
	 * 
	 * @param column
	 * @param valueIfNull
	 * @return
	 */
	public static <T> SimpleTemplate<T> ifnull(Expression<T> column, T valueIfNull) {
		return Expressions.template(column.getType(), "IFNULL({0},{1})", column, valueIfNull);
	}

	public static <T> SimpleTemplate<T> sum(Expression<T> column) {
		return Expressions.template(column.getType(), "SUM({0})", column);
	}

	/**
	 * 加日期
	 * 
	 * @param column
	 * @param value
	 * @param type
	 * @return
	 */
	public static <T extends Comparable<?>> DateTimeTemplate<T> dateAdd(DateTimeExpression<T> column, int value,
			DateAddType type) {
		return Expressions.dateTimeTemplate(column.getType(), "DATE_ADD({0},INTERVAL {1} " + type.toString() + " )",
				column, value);
	}

	/**
	 * 加日期
	 * 
	 * @param datetime
	 * @param value
	 * @param type
	 * @return
	 */
	public static <T extends Comparable<?>> DateTimeTemplate<LocalDateTime> dateAdd(LocalDateTime datetime, int value,
			DateAddType type) {
		return Expressions.dateTimeTemplate(LocalDateTime.class, "DATE_ADD({0},INTERVAL {1} " + type.toString() + " )",
				datetime, value);
	}

	/**
	 * 加日期
	 * 
	 * @param datetime
	 * @param value
	 * @param type
	 * @return
	 */
	public static <T extends Comparable<?>> DateTimeTemplate<LocalDateTime> dateAdd(LocalDateTime datetime,
			NumberExpression<?> value, DateAddType type) {
		return Expressions.dateTimeTemplate(LocalDateTime.class, "DATE_ADD({0},INTERVAL {1} " + type.toString() + " )",
				datetime, value);
	}

	/**
	 * 加日期
	 * 
	 * @param column
	 * @param value
	 * @param type
	 * @return
	 */
	public static <T extends Comparable<?>> DateTimeTemplate<T> dateAdd(DateTimeExpression<T> column,
			NumberExpression<?> value, DateAddType type) {
		return Expressions.dateTimeTemplate(column.getType(), "DATE_ADD({0},INTERVAL {1} " + type.toString() + " )",
				column, value);
	}

	/**
	 * 加日期
	 * 
	 * @param column
	 * @param value
	 * @param type
	 * @return
	 */
	public static <T extends Comparable<?>> DateTemplate<T> dateAdd(DateExpression<T> column, int value,
			DateAddType type) {
		return Expressions.dateTemplate(column.getType(), "DATE_ADD({0},INTERVAL {1} " + type.toString() + " )", column,
				value);
	}

	/**
	 * 加日期
	 * 
	 * @param date
	 * @param value
	 * @param type
	 * @return
	 */
	public static <T extends Comparable<?>> DateTemplate<LocalDate> dateAdd(LocalDate date, int value,
			DateAddType type) {
		return Expressions.dateTemplate(LocalDate.class, "DATE_ADD({0},INTERVAL {1} " + type.toString() + " )", date,
				value);
	}

	/**
	 * 加日期
	 * 
	 * @param date
	 * @param value
	 * @param type
	 * @return
	 */
	public static <T extends Comparable<?>> DateTemplate<LocalDate> dateAdd(LocalDate date, NumberExpression<?> value,
			DateAddType type) {
		return Expressions.dateTemplate(LocalDate.class, "DATE_ADD({0},INTERVAL {1} " + type.toString() + " )", date,
				value);
	}

	/**
	 * 加日期
	 * 
	 * @param column
	 * @param value
	 * @param type
	 * @return
	 */
	public static <T extends Comparable<?>> DateTemplate<T> dateAdd(DateExpression<T> column, NumberExpression<?> value,
			DateAddType type) {
		return Expressions.dateTemplate(column.getType(), "DATE_ADD({0},INTERVAL {1} " + type.toString() + " )", column,
				value);
	}

	/**
	 * DATE函数 datetime转date
	 * 
	 * @param column
	 * @return
	 */
	public static <T extends Comparable<?>> DateTemplate<T> date(DateTimeExpression<T> column) {
		return Expressions.dateTemplate(column.getType(), "DATE({0})", column);
	}

	/**
	 * DATE_FORMAT(column, format)
	 * 
	 * @param column
	 * @param format
	 * @return
	 */
	public static <T extends Comparable<?>> DateTimeTemplate<T> dateFormat(DateTimeExpression<T> column,
			String format) {
		return Expressions.dateTimeTemplate(column.getType(), "DATE_FORMAT({0},{1})", column,
				Expressions.constant(format));
	}

	/**
	 * DATE_FORMAT(column, format)
	 * 
	 * @param column
	 * @param format
	 * @return
	 */
	public static <T extends Comparable<?>> DateTemplate<T> dateFormat(DateExpression<T> column, String format) {
		return Expressions.dateTemplate(column.getType(), "DATE_FORMAT({0},{1})", column, Expressions.constant(format));
	}

	/**
	 * DATE_FORMAT(column, format)
	 * 
	 * @param column
	 * @param format
	 * @return
	 */
	public static <T extends Comparable<?>> StringTemplate dateFormatAsString(DateTimeExpression<T> column,
			String format) {
		return Expressions.stringTemplate("DATE_FORMAT({0},{1})", column, Expressions.constant(format));
	}

	public static <T extends Comparable<?>> StringTemplate dateFormatAsString(DateExpression<T> column, String format) {
		return Expressions.stringTemplate("DATE_FORMAT({0},{1})", column, Expressions.constant(format));
	}

	/**
	 * 赋值语句 := <br>
	 * 例如 varEq("@var", 35) 返回 @var := 25
	 * 
	 * @param v1
	 * @param v2
	 * @return v1 := v2
	 */
	public static <T extends Number & Comparable<?>> NumberTemplate<T> varEq(NumberExpression<T> v1,
			NumberExpression<T> v2) {
		return Expressions.numberTemplate(v1.getType(), String.format("%s := %s", v1, v2));
	}

	/**
	 * 赋值语句 := <br>
	 * 例如 varEq("@var", 35) 返回 @var := 25
	 * 
	 * @param v1
	 * @param v2
	 * @return v1 := v2
	 */
	public static <T extends Number & Comparable<?>> NumberTemplate<T> varEq(NumberExpression<T> v1, T v2) {
		return Expressions.numberTemplate(v1.getType(), String.format("%s := {0}", v1), v2);
	}

	
}
