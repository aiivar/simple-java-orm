package com.aiivar.sjorm.session;

import com.aiivar.sjorm.annotations.Column;
import com.aiivar.sjorm.annotations.Entity;
import com.aiivar.sjorm.annotations.Id;
import com.aiivar.sjorm.annotations.Table;
import com.aiivar.sjorm.exceptions.OrmException;
import com.aiivar.sjorm.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Session implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(Session.class);

    private final Connection connection;
    private final Transaction transaction;

    public Session(Connection connection) {
        this.connection = connection;
        this.transaction = new Transaction(connection);
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public <T> void save(T entity) {
        logger.debug("Saving entity: {}", entity);
        try {
            Class<?> clazz = entity.getClass();
            if (!clazz.isAnnotationPresent(Entity.class)) {
                throw new OrmException("The class is not annotated with @Entity");
            }

            String tableName = getTableName(clazz);
            Map<String, Object> columnValues = getColumnValues(entity);

            String columns = String.join(", ", columnValues.keySet());
            String placeholders = String.join(", ", columnValues.keySet().stream().map(key -> "?").toArray(String[]::new));

            // Обернем имена столбцов в кавычки
            columns = columns.replaceAll("([^, ]+)", "\"$1\"");

            String sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + placeholders + ")";
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                int index = 1;
                for (Object value : columnValues.values()) {
                    statement.setObject(index++, value);
                }
                statement.executeUpdate();

                try (var generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        setIdValue(entity, generatedKeys.getObject(1));
                    }
                }
            }
            logger.debug("Entity saved successfully: {}", entity);
        } catch (SQLException | IllegalAccessException e) {
            logger.error("Failed to save entity", e);
            throw new OrmException("Failed to save entity", e);
        }
    }

    public <T> T find(Class<T> entityClass, Object primaryKey) {
        logger.debug("Finding entity of class {} with primary key {}", entityClass.getName(), primaryKey);
        try {
            if (!entityClass.isAnnotationPresent(Entity.class)) {
                throw new OrmException("The class is not annotated with @Entity");
            }

            String tableName = getTableName(entityClass);
            String idColumn = getIdColumn(entityClass);

            String sql = "SELECT * FROM " + tableName + " WHERE " + idColumn + " = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setObject(1, primaryKey);
                try (var resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        T entity = entityClass.getDeclaredConstructor().newInstance();
                        for (Field field : entityClass.getDeclaredFields()) {
                            if (field.isAnnotationPresent(Column.class)) {
                                String columnName = field.getAnnotation(Column.class).name();
                                field.setAccessible(true);
                                field.set(entity, resultSet.getObject(columnName));
                            }
                        }
                        logger.debug("Entity found: {}", entity);
                        return entity;
                    } else {
                        throw new OrmException("Entity not found");
                    }
                }
            }
        } catch (SQLException | ReflectiveOperationException e) {
            logger.error("Failed to find entity", e);
            throw new OrmException("Failed to find entity", e);
        }
    }

    public <T> void update(T entity) {
        logger.debug("Updating entity: {}", entity);
        try {
            Class<?> clazz = entity.getClass();
            if (!clazz.isAnnotationPresent(Entity.class)) {
                throw new OrmException("The class is not annotated with @Entity");
            }

            String tableName = getTableName(clazz);
            Map<String, Object> columnValues = getColumnValues(entity);
            String idColumn = getIdColumn(clazz);
            Object idValue = getIdValue(entity);

            String setClause = String.join(", ", columnValues.keySet().stream().map(key -> "\"" + key + "\" = ?").toArray(String[]::new));

            String sql = "UPDATE " + tableName + " SET " + setClause + " WHERE " + idColumn + " = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                int index = 1;
                for (Object value : columnValues.values()) {
                    statement.setObject(index++, value);
                }
                statement.setObject(index, idValue);
                statement.executeUpdate();
            }
            logger.debug("Entity updated successfully: {}", entity);
        } catch (SQLException | IllegalAccessException e) {
            logger.error("Failed to update entity", e);
            throw new OrmException("Failed to update entity", e);
        }
    }

    public <T> void delete(T entity) {
        logger.debug("Deleting entity: {}", entity);
        try {
            Class<?> clazz = entity.getClass();
            if (!clazz.isAnnotationPresent(Entity.class)) {
                throw new OrmException("The class is not annotated with @Entity");
            }

            String tableName = getTableName(clazz);
            String idColumn = getIdColumn(clazz);
            Object idValue = getIdValue(entity);

            String sql = "DELETE FROM " + tableName + " WHERE " + idColumn + " = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setObject(1, idValue);
                statement.executeUpdate();
            }
            logger.debug("Entity deleted successfully: {}", entity);
        } catch (SQLException | IllegalAccessException e) {
            logger.error("Failed to delete entity", e);
            throw new OrmException("Failed to delete entity", e);
        }
    }

    public <T> void createTable(Class<T> entityClass) {
        logger.debug("Creating table for entity class {}", entityClass.getName());
        try {
            if (!entityClass.isAnnotationPresent(Entity.class)) {
                throw new OrmException("The class is not annotated with @Entity");
            }

            String tableName = getTableName(entityClass);
            List<String> columnDefinitions = new ArrayList<>();

            for (Field field : entityClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Column.class)) {
                    String columnName = field.getAnnotation(Column.class).name();
                    String columnType = getColumnType(field.getType());

                    if (field.isAnnotationPresent(Id.class)) {
                        columnDefinitions.add("\"" + columnName + "\" " + columnType + " GENERATED BY DEFAULT AS IDENTITY");
                    } else {
                        columnDefinitions.add("\"" + columnName + "\" " + columnType);
                    }
                }
            }

            String sql = "CREATE TABLE " + tableName + " (" + String.join(", ", columnDefinitions) + ")";
            try (Statement statement = connection.createStatement()) {
                statement.execute(sql);
            }
            logger.debug("Table created successfully for entity class {}", entityClass.getName());
        } catch (SQLException e) {
            logger.error("Failed to create table for entity class {}", entityClass.getName(), e);
            throw new OrmException("Failed to create table for entity class " + entityClass.getName(), e);
        }
    }

    private String getColumnType(Class<?> fieldType) {
        if (fieldType == String.class) {
            return "VARCHAR(255)";
        } else if (fieldType == int.class || fieldType == Integer.class) {
            return "INT";
        } else if (fieldType == long.class || fieldType == Long.class) {
            return "BIGINT";
        } else if (fieldType == boolean.class || fieldType == Boolean.class) {
            return "BOOLEAN";
        }
        throw new OrmException("Unsupported field type: " + fieldType.getName());
    }

    private String getTableName(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Table.class)) {
            return clazz.getAnnotation(Table.class).name();
        } else {
            return clazz.getSimpleName().toLowerCase();
        }
    }

    private String getIdColumn(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                return field.isAnnotationPresent(Column.class) ? "\"" + field.getAnnotation(Column.class).name() + "\"" : field.getName();
            }
        }
        throw new OrmException("No field annotated with @Id");
    }

    private Object getIdValue(Object entity) throws IllegalAccessException {
        Class<?> clazz = entity.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                field.setAccessible(true);
                return field.get(entity);
            }
        }
        throw new OrmException("No field annotated with @Id");
    }

    private void setIdValue(Object entity, Object value) throws IllegalAccessException {
        Class<?> clazz = entity.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                field.setAccessible(true);
                field.set(entity, value);
                return;
            }
        }
        throw new OrmException("No field annotated with @Id");
    }

    private Map<String, Object> getColumnValues(Object entity) throws IllegalAccessException {
        Map<String, Object> columnValues = new HashMap<>();
        Class<?> clazz = entity.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class) && !field.isAnnotationPresent(Id.class)) {
                String columnName = field.getAnnotation(Column.class).name();
                field.setAccessible(true);
                columnValues.put(columnName, field.get(entity));
            }
        }
        return columnValues;
    }

    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
