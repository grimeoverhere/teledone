package com.goh.teledone.telegrambot.ability;

import com.goh.teledone.BouncerService;
import com.goh.teledone.taskmanager.TaskListType;
import com.goh.teledone.taskmanager.TaskManagerService;
import com.goh.teledone.telegrambot.TaskToTelegramMessageConverter;
import com.goh.teledone.telegrambot.TeledoneAbilityBot;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.abilitybots.api.util.AbilityUtils;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;

@Component
public class DoneTaskAbility extends WorkWithTaskAbility {

    public DoneTaskAbility(@NonNull TaskManagerService taskManager,
                           @NonNull TeledoneAbilityBot abilityBot,
                           @NonNull BouncerService bouncerService,
                           @NonNull TaskToTelegramMessageConverter converter) {
        super(taskManager, abilityBot, bouncerService, converter);
    }

    @Override
    @PostConstruct
    public void activateAbility() {
        abilityBot.addExtension(this);
    }

    @Override
    protected TaskListType getTaskListType() {
        return TaskListType.DONE;
    }

    @Override
    protected void getAction(MessageContext ctx) {
        Flux.fromStream(taskManager.getTasks(getTaskListType()).stream())
                .publishOn(Schedulers.boundedElastic())
                .subscribe(task -> {
                    try {
                        if (StringUtils.hasLength(task.getFileId())) {
                            abilityBot.execute(converter.convertTaskToSendVoiceWithoutKeyboard(task, AbilityUtils.getChatId(ctx.update())));
                        } else {
                            abilityBot.execute(converter.convertTaskToSendMessageWithoutKeyboard(task, AbilityUtils.getChatId(ctx.update())));
                        }
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

}
