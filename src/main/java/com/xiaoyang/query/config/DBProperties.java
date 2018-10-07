package com.xiaoyang.query.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "db")
public class DBProperties {

	private String enumPackages;

	public String getEnumPackages() {
		return enumPackages;
	}

	public void setEnumPackages(String enumPackages) {
		this.enumPackages = enumPackages;
	}

}
