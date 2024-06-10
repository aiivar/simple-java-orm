package com.aiivar.sjorm.query.builder.select;

import com.aiivar.sjorm.annotations.Column;
import com.aiivar.sjorm.annotations.Entity;
import com.aiivar.sjorm.annotations.Id;
import com.aiivar.sjorm.exceptions.OrmException;
import com.aiivar.sjorm.query.Query;
import com.aiivar.sjorm.query.builder.AbstractQueryBuilder;
import com.aiivar.sjorm.query.select.SelectQuery;
import com.aiivar.sjorm.query.select.SelectQueryImpl;

import java.lang.reflect.Field;

public class SimpleSelectQueryBuilder<T> extends AbstractQueryBuilder implements SelectQueryBuilder<T> {

    @Override
    public Query buildQuery(Class<T> entityClass) {
        throw new UnsupportedOperationException("Use buildQuery with primary key for select queries");
    }

    @Override
    public SelectQuery buildQuery(Class<T> entityClass, Object primaryKey) {
        if (!entityClass.isAnnotationPresent(Entity.class)) {
            throw new OrmException("The class is not annotated with @Entity");
        }

        String tableName = getTableName(entityClass);
        String idColumn = getIdColumn(entityClass);

        String sql = "SELECT * FROM " + tableName + " WHERE " + idColumn + " = ?";
        return new SelectQueryImpl(sql);
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
