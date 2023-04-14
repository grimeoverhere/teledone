package com.goh.teledone.stt;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;

/**
 * Интерфейс для взаимодействия с сервисом получения файлов из Telegram.
 */
public interface TelegramFileService {

    /**
     * Метод для получения ссылки на скачивание голосового сообщения.
     *
     * @param message сообщение
     * @return ссылка на файл
     */
    String getFileUrl(Update message);

    /**
     * Метод для скачивания голосового сообщения в оперативную память.
     *
     * @param message сообщение
     * @return содержимое файла
     */
    byte[] downloadFile(Update message);

    /**
     * Метод для скачивания голосового сообщения в оперативную память.
     * (используя GohovenokAbilityBot)
     *
     * @param message сообщение
     * @return содержимое файла
     */
    byte[] simpleDownloadFile(Update message) throws TelegramApiException, IOException;

}
