package com.aiivar.sjorm.query.executor;

import com.aiivar.sjorm.query.Query;

import java.sql.SQLException;

public interface QueryExecutor<T extends Query> {

    void execute(T query, Object... params) throws SQLException;

    Object executeWithResultReturn(T query, Object... params) throws SQLException;

    Class<T> getSupportedQuery();
}
