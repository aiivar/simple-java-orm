package com.aiivar.sjorm.session;

import com.aiivar.sjorm.config.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SessionFactory {

    private final Configuration configuration;

    public SessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    public Session openSession() throws SQLException {
        Connection connection = DriverManager.getConnection(
                configuration.getJdbcUrl(),
                configuration.getUsername(),
                configuration.getPassword()
        );
        return new Session(connection);
    }
}