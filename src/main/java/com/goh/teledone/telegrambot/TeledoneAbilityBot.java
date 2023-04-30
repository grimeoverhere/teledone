package com.goh.teledone.telegrambot;

import com.goh.teledone.log.CommonLoggerService;
import lombok.Getter;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;

import static com.goh.teledone.log.CommonLoggingConst.RAW_INBOUND_DATA_MARKER;

public class TeledoneAbilityBot extends AbilityBot {
    @Getter
    private final long creatorId;

    private CommonLoggerService logger;


    public TeledoneAbilityBot(String botToken, long creatorId, String botUsername,
                              DefaultBotOptions defaultBotOptions, CommonLoggerService loggerService
    ) {
        super(botToken, botUsername, defaultBotOptions);
        this.creatorId = creatorId;
        this.logger = loggerService;
    }

    @Override
    public long creatorId() {
        return creatorId;
    }

    @Override
    public void addExtension(AbilityExtension extension) {
        super.addExtension(extension);
    }

    @Override
    public void onUpdateReceived(Update update) {
        logger.logRawInboundDataAsJson(update, RAW_INBOUND_DATA_MARKER, "Received new update");
        super.onUpdateReceived(update);
    }

    public File downloadFileWithId(String fileId) throws TelegramApiException {
        return sender.downloadFile(sender.execute(GetFile.builder().fileId(fileId).build()));
    }

}
