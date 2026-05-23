package com.nuvemite.cms.licenses.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;

public record CreateLicenseRequest(
        @NotNull UUID companyId,
        @NotNull UUID premiseId,
        String premiseName,
        @NotBlank String licenseType,
        @NotNull UUID chemicalId,
        @NotBlank String chemicalName,
        UUID applicationTemplateId,
        Map<String, Object> applicationAnswers) {}
