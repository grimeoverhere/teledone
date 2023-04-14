package com.goh.teledone.taskmanager;

import com.goh.teledone.taskmanager.model.Task;

import java.util.List;

public interface TaskManagerService {

    Long saveInbox(String text);
    Long saveInboxFromVoice(String text, String telegramFileId);
    List<Task> getInbox();
    List<Task> getTasks(TaskListType taskListType);
    void move(Long taskId, TaskListType taskListType);


}
