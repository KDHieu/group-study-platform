package com.grouplearning.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.livekit")
public class LiveKitProperties {

    private String internalUrl;

    private String publicUrl;

    private String apiKey;

    private String apiSecret;

    public String getInternalUrl() {
        return internalUrl;
    }

    public void setInternalUrl(
            String internalUrl
    ) {
        this.internalUrl =
                internalUrl;
    }

    public String getPublicUrl() {
        return publicUrl;
    }

    public void setPublicUrl(
            String publicUrl
    ) {
        this.publicUrl =
                publicUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(
            String apiKey
    ) {
        this.apiKey =
                apiKey;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public void setApiSecret(
            String apiSecret
    ) {
        this.apiSecret =
                apiSecret;
    }
}