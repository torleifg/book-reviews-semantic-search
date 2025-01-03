package com.github.torleifg.semanticsearch.gateway.bokbasen;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "bokbasen")
class BokbasenProperties {
    private String serviceUri;
    private String after;
    private String subscription;
    private String mapper;
}
