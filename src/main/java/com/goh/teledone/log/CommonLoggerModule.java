package com.goh.teledone.log;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonLoggerModule {
    @Bean(destroyMethod = "close")
    CommonLoggerService commonLoggerService() {
        return new CommonLoggerServiceImpl();
    }

//    @Bean
//    public BasicThreadFactory threadFactory(String poolName) {
//        return new BasicThreadFactory.Builder()
//                .namingPattern(poolName + "-%d")
//                .uncaughtExceptionHandler((t, e) -> log.error("Failed to execute task", e))
//                .build();
//    }
}


