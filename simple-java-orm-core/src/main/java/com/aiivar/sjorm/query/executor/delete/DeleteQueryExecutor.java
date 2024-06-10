package com.aiivar.sjorm.query.executor.delete;

import com.aiivar.sjorm.query.delete.DeleteQuery;
import com.aiivar.sjorm.query.executor.QueryExecutor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DeleteQueryExecutor implements QueryExecutor<DeleteQuery> {

    private final Connection connection;

    public DeleteQueryExecutor(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void execute(DeleteQuery query, Object... params) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(query.getSql())) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            statement.executeUpdate();
        }
    }

    @Override
    public Object executeWithResultReturn(DeleteQuery query, Object... params) {
        throw new UnsupportedOperationException("DeleteQueryExecutor does not support executeWithResultReturn");
    }

    @Override
    public Class<DeleteQuery> getSupportedQuery() {
        return DeleteQuery.class;
    }
}
