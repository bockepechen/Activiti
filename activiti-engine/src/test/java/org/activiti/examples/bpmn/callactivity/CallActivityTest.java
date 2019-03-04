/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.examples.bpmn.callactivity;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.engine.test.Deployment;

/**

 */
public class CallActivityTest extends PluggableActivitiTestCase {

  @Deployment(resources = { "org/activiti/examples/bpmn/callactivity/orderProcess.bpmn20.xml", "org/activiti/examples/bpmn/callactivity/checkCreditProcess.bpmn20.xml" })
  public void testOrderProcessWithCallActivity() {
    // After the process has started, the 'verify credit history' task
    // should be active
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("orderProcess");
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task verifyCreditTask = taskQuery.singleResult();
    assertEquals("Verify credit history", verifyCreditTask.getName());

    // Verify with Query API
    ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(pi.getId()).singleResult();
    assertNotNull(subProcessInstance);
    assertEquals(pi.getId(), runtimeService.createProcessInstanceQuery().subProcessInstanceId(subProcessInstance.getId()).singleResult().getId());

    // Completing the task with approval, will end the subprocess and
    // continue the original process
    taskService.complete(verifyCreditTask.getId(), CollectionUtil.singletonMap("creditApproved", true));
    Task prepareAndShipTask = taskQuery.singleResult();
    assertEquals("Prepare and Ship", prepareAndShipTask.getName());
  }

  @Deployment(resources = { "org/activiti/examples/bpmn/callactivity/mainProcess.bpmn20.xml", "org/activiti/examples/bpmn/callactivity/childProcess.bpmn20.xml" })
  public void testCallActivityWithModeledDataObjectsInSubProcess() {
    // After the process has started, the 'verify credit history' task should be active
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("mainProcess");
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task verifyCreditTask = taskQuery.singleResult();
    assertEquals("User Task 1", verifyCreditTask.getName());

    // Verify with Query API
    ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(pi.getId()).singleResult();
    assertNotNull(subProcessInstance);
    assertEquals(pi.getId(), runtimeService.createProcessInstanceQuery().subProcessInstanceId(subProcessInstance.getId()).singleResult().getId());

    assertEquals("Batman", runtimeService.getVariable(subProcessInstance.getId(), "Name"));
  }

  @Deployment(resources = { "org/activiti/examples/bpmn/callactivity/mainProcess.bpmn20.xml",
                            "org/activiti/examples/bpmn/callactivity/childProcess.bpmn20.xml",
                            "org/activiti/examples/bpmn/callactivity/mainProcessBusinessKey.bpmn20.xml",
                            "org/activiti/examples/bpmn/callactivity/mainProcessInheritBusinessKey.bpmn20.xml"})
  public void testCallActivityWithBusinessKey() {
    // No use of business key attributes
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("mainProcess");
    ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(pi.getId()).singleResult();
    assertNull(subProcessInstance.getBusinessKey());

    // Modeled using expression: businessKey="${busKey}"
    Map<String,Object> variables = new HashMap<>();
    variables.put("busKey", "123");
    pi = runtimeService.startProcessInstanceByKey("mainProcessBusinessKey", variables);
    subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(pi.getId()).singleResult();
    assertEquals("123", subProcessInstance.getBusinessKey());

    // Inherit business key
    pi = runtimeService.startProcessInstanceByKey("mainProcessInheritBusinessKey", "123");
    subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(pi.getId()).singleResult();
    assertEquals("123", subProcessInstance.getBusinessKey());
  }

  @Deployment(resources = {"org/activiti/examples/bpmn/callactivity/parentProcess.bpmn20.xml",
          "org/activiti/examples/bpmn/callactivity/subProcess.bpmn20.xml"})
  public void testCheckSubProcessTaskWhenCallActivity(){
    // After the process has started, the 'subProcess task should be active
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("parentproc-843144bc-3797-40db-8edc-d23190b118e3");
    Task verifyCreditTask = taskService.createTaskQuery().singleResult();
    assertEquals("my-task", verifyCreditTask.getName());

    // Verify with Query API
    ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(pi.getId()).singleResult();
    assertNotNull(subProcessInstance);
  }
}
