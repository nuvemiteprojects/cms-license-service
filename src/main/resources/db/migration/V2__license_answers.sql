ALTER TABLE license_application
  ADD COLUMN application_answers JSONB NOT NULL DEFAULT '{}',
  ADD COLUMN checklist_answers JSONB NOT NULL DEFAULT '{}';
