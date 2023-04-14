package com.goh.teledone.telegrambot.ability;

import com.goh.teledone.BouncerService;
import com.goh.teledone.model.TaskMovementDTO;
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
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuples;

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
                    return Tuples.of(callbackQuery, taskMovementDTO);
                })
                .doOnNext(tup -> taskManager.move(tup.getT2().taskId(), tup.getT2().taskListType()))
                .doOnNext(tup -> {
                    AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery(tup.getT1().getId());
                    answerCallbackQuery.setShowAlert(true);
                    answerCallbackQuery.setText(TASK_HAS_BEEN_MOVED_MESSAGE.formatted(
                            tup.getT2().taskId(), tup.getT2().taskListType().name()));
                    bot.silent().execute(answerCallbackQuery);
                })
                .subscribe(tup -> {
                    DeleteMessage deleteMessage = new DeleteMessage();
                    deleteMessage.setChatId(AbilityUtils.getChatId(upd));
                    deleteMessage.setMessageId(tup.getT1().getMessage().getMessageId());
                    bot.silent().execute(deleteMessage);
                });

        return Reply.of(action,
                isCallbackQuery()
        );
    }


    private Predicate<Update> isCallbackQuery() {
        return update -> update.getCallbackQuery() != null && update.getCallbackQuery().getId() != null;
    }

}
