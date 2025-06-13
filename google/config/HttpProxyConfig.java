package com.ctrip.flight.intl.exchange.hackathonnfc.ai.google.config;

import org.springframework.stereotype.Component;
import qunar.tc.qconfig.client.spring.QMapConfig;

import java.util.Map;

/**
 * @author white
 * created at 2025-06-10 10:16
 * HttpProxyConfig
 * @version 1.0
 */
@Component
public class HttpProxyConfig {
    @QMapConfig("http.proxy.properties")
    private Map<String, String> configs;

    public String getHttpProxyHost() {
        return configs.getOrDefault("proxy.host", "proxygate2.ctripcorp.com");
    }

    public Integer getHttpProxyPort() {
        String port = configs.getOrDefault("proxy.port", "8080");
        return Integer.parseInt(port);
    }
}
