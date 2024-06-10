package com.aiivar.sjorm.query.builder.insert;

import com.aiivar.sjorm.annotations.Column;
import com.aiivar.sjorm.annotations.Entity;
import com.aiivar.sjorm.annotations.Id;
import com.aiivar.sjorm.exceptions.OrmException;
import com.aiivar.sjorm.query.builder.AbstractQueryBuilder;
import com.aiivar.sjorm.query.insert.InsertQuery;
import com.aiivar.sjorm.query.insert.InsertQueryImpl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class SimpleInsertQueryBuilder<T> extends AbstractQueryBuilder implements InsertQueryBuilder<T> {

    @Override
    public InsertQuery buildQuery(T entity) {
        Class<?> clazz = entity.getClass();
        if (!clazz.isAnnotationPresent(Entity.class)) {
            throw new OrmException("The class is not annotated with @Entity");
        }

        String tableName = getTableName(clazz);
        List<String> columns = new ArrayList<>();
        List<String> placeholders = new ArrayList<>();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class) && !field.isAnnotationPresent(Id.class)) {
                columns.add("\"" + field.getAnnotation(Column.class).name() + "\"");
                placeholders.add("?");
            }
        }

        String columnsString = String.join(", ", columns);
        String placeholdersString = String.join(", ", placeholders);

        String sql = "INSERT INTO " + tableName + " (" + columnsString + ") VALUES (" + placeholdersString + ")";
        return new InsertQueryImpl(sql);
    }
}