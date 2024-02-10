package com.goh.teledone.api.controller;

import com.goh.teledone.api.dto.TaskDto;
import com.goh.teledone.api.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping(value = "/tasks/{userId}")
    public List<TaskDto> getTasks(@PathVariable(value = "userId") String userId) {
        return taskService.getAllTasksFromVault(Long.parseLong(userId));
    }


    @PostMapping(value = "/tasks/{userId}")
    public List<TaskDto> addTasks(@RequestBody List<TaskDto> taskDtoList,
                                        @PathVariable(value = "userId") String userId) {
        Long telegramUserId = Long.valueOf(userId);
        return taskService.addTasks(telegramUserId, taskDtoList);
    }

    @PutMapping(value = "/tasks/{userId}")
    public List<TaskDto> updateTasks(@RequestBody List<TaskDto> taskDtoList,
                                      @PathVariable(value = "userId") String userId) {
        Long telegramUserId = Long.valueOf(userId);
        return taskService.editAndMoveTasks(telegramUserId, taskDtoList);
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
