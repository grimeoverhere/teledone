package com.goh.teledone.telegrambot.ability;

import com.goh.teledone.BouncerService;
import com.goh.teledone.stt.ActionWrapperService;
import com.goh.teledone.taskmanager.TaskListType;
import com.goh.teledone.taskmanager.TaskManagerService;
import com.goh.teledone.telegrambot.TaskToTelegramMessageConverter;
import com.goh.teledone.telegrambot.TeledoneAbilityBot;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.abilitybots.api.objects.Reply;
import org.telegram.abilitybots.api.util.AbilityUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuples;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

//todo:: split in two abilities
@Component
public class ShowAndAddToInboxAbility extends ShowTaskListAbility {
    @NonNull
    protected ActionWrapperService actionWrapper;

    public ShowAndAddToInboxAbility(
            @NonNull ActionWrapperService actionWrapper,
            @NonNull TaskManagerService taskManager,
            @NonNull TeledoneAbilityBot abilityBot,
            @NonNull BouncerService bouncerService,
            @NonNull TaskToTelegramMessageConverter converter
    ) {
        super(taskManager, abilityBot, bouncerService, converter);
        this.actionWrapper = actionWrapper;
    }

    @Override
    protected TaskListType getTaskListType() {
        return TaskListType.INBOX;
    }

    public Reply speechToTextAndThenToTask() {
        BiConsumer<BaseAbilityBot, Update> action = (bot, upd) -> Mono.just(upd)
                .publishOn(Schedulers.boundedElastic())
                .map(actionWrapper::processMessage)
                .map(voiceToText -> Tuples.of(taskManager.saveInboxFromVoice(AbilityUtils.getChatId(upd), voiceToText, upd.getMessage().getVoice().getFileId()), voiceToText))
                .map(tup -> {
                    var voiceToText = tup.getT2();
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(AbilityUtils.getChatId(upd));
                    sendMessage.setReplyToMessageId(upd.getMessage().getMessageId());
                    sendMessage.setText("Saved recognized text as a task with id = %d. \n\nTask text: %s".formatted(tup.getT1(), voiceToText));
                    return sendMessage;
                })
                .subscribe(message -> bot.silent().execute(message));

        return Reply.of(action,
                hasVoice(),
                bouncerService::isAllowedToUseBot
        );
    }

    public Reply saveAnyMessageToTask() {
        BiConsumer<BaseAbilityBot, Update> action = (bot, upd) -> Mono.just(upd)
                .publishOn(Schedulers.boundedElastic())
                .map(update -> taskManager.saveInbox(AbilityUtils.getChatId(update), update.getMessage().getText()))
                .map(taskId -> {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(AbilityUtils.getChatId(upd));
                    sendMessage.setReplyToMessageId(upd.getMessage().getMessageId());
                    sendMessage.setText("Added a new task with id=%d to Inbox".formatted(taskId));
                    return sendMessage;
                })
                .subscribe(message -> bot.silent().execute(message));

        return Reply.of(action,
                bouncerService::isAllowedToUseBot,
                Predicate.not(isReplyToMessage()),
                Predicate.not(isStartingWithSlash()),
                hasText()
        );
    }


    private Predicate<Update> isStartingWithSlash() {
        return update ->  update != null
                && update.getMessage() != null
                && update.getMessage().getText() != null
                && update.getMessage().getText().startsWith("/");
    }

    private Predicate<Update> isReplyToMessage() {
        return update -> update != null
                && update.getMessage() != null
                && update.getMessage().isReply();
    }
    private Predicate<Update> hasText() {
        return update -> StringUtils.hasLength(update.getMessage().getText());
    }
    private Predicate<Update> hasVoice() {
        return update -> update.hasMessage() && update.getMessage().getVoice() != null;
    }
}
