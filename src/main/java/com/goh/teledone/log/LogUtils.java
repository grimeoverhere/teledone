package com.goh.teledone.log;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Deprecated
@UtilityClass
public class LogUtils {

    private final static Logger rawInboundDataLogger = LoggerFactory.getLogger("raw-inbound-data-logger");
    private final static Logger rawOutboundDataLogger = LoggerFactory.getLogger("raw-outbound-data-logger");

    public static final String WHITESPACE = " ";

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .setSerializationInclusion(NON_NULL)
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

    @SneakyThrows
    private String asJson(Object o) {
        return objectMapper.writeValueAsString(o);
    }

    public void logRawInboundData(Object data, String markerName, String message) {
        logRawData(data, markerName, message, rawInboundDataLogger);
    }

    public static void logRawInboundDataAsJson(Object data, String markerName, String message, Object... params) {
        logRawData(asJson(data), markerName, message, rawInboundDataLogger, params);
    }

    public static void logRawInboundDataAsJson(Object data, List<String> markerName, String message, Object... params) {
        logRawInboundDataAsJson(data, String.join(WHITESPACE, markerName), message, params);
    }

    public static void logRawOutboundData(Object data, String markerName, String message, Object... params) {
        logRawData(data, markerName, message, rawOutboundDataLogger, params);
    }

    public static void logRawOutboundDataAsJson(Object data, String markerName, String message, Object... params) {
        logRawData(asJson(data), markerName, message, rawOutboundDataLogger, params);
    }

    private static void logRawData(Object data, String markerName, String message, Logger logger, Object... params) {
        var marker = MarkerFactory.getMarker(markerName);

        if (logger.isTraceEnabled()) {
            List<Object> objects = new LinkedList<>(Arrays.asList(params));
            objects.add(data);

            logger.trace(marker, message + " rawData: '{}'", objects.toArray());
            return;
        }

        logger.debug(marker, message, params);
    }

}
