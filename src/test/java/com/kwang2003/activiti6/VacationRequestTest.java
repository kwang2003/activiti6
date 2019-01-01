package com.kwang2003.activiti6;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VacationRequestTest extends Activiti6ApplicationTests {

	@Test
	@Disabled
	@DisplayName("新建工作流或者修改工作流时，需要重新deploy一下")
	public void deploy() {
		Deployment deployment = repositoryService.createDeployment().addClasspathResource("processes/VacationRequest.bpmn").name("改动描述").deploy();
		log.info("{}",deployment);
	}

	@Test
	@DisplayName("统计流程个数")
	public void testCount() {
		long total = repositoryService.createProcessDefinitionQuery().count();
		log.info("流程总个数：{}", total);
	}

	@Test
	@DisplayName("启动流程")
	public void testStartProcess() {
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("employeeName", "Kevin");
		variables.put("numberOfDays", new Integer(1));
		variables.put("vacationMotivation", "Kevin 休假申请!");
		variables.put("startDate", new SimpleDateFormat("yyyy-MM-dd hh:mm").format(new Date()));

		//启动 vacationRequest最新版本的流程
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("vacationRequest", variables);
		log.info("activityId:{}", processInstance.getId());
		log.info("进行中的流程：{}", runtimeService.createProcessInstanceQuery().count());
	}
	
	@Test
	@DisplayName("management组所能看到的task列表")
	public void testCompletingTasks() {
		List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup("management").list();;
		tasks.forEach(task -> {
			log.info("{}",task);
		});
	}
	
	@Test
	@DisplayName("获取流程图")
	public void testRetriveProcessImage() throws IOException{
		RepositoryService repositoryService = processEngine.getRepositoryService();
		List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().processDefinitionKey("vacationRequest").list();
		for(ProcessDefinition processDefinition : processDefinitions) {
			String diagramResourceName = processDefinition.getDiagramResourceName();
			@Cleanup
			InputStream inputStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), diagramResourceName);
			Path path = FileSystems.getDefault().getPath("d:/", diagramResourceName);;
			Files.copy(inputStream, path);
		}
		
	}
}
