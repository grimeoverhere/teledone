package com.goh.teledone.telegrambot;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.goh.teledone.model.TaskMovementDTO;
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

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.goh.teledone.Utils.escapeForTelegramMarkdownV2;
import static com.goh.teledone.taskmanager.TaskListType.*;
import static com.goh.teledone.taskmanager.TaskListType.BACKLOG;

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

    public SendVoice convertTaskToSendVoice(Task task, Long chatId) {
        SendVoice sendVoice = convertTaskToSendVoiceWithoutKeyboard(task, chatId);
        sendVoice.setReplyMarkup(inlineKeyboardMarkupForTask(task));
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

    public SendMessage convertTaskToSendMessage(Task task, Long chatId) {
        SendMessage sendMessage = convertTaskToSendMessageWithoutKeyboard(task, chatId);
        sendMessage.setReplyMarkup(inlineKeyboardMarkupForTask(task));
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

    private InlineKeyboardMarkup inlineKeyboardMarkupForTask(Task task) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(
                        inlineKeyboardButton(task.getId(), TODAY),
                        inlineKeyboardButton(task.getId(), WEEK),
                        inlineKeyboardButton(task.getId(), BACKLOG),
                        new InlineKeyboardButton() {{ setText("✅"); setCallbackData(asJson(new TaskMovementDTO(task.getId(), DONE)));}}
                )).build();
    }

    private InlineKeyboardButton inlineKeyboardButton(Long taskId, TaskListType taskListType) {
        var inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText(taskListType.name());
        inlineKeyboardButton.setCallbackData(asJson(new TaskMovementDTO(taskId, taskListType)));
        return inlineKeyboardButton;
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
