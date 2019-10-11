package com.fengye.example.service;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

/**
 * @author: Limy
 * @create: 2019/08/26 14:02
 * @description: ${description}
 */
@Service
public class FirstService {

    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private IdentityService identityService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private ProcessEngine processEngine;

    //开始审批流程
    public String start() {
        ProcessInstance first = runtimeService.startProcessInstanceByKey("first");
        System.out.println("开始审批流程，当前ID="+first.getId());
        return first.getId();
    }

    //获得某个人的任务别表
//    public List<Task> getTasks(String assignee) {
//        return taskService.createTaskQuery().taskCandidateUser(assignee).list();
//    }

    //提交审批申请
    public void insert(String pid) {
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(pid).list();
        //重新查找流程任务
        for (Task task : tasks) {
            System.out.println("当前流程节点：" + task.getName());
//            Execution exe = runtimeService.createExecutionQuery().executionId(task.getExecutionId()).singleResult();
//            System.out.println("经理参数：" + runtimeService.getVariable(exe.getId(), "managerVar"));
//            System.out.println("HR参数：" + runtimeService.getVariable(exe.getId(), "hrVar"));
        }
        System.out.println("写入审批表");
    }

    //当前节点名称
    public String getName(String pid) {
        //查询当前审批节点
        Task vacationAudit = taskService.createTaskQuery().processInstanceId(pid).singleResult();
        System.out.println("当前流程节点：" + vacationAudit.getName());
        return vacationAudit.getName();
    }


    //完成任务
    public void audit(Boolean auditFlag, String pid) {
//        Map<String, Object> taskVariables = new HashMap<String, Object>();
//        taskVariables.put("joinApproved", auditFlag);
//        taskService.complete(pid, taskVariables);

        //查询当前审批节点
        Task vacationAudit = taskService.createTaskQuery().processInstanceId(pid).singleResult();
        if (auditFlag) {//审批通过
            //设置流程参数：审批ID
//            Map<String, Object> args = new HashMap<>();
//            args.put("auditId", auditId);
//            //设置审批任务的执行人
//            taskService.claim(vacationAudit.getId(), req.getUserId().toString());
            //完成审批任务
            taskService.complete(vacationAudit.getId());
            System.out.println("审批通过，进入下一个流程");
        } else {
            //审批不通过，结束流程
            runtimeService.deleteProcessInstance(vacationAudit.getProcessInstanceId(),"审批不通过，结束流程");
            System.out.println("审批不通过，结束流程");
        }
    }



    //service层代码
    public InputStream getDiagram(String processInstanceId) {
        //获得流程实例
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();
        String processDefinitionId = StringUtils.EMPTY;
        if (processInstance == null) {
            //查询已经结束的流程实例
            HistoricProcessInstance processInstanceHistory =
                    historyService.createHistoricProcessInstanceQuery()
                            .processInstanceId(processInstanceId).singleResult();
            if (processInstanceHistory == null)
                return null;
            else
                processDefinitionId = processInstanceHistory.getProcessDefinitionId();
        } else {
            processDefinitionId = processInstance.getProcessDefinitionId();
        }

        //使用宋体
        String fontName = "宋体";
        //获取BPMN模型对象
        BpmnModel model = repositoryService.getBpmnModel(processDefinitionId);
        //获取流程实例当前的节点，需要高亮显示
        List<String> currentActs = Collections.EMPTY_LIST;
        if (processInstance != null)
            currentActs = runtimeService.getActiveActivityIds(processInstance.getId());

        return processEngine.getProcessEngineConfiguration()
                .getProcessDiagramGenerator()
                .generateDiagram(model, "png", currentActs, new ArrayList<String>(),
                        fontName, fontName, fontName, null, 1.0);
    }

}
