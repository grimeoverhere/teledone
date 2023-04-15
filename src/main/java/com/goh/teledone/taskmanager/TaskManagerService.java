package com.goh.teledone.taskmanager;

import com.goh.teledone.taskmanager.model.Task;

import java.util.List;

public interface TaskManagerService {

    Long saveInbox(Long chatId, String text);
    Long saveInboxFromVoice(Long chatId, String text, String telegramFileId);
    List<Task> getTasks(Long chatId, TaskListType taskListType);
    void move(Long chatId, Long taskId, TaskListType taskListType);


}
