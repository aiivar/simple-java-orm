package com.aiivar.sjorm;

import com.aiivar.sjorm.config.Configuration;
import com.aiivar.sjorm.entity.TestEntity;
import com.aiivar.sjorm.exceptions.OrmException;
import com.aiivar.sjorm.session.Session;
import com.aiivar.sjorm.session.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;

import static org.junit.Assert.*;

public class TestEntitySessionTest {
    private static final String JDBC_URL = "jdbc:h2:mem:testdb";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    private Connection connection;
    private SessionFactory sessionFactory;

    @Before
    public void setUp() throws Exception {
        connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);

        Configuration configuration = new Configuration(JDBC_URL, USER, PASSWORD);
        sessionFactory = new SessionFactory(configuration);

        try (Session session = sessionFactory.openSession()) {
            session.createTable(TestEntity.class);
        }
    }

    @After
    public void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    public void testSaveAndFind() {
        try (Session session = sessionFactory.openSession()) {
            TestEntity entity = new TestEntity();
            entity.setName("Test Name");
            entity.setValue("Test Value");
            session.save(entity);

            assertNotNull(entity.getId());

            TestEntity foundEntity = session.find(TestEntity.class, entity.getId());
            assertNotNull(foundEntity);
            assertEquals("Test Name", foundEntity.getName());
            assertEquals("Test Value", foundEntity.getValue());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testUpdate() {
        try (Session session = sessionFactory.openSession()) {
            TestEntity entity = new TestEntity();
            entity.setName("Test Name");
            entity.setValue("Test Value");
            session.save(entity);

            entity.setValue("Updated Value");
            session.update(entity);

            TestEntity updatedEntity = session.find(TestEntity.class, entity.getId());
            assertNotNull(updatedEntity);
            assertEquals("Updated Value", updatedEntity.getValue());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDelete() {
        try (Session session = sessionFactory.openSession()) {
            TestEntity entity = new TestEntity();
            entity.setName("Test Name");
            entity.setValue("Test Value");
            session.save(entity);

            session.delete(entity);

            try {
                session.find(TestEntity.class, entity.getId());
                fail("Expected OrmException to be thrown");
            } catch (OrmException e) {
                assertEquals("Entity not found", e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
