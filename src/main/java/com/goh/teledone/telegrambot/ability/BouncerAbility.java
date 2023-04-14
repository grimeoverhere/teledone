package com.goh.teledone.telegrambot.ability;

import com.goh.teledone.BouncerService;
import com.goh.teledone.telegrambot.TeledoneAbilityBot;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.abilitybots.api.objects.Reply;
import org.telegram.abilitybots.api.util.AbilityExtension;
import org.telegram.abilitybots.api.util.AbilityUtils;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.annotation.PostConstruct;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
public class BouncerAbility implements AbilityExtension {

    @NonNull
    private TeledoneAbilityBot gohovenokBot;
    @NonNull
    private BouncerService bouncerService;

    @PostConstruct
    public void activateAbility() {
        gohovenokBot.addExtension(this);
    }


    public Reply rejectRequestsFromNonWhitelistUsers() {
        BiConsumer<BaseAbilityBot, Update> action = (bot, upd) -> bot.silent().send(
                "The bot is under construction... Try again later.",
                AbilityUtils.getChatId(upd)
        );

        return Reply.of(action,
                bouncerService::isForbidToUseBot,
                Predicate.not(isCallbackQuery())
        );
    }

    private Predicate<Update> isCallbackQuery() {
        return update -> update != null
                && update.getCallbackQuery() != null
                && update.getCallbackQuery().getId() != null;
    }

}
