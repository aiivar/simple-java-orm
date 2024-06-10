package com.aiivar.sjorm.query.builder.update;

import com.aiivar.sjorm.annotations.Column;
import com.aiivar.sjorm.annotations.Entity;
import com.aiivar.sjorm.annotations.Id;
import com.aiivar.sjorm.exceptions.OrmException;
import com.aiivar.sjorm.query.builder.AbstractQueryBuilder;
import com.aiivar.sjorm.query.update.UpdateQuery;
import com.aiivar.sjorm.query.update.UpdateQueryImpl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class SimpleUpdateQueryBuilder<T> extends AbstractQueryBuilder implements UpdateQueryBuilder<T> {

    @Override
    public UpdateQuery buildQuery(T entity) {
        Class<?> clazz = entity.getClass();
        if (!clazz.isAnnotationPresent(Entity.class)) {
            throw new OrmException("The class is not annotated with @Entity");
        }

        String tableName = getTableName(clazz);
        List<String> setClauses = new ArrayList<>();
        String idColumn = getIdColumn(clazz);

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class) && !field.isAnnotationPresent(Id.class)) {
                setClauses.add("\"" + field.getAnnotation(Column.class).name() + "\" = ?");
            }
        }

        String setClauseString = String.join(", ", setClauses);

        String sql = "UPDATE " + tableName + " SET " + setClauseString + " WHERE " + idColumn + " = ?";
        return new UpdateQueryImpl(sql);
    }

    @Override
    protected String getIdColumn(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                return "\"" + field.getAnnotation(Column.class).name() + "\"";
            }
        }
        throw new OrmException("No field annotated with @Id");
    }
}
