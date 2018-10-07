package com.xiaoyang.query;

import java.util.Set;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.querydsl.sql.Configuration;
import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.spring.SpringConnectionProvider;
import com.querydsl.sql.types.EnumByOrdinalType;
import com.xiaoyang.query.annotation.DBEnum;
import com.xiaoyang.query.config.DBProperties;
import com.xiaoyang.query.operater.DBOperater;
import com.xiaoyang.query.util.PackageUtil;

@Configurable
@EnableConfigurationProperties({ DBProperties.class }) // 指定类的配置
//@EnableTransactionManagement
public class AutoConfiguration {

	private Logger logger = LoggerFactory.getLogger(AutoConfiguration.class);

	@Autowired
	private DataSource dataSource;

	@Autowired
	private DBProperties dbProperties;

	/**
	 * 配置spring事务
	 * 
	 * @return
	 */

	
	// @Bean
	// public DataSourceTransactionManager dataSourceTransactionManager() {
	// return new DataSourceTransactionManager(dataSource);
	// }

	@SuppressWarnings("unchecked")
	@Bean
	public Configuration configuration() {
		Configuration configuration = new Configuration(new MySQLTemplates());
		// 获取定义的注解
		String enumPackages = dbProperties.getEnumPackages();
		if (enumPackages != null) {
			Set<Class<?>> classs = PackageUtil.findAnnotationClasss(enumPackages, DBEnum.class);
			for (Class<?> _class : classs) {
				if (_class.isEnum()) {
					DBEnum dbEnum = _class.getAnnotation(DBEnum.class);
					String[] tableNames = dbEnum.tableNames();
					String[] columns = dbEnum.columns();
					for (int i = 0; i < tableNames.length; i++) {
						String column = null;
						if (i + 1 > columns.length) {
							column = columns[columns.length - 1];
						} else {
							column = columns[i];
						}
						String table = tableNames[i];
						logger.info("register enum={},table={},column={}", _class.getSimpleName(),
								table + "--" + column);
						configuration.register(new EnumByOrdinalType((Class<? extends Enum<?>>) _class));
						configuration.register(table, column, _class);
					}
				}
			}
		}
		return configuration;
	}

	@Bean
	@ConditionalOnMissingBean(DBOperater.class) // 当容器中没有指定Bean的情况下
	public DBOperater dBOperater() {
		DBOperater dBOperater = new DBOperater(configuration(), new SpringConnectionProvider(dataSource), dataSource,
				null);
		return dBOperater;
	}
}
