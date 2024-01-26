package com.goh.teledone.api.controller;

import com.goh.teledone.api.dto.TaskDto;
import com.goh.teledone.taskmanager.TaskListType;
import com.goh.teledone.taskmanager.TaskManagerServiceImpl;
import com.goh.teledone.taskmanager.model.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
public class TaskController {

    private final TaskManagerServiceImpl managerService;

    public TaskController(TaskManagerServiceImpl managerService) {
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
    public List<TaskDto> syncTasks(@RequestBody List<TaskDto> taskDtoList,
                            @PathVariable(value = "userId") String userId) {
        log.info("Synchronize tasks request");
        log.info("User telegram id = " + userId);
        Long telegramUserId = Long.valueOf(userId);
        List<TaskDto> responseList = new ArrayList<>();

        for (TaskDto task : taskDtoList) {
            if (task.getId() == 0) {
                Long taskId = managerService.saveInbox(telegramUserId, task.getTitle());
                task.setId(taskId);
            }

            TaskListType type = switch (task.getType()) {
                case "INBOX" -> TaskListType.INBOX;
                case "TODAY" -> TaskListType.TODAY;
                case "WEEK" -> TaskListType.WEEK;
                default -> TaskListType.BACKLOG;
            };
            managerService.moveToTaskList(telegramUserId, task.getId(), type);

            if (task.isDone()) {
                managerService.moveToTaskList(telegramUserId, task.getId(), TaskListType.DONE);
            }

            responseList.add(task);
        }

        return responseList;
    }

/*
    @PutMapping(value = "/")
    public TaskDtoResponse updateTask(@RequestBody TaskDtoRequest taskDtoRequest,
                                   @RequestParam(value = "id") String id) {
        // изменяем задачу (любую)
        return null;
    }

    @DeleteMapping(value = "/")
    public void deleteTask(@RequestParam(value = "taskId") String taskId,
                                      @RequestParam(value = "id") String id) {
        // удаляем задачу (любую)
    }


    @PutMapping(value = "/move")
    public void moveTask(@RequestParam(value = "category") String category,
                         @RequestParam(value = "taskId") String taskId,
                           @RequestParam(value = "id") String id) {
        // перемещаем задачу в указанную категорию (inbox, today, week, backlog, done)
    }*/

}
