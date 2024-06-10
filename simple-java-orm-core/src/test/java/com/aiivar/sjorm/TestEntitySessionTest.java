package com.aiivar.sjorm;

import com.aiivar.sjorm.entity.TestEntity;
import com.aiivar.sjorm.exceptions.OrmException;
import com.aiivar.sjorm.session.Session;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class TestEntitySessionTest {

    private static Connection connection;
    private static Session session;

    @BeforeClass
    public static void setupDatabase() throws SQLException {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("sa");

        connection = dataSource.getConnection();
        session = new Session(connection);

        // Create table for TestEntity
        session.createTable(TestEntity.class);
    }

    @AfterClass
    public static void closeDatabase() throws SQLException {
        session.close();
        connection.close();
    }

    @Test
    public void testSaveAndFind() throws SQLException {
        TestEntity entity = new TestEntity();
        entity.setName("Test Name");
        entity.setValue("Test Value");

        session.save(entity);
        assertNotNull(entity.getId());

        TestEntity foundEntity = session.find(TestEntity.class, entity.getId());
        assertNotNull(foundEntity);
        assertEquals("Test Name", foundEntity.getName());
        assertEquals("Test Value", foundEntity.getValue());
    }

    @Test
    public void testUpdate() throws SQLException {
        TestEntity entity = new TestEntity();
        entity.setName("Test Name");
        entity.setValue("Test Value");

        session.save(entity);
        assertNotNull(entity.getId());

        entity.setName("Updated Name");
        entity.setValue("Updated Value");
        session.update(entity);

        TestEntity updatedEntity = session.find(TestEntity.class, entity.getId());
        assertNotNull(updatedEntity);
        assertEquals("Updated Name", updatedEntity.getName());
        assertEquals("Updated Value", updatedEntity.getValue());
    }

    @Test
    public void testDelete() throws SQLException {
        TestEntity entity = new TestEntity();
        entity.setName("Test Name");
        entity.setValue("Test Value");

        session.save(entity);
        assertNotNull(entity.getId());

        session.delete(entity);

        try {
            session.find(TestEntity.class, entity.getId());
            fail("Expected an OrmException to be thrown");
        } catch (OrmException e) {
            assertEquals("Failed to find entity", e.getMessage());
        }
    }
}
