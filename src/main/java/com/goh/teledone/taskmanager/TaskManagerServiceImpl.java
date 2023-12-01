package com.goh.teledone.taskmanager;

import com.goh.teledone.gpt.GPTService;
import com.goh.teledone.taskmanager.model.Task;
import com.goh.teledone.telegrambot.TeledoneAbilityBot;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.eclipse.collections.impl.block.comparator.primitive.LongFunctionComparator;
import org.springframework.stereotype.Service;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.goh.teledone.taskmanager.TaskListType.INBOX;
import static com.goh.teledone.taskmanager.TaskListType.TODAY;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskManagerServiceImpl implements TaskManagerService {

    private static final String MOVE_LOG = "Move a task with id={} from {} to {}.";
    private static final String MOVE_TRY_LOG = "Tried to move a task with id={} to {}. But there is no task with this id.";
    private static final String DELETE_LOG = "Delete a task with id={} from {} list.";
    private static final String TRIED_TO_DELETE_LOG = "Tried to Delete a task with id={}. But there is no task with this id.";
    private static final String EDIT_TASK_LOG = "Edit a task with id={} from {} list.";
    private static final String TRIED_TO_EDIT_TASK_LOG = "Tried to Edit a task with id={}. But there is no task with this id.";


    @NonNull
    private TeledoneAbilityBot teledoneBot;
    @NonNull
    private GPTService gptService;

    @Override
    public Long saveInbox(Long chatId, String text) {
        var newTaskId = lastTaskId(chatId) + 1;
        teledoneBot.db().getList("OLD_" + chatId).add(Task.builder().title(text).id(newTaskId).build());

        String processedText = gptService.sendMessageToGPT(text);
        taskList(chatId, INBOX).add(Task.builder().title(processedText).id(newTaskId).build());

        teledoneBot.db().commit();
        return newTaskId;
    }

    @Override
    public Long saveInboxFromVoice(Long chatId, String text, String telegramFileId) {
        var newTaskId = lastTaskId(chatId) + 1;
        teledoneBot.db().getList("OLD_" + chatId).add(Task.builder().title(text).fileId(telegramFileId).id(newTaskId).build());

        String processedText = gptService.sendMessageToGPT(text);
        taskList(chatId, INBOX).add(Task.builder().title(processedText).fileId(telegramFileId).id(newTaskId).build());

        teledoneBot.db().commit();
        return newTaskId;
    }

    @Override
    public List<Task> getTasks(Long chatId, TaskListType taskListType) {
        return taskList(chatId, taskListType);
    }

    @Override
    public void moveToTaskList(Long chatId, Long taskId, TaskListType taskListType) {
        moveTask(chatId, taskId, taskListType);
    }

    @Override
    public void delete(Long chatId, Long taskId) {
        Optional<Tuple2<TaskListType, Task>> tup = findTask(chatId, taskId);
        if (tup.isPresent()) {
            var from = tup.get().getT1();
            var task = tup.get().getT2();
            log.info(DELETE_LOG, taskId, from);
            taskList(chatId, from).remove(task);
            teledoneBot.db().commit();
        } else {
            log.info(TRIED_TO_DELETE_LOG, taskId);
        }
    }

    @Override
    public Optional<Tuple2<TaskListType, Task>> edit(Long chatId, Long taskId, String text) {
        var tup = findTask(chatId, taskId);
        if (tup.isPresent()) {
            var from = tup.get().getT1();
            var task = tup.get().getT2();
            taskList(chatId, from).remove(task);
            task.setTitle(text);
            taskList(chatId, from).add(task);
            log.info(EDIT_TASK_LOG, taskId, from);
            teledoneBot.db().commit();
        } else {
            log.info(TRIED_TO_EDIT_TASK_LOG, taskId);
        }
        return tup;
    }

    private void moveTask(Long chatId, Long taskId, TaskListType to) {
        var tup = findTask(chatId, taskId);
        if (tup.isPresent()) {
            var from = tup.get().getT1();
            var task = tup.get().getT2();
            log.info(MOVE_LOG, taskId, from, TODAY);
            taskList(chatId, from).remove(task);
            taskList(chatId, to).add(task);
            teledoneBot.db().commit();
        } else {
            log.info(MOVE_TRY_LOG, taskId, TODAY);
        }
    }

    private Optional<Tuple2<TaskListType, Task>> findTask(Long chatId, Long taskId) {
        return StreamEx.of(TaskListType.stream())
                .map(type -> Tuples.of(type, taskList(chatId, type)))
                .filter(tup -> tup.getT2().stream().anyMatch(task -> task.getId().equals(taskId)))
                .map(tup -> {
                    Optional<Task> foundTask = tup.getT2().stream()
                            .filter(task -> task.getId().equals(taskId))
                            .findFirst();
                    return foundTask.map(task -> Tuples.of(tup.getT1(), task)).orElse(null);
                })
                .nonNull()
                .findAny();
    }

    private Long lastTaskId(Long chatId) {
        return allTasks(chatId).stream().max(new LongFunctionComparator<>(Task::getId))
                .map(Task::getId)
                .orElse(0L);
    }

    private List<Task> allTasks(Long chatId) {
        return TaskListType.stream()
                .map(taskListType -> taskList(chatId, taskListType))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private String name(Long chatId, TaskListType type) {
        return type.name() + "_"  +chatId;
    }
    private List<Task> taskList(Long chatId, TaskListType type) {
        return teledoneBot.db().getList(name(chatId, type));
    }

}
