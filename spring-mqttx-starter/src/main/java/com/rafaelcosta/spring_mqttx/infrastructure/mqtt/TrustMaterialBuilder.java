package com.rafaelcosta.spring_mqttx.infrastructure.mqtt;

import org.springframework.core.io.Resource;

import javax.net.ssl.TrustManagerFactory;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;

public class TrustMaterialBuilder {

    private final CertificateResourceLoader resourceLoader;

    public TrustMaterialBuilder(CertificateResourceLoader resourceLoader) {
        this.resourceLoader = Objects.requireNonNull(resourceLoader, "resourceLoader");
    }

    public TrustManagerFactory build(MqttProperties.Ssl ssl) throws Exception {
        if (ssl == null) {
            return null;
        }

        if (hasText(ssl.getTrustCertificateLocation())) {
            return buildFromCertificate(ssl.getTrustCertificateLocation(), ssl.getTrustCertificateFormat());
        }

        if (hasText(ssl.getTrustStoreLocation())) {
            return buildFromTrustStore(
                    ssl.getTrustStoreLocation(),
                    ssl.getTrustStoreType(),
                    ssl.getTrustStorePassword()
            );
        }

        return null;
    }

    private TrustManagerFactory buildFromTrustStore(String location, String type, String password) throws Exception {
        try (InputStream inputStream = resourceLoader.open(location)) {
            KeyStore trustStore = KeyStore.getInstance(defaultIfBlank(type, "PKCS12"));
            char[] trustStorePassword = password != null ? password.toCharArray() : null;
            trustStore.load(inputStream, trustStorePassword);
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);
            return trustManagerFactory;
        }
    }

    private TrustManagerFactory buildFromCertificate(String location, String format) throws Exception {
        Resource resource = resourceLoader.getResource(location);
        CertificateFactory certificateFactory = createCertificateFactory(format, resource.getFilename());

        KeyStore inMemoryTrustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        inMemoryTrustStore.load(null, null);

        try (InputStream inputStream = new BufferedInputStream(resource.getInputStream())) {
            Collection<? extends Certificate> certificates = certificateFactory.generateCertificates(inputStream);
            if (certificates == null || certificates.isEmpty()) {
                throw new CertificateException("Nenhum certificado foi encontrado em " + location);
            }

            int index = 0;
            for (Certificate certificate : certificates) {
                inMemoryTrustStore.setCertificateEntry("mqtt-trust-cert-" + index++, certificate);
            }
        }

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(inMemoryTrustStore);
        return trustManagerFactory;
    }

    private CertificateFactory createCertificateFactory(String configuredFormat, String filename) throws CertificateException {
        String normalized = defaultIfBlank(configuredFormat, "AUTO").trim().toUpperCase(Locale.ROOT);

        if ("AUTO".equals(normalized)) {
            String extension = resolveExtension(filename);
            if ("PEM".equals(extension) || "CRT".equals(extension) || "CER".equals(extension) || "DER".equals(extension)) {
                return CertificateFactory.getInstance("X.509");
            }
            return CertificateFactory.getInstance("X.509");
        }

        if ("X509".equals(normalized) || "X.509".equals(normalized) || "PEM".equals(normalized) || "CRT".equals(normalized) || "CER".equals(normalized) || "DER".equals(normalized)) {
            return CertificateFactory.getInstance("X.509");
        }

        return CertificateFactory.getInstance(configuredFormat);
    }

    private String resolveExtension(String filename) {
        if (!hasText(filename) || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).trim().toUpperCase(Locale.ROOT);
    }

    private String defaultIfBlank(String value, String fallback) {
        return hasText(value) ? value : fallback;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
