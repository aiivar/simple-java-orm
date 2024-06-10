package com.aiivar.sjorm.query.executor.update;

import com.aiivar.sjorm.query.executor.QueryExecutor;
import com.aiivar.sjorm.query.update.UpdateQuery;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UpdateQueryExecutor implements QueryExecutor<UpdateQuery> {
    private final Connection connection;

    public UpdateQueryExecutor(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void execute(UpdateQuery query, Object... params) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(query.getSql())) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            statement.executeUpdate();
        }
    }

    @Override
    public Object executeWithResultReturn(UpdateQuery query, Object... params) {
        throw new UnsupportedOperationException("UpdateQueryExecutor does not support executeWithResultReturn");
    }

    @Override
    public Class<UpdateQuery> getSupportedQuery() {
        return UpdateQuery.class;
    }
}
