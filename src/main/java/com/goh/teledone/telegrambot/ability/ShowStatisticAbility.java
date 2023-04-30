package com.goh.teledone.telegrambot.ability;

import com.goh.teledone.BouncerService;
import com.goh.teledone.taskmanager.TaskListType;
import com.goh.teledone.taskmanager.TaskManagerService;
import com.goh.teledone.telegrambot.TeledoneAbilityBot;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.util.streamex.StreamEx;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.abilitybots.api.util.AbilityExtension;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

import static org.telegram.abilitybots.api.objects.Locality.ALL;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;

@Component
@RequiredArgsConstructor
public class ShowStatisticAbility implements AbilityExtension {



    @NonNull
    protected TaskManagerService taskManager;
    @NonNull
    protected TeledoneAbilityBot abilityBot;
    @NonNull
    protected BouncerService bouncerService;

    @PostConstruct
    public void activateAbility() {
        abilityBot.addExtension(this);
    }

    public Ability getStatistic() {
        return Ability.builder()
                .name("statistic")
                .privacy(PUBLIC)
                .locality(ALL)
                .setStatsEnabled(true)
                .info("Show amount of tasks for today, this week and backlog. Shows completed tasks as well;")
                .action(this::getAction)
                .build();
    }

    private void getAction(MessageContext context) {
        var text = StreamEx.of(TaskListType.stream())
                .mapToEntry(taskListType -> taskManager.getTasks(context.chatId(), taskListType))
                .mapValues(List::size)
                .join(": ")
                .collect(Collectors.joining("\n", "Your statistic:\n\n", "\n\nWell done!"));

        context.bot().silent().send(text, context.chatId());
    }

}
