package com.nuvemite.cms.licenses.web.dto;

import jakarta.validation.constraints.NotBlank;

public record RejectLicenseRequest(@NotBlank String reason) {}
