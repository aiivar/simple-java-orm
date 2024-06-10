package com.aiivar.sjorm.query.builder;

import com.aiivar.sjorm.query.Query;

public interface QueryBuilder<T> {

    Query buildQuery(T entity);
}