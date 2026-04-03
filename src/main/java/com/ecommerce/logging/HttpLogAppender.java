package com.ecommerce.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.encoder.Encoder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

public class HttpLogAppender extends AppenderBase<ILoggingEvent> {

    private final RestTemplate restTemplate = new RestTemplate();
    private Encoder<ILoggingEvent> encoder;
    private static final String TARGET_URL = "http://localhost:8082/anomalies/ingest-log";

    public Encoder<ILoggingEvent> getEncoder() {
        return encoder;
    }

    public void setEncoder(Encoder<ILoggingEvent> encoder) {
        this.encoder = encoder;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        if (encoder == null) return;
        try {
            byte[] encodedBytes = encoder.encode(eventObject);
            String jsonPayload = new String(encodedBytes);

            if (jsonPayload.contains("\"eventType\":\"REQUEST\"") ||
                jsonPayload.contains("\"eventType\":\"BUSINESS_EVENT\"") ||
                jsonPayload.contains("\"eventType\":\"SYSTEM_EVENT\"")) {

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);

                restTemplate.postForObject(TARGET_URL, request, String.class);
            }
        } catch (Exception e) {
            // Silently skip if anomaly service is down or network error occurs
        }
    }
}
