package com.xiaoyang.query.generate;

import org.apache.commons.lang3.StringUtils;

import com.querydsl.sql.codegen.DefaultNamingStrategy;

public class CustomImprovedNamingStrategy extends DefaultNamingStrategy {

	private String removeTablePrefix;

	public CustomImprovedNamingStrategy(String removeTablePrefix) {
		this.removeTablePrefix = removeTablePrefix;
	}

	@Override
	public String getClassName(String tableName) {
		if (StringUtils.isNotBlank(removeTablePrefix)) {
			tableName = tableName.replaceFirst(removeTablePrefix, "");
		}
		return super.getClassName(tableName);
	}
	
	
	
	
	
}
