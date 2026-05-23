CREATE TABLE license_timeline_event (
  id              UUID PRIMARY KEY,
  application_id  UUID NOT NULL REFERENCES license_application (id) ON DELETE CASCADE,
  event_type      VARCHAR(64) NOT NULL,
  actor_ref       VARCHAR(128),
  notes           TEXT,
  occurred_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_license_timeline_app ON license_timeline_event (application_id);
