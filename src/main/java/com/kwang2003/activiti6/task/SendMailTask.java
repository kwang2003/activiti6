package com.kwang2003.activiti6.task;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SendMailTask implements JavaDelegate {
	@Override
	public void execute(DelegateExecution execution) {
		log.info("调用邮件网关发送邮件,processId={},businessKey={}",execution.getProcessDefinitionId(),execution.getProcessInstanceBusinessKey());
	}
}
