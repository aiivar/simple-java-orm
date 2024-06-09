package com.aiivar.sjorm.transaction;

import com.aiivar.sjorm.exceptions.TransactionException;

import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class  Transaction {

    private static final Logger logger = LoggerFactory.getLogger(Transaction.class);

    private Connection connection;

    public Transaction(Connection connection) {
        this.connection = connection;
    }

    public void begin() {
        logger.debug("Beginning transaction");
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            logger.error("Failed to begin transaction", e);
            throw new TransactionException("Failed to begin transaction", e);
        }
    }

    public void commit() {
        logger.debug("Committing transaction");
        try {
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            logger.error("Failed to commit transaction", e);
            throw new TransactionException("Failed to commit transaction", e);
        }
    }

    public void rollback() {
        logger.debug("Rolling back transaction");
        try {
            connection.rollback();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            logger.error("Failed to rollback transaction", e);
            throw new TransactionException("Failed to rollback transaction", e);
        }
    }
}
