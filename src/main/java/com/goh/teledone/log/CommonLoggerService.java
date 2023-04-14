package com.goh.teledone.log;

import java.util.List;

/**
 * Logging service
 *
 * @since mixer-0.7.278
 */
public interface CommonLoggerService {

    /**
     * Logging inbound messages toString
     *
     * @param data       - Object to logging
     * @param markerName - Text marker
     * @param message    - template message
     */
    void logRawInboundData(Object data, String markerName, String message);

    /**
     * Logging inbound messages use JSON
     *
     * @param data       - Object to logging
     * @param markerName - Text marker
     * @param message    - Template message
     * @param params     - Parameter for logging
     */
    void logRawInboundDataAsJson(Object data, String markerName, String message, Object... params);

    /**
     * Logging inbound messages use JSON
     *
     * @param data       - Object to logging
     * @param markerName - Text marker
     * @param message    - Template message
     * @param params     - Parameter for logging
     */
    void logRawInboundDataAsJson(Object data, List<String> markerName, String message, Object... params);

    /**
     * Logging outbound messages use object toString
     *
     * @param data       - Object to logging
     * @param markerName - Text marker
     * @param message    - Template message
     * @param params     - Parameter for logging
     */
    void logRawOutboundData(Object data, String markerName, String message, Object... params);

    /**
     * Logging outbound messages user JSON format
     *
     * @param data       - Object to logging
     * @param markerName - Text marker
     * @param message    - Template message
     * @param params     - Parameter for logging
     */
    void logRawOutboundDataAsJson(Object data, String markerName, String message, Object... params);

    /**
     * Close logging system
     */
    void close();
}
