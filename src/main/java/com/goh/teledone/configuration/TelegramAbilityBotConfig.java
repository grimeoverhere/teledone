package com.goh.teledone.configuration;

import com.goh.teledone.log.CommonLoggerService;
import com.goh.teledone.telegrambot.TeledoneAbilityBot;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class TelegramAbilityBotConfig {

    @Bean
    public TeledoneAbilityBot getGohovenokBot(
            @Value("${integration.telegram.bot-token}") String botToken,
            @Value("${integration.telegram.bot-creator-id}") Long creatorId,
            @Value("${integration.telegram.bot-username}") String botUsername,
            CommonLoggerService loggerService) {
        return new TeledoneAbilityBot(botToken, creatorId, botUsername, defaultBotOptions(), loggerService);
    }

    @Bean
    @SneakyThrows
    public BotSession initTelegramBots(TeledoneAbilityBot gohovenokAbilityBot) {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        return botsApi.registerBot(gohovenokAbilityBot);
    }

    private DefaultBotOptions defaultBotOptions() {
        DefaultBotOptions defaultBotOptions = new DefaultBotOptions();
        defaultBotOptions.setMaxThreads(32);
        return defaultBotOptions;
    }

}
