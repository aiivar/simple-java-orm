package com.aiivar.sjorm.query.builder.delete;

import com.aiivar.sjorm.query.Query;
import com.aiivar.sjorm.query.builder.QueryBuilder;

public interface DeleteQueryBuilder<T> extends QueryBuilder<T> {

    Query buildQuery(Class<T> entityClass, Object primaryKey);
}
