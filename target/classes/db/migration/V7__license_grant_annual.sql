ALTER TABLE license_grant
  ADD COLUMN last_annual_inspection_date DATE,
  ADD COLUMN last_annual_due_emitted_at DATE;
