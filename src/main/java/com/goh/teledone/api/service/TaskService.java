package com.goh.teledone.api.service;

import com.goh.teledone.api.dto.TaskDto;
import com.goh.teledone.taskmanager.TaskListType;
import com.goh.teledone.taskmanager.TaskManagerServiceImpl;
import com.goh.teledone.taskmanager.model.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
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

    public List<TaskDto> getAllTasksFromVault(Long userId) {
        List<Task> inboxTasks = managerService.getTasks(userId, TaskListType.INBOX);
        List<Task> todayTasks = managerService.getTasks(userId, TaskListType.TODAY);
        List<Task> weekTasks = managerService.getTasks(userId, TaskListType.WEEK);
        List<Task> backlogTasks = managerService.getTasks(userId, TaskListType.BACKLOG);
        List<Task> doneTasks = managerService.getTasks(userId, TaskListType.DONE);

        List<TaskDto> responseList = new ArrayList<>();

        mergeTaskLists(responseList, inboxTasks, TaskListType.INBOX);
        mergeTaskLists(responseList, todayTasks, TaskListType.TODAY);
        mergeTaskLists(responseList, weekTasks, TaskListType.WEEK);
        mergeTaskLists(responseList, backlogTasks, TaskListType.BACKLOG);
        mergeTaskLists(responseList, doneTasks, TaskListType.DONE);

        log.info("getAllTasksFromVault(), responseList = " + responseList);

        return responseList;
    }

    public List<TaskDto> addTasks(Long userId, List<TaskDto> taskDtoList) {
        log.info("Add tasks request");
        log.info("User telegram id = " + userId);

        List<TaskDto> responseList = new ArrayList<>(taskDtoList);
        for (int i = 0; i < taskDtoList.size(); i++) {
            TaskDto task = taskDtoList.get(i);
            if (task.getId() == 0) {
                Long taskId = managerService.saveInbox(userId, task.getTitle(), task.getNotes(), Instant.parse(task.getCreateDate()));
                task.setId(taskId);
                moveTask(userId, task);
            }
            responseList.set(i, task);
        }

        checkConsistentData(userId, responseList);

        log.info("addTasks(), responseList = " + responseList);

        return responseList;
    }

    public List<TaskDto> editAndMoveTasks(Long userId, List<TaskDto> taskDtoList) {
        log.info("Edit tasks request");
        log.info("User telegram id = " + userId);
        List<TaskDto> responseList = new ArrayList<>(taskDtoList);
        List<TaskDto> tasksFromDb = getAllTasksFromVault(userId);
        for (TaskDto task : taskDtoList) {
            for (TaskDto dbTask : tasksFromDb) {
                if (Objects.equals(task.getId(), dbTask.getId())) {
                    Instant taskDate = Instant.parse(task.getModifyDate());
                    Instant dbTaskDate = Instant.parse(dbTask.getModifyDate());
                    if (taskDate.isAfter(dbTaskDate)) {
                        managerService.edit(userId, task.getId(), task.getTitle(), task.getNotes(), taskDate);
                        moveTask(userId, task);
                        log.info("editAndMoveTasks(), client task major = " + task);
                    } else if (taskDate.isBefore(dbTaskDate)) {
                        dbTask.setLocalId(task.getLocalId());
                        responseList.remove(task);
                        responseList.add(dbTask);
                        log.info("editAndMoveTasks(), db task major = " + dbTask);
                    }
                    break;
                }
            }
        }
        return responseList;
    }

    public void deleteTask(Long userId, Long taskId) {
        log.info("Deleting task with id = " + taskId);
        log.info("User telegram id = " + userId);
        List<TaskDto> tasksFromDb = getAllTasksFromVault(userId);
        for (TaskDto dbTask : tasksFromDb) {
            if (Objects.equals(taskId, dbTask.getId())) {
                managerService.delete(userId, taskId);
                break;
            }
        }
    }

    private void moveTask(Long userId, TaskDto task) {
        TaskListType type = switch (task.getType()) {
            case "INBOX" -> TaskListType.INBOX;
            case "TODAY" -> TaskListType.TODAY;
            case "WEEK" -> TaskListType.WEEK;
            case "BACKLOG" -> TaskListType.BACKLOG;
            default -> TaskListType.DONE;
        };
        managerService.moveToTaskList(userId, task.getId(), type, Instant.parse(task.getModifyDate()));
    }

    private void mergeTaskLists(List<TaskDto> responseList, List<Task> inputList, TaskListType type) {
        inputList.forEach((task) ->
                responseList.add(new TaskDto(
                        task.getId(),
                        null,
                        task.getCreateDate().toString(),
                        task.getModifyDate().toString(),
                        task.getTitle(),
                        task.getNotes(),
                        type.name(),
                        task.isDone()
                ))
        );
    }

    private void checkConsistentData(Long userId, List<TaskDto> checkingList) {

        // check added tasks by telegram
        List<TaskDto> tasksFromDb = getAllTasksFromVault(userId);
        for (TaskDto dbTask : tasksFromDb) {
            boolean isFound = false;
            for (TaskDto responseTask : checkingList) {
                if (dbTask.getId().equals(responseTask.getId())) {
                    isFound = true;
                    break;
                }
            }
            if (!isFound) {
                dbTask.setLocalId(UUID.randomUUID().toString());
                checkingList.add(dbTask);
            }
        }

        // check deleted tasks by telegram
        List<TaskDto> tempResponseTask = new ArrayList<>(checkingList);
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
                checkingList.remove(responseTask);
                log.info("Deleted redundant responseTask = " + responseTask);
            }
        }
    }

}
