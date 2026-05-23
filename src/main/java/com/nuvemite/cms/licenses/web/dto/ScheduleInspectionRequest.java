package com.nuvemite.cms.licenses.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ScheduleInspectionRequest(
        @NotNull LocalDate inspectionDate,
        @NotBlank String inspectorName) {}
