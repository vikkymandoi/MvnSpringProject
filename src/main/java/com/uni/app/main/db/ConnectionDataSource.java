package com.uni.app.main.db;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

@Component
public class ConnectionDataSource {
	@Autowired
	private Environment env;
	private static final Logger logger = LoggerFactory.getLogger(ConnectionDataSource.class);
	private Map<String, DataSource> connectionMap = new HashMap<String, DataSource>();
	
	public DataSource getDataSource(String envPrefix) {
		DataSource dataSource = connectionMap.get(envPrefix);
		if(dataSource == null) {
			dataSource = createDataSource(envPrefix);
		}
		return dataSource;
	}
	
	private DataSource createDataSource(String envPrefix) {
		logger.info("Settting up data source for Environment : {}", envPrefix);
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		try {
			dataSource.setDriverClassName(env.getProperty(envPrefix+".oracle.db_driverclass"));
			dataSource.setUrl(env.getProperty(envPrefix+".oracle.db_url"));
			dataSource.setUsername(env.getProperty(envPrefix+".oracle.dbUser"));
			dataSource.setPassword(env.getProperty(envPrefix+".oracle.dbPass"));
			return dataSource;
		} catch(Exception e) {
			logger.error("Error Creating Data Source for Env :"+ envPrefix + " ----------Exception {}",e);
		}
		return null;
	}
}
