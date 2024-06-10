package com.aiivar.sjorm.query.executor.select;

import com.aiivar.sjorm.query.executor.QueryExecutor;
import com.aiivar.sjorm.query.select.SelectQuery;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SelectQueryExecutor implements QueryExecutor<SelectQuery> {
    private final Connection connection;

    public SelectQueryExecutor(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void execute(SelectQuery query, Object... params) throws SQLException {
        throw new UnsupportedOperationException("SelectQueryExecutor does not support execute without result return");
    }

    @Override
    public List<Object[]> executeWithResultReturn(SelectQuery query, Object... params) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(query.getSql())) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Object[]> results = new ArrayList<>();
                int columnCount = resultSet.getMetaData().getColumnCount();

                while (resultSet.next()) {
                    Object[] row = new Object[columnCount];
                    for (int i = 0; i < columnCount; i++) {
                        row[i] = resultSet.getObject(i + 1);
                    }
                    results.add(row);
                }

                return results;
            }
        }
    }

    @Override
    public Class<SelectQuery> getSupportedQuery() {
        return SelectQuery.class;
    }
}
