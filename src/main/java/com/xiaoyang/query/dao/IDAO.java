package com.xiaoyang.query.dao;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

import com.querydsl.core.NonUniqueResultException;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.RelationalPathBase;

public interface IDAO<M extends RelationalPathBase<E>, E, ID extends Comparable<? super ID> & Serializable> {
	/**
	 * 根据ID 获取域模型
	 * 
	 * @param id
	 *            主键ID
	 * @return
	 */
	E get(ID id);

	/**
	 * 根据ID 获取域模型的某个字段
	 * 
	 * @param id
	 *            主键ID
	 * @param column
	 *            字段
	 * @return
	 */
	<T> T get(ID id, Function<M, Expression<T>> column);

	/**
	 * 根据ID删除域模型
	 * 
	 * @param id
	 */
	void delete(ID id);

	/**
	 * 保存域模型
	 * 
	 * @param entity
	 * @return 主键ID
	 */
	ID save(E entity);

	/**
	 * 批量保存域模型
	 * 
	 * @param entitys
	 * @return
	 */
	List<? extends ID> batchSave(List<E> entitys);

	/**
	 * 更新域模型
	 * 
	 * @param entity
	 * @return 最终被修改到的(收影响的)记录数, 0表示没有任何数据被修改到
	 */
	long update(E entity);

	/**
	 * 更新域模型(忽略模型中值为null的字段)
	 * 
	 * @param entity
	 * @return 最终被修改到的(收影响的)记录数, 0表示没有任何数据被修改到
	 */
	long updateIgoreNullFields(E entity);

	/**
	 * 更新一条记录的一个字段
	 * 
	 * @param id
	 *            主键
	 * @param field
	 *            字段
	 * @param value
	 *            值
	 * @return 最终被修改到的(收影响的)记录数, 0表示没有任何数据被修改到
	 */
	<T> long update(ID id, Function<M, Path<T>> field, T value);

	/**
	 * 将符合条件condition的所有记录的field字段的值修改为value
	 * 
	 * @param condition
	 *            条件
	 * @param field
	 *            要修改的字段
	 * @param value
	 *            要修改的值
	 * @return 最终被修改到的(收影响的)记录数, 0表示没有任何数据被修改到
	 */
	<T> long update(Function<M, Predicate> condition, Function<M, Path<T>> field, T value);

	/**
	 * 将所有记录的field字段的值修改为value
	 * 
	 * @param field
	 *            要修改的字段
	 * @param value
	 *            要修改的值 @return, 0表示没有任何数据被修改到
	 */
	<T> long update(Function<M, Path<T>> field, T value);

	/**
	 * 根据id更新多个字段
	 * 
	 * @param id
	 *            id
	 * @param paths
	 * @param values
	 * @return
	 */
	long updateFields(ID id, Function<M, List<? extends Path<?>>> paths, List<?> values);
	
	
	long updateFields(Function<M, Predicate> condition, Function<M, List<? extends Path<?>>> paths, List<?> values);

	/**
	 * 更新指定主键对应记录的某个字段，更新的值使用表达式
	 * 
	 * @param id
	 *            主键ID值
	 * @param field
	 *            哪个字段
	 * @param expression
	 *            要更新的表达式
	 * @return 成功更新的记录数
	 */
	<T> long updateByExpression(ID id, Function<M, Path<T>> field, Function<M, Expression<? extends T>> expression);

	/**
	 * 更新指定条件对应记录的某个字段，更新的值使用表达式
	 * 
	 * @param condition
	 *            指定条件
	 * @param field
	 *            哪个字段
	 * @param expression
	 *            要更新的表达式
	 * @return 成功更新的记录数
	 */
	<T> long updateByExpression(Function<M, Predicate> condition, Function<M, Path<T>> field,
			Function<M, Expression<? extends T>> expression);

	/**
	 * 保存域模型, 但如果唯一索引冲突, 则什么也不干
	 * 
	 * @param entity
	 * @return 主键ID
	 */
	ID saveIgnoreDuplicate(E entity);

	/**
	 * 保存域模型, 但如果唯一索引冲突, 则只更新某个字段
	 * 
	 * @param entity
	 * @param columns
	 *            冲突时只更新哪些字段
	 * @return 主键ID
	 */
	ID saveOnDuplicateUpdate(E entity, Path<?>... columns);

