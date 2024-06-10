package com.aiivar.sjorm.query.executor.insert;

import com.aiivar.sjorm.query.executor.QueryExecutor;
import com.aiivar.sjorm.query.insert.InsertQuery;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InsertQueryExecutor implements QueryExecutor<InsertQuery> {

    private final Connection connection;

    public InsertQueryExecutor(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void execute(InsertQuery query, Object... params) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(query.getSql(), PreparedStatement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            statement.executeUpdate();
        }
    }

    @Override
    public Object executeWithResultReturn(InsertQuery query, Object... params) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(query.getSql(), PreparedStatement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1); // Возвращаем сгенерированный ID
                } else {
                    throw new SQLException("Creating entity failed, no ID obtained.");
                }
            }
        }
    }

    @Override
    public Class<InsertQuery> getSupportedQuery() {
        return InsertQuery.class;
    }
}
