package com.goh.teledone.telegrambot.ability;

import com.goh.teledone.BouncerService;
import com.goh.teledone.model.TaskMovementDTO;
import com.goh.teledone.taskmanager.TaskListType;
import com.goh.teledone.taskmanager.TaskManagerService;
import com.goh.teledone.telegrambot.TaskToTelegramMessageConverter;
import com.goh.teledone.telegrambot.TeledoneAbilityBot;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.abilitybots.api.objects.Reply;
import org.telegram.abilitybots.api.util.AbilityExtension;
import org.telegram.abilitybots.api.util.AbilityUtils;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
public class HandleTaskCallbackQueryAbility implements AbilityExtension {

    private static final String TASK_HAS_BEEN_MOVED_MESSAGE = """
            Task %d has been moved to status %s.
            """;

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

    public Reply handleInlineKeyboardReply() {
        BiConsumer<BaseAbilityBot, Update> action = (bot, upd) -> Mono.just(upd)
                .publishOn(Schedulers.boundedElastic())
                .map(update -> {
                    CallbackQuery callbackQuery = update.getCallbackQuery();
                    TaskMovementDTO taskMovementDTO = converter.fromJson(callbackQuery.getData(), TaskMovementDTO.class);
                    return new CallbackProperties(callbackQuery, taskMovementDTO);
                })
                .doOnNext(prop -> {
                    switch (prop.taskMovementDTO().taskAction()) {
                        case MOVE_TODAY -> taskManager.moveToTaskList(AbilityUtils.getChatId(upd), prop.taskMovementDTO().taskId(), TaskListType.TODAY);
                        case MOVE_TO_THIS_WEEK -> taskManager.moveToTaskList(AbilityUtils.getChatId(upd), prop.taskMovementDTO().taskId(), TaskListType.WEEK);
                        case MOVE_TO_BACKLOG -> taskManager.moveToTaskList(AbilityUtils.getChatId(upd), prop.taskMovementDTO().taskId(), TaskListType.BACKLOG);
                        case MARK_DONE -> taskManager.moveToTaskList(AbilityUtils.getChatId(upd), prop.taskMovementDTO().taskId(), TaskListType.DONE);
                        case EDIT -> {
                            //TODO
//                            taskManager.moveToTaskList(AbilityUtils.getChatId(upd), prop.taskMovementDTO().taskId(), TaskListType.DONE);
                        }
                        case DELETE -> taskManager.delete(AbilityUtils.getChatId(upd), prop.taskMovementDTO().taskId());
                    }
                })
//                .doOnNext(tup -> taskManager.move(AbilityUtils.getChatId(upd), tup.getT2().taskId(), tup.getT2().taskAction()))
                .subscribe(prop -> {
                    switch (prop.taskMovementDTO().taskAction()) {
                        case MOVE_TODAY, MOVE_TO_THIS_WEEK, MOVE_TO_BACKLOG, MARK_DONE, DELETE -> {
                            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery(prop.callbackQuery().getId());
                            answerCallbackQuery.setShowAlert(true);
                            answerCallbackQuery.setText(TASK_HAS_BEEN_MOVED_MESSAGE.formatted(
                                    prop.taskMovementDTO().taskId(), prop.taskMovementDTO().taskAction().getActionTextForButton()));
                            bot.silent().execute(answerCallbackQuery);

                            DeleteMessage deleteMessage = new DeleteMessage();
                            deleteMessage.setChatId(AbilityUtils.getChatId(upd));
                            deleteMessage.setMessageId(prop.callbackQuery().getMessage().getMessageId());
                            bot.silent().execute(deleteMessage);
                        }
                        case EDIT -> {
                            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery(prop.callbackQuery().getId());
                            bot.silent().execute(answerCallbackQuery);

                            Integer messageId = prop.callbackQuery().getMessage().getMessageId();
                            var sendMessage = new SendMessage();
                            sendMessage.setChatId(AbilityUtils.getChatId(upd));
                            sendMessage.setText("Send me new description of the task with id=%d (messageId=%d)".formatted(prop.taskMovementDTO().taskId(), messageId));
                            sendMessage.setReplyToMessageId(prop.callbackQuery().getMessage().getMessageId());
                            sendMessage.setReplyMarkup(new ForceReplyKeyboard(true, false, "Somthing to check"));
                            bot.silent().execute(sendMessage);
                        }
                    }

                });

        return Reply.of(action,
                isCallbackQuery()
        );
    }


    private Predicate<Update> isCallbackQuery() {
        return update -> update.getCallbackQuery() != null && update.getCallbackQuery().getId() != null;
    }

    record CallbackProperties(
            CallbackQuery callbackQuery,
            TaskMovementDTO taskMovementDTO
    ) {}

}
