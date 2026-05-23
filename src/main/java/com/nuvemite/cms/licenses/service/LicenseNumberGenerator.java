package com.nuvemite.cms.licenses.service;

import java.time.Year;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

@Component
public class LicenseNumberGenerator {

    public String next() {
        int suffix = ThreadLocalRandom.current().nextInt(100_000, 999_999);
        return "LIC-" + Year.now().getValue() + "-" + suffix;
    }
}
