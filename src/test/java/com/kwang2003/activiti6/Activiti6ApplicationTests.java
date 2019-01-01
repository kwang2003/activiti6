package com.kwang2003.activiti6;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class Activiti6ApplicationTests {
	@Autowired
	protected ProcessEngine processEngine;
	
	protected RepositoryService repositoryService;
	protected HistoryService historyService;
	protected RuntimeService runtimeService;
	protected TaskService taskService;
	
	@BeforeEach
	public void initServices() {
		this.repositoryService = processEngine.getRepositoryService();
		this.historyService = processEngine.getHistoryService();
		this.runtimeService = processEngine.getRuntimeService();
		this.taskService = processEngine.getTaskService();
	}
}

