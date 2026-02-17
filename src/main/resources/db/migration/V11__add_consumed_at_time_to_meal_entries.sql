ALTER TABLE meal_entries ADD COLUMN consumed_at_time TIME;
UPDATE meal_entries SET consumed_at_time = '12:00:00' WHERE consumed_at_time IS NULL;
ALTER TABLE meal_entries ALTER COLUMN consumed_at_time SET NOT NULL;