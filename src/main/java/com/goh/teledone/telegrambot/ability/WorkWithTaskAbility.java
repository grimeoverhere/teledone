package com.goh.teledone.telegrambot.ability;

import com.goh.teledone.BouncerService;
import com.goh.teledone.taskmanager.TaskListType;
import com.goh.teledone.taskmanager.TaskManagerService;
import com.goh.teledone.telegrambot.TaskToTelegramMessageConverter;
import com.goh.teledone.telegrambot.TeledoneAbilityBot;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.abilitybots.api.util.AbilityExtension;
import org.telegram.abilitybots.api.util.AbilityUtils;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import java.util.Locale;

import static org.telegram.abilitybots.api.objects.Locality.ALL;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;

@RequiredArgsConstructor
public abstract class WorkWithTaskAbility implements AbilityExtension {

    @NonNull
    protected TaskManagerService taskManager;
    @NonNull
    protected TeledoneAbilityBot abilityBot;
    @NonNull
    protected BouncerService bouncerService;
    @NonNull
    protected TaskToTelegramMessageConverter converter;

    @PostConstruct
    public void activateAbility() {
        abilityBot.addExtension(this);
    }

    protected abstract TaskListType getTaskListType();
    private String getActionInfo() {
        return "Returns all tasks from %s".formatted(getTaskListType().name().toLowerCase(Locale.ROOT));
    }

    public Ability getInbox() {
        return Ability.builder()
                .name(getTaskListType().name().toLowerCase(Locale.ROOT))
                .privacy(PUBLIC)
                .locality(ALL)
                .setStatsEnabled(true)
                .info(getActionInfo())
                .action(this::getAction)
                .build();
    }

    protected void getAction(MessageContext ctx) {
        Flux.fromStream(taskManager.getTasks(getTaskListType()).stream())
                .publishOn(Schedulers.boundedElastic())
                .subscribe(task -> {
                    try {
                        if (StringUtils.hasLength(task.getFileId())) {
                            abilityBot.execute(converter.convertTaskToSendVoice(task, AbilityUtils.getChatId(ctx.update())));
                        } else {
                            abilityBot.execute(converter.convertTaskToSendMessage(task, AbilityUtils.getChatId(ctx.update())));
                        }
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

}
