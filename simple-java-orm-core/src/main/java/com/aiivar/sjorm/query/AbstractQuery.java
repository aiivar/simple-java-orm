package com.aiivar.sjorm.query;

public abstract class AbstractQuery implements Query {

    protected final String sql;

    public AbstractQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return sql;
    }
}