	/**
	 * 将结果按照唯一结果取出, 如不存在,返回null <br>
	 * Get the projection as a unique result or null if no result is found
	 * 
	 * @param column
	 *            要取出的字段
	 * @param condition
	 *            条件
	 * @throws NonUniqueResultException
	 *             当结果不止一个时(not unique)抛出
	 * @return 唯一结果
	 */
	<T> T getUnique(Function<M, Predicate> condition, Function<M, Expression<T>> column);

	/**
	 * 将结果按照唯一结果取出, 如不存在,返回null <br>
	 * Get the projection as a unique result or null if no result is found
	 * 
	 * @param condition
	 *            条件
	 * @throws NonUniqueResultException
	 *             当结果不止一个时(not unique)抛出
	 * @return 唯一结果
	 */
	E getUnique(Function<M, Predicate> condition);

	/**
	 * 取出某个字段的一系列结果
	 * 
	 * @param field
	 * @return
	 */
	<T> List<T> getFieldList(Function<M, Expression<T>> field);

	/**
	 * 按条件, 取出某个字段的一系列结果
	 * 
	 * @param field
	 *            哪个字段
	 * @param condition
	 *            什么条件
	 * @return
	 */
	<T> List<T> getFieldList(Function<M, Expression<T>> field, Function<M, Predicate> condition);

	/**
	 * 按条件, 取出某个字段的一系列结果
	 * 
	 * @param field
	 *            哪个字段
	 * @param condition
	 *            什么条件
	 * @param offset
	 * @param limit
	 * @return
	 */
	<T> List<T> getFieldList(Function<M, Expression<T>> field, Function<M, Predicate> condition, long offset,
			long limit);

	/**
	 * 按条件, 取出某个字段的一系列结果
	 * 
	 * @param field
	 *            哪个字段
	 * @param condition
	 *            什么条件
	 * @param offset
	 * @param limit
	 * @param orderBy
	 *            按什么排序
	 * @return
	 */
	<T> List<T> getFieldList(Function<M, Expression<T>> field, Function<M, Predicate> condition, long offset,
			long limit, Function<M, OrderSpecifier<?>> orderBy);

	/**
	 * 按条件, 取出某个字段的一系列结果
	 * 
	 * @param field
	 *            字段
	 * @param condition
	 *            条件
	 * @param orderBy
	 *            排序依据
	 * @return
	 */
	<T> List<T> getFieldList(Function<M, Expression<T>> field, Function<M, Predicate> condition,
			Function<M, OrderSpecifier<?>> orderBy);

	/**
	 * 取出所有记录(当记录数过多时,慎用)
	 * 
	 * @return
	 */
	List<E> getAll();

	/**
	 * 取出所有记录(当记录数过多时,慎用)
	 * 
	 * @return
	 */
	<T> List<T> getAll(Function<M, Expression<T>> field);

	/**
	 * 按条件, 取出一系列结果
	 * 
	 * @param condition
	 *            条件
	 * @return
	 */
	List<E> getList(Function<M, Predicate> condition);

	/**
	 * 按条件, 取出一系列结果
	 * 
	 * @param condition
	 *            条件
	 * @param offset
	 * @param limit
	 * @return
	 */
	List<E> getList(Function<M, Predicate> condition, long offset, long limit);

	/**
	 * 按条件, 取出一系列结果
	 * 
	 * @param condition
	 *            条件
	 * @param orderBy
	 *            排序依据
	 * @return
	 */
	List<E> getList(Function<M, Predicate> condition, Function<M, OrderSpecifier<?>> orderBy);

	/**
	 * 按条件, 取出一系列结果
	 * 
	 * @param condition
	 *            条件
	 * @param offset
	 * @param limit
	 * @param orderBy
	 *            排序依据
	 * @return
	 */
	List<E> getList(Function<M, Predicate> condition, long offset, long limit, Function<M, OrderSpecifier<?>> orderBy);

	/**
	 * 查询符合条件的结果数量
	 * 
	 * @param condition
	 *            条件
	 * @return
	 */
	long getCount(Function<M, Predicate> condition);

	/**
	 * 对某个字段求和, 对应SQL为select sum(X) from ...
	 * 
	 * @param field
	 *            要求和的字段
	 * @param condition
	 *            条件
	 * @param <T>
	 *            字段类型
	 * @return 求和结果
	 */
	<T extends Number & Comparable<?>> T getSum(Function<M, NumberPath<T>> field, Function<M, Predicate> condition);

