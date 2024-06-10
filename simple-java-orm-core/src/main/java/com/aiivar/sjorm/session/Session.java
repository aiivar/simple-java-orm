package com.aiivar.sjorm.session;

import com.aiivar.sjorm.annotations.Column;
import com.aiivar.sjorm.annotations.Id;
import com.aiivar.sjorm.exceptions.OrmException;
import com.aiivar.sjorm.query.Query;
import com.aiivar.sjorm.query.builder.createtable.CreateTableQueryBuilder;
import com.aiivar.sjorm.query.builder.createtable.SimpleCreateTableQueryBuilder;
import com.aiivar.sjorm.query.builder.delete.DeleteQueryBuilder;
import com.aiivar.sjorm.query.builder.delete.SimpleDeleteQueryBuilder;
import com.aiivar.sjorm.query.builder.insert.InsertQueryBuilder;
import com.aiivar.sjorm.query.builder.insert.SimpleInsertQueryBuilder;
import com.aiivar.sjorm.query.builder.select.SelectQueryBuilder;
import com.aiivar.sjorm.query.builder.select.SimpleSelectQueryBuilder;
import com.aiivar.sjorm.query.builder.update.SimpleUpdateQueryBuilder;
import com.aiivar.sjorm.query.builder.update.UpdateQueryBuilder;
import com.aiivar.sjorm.query.createtable.CreateTableQuery;
import com.aiivar.sjorm.query.createtable.CreateTableQueryImpl;
import com.aiivar.sjorm.query.delete.DeleteQuery;
import com.aiivar.sjorm.query.delete.DeleteQueryImpl;
import com.aiivar.sjorm.query.executor.QueryExecutor;
import com.aiivar.sjorm.query.executor.createtable.CreateTableQueryExecutor;
import com.aiivar.sjorm.query.executor.delete.DeleteQueryExecutor;
import com.aiivar.sjorm.query.executor.insert.InsertQueryExecutor;
import com.aiivar.sjorm.query.executor.select.SelectQueryExecutor;
import com.aiivar.sjorm.query.executor.update.UpdateQueryExecutor;
import com.aiivar.sjorm.query.insert.InsertQuery;
import com.aiivar.sjorm.query.insert.InsertQueryImpl;
import com.aiivar.sjorm.query.select.SelectQuery;
import com.aiivar.sjorm.query.select.SelectQueryImpl;
import com.aiivar.sjorm.query.update.UpdateQuery;
import com.aiivar.sjorm.query.update.UpdateQueryImpl;
import com.aiivar.sjorm.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Session implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(Session.class);

    private final Connection connection;
    private final Transaction transaction;
    private final Map<Class<? extends Query>, QueryExecutor<? extends Query>> executors;

    public Session(Connection connection) {
        this.connection = connection;
        this.transaction = new Transaction(connection);
        this.executors = new HashMap<>();
        registerExecutors();
    }

    private void registerExecutors() {
        executors.put(InsertQueryImpl.class, new InsertQueryExecutor(connection));
        executors.put(SelectQueryImpl.class, new SelectQueryExecutor(connection));
        executors.put(UpdateQueryImpl.class, new UpdateQueryExecutor(connection));
        executors.put(DeleteQueryImpl.class, new DeleteQueryExecutor(connection));
        executors.put(CreateTableQueryImpl.class, new CreateTableQueryExecutor(connection));
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public <T> void save(T entity) {
        logger.debug("Saving entity: {}", entity);
        try {
            InsertQueryBuilder<T> queryBuilder = new SimpleInsertQueryBuilder<>();
            InsertQuery query = (InsertQuery) queryBuilder.buildQuery(entity);
            Map<String, Object> columnValues = getColumnValues(entity);
            Long generatedId = executeInsertWithResultReturn(query, columnValues.values().toArray());

            // Получение сгенерированного ID и установка его в сущность
            Field idField = getIdField(entity.getClass());
            idField.setAccessible(true);
            idField.set(entity, generatedId);

            logger.debug("Entity saved successfully with ID {}: {}", generatedId, entity);
        } catch (SQLException | IllegalAccessException e) {
            logger.error("Failed to save entity", e);
            throw new OrmException("Failed to save entity", e);
        }
    }

    public <T> T find(Class<T> entityClass, Object primaryKey) {
        logger.debug("Finding entity of class {} with primary key {}", entityClass.getName(), primaryKey);
        try {
            SelectQueryBuilder<T> queryBuilder = new SimpleSelectQueryBuilder<>();
            SelectQuery query = (SelectQuery) queryBuilder.buildQuery(entityClass, primaryKey);
            List<Object[]> results = executeSelectWithResultReturn(query, primaryKey);

            if (results.isEmpty()) {
                throw new OrmException("Failed to find entity");
            }

            T entity = entityClass.getDeclaredConstructor().newInstance();
            Object[] row = results.getFirst();
            Field[] fields = entityClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Column.class)) {
                    String columnName = field.getAnnotation(Column.class).name();
                    field.setAccessible(true);
                    field.set(entity, row[getColumnIndex(columnName, fields)]);
                }
            }

            return entity;
        } catch (SQLException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> void update(T entity) {
        logger.debug("Updating entity: {}", entity);
        try {
            UpdateQueryBuilder<T> queryBuilder = new SimpleUpdateQueryBuilder<>();
            UpdateQuery query = (UpdateQuery) queryBuilder.buildQuery(entity);
            Map<String, Object> columnValues = getColumnValues(entity);
            Object idValue = getIdValue(entity);
            Object[] params = new Object[columnValues.size() + 1];

            int i = 0;
            for (Object value : columnValues.values()) {
                params[i++] = value;
            }
            params[i] = idValue;  // Добавляем id в параметры в конце

            execute(query, params);
            logger.debug("Entity updated successfully: {}", entity);
        } catch (SQLException e) {
            logger.error("Failed to update entity", e);
            throw new OrmException("Failed to update entity", e);
        }
    }

    public <T> void delete(T entity) {
        logger.debug("Deleting entity: {}", entity);
        try {
            DeleteQueryBuilder<T> queryBuilder = new SimpleDeleteQueryBuilder<>();
            DeleteQuery query = (DeleteQuery) queryBuilder.buildQuery((Class<T>) entity.getClass(), getIdValue(entity));
            execute(query, getIdValue(entity));
            logger.debug("Entity deleted successfully: {}", entity);
        } catch (SQLException e) {
            logger.error("Failed to delete entity", e);
            throw new OrmException("Failed to delete entity", e);
        }
    }

    public <T> void createTable(Class<T> entityClass) {
        logger.debug("Creating table for entity class {}", entityClass.getName());
        try {
            CreateTableQueryBuilder<T> queryBuilder = new SimpleCreateTableQueryBuilder<>();
            CreateTableQuery query = (CreateTableQuery) queryBuilder.buildQuery(entityClass);
            execute(query);
            logger.debug("Table created successfully for entity class {}", entityClass.getName());
        } catch (SQLException e) {
            logger.error("Failed to create table for entity class {}", entityClass.getName(), e);
            throw new OrmException("Failed to create table for entity class " + entityClass.getName(), e);
        }
    }

    private Map<String, Object> getColumnValues(Object entity) {
        Map<String, Object> columnValues = new HashMap<>();
        Class<?> clazz = entity.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class) && !field.isAnnotationPresent(Id.class)) {
                String columnName = field.getAnnotation(Column.class).name();
                field.setAccessible(true);
                try {
                    columnValues.put(columnName, field.get(entity));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return columnValues;
    }

    private int getColumnIndex(String columnName, Field[] fields) {
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getAnnotation(Column.class).name().equals(columnName)) {
                return i;
            }
        }
        throw new OrmException("Column not found: " + columnName);
    }

    private Object getIdValue(Object entity) {
        Class<?> clazz = entity.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                field.setAccessible(true);
                try {
                    return field.get(entity);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        throw new OrmException("No field annotated with @Id");
    }

    private Field getIdField(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                return field;
            }
        }
        throw new OrmException("No field annotated with @Id");
    }

    private <T extends Query> void execute(T query, Object... params) throws SQLException {
        QueryExecutor<T> executor = (QueryExecutor<T>) executors.get(query.getClass());
        if (executor == null) {
            throw new OrmException("No executor found for query type: " + query.getClass().getName());
        }
        executor.execute(query, params);
    }

    private List<Object[]> executeSelectWithResultReturn(SelectQuery query, Object... params) throws SQLException {
        QueryExecutor<SelectQuery> executor = (QueryExecutor<SelectQuery>) executors.get(query.getClass());
        if (executor == null) {
            throw new OrmException("No executor found for query type: " + query.getClass().getName());
        }
        return (List<Object[]>) executor.executeWithResultReturn(query, params);
    }

    private Long executeInsertWithResultReturn(InsertQuery query, Object... params) throws SQLException {
        QueryExecutor<InsertQuery> executor = (QueryExecutor<InsertQuery>) executors.get(query.getClass());
        if (executor == null) {
            throw new OrmException("No executor found for query type: " + query.getClass().getName());
        }
        return (Long) executor.executeWithResultReturn(query, params);
    }

    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
