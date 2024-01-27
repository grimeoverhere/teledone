package com.goh.teledone.api.controller;

import com.goh.teledone.api.dto.TaskDto;
import com.goh.teledone.api.service.TaskService;
import com.goh.teledone.taskmanager.TaskListType;
import com.goh.teledone.taskmanager.TaskManagerServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
public class TaskController {

    private final TaskService taskService;
    private final TaskManagerServiceImpl managerService;

    public TaskController(TaskService taskService, TaskManagerServiceImpl managerService) {
        this.taskService = taskService;
        this.managerService = managerService;
    }
/*

    @GetMapping(value = "/tasks/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TaskDto> getTasks(@PathVariable(value = "id") String id) {
        log.info("Get tasks request");
        log.info("User telegram id = " + id);
        Long userId = Long.valueOf(id);
        List<Task> inboxTasks = managerService.getTasks(userId, TaskListType.INBOX);
        List<Task> todayTasks = managerService.getTasks(userId, TaskListType.TODAY);
        List<Task> weekTasks = managerService.getTasks(userId, TaskListType.WEEK);
        List<Task> backlogTasks = managerService.getTasks(userId, TaskListType.BACKLOG);

        List<TaskDto> responseList = new ArrayList<>();

        inboxTasks.forEach((task) ->
                responseList.add(new TaskDto(
                        task.getTitle(),
                        TaskListType.INBOX.name()
                ))
        );
        todayTasks.forEach((task) ->
                responseList.add(new TaskDto(
                        task.getTitle(),
                        TaskListType.TODAY.name()
                ))
        );
        weekTasks.forEach((task) ->
                responseList.add(new TaskDto(
                        task.getTitle(),
                        TaskListType.WEEK.name()
                ))
        );
        backlogTasks.forEach((task) ->
                responseList.add(new TaskDto(
                        task.getTitle(),
                        TaskListType.BACKLOG.name()
                ))
        );
        log.info("Get tasks request. tasks: " + inboxTasks);
        return responseList;
    }
*/

    @PostMapping(value = "/tasks/{userId}")
    public List<TaskDto> addAndGetTasks(@RequestBody List<TaskDto> taskDtoList,
                                        @PathVariable(value = "userId") String userId) {
        Long telegramUserId = Long.valueOf(userId);
        return taskService.addAndGetTasks(telegramUserId, taskDtoList);
    }

    @PutMapping(value = "/tasks/{userId}")
    public void updateTasks(@RequestBody List<TaskDto> taskDtoList,
                                      @PathVariable(value = "userId") String userId) {
        Long telegramUserId = Long.valueOf(userId);
        taskService.editAndMoveTasks(telegramUserId, taskDtoList);
    }

    @DeleteMapping(value = "/tasks/{userId}/{taskId}")
    public void deleteTask(@PathVariable(value = "taskId") String taskId,
                           @PathVariable(value = "userId") String userId) {
        Long telegramUserId = Long.valueOf(userId);
        Long currentTaskId = Long.valueOf(taskId);
        taskService.deleteTask(telegramUserId, currentTaskId);
    }
/*


    @PutMapping(value = "/move")
    public void moveTask(@RequestParam(value = "category") String category,
                         @RequestParam(value = "taskId") String taskId,
                           @RequestParam(value = "id") String id) {
        // перемещаем задачу в указанную категорию (inbox, today, week, backlog, done)
    }
*/

}
