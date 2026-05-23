package com.nuvemite.cms.licenses.web.dto;

import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.util.List;

public record SubmitLicenseRequest(@NotEmpty List<LocalDate> preferredInspectionDates) {}
