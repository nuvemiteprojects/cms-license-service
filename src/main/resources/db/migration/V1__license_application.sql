CREATE TABLE license_application (
  id                          UUID PRIMARY KEY,
  license_number              VARCHAR(64) NOT NULL UNIQUE,
  company_id                  UUID NOT NULL,
  premise_id                  UUID NOT NULL,
  premise_name                VARCHAR(255),
  license_type                VARCHAR(64) NOT NULL,
  chemical_id                 UUID NOT NULL,
  chemical_name               VARCHAR(255) NOT NULL,
  status                      VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
  application_template_id     UUID,
  inspection_template_id      UUID,
  proposed_inspection_dates   DATE[],
  inspection_date             DATE,
  inspector_name              VARCHAR(255),
  inspection_notes            TEXT,
  inspection_outcome          VARCHAR(64),
  approved_at                 TIMESTAMPTZ,
  expires_at                  DATE,
  rejected_at                 TIMESTAMPTZ,
  rejection_reason            TEXT,
  created_at                  TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at                  TIMESTAMPTZ NOT NULL DEFAULT now(),
  version                     BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_license_premise ON license_application (premise_id);
CREATE INDEX idx_license_status ON license_application (status);
