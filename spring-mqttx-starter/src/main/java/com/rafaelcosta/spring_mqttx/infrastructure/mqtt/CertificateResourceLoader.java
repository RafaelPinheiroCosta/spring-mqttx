package com.rafaelcosta.spring_mqttx.infrastructure.mqtt;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

public class CertificateResourceLoader {

    private final DefaultResourceLoader resourceLoader = new DefaultResourceLoader();

    public Resource getResource(String location) {
        return resourceLoader.getResource(location);
    }

    public InputStream open(String location) throws IOException {
        return getResource(location).getInputStream();
    }
}
