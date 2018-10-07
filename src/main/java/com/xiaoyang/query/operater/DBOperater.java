package com.xiaoyang.query.operater;

import java.sql.Connection;

import javax.inject.Provider;
import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import com.querydsl.core.QueryFlag.Position;
import com.querydsl.core.types.Predicate;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.RelationalPathBase;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.types.EnumByOrdinalType;

/**
 * @author Zengfc
 *
 */
public class DBOperater extends SQLQueryFactory{
	
	private DataSource dataSource;


	@SuppressWarnings({ "unchecked", "rawtypes" })
	public DBOperater(Configuration configuration, Provider<Connection> connection, DataSource dataSource, Class<? extends Enum<?>>[] enums) {
		super(configuration, connection);
		 
		if (enums != null && enums.length > 0){
			for (Class<? extends Enum<?>> clazz : enums){
				configuration.register(new EnumByOrdinalType(clazz));
			}
		}
		
		/*
		configuration.register(new JSR310InstantType());
		configuration.register(new JSR310LocalDateTimeType());
		configuration.register(new JSR310LocalDateType());
		configuration.register(new JSR310LocalTimeType());*/
		this.dataSource = dataSource;
		
	}
	

	public <T> boolean exists(RelationalPathBase<?> beanMeta, Predicate... condition){
		return this.select(this.selectOne().from(beanMeta).where(condition).exists()).fetchOne();
	}

	public JdbcTemplate template(){
		return new JdbcTemplate(dataSource);
	}
	
	public boolean existsTable(String tableName){
		String table = this.template().queryForObject("SHOW TABLES LIKE '?';", new Object[]{tableName}, String.class);
		return table != null;
	}
	
	
	/**
     * Create a INSERT IGNORE INTO clause
     *
     * @param entity table to insert to
     * @return insert clause
     */
    public SQLInsertClause insertIgnore(RelationalPath<?> entity) {
        SQLInsertClause insert = insert(entity);
        insert.addFlag(Position.START_OVERRIDE, "insert ignore into ");
        return insert;
    }

    /**
     * Create a INSERT ... ON DUPLICATE KEY UPDATE clause
     *
     * @param entity table to insert to
     * @param clause clause
     * @return insert clause
     */
    public SQLInsertClause insertOnDuplicateKeyUpdate(RelationalPath<?> entity, String clause) {
        SQLInsertClause insert = insert(entity);
        insert.addFlag(Position.END, " on duplicate key update " + clause);
        return insert;
    }


	public DataSource getDataSource() {
		return dataSource;
	}


	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
    
    

}
