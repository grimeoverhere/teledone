package com.goh.teledone.stt;

import com.goh.teledone.telegrambot.TeledoneAbilityBot;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;

/**
 * Сервис для скачивания файлов из Telegram.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramFileServiceImpl implements TelegramFileService {

    @NonNull
    private TeledoneAbilityBot bot;

    @Override
    @SneakyThrows
    public String getFileUrl(final Update message) {
        String fileId = message.getMessage().getVoice().getFileId();

        File file = bot.sender().execute(GetFile.builder()
                .fileId(fileId)
                .build()
        );

        String fileUrl = File.getFileUrl(bot.getBotToken(), file.getFilePath());

        log.info(MessageFormat.format("Telegram file URL: {0}", fileUrl));
        return fileUrl;
    }

    @Override
    public byte[] downloadFile(final Update message) {
        byte[] fileContent;
        try {
            log.info("Downloading file to byte array...");
            fileContent = IOUtils.toByteArray(new URL(getFileUrl(message)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("File downloaded successfully");
        return fileContent;
    }


    @Override
    public byte[] simpleDownloadFile(final Update message) throws TelegramApiException, IOException {
        String fileId = message.getMessage().getVoice().getFileId();

        var telegramFileMeta = bot.sender().execute(GetFile.builder()
                .fileId(fileId)
                .build()
        );
        var file = bot.downloadFile(telegramFileMeta);

        return FileUtils.readFileToByteArray(file);
    }
}
