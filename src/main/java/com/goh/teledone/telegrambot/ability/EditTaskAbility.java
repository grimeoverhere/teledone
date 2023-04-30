package com.goh.teledone.telegrambot.ability;

import com.goh.teledone.BouncerService;
import com.goh.teledone.taskmanager.TaskManagerService;
import com.goh.teledone.telegrambot.TaskToTelegramMessageConverter;
import com.goh.teledone.telegrambot.TeledoneAbilityBot;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.abilitybots.api.objects.Reply;
import org.telegram.abilitybots.api.util.AbilityExtension;
import org.telegram.abilitybots.api.util.AbilityUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import java.util.Scanner;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EditTaskAbility implements AbilityExtension {


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


    //todo:: refactoring
    //todo:: добавить поддержку редактирования голосовых сообщений
    public Reply saveAnyMessageToTask() {
        BiConsumer<BaseAbilityBot, Update> action = (bot, upd) -> Mono.just(upd)
                .publishOn(Schedulers.boundedElastic())
//                .map(update -> taskManager.saveInbox(AbilityUtils.getChatId(update), update.getMessage().getText()))
                .subscribe(update -> {

                    var ids = new Scanner(update.getMessage().getReplyToMessage().getText()).useDelimiter("\\D+").tokens().map(Long::parseLong).collect(Collectors.toList());

                    var taskId = ids.get(0);
                    var messageId = ids.get(1);

                    var toDel = new DeleteMessage();
                    toDel.setMessageId(messageId.intValue());
                    toDel.setChatId(AbilityUtils.getChatId(upd));
                    bot.silent().execute(toDel);

                    var editedTask = taskManager.edit(AbilityUtils.getChatId(upd), taskId, upd.getMessage().getText()).get();
                    SendMessage sendMessageWithEditedTask = converter.convertTaskToSendMessage(editedTask.getT2(), AbilityUtils.getChatId(upd), editedTask.getT1());
                    sendMessageWithEditedTask.setReplyToMessageId(upd.getMessage().getMessageId());
                    var execute = abilityBot.silent().execute(sendMessageWithEditedTask);


                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(AbilityUtils.getChatId(upd));
                    execute.map(Message::getMessageId).ifPresent(sendMessage::setReplyToMessageId);
                    sendMessage.setText("Task with id=%d has been edited".formatted(taskId));
                    bot.silent().execute(sendMessage);

//                    return sendMessage;
                });

        return Reply.of(action,
                bouncerService::isAllowedToUseBot,
                Predicate.not(isStartingWithSlash()),
                isReplyToMessage(),
                //todo:: добавить проверку того, что сообщение, на которое совершается reply, startsWith "текст_из_сообщения_с_редактированием", сейчас это "Send me new description of the task with id=%d (messageId=%d)"
                hasText()
        );
    }


    private Predicate<Update> isReplyToMessage() {
        return update -> update != null
                && update.getMessage() != null
                && update.getMessage().isReply();
    }

    private Predicate<Update> isStartingWithSlash() {
        return update ->  update != null
                && update.getMessage() != null
                && update.getMessage().getText() != null
                && update.getMessage().getText().startsWith("/");
    }
    private Predicate<Update> hasText() {
        return update -> StringUtils.hasLength(update.getMessage().getText());
    }

}
