package com.kwang2003.activiti6;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VacationRequestTest extends Activiti6ApplicationTests {
	@Autowired
	private ProcessEngine processEngine;
	@Test
	@DisplayName("统计流程个数")
	public void testCount() {
		RepositoryService repositoryService = processEngine.getRepositoryService();
		long total = repositoryService.createProcessDefinitionQuery().count();
		log.info("流程总个数：{}",total);
	}
	
	@Test
	@DisplayName("启动流程")
	public void testStartProcess() {
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("employeeName", "Tom");
		variables.put("numberOfDays", new Integer(2));
		variables.put("vacationMotivation", "Tom休假申请!");
		
		RuntimeService runtimeService = processEngine.getRuntimeService();
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("vacationRequest",variables);
		log.info("activityId:{}",processInstance.getId());
		log.info("进行中的流程：{}",runtimeService.createProcessInstanceQuery().count());
	}
}
