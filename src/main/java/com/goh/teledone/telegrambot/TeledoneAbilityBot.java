package com.goh.teledone.telegrambot;

import com.goh.teledone.log.CommonLoggerService;
import lombok.Getter;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;

public class TeledoneAbilityBot extends AbilityBot {
    @Getter
    private final long creatorId;

    private CommonLoggerService log;


    public TeledoneAbilityBot(String botToken, long creatorId, String botUsername,
                              DefaultBotOptions defaultBotOptions, CommonLoggerService loggerService
    ) {
        super(botToken, botUsername, defaultBotOptions);
        this.creatorId = creatorId;
        this.log = loggerService;
        clearChatGRPConversations();
    }

    @Override
    public long creatorId() {
        return creatorId;
    }

    @Override
    public void addExtension(AbilityExtension extension) {
        super.addExtension(extension);
    }

    public File downloadFileWithId(String fileId) throws TelegramApiException {
        return sender.downloadFile(sender.execute(GetFile.builder().fileId(fileId).build()));
    }

    private void clearChatGRPConversations() {
        db.<Long>getSet("CONVERSATIONS").clear();
    }

}