	/**
	 * 对某个字段求和, 对应SQL为select sum(X) from ...
	 * 
	 * @param field
	 *            要求和的字段
	 * @param <T>
	 *            字段类型
	 * @return 求和结果
	 */
	<T extends Number & Comparable<?>> T getSum(Function<M, NumberPath<T>> field);

	/**
	 * 对某个字段求平均数, 对应SQL为select avg(X) from ...
	 * 
	 * @param field
	 *            要求平均数的字段
	 * @param condition
	 *            条件
	 * @param <T>
	 *            字段类型
	 * @return 求平均数结果
	 */
	<T extends Number & Comparable<?>> Double getAvg(Function<M, NumberPath<T>> field,
			Function<M, Predicate> condition);

	/**
	 * 对某个字段求平均数, 对应SQL为select avg(X) from ...
	 * 
	 * @param field
	 *            要求平均数的字段
	 * @param <T>
	 *            字段类型
	 * @return 求平均数结果
	 */
	<T extends Number & Comparable<?>> Double getAvg(Function<M, NumberPath<T>> field);

	/**
	 * 对某个字段求最大值, 对应SQL为select max(X) from ...
	 * 
	 * @param field
	 *            要求最大值的字段,即X
	 * @param condition
	 *            条件
	 * @param <T>
	 *            字段类型
	 * @return 最大值
	 */
	<T extends Number & Comparable<?>> T getMax(Function<M, NumberPath<T>> field, Function<M, Predicate> condition);

	/**
	 * 对某个字段求最大值, 对应SQL为select max(X) from ...
	 * 
	 * @param field
	 *            要求最大值的字段,即X
	 * @param <T>
	 *            字段类型
	 * @return 最大值
	 */
	<T extends Number & Comparable<?>> T getMax(Function<M, NumberPath<T>> field);

	/**
	 * 对某个字段求最小值, 对应SQL为select min(X) from ...
	 * 
	 * @param field
	 *            要求最小值的字段,即X
	 * @param condition
	 *            条件
	 * @param <T>
	 *            字段类型
	 * @return 最小值
	 */
	<T extends Number & Comparable<?>> T getMin(Function<M, NumberPath<T>> field, Function<M, Predicate> condition);

	/**
	 * 对某个字段求最小值, 对应SQL为select min(X) from ...
	 * 
	 * @param field
	 *            要求最小值的字段,即X
	 * @param <T>
	 *            字段类型
	 * @return 最小值
	 */
	<T extends Number & Comparable<?>> T getMin(Function<M, NumberPath<T>> field);

	/**
	 * 获取满足条件的最后一条记录(按主键排序)
	 * 
	 * @param condition
	 *            条件
	 * @return
	 */
	E getLast(Function<M, Predicate> condition);

	/**
	 * 获取满足条件的最后一条记录(按orderBy排序)
	 * 
	 * @param condition
	 *            条件
	 * @param orderBy
	 *            排序条件
	 * @return
	 */
	E getLast(Function<M, Predicate> condition, Function<M, ComparableExpressionBase<?>> orderBy);

	/**
	 * 获取满足条件的第一条记录(按主键排序)
	 * 
	 * @param condition
	 *            条件
	 * @return
	 */
	E getFirst(Function<M, Predicate> condition);

	/**
	 * 获取满足条件的第一条记录(按orderBy排序)
	 * 
	 * @param condition
	 *            条件
	 * @param orderBy
	 *            排序依据
	 * @return
	 */
	E getFirst(Function<M, Predicate> condition, Function<M, ComparableExpressionBase<?>> orderBy);

	/**
	 * 按条件, 取出一系列结果
	 * 
	 * @param offset
	 * @param limit
	 * @param orderBy
	 *            排序依据
	 * @return
	 */
	List<E> getList(long offset, long limit, Function<M, OrderSpecifier<?>> orderBy);

	/**
	 * 按条件, 删除一系列结果
	 * 
	 * @param condition
	 * @return
	 */
	long delete(Function<M, Predicate> condition);

	/**
	 * 按条件赛选的结果是否存在
	 * 
	 * @param condition
	 * @return
	 */
	boolean exists(Function<M, Predicate> condition);

	/**
	 * 按条件, 取出某个字段的一系列结果
	 * 
	 * @param field
	 *            哪个字段
	 * @param condition
	 *            什么条件
	 * @return
	 */
	<T> List<T> getDistinctList(Function<M, Expression<T>> field, Function<M, Predicate> condition);

}
