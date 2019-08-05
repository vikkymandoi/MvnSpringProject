package com.uni.app.main.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@DependsOn("connectionDataSource")
public class ConnectionPool {
	private static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class); 
	private static final int MAX_POOL_SIZE_LIMIT = 10;
	private static int INIT_POOL_SIZE_LIMIT = 1;
	private BlockingQueue<Connection> activeConnectinoQueue = new LinkedBlockingQueue<>();
	private BlockingQueue<Connection> usedConnectinoList = new LinkedBlockingQueue<>();
	private DataSource dataSource;
	
	@Autowired
	private ConnectionDataSource connectionDataSource;
	
	public void initConnectionPool(String env) {
		logger.info("ConnectionPool initialization started.");
		dataSource = connectionDataSource.getDataSource(env);
		if (activeConnectinoQueue.isEmpty() && usedConnectinoList.isEmpty()) {
			for (int i = 0; i < INIT_POOL_SIZE_LIMIT; i++) {
				createConnections(env);
			}
		}
		logger.info("ConnectionPool initialization completed. ConnectionPool size : {}", activeConnectinoQueue.size());
	}

	private void createConnections(String env) {
		try {
			Connection connection = dataSource.getConnection();
			activeConnectinoQueue.add(connection);
		} catch (SQLException e) {
			logger.error("Error in getting connection from pool : ", e);
		}
	}

	public Connection getConnection() {
		if (activeConnectinoQueue.isEmpty()) {
			logger.info("No Connections in Active Queue.");
			return null;
		}
		Connection connection = activeConnectinoQueue.remove();

		try {
			if (connection.isClosed()) {
				connection = dataSource.getConnection();
			}
		} catch (SQLException e) {
			logger.error("Error while getting connection from pool : ", e);
		}

		usedConnectinoList.add(connection);
		return connection;
	}

	public void releaseConnection(Connection connection) {
		if (connection != null) {
			usedConnectinoList.remove(connection);
			activeConnectinoQueue.add(connection);
		}
	}

	public void setInitialPoolSize(int initialPoolSize) {
		if (!(initialPoolSize < 0 || initialPoolSize > MAX_POOL_SIZE_LIMIT)) {
			INIT_POOL_SIZE_LIMIT = initialPoolSize;
		}
	}

	public int getInitialPoolSize() {
		return INIT_POOL_SIZE_LIMIT;
	}

	public int getConnectionPoolSize() {
		return activeConnectinoQueue.size() + usedConnectinoList.size();
	}

	public void setDataSource(AbstractDataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void closeConnectionPool() {
		logger.info("Closing connectionPool started.");
		close(usedConnectinoList);
		close(activeConnectinoQueue);
		logger.info("ConnectionPool Closed.");
	}

	private void close(BlockingQueue<Connection> connectinosQueue) {
		for (int i = 0; i < connectinosQueue.size(); i++) {
			Connection connection = connectinosQueue.remove();
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					logger.error("Error in initializing connection pool : ", e);
				}
			}
		}
	}
}