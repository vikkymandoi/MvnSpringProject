package com.uni.app.main.repository;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import com.uni.app.main.db.ConnectionPool;

@Configuration
@DependsOn("connectionPool")
public class ConnectionRepository implements ApplicationContextAware{
	private static final Logger logger = LoggerFactory.getLogger(ConnectionRepository.class); 
	@Value("${config.connpool.load.env}")
	private String connPoolEnvList;
	private static Map<String, ConnectionPool> map = new HashMap<String, ConnectionPool>();
	private ApplicationContext applicationContext;
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	@PostConstruct
	public void createConnectionPool() {
		for(String env : connPoolEnvList.split(",")) {
			logger.info("Initializing Connection Repository");
			ConnectionPool connectionPool = applicationContext.getBean("connectionPool", ConnectionPool.class);
			connectionPool.initConnectionPool(env);
			map.put(env, connectionPool);
			logger.info("Connection Repository Initialized Successfully..>!");
		}
	}
	
	public Connection getConnection(String env) {
		if(map.isEmpty()) {
			createConnectionPool();
		}
		return map.get(env).getConnection();
	}
}
