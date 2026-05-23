package com.nuvemite.cms.licenses.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cms.licenses")
public record LicensesProperties(Outbox outbox, int grantValidityYears) {

    public record Outbox(long pollIntervalMs, int batchSize) {}
}
