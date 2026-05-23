package com.nuvemite.cms.licenses.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "license_grant")
public class LicenseGrant {

    @Id
    private UUID id;

    @Column(name = "application_id", nullable = false, unique = true)
    private UUID applicationId;

    @Column(name = "premise_id", nullable = false)
    private UUID premiseId;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "license_number", nullable = false, unique = true)
    private String licenseNumber;

    @Column(name = "license_type", nullable = false)
    private String licenseType;

    @Column(name = "chemical_id", nullable = false)
    private UUID chemicalId;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_until", nullable = false)
    private LocalDate validUntil;

    @Column(name = "issued_by")
    private String issuedBy;

    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(name = "last_annual_inspection_date")
    private LocalDate lastAnnualInspectionDate;

    @Column(name = "last_annual_due_emitted_at")
    private LocalDate lastAnnualDueEmittedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected LicenseGrant() {}

    public static LicenseGrant fromApplication(LicenseApplication app, LocalDate validFrom, LocalDate validUntil, String issuedBy) {
        LicenseGrant grant = new LicenseGrant();
        grant.id = UUID.randomUUID();
        grant.applicationId = app.getId();
        grant.premiseId = app.getPremiseId();
        grant.companyId = app.getCompanyId();
        grant.licenseNumber = app.getLicenseNumber();
        grant.licenseType = app.getLicenseType();
        grant.chemicalId = app.getChemicalId();
        grant.validFrom = validFrom;
        grant.validUntil = validUntil;
        grant.issuedBy = issuedBy;
        grant.createdAt = Instant.now();
        return grant;
    }

    public UUID getId() {
        return id;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public UUID getPremiseId() {
        return premiseId;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public String getLicenseType() {
        return licenseType;
    }

    public UUID getChemicalId() {
        return chemicalId;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public LocalDate getValidUntil() {
        return validUntil;
    }

    public String getIssuedBy() {
        return issuedBy;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public LocalDate getLastAnnualInspectionDate() {
        return lastAnnualInspectionDate;
    }

    public LocalDate annualBaselineDate() {
        return lastAnnualInspectionDate != null ? lastAnnualInspectionDate : validFrom;
    }

    public boolean isDueForAnnualNotification(LocalDate today) {
        LocalDate due = annualBaselineDate().plusYears(1);
        if (due.isAfter(today)) {
            return false;
        }
        return lastAnnualDueEmittedAt == null || lastAnnualDueEmittedAt.isBefore(due);
    }

    public void markAnnualDueEmitted(LocalDate today) {
        this.lastAnnualDueEmittedAt = today;
    }
}
