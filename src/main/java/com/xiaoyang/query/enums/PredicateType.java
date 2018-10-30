package com.xiaoyang.query.enums;

public enum PredicateType {
	EQ("eq"), LIKE("like"), IN("in"), NOT_IN("notIn"), GT("gt"), GOE("goe"), LT("lt"), LOE("loe"), BETWEEN("between"),NOT_EQ("ne");
	
	/**
	 * 方法名称
	 */
	private String functionName;
	/**
	 * 方法参数类型
	 */
	private Object[] paramTypes;
	
	private PredicateType(String functionName) {
		this.functionName = functionName;
	}

	public String getFunctionName() {
		return functionName;
	}

	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

	public Object[] getParamTypes() {
		return paramTypes;
	}

	public void setParamTypes(Object[] paramTypes) {
		this.paramTypes = paramTypes;
	}
	
	
	
	
}
