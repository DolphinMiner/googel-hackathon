package com.ctrip.flight.intl.exchange.hackathonnfc.ai.google.factory;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.auth.http.HttpTransportFactory;

import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * @author cj.huang
 * created at 2024-06-21 11:38
 * ProxiedHttpTransportFactory
 * @version 1.0
 */
public class ProxiedHttpTransportFactory implements HttpTransportFactory {
    private final String proxyHost;
    private final Integer proxyPort;

    public ProxiedHttpTransportFactory(String proxyHost, Integer proxyPort) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }

    @Override
    public HttpTransport create() {
        return new NetHttpTransport.Builder()
                .setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(this.proxyHost, this.proxyPort))).build();

    }
}
