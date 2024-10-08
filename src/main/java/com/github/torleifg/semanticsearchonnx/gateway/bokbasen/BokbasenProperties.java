package com.github.torleifg.semanticsearchonnx.gateway.bokbasen;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "bokbasen")
public class BokbasenProperties {
    private String serviceUri;
    private String after;
    private String subscription;
}
