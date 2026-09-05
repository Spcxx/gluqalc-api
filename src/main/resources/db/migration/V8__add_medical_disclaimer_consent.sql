INSERT INTO consent_definitions (
    id,
    code,
    version,
    content,
    required,
    active,
    created_at,
    updated_at
) VALUES (
    '2d0b1d74-4d67-4ae6-9a9b-fd2c7d8a2c10',
    'MEDICAL_DISCLAIMER',
    '1.0.0',
    'Medical Disclaimer

This application is designed solely as a supportive informational and tracking tool for managing nutritional data, glucose records, and suggested insulin estimates. It is not a certified medical device and is not intended to replace clinical judgment.

1. Not Medical Advice

The calculations, nutritional insights, and dose estimates provided by this system do not constitute professional medical advice, diagnosis, or prescription.

2. Independent Verification Required

All bolus recommendations, carbohydrate ratios, and correction doses must be verified independently before administering medication. Factors such as physical activity, illness, stress, and individual metabolic variations cannot be fully accounted for by this software.

3. Consult Healthcare Professionals

Never alter your prescribed therapy, medication schedule, or target ranges without direct guidance from your physician or qualified diabetes care team.

4. Emergency Situations

This platform does not monitor acute clinical conditions or life-threatening events. In the event of severe hypoglycemia, hyperglycemia, or any medical emergency, seek immediate local emergency assistance.

By tapping "Accept", you confirm that you have read, understood, and agreed to this disclaimer, and you assume full responsibility for verifying all dosing calculations before taking any clinical action.',
    TRUE,
    TRUE,
    NOW(),
    NOW()
);