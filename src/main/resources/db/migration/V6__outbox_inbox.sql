CREATE TABLE outbox_event (
  id              UUID PRIMARY KEY,
  aggregate_type  VARCHAR(64) NOT NULL,
  aggregate_id    UUID NOT NULL,
  event_type      VARCHAR(128) NOT NULL,
  payload         JSONB NOT NULL,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  published_at    TIMESTAMPTZ
);
CREATE INDEX idx_outbox_unpublished ON outbox_event (created_at) WHERE published_at IS NULL;

CREATE TABLE inbox_processed_event (
  event_id        VARCHAR(128) NOT NULL,
  consumer_group  VARCHAR(128) NOT NULL,
  processed_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (event_id, consumer_group)
);
