package com.kwang2003.activiti6;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.activiti.engine.history.ProcessInstanceHistoryLog;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@DisplayName("财务报表创建、审批流程测试")
public class FinancialReportProcessTest extends Activiti6ApplicationTests {
	private static final String PROCESS_INSTANCE_KEY = "financialReport";
	@Test
	@Disabled
	@DisplayName("部署")
	public void testDeploy() {
		Deployment deployment = repositoryService.createDeployment().addClasspathResource("processes/FinancialReportProcess.bpmn").name("新建财务报表审批流程").deploy();
		log.info("{}",deployment);
	}
	
	@Test
	@DisplayName("启动财务报表流程")
	public void testStartProcess() {
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("financialReport");
		log.info("{}",processInstance);
	}
	
	@Test
	@DisplayName("查询 accountancy组的任务列表")
	public void testAccountancyTaskList() {
		List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup("accountancy").list();
		tasks.forEach(task -> {
			log.info("{}",task);
		});
	}
	
	@Test
	@DisplayName("accountancy组认领任务")
	public void testAccountancyClaimingTask() {
		List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup("accountancy").list();
		tasks.forEach(task -> {
			taskService.claim(task.getId(), "Kevin");
		});
		
		//显示Kevin的代办任务
		List<Task> kevinTasks = taskService.createTaskQuery().taskAssignee("Kevin").list();
		kevinTasks.forEach(task ->{
			log.info("kevin的代办任务：{}",task);
			taskService.complete(task.getId());
		});
	}
	
	@Test
	@DisplayName("一个完整的流程")
	public void testWholeProcess() {
		log.info("启动工作流...");
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_INSTANCE_KEY);
		log.info("流程已经启动，{}",processInstance);
		
		log.info("查看accountancy组进行中的任务列表...");
		List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup("accountancy").processDefinitionKey(PROCESS_INSTANCE_KEY).list();
		log.info("查询到accountancy组的代办任务{}条",tasks.size());
		tasks.forEach(task ->{
			log.info("- 代办任务：{}",task);
			log.info("用户Kevin开始处理accountancy组的任务-{}",task);
			taskService.claim(task.getId(), "kevin");
		});
		
		log.info("查询用户kevin的代办任务...");
		tasks = taskService.createTaskQuery().taskAssignee("kevin").processDefinitionKey(PROCESS_INSTANCE_KEY).list();
		log.info("用户Kevin的待办任务累计{}条");
		tasks.forEach(task ->{
			taskService.complete(task.getId());
			log.info("用户kevin完成任务：{}",task);
		});
		
		log.info("查询management组中进行的任务列表...");
		tasks = taskService.createTaskQuery().taskCandidateGroup("finance_management").processDefinitionKey(PROCESS_INSTANCE_KEY).list();
		log.info("management组的代办任务累计{}条",tasks.size());
		tasks.forEach(task ->{
			log.info("# 代办任务：{}",task);
			log.info("用户John开始处理management组的任务-{}",task);
			taskService.claim(task.getId(), "john");
		});
		
		log.info("开始查询john的代办任务...");
		tasks = taskService.createTaskQuery().taskAssignee("john").processDefinitionKey(PROCESS_INSTANCE_KEY).list();
		log.info("用户john的代办任务数量{}条",tasks.size());
		tasks.forEach(task ->{
			taskService.complete(task.getId());
			log.info("用户john完成任务：{}",task);
		});
		
		ProcessInstanceHistoryLog history = historyService.createProcessInstanceHistoryLogQuery(processInstance.getId()).includeTasks().singleResult();
		assertNotNull(history);
		log.info("{}",history);
	}
}
