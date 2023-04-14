package com.goh.teledone.telegrambot.ability;

import com.goh.teledone.BouncerService;
import com.goh.teledone.stt.ActionWrapperService;
import com.goh.teledone.taskmanager.TaskManagerService;
import com.goh.teledone.taskmanager.model.Task;
import com.goh.teledone.telegrambot.TeledoneAbilityBot;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.abilitybots.api.objects.Reply;
import org.telegram.abilitybots.api.util.AbilityExtension;
import org.telegram.abilitybots.api.util.AbilityUtils;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuples;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static com.goh.teledone.Utils.escapeForTelegramMarkdownV2;
import static com.goh.teledone.taskmanager.TaskListType.*;
import static org.telegram.abilitybots.api.objects.Locality.ALL;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;

@Component
@RequiredArgsConstructor
public class TaskManagerAbility implements AbilityExtension {

    private static final String TASK_MESSAGE = """
            *Task %d*
            *Tittle:* %s
            """;

    @NonNull
    private TaskManagerService taskManager;
    @NonNull
    private ActionWrapperService actionWrapper;
    @NonNull
    private TeledoneAbilityBot abilityBot;
    @NonNull
    private BouncerService bouncerService;

    @PostConstruct
    public void activateAbility() {
        abilityBot.addExtension(this);
    }

    public Reply speechToTextAndThenToTask() {
        BiConsumer<BaseAbilityBot, Update> action = (bot, upd) -> Mono.just(upd)
                .publishOn(Schedulers.boundedElastic())
                .map(actionWrapper::processMessage)
                .map(voiceToText -> Tuples.of(taskManager.saveInboxFromVoice(voiceToText, upd.getMessage().getVoice().getFileId()), voiceToText))
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

    private Predicate<Update> hasVoice() {
        return update -> update.hasMessage() && update.getMessage().getVoice() != null;
    }

    public Reply saveAnyMessageToTask() {
        BiConsumer<BaseAbilityBot, Update> action = (bot, upd) -> Mono.just(upd)
                .publishOn(Schedulers.boundedElastic())
                .map(update -> taskManager.saveInbox(update.getMessage().getText()))
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
                Predicate.not(isStartingWithSlash()),
                hasText()
        );
    }

    public Ability getInbox() {
        return Ability.builder()
                .name("inbox")
                .privacy(PUBLIC)
                .locality(ALL)
                .setStatsEnabled(true)
                .info("Returns all tasks from Inbox")
                .action(this::getInboxAction)
                .build();
    }

    private void getInboxAction(MessageContext ctx) {
        Flux.fromStream(taskManager.getInbox().stream())
                .publishOn(Schedulers.boundedElastic())
                .subscribe(task -> {
                    try {
                        if (StringUtils.hasLength(task.getFileId())) {
                            abilityBot.execute(convertTaskToSendVoice(task, AbilityUtils.getChatId(ctx.update())));
                        } else {
                            abilityBot.execute(convertTaskToSendMessage(task, AbilityUtils.getChatId(ctx.update())));
                        }
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private SendVoice convertTaskToSendVoice(Task task, Long chatId) {
        SendVoice sendVoice = new SendVoice();
        sendVoice.setChatId(chatId);
        sendVoice.setVoice(new InputFile(task.getFileId()));
        sendVoice.setCaption(TASK_MESSAGE.formatted(
                task.getId(),
                escapeForTelegramMarkdownV2(task.getTitle())
        ));
        sendVoice.setParseMode(ParseMode.MARKDOWNV2);
        sendVoice.setReplyMarkup(inlineKeyboardMarkupForTask(task));
        return sendVoice;
    }

    private SendMessage convertTaskToSendMessage(Task task, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
//        sendMessage.setReplyToMessageId(ctx.update().getMessage().getMessageId());
        sendMessage.setParseMode(ParseMode.MARKDOWNV2);
        sendMessage.setText(TASK_MESSAGE.formatted(
                task.getId(),
                escapeForTelegramMarkdownV2(task.getTitle())
        ));
        sendMessage.setReplyMarkup(inlineKeyboardMarkupForTask(task));

        return sendMessage;
    }

    private InlineKeyboardMarkup inlineKeyboardMarkupForTask(Task task) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(
                        new InlineKeyboardButton() {{ setText(TODAY.name()); setCallbackData(TODAY.name());}},
                        new InlineKeyboardButton() {{ setText(WEEK.name()); setCallbackData(WEEK.name());}},
                        new InlineKeyboardButton() {{ setText(BACKLOG.name()); setCallbackData(BACKLOG.name());}}
                )).build();
    }

    private ReplyKeyboardMarkup taskKeyboard(Task task) {
        KeyboardButton todayButton = new KeyboardButton();
        todayButton.setText(TODAY.name());

        KeyboardButton weekButton = new KeyboardButton();
        weekButton.setText(WEEK.name());

        KeyboardButton backlogButton = new KeyboardButton();
        backlogButton.setText(BACKLOG.name());


        var row = new KeyboardRow();
        row.add(todayButton);
        row.add(weekButton);
        row.add(backlogButton);

        return ReplyKeyboardMarkup.builder().keyboardRow(row).build();
    }

    private Predicate<Update> isStartingWithSlash() {
        return update -> update.getMessage().getText().startsWith("/");
    }

    private Predicate<Update> hasText() {
        return update -> StringUtils.hasLength(update.getMessage().getText());
    }
}