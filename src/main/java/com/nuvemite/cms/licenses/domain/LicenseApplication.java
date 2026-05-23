package com.nuvemite.cms.licenses.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "license_application")
public class LicenseApplication {

    @Id
    private UUID id;

    @Column(name = "license_number", nullable = false, unique = true)
    private String licenseNumber;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "premise_id", nullable = false)
    private UUID premiseId;

    @Column(name = "premise_name")
    private String premiseName;

    @Column(name = "license_type", nullable = false)
    private String licenseType;

    @Column(name = "chemical_id", nullable = false)
    private UUID chemicalId;

    @Column(name = "chemical_name", nullable = false)
    private String chemicalName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LicenseApplicationStatus status = LicenseApplicationStatus.DRAFT;

    @Column(name = "application_template_id")
    private UUID applicationTemplateId;

    @Column(name = "inspection_template_id")
    private UUID inspectionTemplateId;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "proposed_inspection_dates", columnDefinition = "date[]")
    private LocalDate[] proposedInspectionDates;

    @Column(name = "inspection_date")
    private LocalDate inspectionDate;

    @Column(name = "inspector_name")
    private String inspectorName;

    @Column(name = "inspection_notes")
    private String inspectionNotes;

    @Column(name = "inspection_outcome")
    private String inspectionOutcome;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "application_answers", nullable = false, columnDefinition = "jsonb")
    private String applicationAnswers = "{}";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "checklist_answers", nullable = false, columnDefinition = "jsonb")
    private String checklistAnswers = "{}";

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "expires_at")
    private LocalDate expiresAt;

    @Column(name = "rejected_at")
    private Instant rejectedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    protected LicenseApplication() {}

    public static LicenseApplication createDraft(
            String licenseNumber,
            UUID companyId,
            UUID premiseId,
            String premiseName,
            String licenseType,
            UUID chemicalId,
            String chemicalName,
            UUID applicationTemplateId,
            String applicationAnswers) {
        LicenseApplication app = new LicenseApplication();
        app.id = UUID.randomUUID();
        app.licenseNumber = licenseNumber;
        app.companyId = companyId;
        app.premiseId = premiseId;
        app.premiseName = premiseName;
        app.licenseType = licenseType;
        app.chemicalId = chemicalId;
        app.chemicalName = chemicalName;
        app.applicationTemplateId = applicationTemplateId;
        if (applicationAnswers != null) {
            app.applicationAnswers = applicationAnswers;
        }
        Instant now = Instant.now();
        app.createdAt = now;
        app.updatedAt = now;
        return app;
    }

    public void touch() {
        this.updatedAt = Instant.now();
    }

    public void submit(LocalDate[] preferredInspectionDates) {
        requireStatus(LicenseApplicationStatus.DRAFT);
        if (preferredInspectionDates == null || preferredInspectionDates.length == 0) {
            throw new IllegalArgumentException("At least one preferred inspection date is required");
        }
        this.proposedInspectionDates = preferredInspectionDates;
        this.status = LicenseApplicationStatus.INSPECTION_DATE_PENDING;
        touch();
    }

    public void agreeInspectionDate(LocalDate confirmedDate) {
        requireStatus(LicenseApplicationStatus.INSPECTION_DATE_PENDING);
        this.inspectionDate = confirmedDate;
        this.proposedInspectionDates = new LocalDate[] {confirmedDate};
        this.status = LicenseApplicationStatus.INSPECTION_PROPOSED;
        touch();
    }

    public void proposeInspection(LocalDate[] dates) {
        requireStatus(LicenseApplicationStatus.INSPECTION_DATE_PENDING);
        this.proposedInspectionDates = dates;
        this.status = LicenseApplicationStatus.INSPECTION_PROPOSED;
        touch();
    }

    public void scheduleInspection(LocalDate date, String inspectorName) {
        requireStatus(LicenseApplicationStatus.INSPECTION_PROPOSED);
        this.inspectionDate = date;
        this.inspectorName = inspectorName;
        this.status = LicenseApplicationStatus.INSPECTION_SCHEDULED;
        touch();
    }

    public void completeInspection(String outcome, String checklistAnswers, String notes) {
        requireStatus(LicenseApplicationStatus.INSPECTION_SCHEDULED);
        this.inspectionOutcome = outcome;
        this.inspectionNotes = notes;
        if (checklistAnswers != null) {
            this.checklistAnswers = checklistAnswers;
        }
        this.status = LicenseApplicationStatus.INSPECTION_COMPLETE;
        touch();
    }

    public void approve(LocalDate validFrom, LocalDate validUntil) {
        requireStatus(LicenseApplicationStatus.INSPECTION_COMPLETE);
        this.status = LicenseApplicationStatus.ACTIVE;
        this.approvedAt = Instant.now();
        this.expiresAt = validUntil;
        touch();
    }

    public void reject(String reason) {
        if (status == LicenseApplicationStatus.ACTIVE || status == LicenseApplicationStatus.REJECTED) {
            throw new IllegalStateException("Cannot reject application in status " + status);
        }
        this.status = LicenseApplicationStatus.REJECTED;
        this.rejectionReason = reason;
        this.rejectedAt = Instant.now();
        touch();
    }

    private void requireStatus(LicenseApplicationStatus expected) {
        if (this.status != expected) {
            throw new IllegalStateException(
                    "Expected status " + expected + " but was " + status);
        }
    }

    public UUID getId() {
        return id;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public UUID getPremiseId() {
        return premiseId;
    }

    public String getPremiseName() {
        return premiseName;
    }

    public String getLicenseType() {
        return licenseType;
    }

    public UUID getChemicalId() {
        return chemicalId;
    }

    public String getChemicalName() {
        return chemicalName;
    }

    public LicenseApplicationStatus getStatus() {
        return status;
    }

    public UUID getApplicationTemplateId() {
        return applicationTemplateId;
    }

    public UUID getInspectionTemplateId() {
        return inspectionTemplateId;
    }

    public void setInspectionTemplateId(UUID inspectionTemplateId) {
        this.inspectionTemplateId = inspectionTemplateId;
    }

    public LocalDate[] getProposedInspectionDates() {
        return proposedInspectionDates;
    }

    public LocalDate getInspectionDate() {
        return inspectionDate;
    }

    public String getInspectorName() {
        return inspectorName;
    }

    public String getInspectionNotes() {
        return inspectionNotes;
    }

    public String getInspectionOutcome() {
        return inspectionOutcome;
    }

    public String getApplicationAnswers() {
        return applicationAnswers;
    }

    public String getChecklistAnswers() {
        return checklistAnswers;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public LocalDate getExpiresAt() {
        return expiresAt;
    }

    public Instant getRejectedAt() {
        return rejectedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public long getVersion() {
        return version;
    }
}
