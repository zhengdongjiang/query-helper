package com.xiaoyang.query.dao.impl;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.querydsl.core.QueryFlag.Position;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.RelationalPathBase;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.dml.DefaultMapper;
import com.querydsl.sql.dml.SQLInsertClause;
import com.xiaoyang.query.dao.IDAO;
import com.xiaoyang.query.enums.PredicateBorder;
import com.xiaoyang.query.enums.PredicateType;
import com.xiaoyang.query.operater.DBOperater;
import com.xiaoyang.query.util.QueryBeanUtil;

/**
 * 基本DAO的抽象实现
 * 
 * @author ZengFC
 *
 * @param <M>
 *            域模型的元信息, 如QMember
 * @param <E>
 *            域模型
 * @param <ID>
 *            域模型的主键类型
 */
public abstract class AbstractDao<M extends RelationalPathBase<E>, E, ID extends Comparable<? super ID> & Serializable>
		implements IDAO<M, E, ID> {
	@Autowired
	protected DBOperater db;
	protected M meta;
	protected ComparableExpressionBase<ID> primary;

	protected <T> QBean<T> reconstruct(Class<? extends T> type, Expression<?>... exprs) {
		return reconstruct(type, meta, exprs);
	}

	protected <T, N> QBean<T> reconstruct(Class<? extends T> type, RelationalPathBase<N> path, Expression<?>... exprs) {
		Field[] fields = path.getClass().getFields();
		List<Expression<?>> propsList = Stream.of(fields)
				.filter(f -> Stream.of(f.getType().getInterfaces()).anyMatch(t -> t == Path.class)).map(f -> {
					try {
						return (Expression<?>) f.get(path);
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}).collect(Collectors.toList());
		if (exprs != null && exprs.length > 0) {
			propsList.addAll(Arrays.asList(exprs));
		}

		return Projections.bean(type, propsList.stream().toArray(Expression[]::new));

	}

	/**
	 * 解析查询参数，自动生成查询条件
	 * 
	 * @param query
	 * @return
	 */
	protected List<Predicate> parseQueryParams(Object query) {
		Class<?> clazz = query.getClass();
		List<Predicate> predicates = new ArrayList<>();
		for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				// predicates.add(e)
				com.xiaoyang.query.annotation.Predicate[] annotations = field
						.getAnnotationsByType(com.xiaoyang.query.annotation.Predicate.class);
				if (ArrayUtils.isNotEmpty(annotations)) {
					if (annotations.length > 1) {
						throw new RuntimeException("Predicate 注解在同一个属性上含有多个");
					}
					field.setAccessible(true);
					Object fieldValue = null;
					try {
						fieldValue = field.get(query);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
					com.xiaoyang.query.annotation.Predicate predicate = annotations[0];
					/*
					 * 1.解析注解上的字段，如果没指定映射的数据库字段，则默认使用类字段名称 2.字段默认匹配方式，数组类和集合类默认使用in ，其他使用eq
					 */
					String filedName = predicate.field();
					if (StringUtils.isBlank(filedName)) {
						filedName = field.getName();
					}
					PredicateType predicateType = predicate.type();
					if (predicateType == null) {
						if (field.getClass().isArray() || field.getGenericType() instanceof List) {
							predicateType = PredicateType.IN;
						} else {
							predicateType = PredicateType.EQ;
						}
					}

					/*
					 * 判断值是否在边界，默认集合数组非空，字符串非bank，数字类型大于0
					 * 
					 */

					if (fieldValue == null) {
						continue;
					}

					if (field.getClass().isArray()) {
						if (Array.getLength(fieldValue) == 0) {
							continue;
						}
					} else if (field.getGenericType() instanceof List) {
						if (((List<?>) fieldValue).isEmpty()) {
							continue;
						}
					} else if (field.getType().isAssignableFrom(String.class)) {
						if (StringUtils.isBlank((String) fieldValue)) {
							continue;
						}
					} else if (field.getType().isAssignableFrom(Number.class)) {
						if (predicate.border() == null) {
							if (((Number) fieldValue).longValue() <= 0) {
								continue;
							}
						} else if (predicate.border() == PredicateBorder.ZERO) {
							if (((Number) fieldValue).longValue() < 0) {
								continue;
							}
						}
					}

					// SimpleExpression<?> simpleExpression = null;
					Object simpleExpression = null;
					// 生成条件
					try {
						simpleExpression = QueryBeanUtil.getFieldValueByClasss(filedName, meta);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
					try {

						Method method = QueryBeanUtil.getDeclaredMethod1(simpleExpression,
								predicateType.getFunctionName(), fieldValue);
						method.setAccessible(true);
						predicates.add((Predicate) method.invoke(simpleExpression, fieldValue));
					} catch (Exception e) {
						throw new RuntimeException(e);
					}

				}
			}
		}
		return predicates;
	}

	/**
	 * 将字符串转化为排序字段，_开头表示升序排列，否则降序排列
	 * 
	 * @param orderByFileds
	 *            排序字段
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public OrderSpecifier<?>[] orderBy(String[] orderByFileds) {
		if (ArrayUtils.isNotEmpty(orderByFileds)) {
			OrderSpecifier<?>[] orders = new OrderSpecifier<?>[orderByFileds.length];
			for (int i = 0; i < orderByFileds.length; i++) {
				String filed = orderByFileds[i];
				try {
					orders[i] = new OrderSpecifier<>(filed.startsWith("_") ? Order.ASC : Order.DESC,
							(Path) QueryBeanUtil.getFieldValueByClasss(filed.replaceAll("^_", ""), meta));
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
			return orders;
		}
		return null;
	}
	
	

	public OrderSpecifier<?>[] orderBy(String[] orderByFileds, String defaultOrderBy) {
		if (ArrayUtils.isEmpty(orderByFileds)) {
			return orderBy(new String[] { defaultOrderBy });
		} else {
			return orderBy(orderByFileds);
		}
	}

	public E get(ID id) {
		return db.selectFrom(meta).where(primary.eq(id)).fetchOne();
	}

	public <T> T get(ID id, Class<T> clazzWrapper) {
		return db.select(Projections.bean(clazzWrapper)).from(meta).where(primary.eq(id)).fetchOne();
	}

	@SuppressWarnings("unchecked")
	public AbstractDao() {
		super();

		// 先找到泛型参数的第一个
		Class<?> clazz = getClass();
		Type type = clazz.getGenericSuperclass();
		while (type != null && !(type instanceof ParameterizedType)) {
			clazz = getClass().getSuperclass();
			type = clazz.getGenericSuperclass();
		}

		Type types[] = ((ParameterizedType) type).getActualTypeArguments();

		// metaClass是QMember;
		Class<M> metaClazz = (Class<M>) types[0];

		// 找QMember.member那个静态static final属性, 并获取
		Field[] fields = metaClazz.getDeclaredFields();
		for (Field f : fields) {
			if (Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())
					&& Modifier.isPublic(f.getModifiers())) {
				try {
					meta = (M) f.get(null);
				} catch (Exception e) {
					throw new IllegalArgumentException(e);
				}
			}
		}

		primary = (ComparableExpressionBase<ID>) meta.getPrimaryKey().getLocalColumns().get(0);
	}

	@Override
	public void delete(ID id) {
		db.delete(meta).where(primary.eq(id)).execute();
	}

	@Override
	public ID save(E entity) {
		return db.insert(meta).populate(entity).executeWithKey(primary.getType());
	}

	@Override
	public List<? extends ID> batchSave(List<E> entitys) {
		SQLInsertClause insert = db.insert(meta);
		for (E entity : entitys) {
			insert.populate(entity).addBatch();
		}

		return insert.executeWithKeys(primary.getType());
	}

	@Override
	public ID saveIgnoreDuplicate(E entity) {
		return saveOnDuplicateUpdate(entity);
	}

	@Override
	public long updateIgoreNullFields(E entity) {
		return update(entity, DefaultMapper.DEFAULT);
	}

	@SuppressWarnings("unchecked")
	private long update(E entity, DefaultMapper mapper) {
		ID id;
		try {
			String field = primary.toString().split("\\.")[1];
			id = (ID) BeanUtils.getProperty(entity, field);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
		return db.update(meta).populate(entity, mapper).where(primary.eq(id)).execute();
	}

	@Override
	public long update(E entity) {
		return update(entity, DefaultMapper.WITH_NULL_BINDINGS);
	}

	@Override
	public <T> long update(ID id, Function<M, Path<T>> field, T value) {
		return db.update(meta).set(field.apply(meta), value).where(primary.eq(id)).execute();
	}

	@Override
	public long updateFields(ID id, Function<M, List<? extends Path<?>>> paths, List<?> values) {
		return db.update(meta).set(paths.apply(meta), values).where(primary.eq(id)).execute();
	}

	@Override
	public long updateFields(Function<M, Predicate> condition, Function<M, List<? extends Path<?>>> paths,
			List<?> values) {
		return db.update(meta).set(paths.apply(meta), values).where(condition.apply(meta)).execute();
	}

	@Override
	public <T> long updateByExpression(ID id, Function<M, Path<T>> field,
			Function<M, Expression<? extends T>> expression) {
		return db.update(meta).set(field.apply(meta), expression.apply(meta)).where(primary.eq(id)).execute();
	}

	@Override
	public <T> long update(Function<M, Path<T>> field, T value) {
		return db.update(meta).set(field.apply(meta), value).execute();
	}

	@Override
	public <T> long update(Function<M, Predicate> condition, Function<M, Path<T>> field, T value) {
		return db.update(meta).set(field.apply(meta), value).where(condition.apply(meta)).execute();
	}

	@Override
	public <T> long updateByExpression(Function<M, Predicate> condition, Function<M, Path<T>> field,
			Function<M, Expression<? extends T>> expression) {
		return db.update(meta).set(field.apply(meta), expression.apply(meta)).where(condition.apply(meta)).execute();
	}

	@Override
	@SuppressWarnings("unchecked")
	public ID saveOnDuplicateUpdate(E entity, Path<?>... column) {
		Map<Path<?>, Object> values = DefaultMapper.DEFAULT.createMap(meta, entity);
		SQLInsertClause insert = db.insert(meta);
		StringBuilder sb = new StringBuilder(" on duplicate key update {0}");
		List<Object> args = new ArrayList<>();
		args.add(primary.eq(primary));
		if (column != null && column.length > 0) {
			int i = 1;
			for (@SuppressWarnings("rawtypes")
			Path c : column) {
				if (primary.equals(c)) {
					continue;
				}
				sb.append(", {").append(i++).append("}");
				Object value = values.get(c);
				args.add(SQLExpressions.set(c, value));
			}
		}
		insert.addFlag(Position.END, ExpressionUtils.template(String.class, sb.toString(), args.toArray()));
		return insert.populate(entity).executeWithKey(primary.getType());
	}

	@Override
	public <T> T get(ID id, Function<M, Expression<T>> field) {
		return db.select(field.apply(meta)).from(meta).where(primary.eq(id)).fetchOne();
	}

	@Override
	public <T> T getUnique(Function<M, Predicate> condition, Function<M, Expression<T>> field) {
		return db.select(field.apply(meta)).from(meta).where(condition.apply(meta)).fetchOne();
	}

	@Override
	public E getUnique(Function<M, Predicate> condition) {
		return db.selectFrom(meta).where(condition.apply(meta)).fetchOne();
	}

	@Override
	public E getLast(Function<M, Predicate> condition) {
		return db.selectFrom(meta).where(condition.apply(meta)).orderBy(primary.desc()).fetchFirst();
	}

	@Override
	public E getLast(Function<M, Predicate> condition, Function<M, ComparableExpressionBase<?>> orderBy) {
		return db.selectFrom(meta).where(condition.apply(meta)).orderBy(orderBy.apply(meta).desc()).fetchFirst();
	}

	@Override
	public E getFirst(Function<M, Predicate> condition) {
		return db.selectFrom(meta).where(condition.apply(meta)).fetchFirst();
	}

	@Override
	public E getFirst(Function<M, Predicate> condition, Function<M, ComparableExpressionBase<?>> orderBy) {
		return db.selectFrom(meta).where(condition.apply(meta)).orderBy(orderBy.apply(meta).asc()).fetchFirst();
	}

	public <T> List<T> getFieldList(Function<M, Expression<T>> field) {
		return db.select(field.apply(meta)).from(meta).fetch();
	}

	@Override
	public <T> List<T> getFieldList(Function<M, Expression<T>> field, Function<M, Predicate> condition) {
		return db.select(field.apply(meta)).from(meta).where(condition.apply(meta)).fetch();
	}

	@Override
	public <T> List<T> getFieldList(Function<M, Expression<T>> field, Function<M, Predicate> condition, long offset,
			long limit) {
		return db.select(field.apply(meta)).from(meta).where(condition.apply(meta)).offset(offset).limit(limit).fetch();
	}

	@Override
	public <T> List<T> getFieldList(Function<M, Expression<T>> field, Function<M, Predicate> condition, long offset,
			long limit, Function<M, OrderSpecifier<?>> orderBy) {
		return db.select(field.apply(meta)).from(meta).where(condition.apply(meta)).orderBy(orderBy.apply(meta))
				.offset(offset).limit(limit).fetch();
	}

	@Override
	public <T> List<T> getFieldList(Function<M, Expression<T>> field, Function<M, Predicate> condition,
			Function<M, OrderSpecifier<?>> orderBy) {
		return db.select(field.apply(meta)).from(meta).where(condition.apply(meta)).orderBy(orderBy.apply(meta))
				.fetch();
	}

	@Override
	public List<E> getList(Function<M, Predicate> condition) {
		return db.selectFrom(meta).where(condition.apply(meta)).fetch();
	}

	@Override
	public List<E> getList(Function<M, Predicate> condition, long offset, long limit) {
		return db.selectFrom(meta).where(condition.apply(meta)).offset(offset).limit(limit).fetch();
	}

	@Override
	public List<E> getList(Function<M, Predicate> condition, long offset, long limit,
			Function<M, OrderSpecifier<?>> orderBy) {
		return db.selectFrom(meta).where(condition.apply(meta)).orderBy(orderBy.apply(meta)).offset(offset).limit(limit)
				.fetch();
	}

	@Override
	public List<E> getList(long offset, long limit, Function<M, OrderSpecifier<?>> orderBy) {
		return db.selectFrom(meta).orderBy(orderBy.apply(meta)).offset(offset).limit(limit).fetch();
	}

	@Override
	public List<E> getList(Function<M, Predicate> condition, Function<M, OrderSpecifier<?>> orderBy) {
		return db.selectFrom(meta).where(condition.apply(meta)).orderBy(orderBy.apply(meta)).fetch();
	}

	@Override
	public long getCount(Function<M, Predicate> condition) {
		return db.selectFrom(meta).where(condition.apply(meta)).fetchCount();
	}

	@Override
	public boolean exists(Function<M, Predicate> condition) {
		return db.exists(meta, condition.apply(meta));
	}

	@Override
	public long delete(Function<M, Predicate> condition) {
		return db.delete(meta).where(condition.apply(meta)).execute();
	}

	@Override
	public <T> List<T> getDistinctList(Function<M, Expression<T>> field, Function<M, Predicate> condition) {
		return db.selectDistinct(field.apply(meta)).from(meta).where(condition.apply(meta)).fetch();
	}

	@Override
	public List<E> getAll() {
		return db.selectFrom(meta).fetch();
	}

	@Override
	public <T> List<T> getAll(Function<M, Expression<T>> field) {
		return db.select(field.apply(meta)).from(meta).fetch();
	}

	@Override
	public <T extends Number & Comparable<?>> T getSum(Function<M, NumberPath<T>> field,
			Function<M, Predicate> condition) {
		return db.select(field.apply(meta).sum()).from(meta).where(condition.apply(meta)).fetchOne();
	}

	@Override
	public <T extends Number & Comparable<?>> T getSum(Function<M, NumberPath<T>> field) {
		return db.select(field.apply(meta).sum()).from(meta).fetchOne();
	}

	@Override
	public <T extends Number & Comparable<?>> Double getAvg(Function<M, NumberPath<T>> field,
			Function<M, Predicate> condition) {
		return db.select(field.apply(meta).avg()).from(meta).where(condition.apply(meta)).fetchOne();
	}

	@Override
	public <T extends Number & Comparable<?>> Double getAvg(Function<M, NumberPath<T>> field) {
		return db.select(field.apply(meta).avg()).from(meta).fetchOne();
	}

	@Override
	public <T extends Number & Comparable<?>> T getMax(Function<M, NumberPath<T>> field,
			Function<M, Predicate> condition) {
		return db.select(field.apply(meta).max()).from(meta).where(condition.apply(meta)).fetchOne();
	}

	@Override
	public <T extends Number & Comparable<?>> T getMax(Function<M, NumberPath<T>> field) {
		return db.select(field.apply(meta).max()).from(meta).fetchOne();
	}

	@Override
	public <T extends Number & Comparable<?>> T getMin(Function<M, NumberPath<T>> field,
			Function<M, Predicate> condition) {
		return db.select(field.apply(meta).min()).from(meta).where(condition.apply(meta)).fetchOne();
	}

	@Override
	public <T extends Number & Comparable<?>> T getMin(Function<M, NumberPath<T>> field) {
		return db.select(field.apply(meta).min()).from(meta).fetchOne();
	}
}
