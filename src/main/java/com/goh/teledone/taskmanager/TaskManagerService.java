package com.goh.teledone.taskmanager;

import com.goh.teledone.taskmanager.model.Task;
import reactor.util.function.Tuple2;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface TaskManagerService {

    Long saveInbox(Long chatId, String text, String notes, ZonedDateTime modifyDate);
    Long saveInboxFromVoice(Long chatId, String text, String telegramFileId);
    List<Task> getTasks(Long chatId, TaskListType taskListType);
    void moveToTaskList(Long chatId, Long taskId, TaskListType taskListType, ZonedDateTime modifyDate);
    void delete(Long chatId, Long taskId);
    Optional<Tuple2<TaskListType, Task>> edit(Long chatId, Long taskId, String text, String notes, ZonedDateTime modifyDate);

}
