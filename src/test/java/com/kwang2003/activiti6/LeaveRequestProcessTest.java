package com.kwang2003.activiti6;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Maps;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;

/**
 * 普通员工：张三、李四，王五 部门经理：张无忌 分管副总:阳顶天 总经理：张三丰 人力：小昭
 * 
 * @author Administrator
 *
 */
@Slf4j
@DisplayName("复杂请假流程示意")
public class LeaveRequestProcessTest extends Activiti6ApplicationTests {
	private static final String KEY = "leaveRequestProcess";
	private static Employee ZHANGSAN = new Employee("a004", "张三", "employee");
	private static Employee ZHANGSANFENG = new Employee("a001", "张三丰", "general_manager");
	private static Employee YANGDINGTIAN = new Employee("a002", "阳顶天", "deputy_general_manager");
	private static Employee ZHANGWUJI = new Employee("a003", "张无忌", "department_manager");
	private static Employee LISI = new Employee("a005", "李四", "employee");
	private static Employee WANGWU = new Employee("a006", "王五", "employee");
	private static Employee XIAOZHAO = new Employee("a007", "小昭", "hr");

	@Test
	@DisplayName("部署")
	public void deploy() throws UnknownHostException {
		Deployment deployment = repositoryService.createDeployment()
				.addClasspathResource("processes/LeaveRequestProcess.bpmn").name("提交申请表单assignee组设置为department_manager")
				.key("LeaveRequestProcess").category("OA").tenantId(Inet4Address.getLocalHost().getHostAddress())
				.deploy();
		log.info("{}", deployment);
	}

	/**
	 * 
	 */
	@Test
	@DisplayName("请假1天全流程测试")
	public void request1Day() {
		// 普通员工个请假流程，提交表单
		employeeSubmitLeaveRequest(ZHANGSAN);
		// 部门经理审核通过
		departmentApproveLeaveRequest(ZHANGWUJI);
		// HR备案
		hrHandle(XIAOZHAO);
	}

	private void hrHandle(Employee employee) {
		List<Task> tasks = taskService.createTaskQuery().processDefinitionKey(KEY).taskCandidateGroup("hr").list();
		assertFalse(tasks.isEmpty());
		tasks.forEach(task -> {
			taskService.complete(task.getId());
		});

		tasks = taskService.createTaskQuery().processDefinitionKey(KEY).taskCandidateGroup("hr").list();
		assertTrue(tasks.isEmpty());
	}

	private void departmentApproveLeaveRequest(Employee employee) {
		List<Task> tasks = taskService.createTaskQuery().processDefinitionKey(KEY).taskCandidateGroup("department_manager")
				.list();
		assertFalse(tasks.isEmpty());
		tasks.forEach(task -> {
			Map<String, Object> variables = Maps.newHashMap();
			variables.put("comment", "同意申请");
			variables.put("approved", "true");
			taskService.complete(task.getId(), variables);
		});
	}

	private void employeeSubmitLeaveRequest(Employee employee) {
		Map<String, Object> variables = Maps.newHashMap();
		variables.put("employeeId", employee.getId());
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(KEY, variables);
		log.info("{}", processInstance);

		Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
		assertNotNull(task);
		taskService.claim(task.getId(), employee.getId());
		Map<String, Object> submitVariables = Maps.newHashMap();
		submitVariables.put("numberOfDays", 1);
		submitVariables.put("startDate", "2019-01-04 10:00");
		submitVariables.put("reason", "世界那么大，我想出去走走.");
		taskService.complete(task.getId(), submitVariables);

		List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId())
				.taskAssignee(employee.getId()).list();
		assertTrue(tasks.isEmpty());
	}

	@Value
	private static class Employee {
		private String id;
		private String name;
		private String role;
	}
}
