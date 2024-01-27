package com.goh.teledone.api.service;

import com.goh.teledone.api.dto.TaskDto;
import com.goh.teledone.taskmanager.TaskListType;
import com.goh.teledone.taskmanager.TaskManagerServiceImpl;
import com.goh.teledone.taskmanager.model.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class TaskService {

    private final TaskManagerServiceImpl managerService;

    public TaskService(TaskManagerServiceImpl managerService) {
        this.managerService = managerService;
    }

    public List<TaskDto> addAndGetTasks(Long userId, List<TaskDto> taskDtoList) {
        log.info("Synchronize tasks request");
        log.info("User telegram id = " + userId);

        List<TaskDto> responseList = new ArrayList<>(taskDtoList);

        for (int i = 0; i < taskDtoList.size(); i++) {
            TaskDto task = taskDtoList.get(i);
            if (task.getId() == 0) {
                Long taskId = managerService.saveInbox(userId, task.getTitle());
                task.setId(taskId);
                moveTask(userId, task);
            }
            responseList.set(i, task);
        }

        List<TaskDto> tasksFromDb = getAllTasksFromVault(userId);
        for (TaskDto dbTask : tasksFromDb) {
            boolean isFound = false;
            for (TaskDto responseTask : responseList) {
                if (dbTask.getId().equals(responseTask.getId())) {
                    isFound = true;
                    break;
                }
            }
            if (!isFound) {
                dbTask.setLocalId(UUID.randomUUID().toString());
                responseList.add(dbTask);
            }
        }

        // check delete task by server
        List<TaskDto> tempResponseTask = new ArrayList<>(responseList);
        for (TaskDto responseTask : tempResponseTask) {
            log.info("checking responseTask = " + responseTask);
            boolean isFoundDeleted = true;
            for (TaskDto dbTask : tasksFromDb) {
                if (dbTask.getId().equals(responseTask.getId())) {
                    isFoundDeleted = false;
                    break;
                }
            }
            if (isFoundDeleted) {
                responseList.remove(responseTask);
                log.info("Deleted redundant responseTask = " + responseTask);
            }
        }

        log.info("addAndGetTasks(), responseList = " + responseList);

        return responseList;
    }

    public void editAndMoveTasks(Long userId, List<TaskDto> taskDtoList) {
        for (TaskDto task : taskDtoList) {
            managerService.edit(userId, task.getId(), task.getTitle());
            moveTask(userId, task);
        }
    }

    public void deleteTask(Long userId, Long taskId) {
        log.info("Deleting task with id = " + taskId);
        List<TaskDto> tasksFromDb = getAllTasksFromVault(userId);
        for (TaskDto dbTask : tasksFromDb) {
            if (Objects.equals(taskId, dbTask.getId())) {
                managerService.delete(userId, taskId);
                log.info("Task deleted with id = " + taskId);
                break;
            }
        }
    }

    private void moveTask(Long userId, TaskDto task) {
        TaskListType type = switch (task.getType()) {
            case "INBOX" -> TaskListType.INBOX;
            case "TODAY" -> TaskListType.TODAY;
            case "WEEK" -> TaskListType.WEEK;
            default -> TaskListType.BACKLOG;
        };
        managerService.moveToTaskList(userId, task.getId(), type);

        /*if (task.isDone()) {
            managerService.moveToTaskList(userId, task.getId(), TaskListType.DONE);
        }*/
    }

    private List<TaskDto> getAllTasksFromVault(Long userId) {
        List<Task> inboxTasks = managerService.getTasks(userId, TaskListType.INBOX);
        List<Task> todayTasks = managerService.getTasks(userId, TaskListType.TODAY);
        List<Task> weekTasks = managerService.getTasks(userId, TaskListType.WEEK);
        List<Task> backlogTasks = managerService.getTasks(userId, TaskListType.BACKLOG);
        //List<Task> doneTasks = managerService.getTasks(userId, TaskListType.DONE);

        List<TaskDto> responseList = new ArrayList<>();

        mergeTaskLists(responseList, inboxTasks, TaskListType.INBOX);
        mergeTaskLists(responseList, todayTasks, TaskListType.TODAY);
        mergeTaskLists(responseList, weekTasks, TaskListType.WEEK);
        mergeTaskLists(responseList, backlogTasks, TaskListType.BACKLOG);

        /*for (Task doneTask : doneTasks) {
            for (int j = 0; j < responseList.size(); j++) {
                TaskDto responseTask = responseList.get(j);
                if (Objects.equals(doneTask.getId(), responseTask.getId())) {
                    responseTask.setDone(true);
                    responseList.set(j, responseTask);
                }
            }
        }*/

        log.info("getAllTasksFromVault(), responseList = " + responseList);

        return responseList;
    }

    private void mergeTaskLists(List<TaskDto> responseList, List<Task> inputList, TaskListType type) {
        inputList.forEach((task) ->
                responseList.add(new TaskDto(
                        task.getId(),
                        null,
                        task.getTitle(),
                        type.name(),
                        task.isDone()
                ))
        );
    }

}
