package com.aiivar.sjorm.query.builder.delete;

import com.aiivar.sjorm.annotations.Column;
import com.aiivar.sjorm.annotations.Entity;
import com.aiivar.sjorm.annotations.Id;
import com.aiivar.sjorm.exceptions.OrmException;
import com.aiivar.sjorm.query.Query;
import com.aiivar.sjorm.query.builder.AbstractQueryBuilder;
import com.aiivar.sjorm.query.delete.DeleteQuery;
import com.aiivar.sjorm.query.delete.DeleteQueryImpl;

import java.lang.reflect.Field;

public class SimpleDeleteQueryBuilder<T> extends AbstractQueryBuilder implements DeleteQueryBuilder<T> {

    @Override
    public Query buildQuery(T entity) {
        throw new UnsupportedOperationException("Use buildQuery with primary key for delete queries");
    }

    @Override
    public DeleteQuery buildQuery(Class<T> entityClass, Object primaryKey) {
        if (!entityClass.isAnnotationPresent(Entity.class)) {
            throw new OrmException("The class is not annotated with @Entity");
        }

        String tableName = getTableName(entityClass);
        String idColumn = getIdColumn(entityClass);

        String sql = "DELETE FROM " + tableName + " WHERE " + idColumn + " = ?";
        return new DeleteQueryImpl(sql);
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
