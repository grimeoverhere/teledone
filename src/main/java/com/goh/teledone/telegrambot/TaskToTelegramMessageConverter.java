package com.goh.teledone.telegrambot;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.goh.teledone.model.TaskMovementDTO;
import com.goh.teledone.taskmanager.TaskAction;
import com.goh.teledone.taskmanager.TaskListType;
import com.goh.teledone.taskmanager.model.Task;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.goh.teledone.Utils.escapeForTelegramMarkdownV2;

@Service
public class TaskToTelegramMessageConverter {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .setSerializationInclusion(NON_NULL)
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

    private static final String TASK_MESSAGE = """
            *Task %d*
            *Tittle:* %s
            """;

    public SendVoice convertTaskToSendVoice(Task task, Long chatId, TaskListType taskListType) {
        SendVoice sendVoice = convertTaskToSendVoiceWithoutKeyboard(task, chatId);
        sendVoice.setReplyMarkup(inlineKeyboardMarkupForTask(task, taskListType));
        return sendVoice;
    }

    public SendVoice convertTaskToSendVoiceWithoutKeyboard(Task task, Long chatId) {
        SendVoice sendVoice = new SendVoice();
        sendVoice.setChatId(chatId);
        sendVoice.setVoice(new InputFile(task.getFileId()));
        sendVoice.setCaption(TASK_MESSAGE.formatted(
                task.getId(),
                escapeForTelegramMarkdownV2(task.getTitle())
        ));
        sendVoice.setParseMode(ParseMode.MARKDOWNV2);
        return sendVoice;
    }

    public SendMessage convertTaskToSendMessage(Task task, Long chatId, TaskListType taskListType) {
        SendMessage sendMessage = convertTaskToSendMessageWithoutKeyboard(task, chatId);
        sendMessage.setReplyMarkup(inlineKeyboardMarkupForTask(task, taskListType));
        return sendMessage;
    }
    public SendMessage convertTaskToSendMessageWithoutKeyboard(Task task, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setParseMode(ParseMode.MARKDOWNV2);
        sendMessage.setText(TASK_MESSAGE.formatted(
                task.getId(),
                escapeForTelegramMarkdownV2(task.getTitle())
        ));

        return sendMessage;
    }

    private InlineKeyboardMarkup inlineKeyboardMarkupForTask(Task task, TaskListType taskListType) {
        var taskActions = Arrays.asList(taskListType.getAvailableActions());

        var actions = taskActions.stream()
                .map(actionsRow -> convertActionsToActionButtons(task, actionsRow))
                .collect(Collectors.toList());

        return InlineKeyboardMarkup.builder().keyboard(actions).build();
    }

    private InlineKeyboardButton inlineKeyboardButton(Long taskId, TaskAction taskAction) {
        var inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText(taskAction.getActionTextForButton());
        inlineKeyboardButton.setCallbackData(asJson(new TaskMovementDTO(taskId, taskAction)));
        return inlineKeyboardButton;
    }

    private List<InlineKeyboardButton> convertActionsToActionButtons(Task task, List<TaskAction> actions) {
        return actions.stream()
                .map(taskAction -> inlineKeyboardButton(task.getId(), taskAction))
                .collect(Collectors.toList());
    }

    @SneakyThrows
    public String asJson(Object o) {
        return objectMapper.writeValueAsString(o);
    }

    @SneakyThrows
    public <T> T fromJson(String object, Class<T> clazz) {
        return objectMapper.readValue(object, clazz);
    }

}
