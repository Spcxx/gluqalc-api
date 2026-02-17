ALTER TABLE user_profiles
    ADD COLUMN ifp_strategy VARCHAR(255),
    ADD COLUMN insulin_sensitivity_factor VARCHAR(255),
    ADD COLUMN insulin_fat_protein_ratio VARCHAR(255),
    ADD COLUMN hourly_carb_ratio VARCHAR(2000);