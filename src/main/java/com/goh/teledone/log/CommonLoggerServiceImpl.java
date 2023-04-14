package com.goh.teledone.log;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Implementation logger service
 *
 * @author Labnik
 * @version 0.0.1
 */
@Slf4j
@RequiredArgsConstructor
public class CommonLoggerServiceImpl implements CommonLoggerService {
    private final static String WHITESPACE = " ";

    @Getter
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    protected final static Logger rawInboundDataLogger = LoggerFactory.getLogger("raw-inbound-data-logger");
    protected final static Logger rawOutboundDataLogger = LoggerFactory.getLogger("raw-outbound-data-logger");

    private final ThreadLocal<ObjectMapper> mapper = ThreadLocal.withInitial(
            () -> new ObjectMapper()
                    .setSerializationInclusion(NON_NULL)
                    .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
                    .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY));

    @Override
    public void logRawInboundData(Object data, String markerName, String message) {
        var originThreadName = Thread.currentThread().getName();
        executorService.submit(() -> logRawData(data, markerName, originThreadName, message, rawInboundDataLogger));
    }

    @Override
    public void logRawInboundDataAsJson(Object data, String markerName, String message, Object... params) {
        var originThreadName = Thread.currentThread().getName();
        executorService.submit(() -> logRawData(asJson(data), markerName, originThreadName, message, rawInboundDataLogger, params));
    }

    @Override
    public void logRawInboundDataAsJson(Object data, List<String> markerName, String message, Object... params) {
        executorService.submit(() -> logRawInboundDataAsJson(data, String.join(WHITESPACE, markerName), message, params));
    }

    @Override
    public void logRawOutboundData(Object data, String markerName, String message, Object... params) {
        var originThreadName = Thread.currentThread().getName();
        executorService.submit(() -> logRawData(data, markerName, originThreadName, message, rawOutboundDataLogger, params));
    }

    @Override
    public void logRawOutboundDataAsJson(Object data, String markerName, String message, Object... params) {
        var originThreadName = Thread.currentThread().getName();
        executorService.submit(() -> logRawData(asJson(data), markerName, originThreadName, message, rawOutboundDataLogger, params));
    }

    @Override
    public void close() {
        executorService.shutdown();

        try {
            if (!executorService.awaitTermination(10, SECONDS)) {
                log.error("Could not shutdown executorService! Waited 10 seconds");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Error shutting down executorService", e);
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    protected static void logRawData(Object data, String markerName, String originThreadName, String message, Logger logger, Object... params) {
        var marker = MarkerFactory.getMarker(markerName);

        if (logger.isInfoEnabled()) {
            List<Object> objects = new LinkedList<>(Arrays.asList(params));
            objects.add(originThreadName);
            objects.add(data);

            logger.info(marker, message + " originThreadName={} rawData: '{}'", objects.toArray());
            return;
        }

        logger.debug(marker, message, params);
    }

    @SneakyThrows
    private String asJson(Object o) {
        return mapper.get().writeValueAsString(o);
    }
}
