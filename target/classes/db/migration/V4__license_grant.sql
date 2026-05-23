CREATE TABLE license_grant (
  id                UUID PRIMARY KEY,
  application_id    UUID NOT NULL UNIQUE REFERENCES license_application (id),
  premise_id        UUID NOT NULL,
  company_id        UUID NOT NULL,
  license_number    VARCHAR(64) NOT NULL UNIQUE,
  license_type      VARCHAR(64) NOT NULL,
  chemical_id       UUID NOT NULL,
  valid_from        DATE NOT NULL,
  valid_until       DATE NOT NULL,
  issued_by         VARCHAR(128),
  status            VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
  created_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);
