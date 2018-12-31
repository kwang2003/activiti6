package com.kwang2003.activiti6.config;

import javax.sql.DataSource;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import com.google.common.collect.Lists;

@Configuration
public class ActivitiConfig {
	@Bean
	public SpringProcessEngineConfiguration springProcessEngineConfiguration(DataSource dataSource,
			PlatformTransactionManager transactionManager) {
		SpringProcessEngineConfiguration configuration = new SpringProcessEngineConfiguration();
		configuration.setDataSource(dataSource);
		configuration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE);
		configuration.setTransactionManager(transactionManager);
		configuration.setAsyncExecutorActivate(true);
		configuration.setHistory(HistoryLevel.AUDIT.getKey());// 默认audit
		configuration.setEventListeners(Lists.newArrayList(new MyEventListener()));
		return configuration;
	}

	@Bean
	public ProcessEngine processEngine(SpringProcessEngineConfiguration springProcessEngineConfiguration)
			throws Exception {
		ProcessEngineFactoryBean bean = new ProcessEngineFactoryBean();
		bean.setProcessEngineConfiguration(springProcessEngineConfiguration);
		ProcessEngine engine = bean.getObject();
//		deployProcess(engine);//新创建的bpmn流程或者修改后的文件需要放开注释
		return engine;
	}
	
	private void deployProcess(ProcessEngine processEngine) {
		RepositoryService repositoryService = processEngine.getRepositoryService();
		repositoryService.createDeployment()
		.addClasspathResource("processes/VacationRequest.bpmn")
		.deploy();
	}

	@Bean
	public PlatformTransactionManager transactionManager(DataSource dataSource) {
		DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager(dataSource);
		return dataSourceTransactionManager;
	}

	public static class MyEventListener implements ActivitiEventListener {
		@Override
		public void onEvent(ActivitiEvent event) {
			switch (event.getType()) {
			case JOB_EXECUTION_SUCCESS:
				System.out.println("A job well done!");
				break;
			case JOB_EXECUTION_FAILURE:
				System.out.println("A job has failed...");
				break;
			default:
				System.out.println("Event received: " + event.getType());
			}
		}

		@Override
		public boolean isFailOnException() {
			// The logic in the onEvent method of this listener is not critical, exceptions
			// can be ignored if logging fails...
			return false;
		}
	}
}