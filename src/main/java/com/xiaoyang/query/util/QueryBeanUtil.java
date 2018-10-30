package com.xiaoyang.query.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;



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

	public static Method getDeclaredMethod(Object object, String methodName, Class<?>... parameterTypes) {
		Method method = null;
		for (Class<?> clazz = object.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
			try {
				method = clazz.getDeclaredMethod(methodName, parameterTypes);
				return method;
			} catch (Exception e) {
				// 这里甚么都不要做！并且这里的异常必须这样写，不能抛出去。
				// 如果这里的异常打印或者往外抛，则就不会执行clazz = clazz.getSuperclass(),最后就不会进入到父类中了
			}
		}
		return null;
	}

	public static Method getDeclaredMethod1(Object object, String methodName, Object... params) {
		for (Class<?> clazz = object.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
			try {
				Method[] methods = clazz.getDeclaredMethods();
				for (Method method : methods) {
					if (method.getName().equals(methodName)) {
						// 获取方法的参数列表
						Class<?>[] parameterTypes = method.getParameterTypes();
						if (parameterTypes == null || parameterTypes.length == 0
								|| parameterTypes.length != params.length) {
							continue;
						}
						for (int i = 0; i < parameterTypes.length; i++) {
							Class<?> _clazz = parameterTypes[i];
							if (!_clazz.isAssignableFrom(params.getClass())) {
								continue;
							}
						}
						return getDeclaredMethod(object, methodName, parameterTypes);
					}
				}
			} catch (Exception e) {
				// 这里甚么都不要做！并且这里的异常必须这样写，不能抛出去。
				// 如果这里的异常打印或者往外抛，则就不会执行clazz = clazz.getSuperclass(),最后就不会进入到父类中了
			}
		}
		return null;
	}

	/**
	 * 
	 * 重写jdk对是否是同一个方法判断，原因是jdk对是否是同一个方法判断太严格
	 * 
	 * @param a1
	 * @param a2
	 * @return
	 */
	private static boolean arrayContentsEq(Object[] a1, Object[] a2) {
		if (a1 == null) {
			return a2 == null || a2.length == 0;
		}

		if (a2 == null) {
			return a1.length == 0;
		}

		if (a1.length != a2.length) {
			return false;
		}

		for (int i = 0; i < a1.length; i++) {
			if (a1[i] != a2[i]) {
				return false;
			}
		}
		
		return true;
	}

}
