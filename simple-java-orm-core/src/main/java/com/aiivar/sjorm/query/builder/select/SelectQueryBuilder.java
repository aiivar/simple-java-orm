package com.aiivar.sjorm.query.builder.select;

import com.aiivar.sjorm.query.Query;
import com.aiivar.sjorm.query.builder.QueryBuilder;

public interface SelectQueryBuilder<T> extends QueryBuilder<Class<T>> {

    Query buildQuery(Class<T> entityClass, Object primaryKey);
}