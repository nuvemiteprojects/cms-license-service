CREATE TABLE license_application_document (
  id              UUID PRIMARY KEY,
  application_id  UUID NOT NULL REFERENCES license_application (id) ON DELETE CASCADE,
  slot_id         VARCHAR(64) NOT NULL,
  label           VARCHAR(255) NOT NULL,
  storage_ref     VARCHAR(512),
  uploaded_at     TIMESTAMPTZ
);
