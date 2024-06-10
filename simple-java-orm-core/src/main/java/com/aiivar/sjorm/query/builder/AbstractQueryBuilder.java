package com.aiivar.sjorm.query.builder;

import com.aiivar.sjorm.annotations.Column;
import com.aiivar.sjorm.annotations.Table;
import com.aiivar.sjorm.exceptions.OrmException;

import java.lang.reflect.Field;

public abstract class AbstractQueryBuilder {

    protected String getTableName(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Table.class)) {
            return clazz.getAnnotation(Table.class).name();
        } else {
            return clazz.getSimpleName().toLowerCase();
        }
    }

    protected String getIdColumn(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                return field.getAnnotation(Column.class).name();
            }
        }
        throw new OrmException("No field annotated with @Id");
    }

    protected String getColumnType(Class<?> fieldType) {
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
}
