package com.nuvemite.cms.licenses.web.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record CompleteInspectionRequest(
        @NotBlank String outcome,
        String notes,
        Map<String, Object> checklistAnswers) {}
