package com.goh.teledone.telegrambot.ability;

import com.goh.teledone.BouncerService;
import com.goh.teledone.taskmanager.TaskListType;
import com.goh.teledone.taskmanager.TaskManagerService;
import com.goh.teledone.telegrambot.TaskToTelegramMessageConverter;
import com.goh.teledone.telegrambot.TeledoneAbilityBot;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ShowTodayTaskAbility extends ShowTaskListAbility {

    public ShowTodayTaskAbility(@NonNull TaskManagerService taskManager,
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
        return TaskListType.TODAY;
    }

}
