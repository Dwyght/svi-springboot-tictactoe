package com.svi.tictactoe.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "app")
public class ApplicationProperties {

    private List<String> allowedOriginPatterns =
            List.of("http://localhost:*");

    public List<String> getAllowedOriginPatterns() {
        return allowedOriginPatterns;
    }

    public void setAllowedOriginPatterns(
            List<String> allowedOriginPatterns
    ) {
        this.allowedOriginPatterns = allowedOriginPatterns;
    }
}