package com.goh.teledone;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.annotation.PostConstruct;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class BouncerService {

    private final Set<Long> allowedIds = ConcurrentHashMap.newKeySet();

    @PostConstruct
    public void init() {
        allowedIds.add(237135484L); //me
        allowedIds.add(640569888L); //my anime tyan
        allowedIds.add(2026924825L); // my working account
        allowedIds.add(236368989L); // Yura
    }

    public boolean isAllowedToUseBot(Update update) {
        return update != null
                && update.getMessage() != null
                && update.getMessage().getFrom() != null
                && allowedIds.contains(update.getMessage().getFrom().getId());
    }

    public boolean isForbidToUseBot(Update update) {
        return !isAllowedToUseBot(update);
    }

}
