package com.xiaoyang.query.util;

import java.lang.reflect.Field;

public class QueryBeanUtil {

	/**
	 * 获取object中字段名为fieldName的值
	 * 
	 * @param fieldName
	 * @param object
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public static Object getFieldValueByClasss(String fieldName, Object object)
			throws IllegalArgumentException, IllegalAccessException {
		Field field = null;
		Class<?> clazz = object.getClass();
		for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
			try {
				if (field != null) {
					break;
				}
				field = clazz.getDeclaredField(fieldName);
			} catch (Exception e) {
				e.printStackTrace();
				// 这里甚么都不能抛出去。

			}
		}
		if (field != null) {
			field.setAccessible(true);
			return field.get(object);
		}
		return null;
	}
}
