package com.goh.teledone.stt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Обёртка над выполняемыми действиями.
 */
@Slf4j
@Service
public class ActionWrapperService {

    /**
     * Сервис распознавания речи.
     */
    private final VkAudioRecognitionService vkAudioRecognitionService;

    /**
     * Конструктор.
     *
     * @param service сервис распознавания речи
     */
    @Autowired
    public ActionWrapperService(final VkAudioRecognitionService service) {
        this.vkAudioRecognitionService = service;
    }

    /**
     * Метод для обработки входящих сообщений.
     *
     * @param update полученное сообщение
     * @return результат распознавания
     */
    public String processMessage(final Update update) {
        String asrResponse = vkAudioRecognitionService.sendToRecognition(update);
        String taskId = vkAudioRecognitionService.startVoiceRecognition(asrResponse);
        return vkAudioRecognitionService.pollForText(taskId);
    }


}
