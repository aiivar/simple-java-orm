package com.aiivar.sjorm.query.executor.createtable;

import com.aiivar.sjorm.query.createtable.CreateTableQuery;
import com.aiivar.sjorm.query.executor.QueryExecutor;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateTableQueryExecutor implements QueryExecutor<CreateTableQuery> {

    private final Connection connection;

    public CreateTableQueryExecutor(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void execute(CreateTableQuery query, Object... params) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(query.getSql());
        }
    }

    @Override
    public Object executeWithResultReturn(CreateTableQuery query, Object... params) {
        throw new UnsupportedOperationException("CreateTableQueryExecutor does not support executeWithResultReturn");
    }

    @Override
    public Class<CreateTableQuery> getSupportedQuery() {
        return CreateTableQuery.class;
    }
}
