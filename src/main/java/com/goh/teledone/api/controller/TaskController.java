package com.goh.teledone.api.controller;

import com.goh.teledone.api.dto.TaskDtoResponse;
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

    @GetMapping(value = "/tasks", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TaskDtoResponse> getTasks(/*@RequestParam(value = "category", required = false) String category,
                                          @RequestParam(value = "id") String id*/) {
        log.info("Get tasks request");
        List<Task> inboxTasks = managerService.getTasks(570984438L, TaskListType.INBOX);
        List<TaskDtoResponse> responseList = new ArrayList<>();

        inboxTasks.forEach((task) ->
                responseList.add(new TaskDtoResponse(
                        task.getTitle(),
                        TaskListType.INBOX.name()
                )));
        log.info("Get tasks request. tasks: " + inboxTasks);
        // получаем список задач по категории (inbox, today, week, backlog, done)
        // если категория не указана, то возвращаются все задачи
        return responseList;
    }
/*

    @PostMapping(value = "/")
    public TaskDtoResponse addTask(@RequestBody TaskDtoRequest taskDtoRequest,
                                    @RequestParam(value = "id") String id) {
        // добавляем задачу в inbox
        return null;
    }


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
    }
*/

}
