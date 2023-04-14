package com.goh.teledone.taskmanager;

import com.goh.teledone.taskmanager.model.Task;
import com.goh.teledone.telegrambot.TeledoneAbilityBot;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.springframework.stereotype.Service;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.goh.teledone.taskmanager.TaskListType.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskManagerServiceImpl implements TaskManagerService {

    private static final String MOVE_LOG = "Move a task with id={} from {} to {}.";
    private static final String MOVE_TRY_LOG = "Tried to move a task with id={} from {} to {}. But there is no task with this id.";

    private final AtomicLong identifier = new AtomicLong(0L); //todo:: as variable in DBContext

    @NonNull
    private TeledoneAbilityBot teledoneBot;

    @Override
    public Long saveInbox(String text) {
        var newTaskId = identifier.incrementAndGet();
        taskList(INBOX).add(Task.builder().title(text).id(newTaskId).build());
        return newTaskId;
    }

    @Override
    public Long saveInboxFromVoice(String text, String telegramFileId) {
        var newTaskId = identifier.incrementAndGet();
        taskList(INBOX).add(Task.builder().title(text).fileId(telegramFileId).id(newTaskId).build());
        return newTaskId;
    }


    @Override
    public List<Task> getInbox() {
        return taskList(INBOX);
    }

    @Override
    public List<Task> getAllTasks() {
        return allTasks();
    }

    @Override
    public void moveToday(Long taskId) {
        moveTask(taskId, TODAY);
    }

    @Override
    public void moveThisWeek(Long taskId) {
        moveTask(taskId, WEEK);
    }

    @Override
    public void moveBacklog(Long taskId) {
        moveTask(taskId, BACKLOG);
    }

    private void moveTask(Long taskId, TaskListType to) {
        var tup = findTask(taskId);
        if (tup.isPresent()) {
            var from = tup.get().getT1();
            var task = tup.get().getT2();
            log.info(MOVE_LOG, taskId, null, TODAY);
            taskList(from).remove(task);
            taskList(to).add(task);
        } else {
            log.info(MOVE_TRY_LOG, taskId, null, TODAY);
        }
    }

    private Optional<Tuple2<TaskListType, Task>> findTask(Long taskId) {
        return StreamEx.of(TaskListType.stream())
                .map(type -> Tuples.of(type, taskList(type)))
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

    private List<Task> allTasks() {
        return TaskListType.stream()
                .map(this::taskList)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private List<Task> taskList(TaskListType type) {
        return teledoneBot.db().getList(type.name());
    }

}
