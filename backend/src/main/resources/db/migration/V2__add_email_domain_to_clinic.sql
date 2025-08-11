-- Add email domain columns to Clinic table
ALTER TABLE Clinic ADD COLUMN Email_Domain VARCHAR(255);
ALTER TABLE Clinic ADD COLUMN Require_Email_Verification BOOLEAN DEFAULT TRUE;

-- Update existing clinics with email domains
UPDATE Clinic SET Email_Domain = 'rafflesmedical.com' WHERE Clinic_Name = 'Raffles Medical Clinic';
UPDATE Clinic SET Email_Domain = 'healthway.com.sg' WHERE Clinic_Name = 'Healthway Medical Centre';
UPDATE Clinic SET Email_Domain = 'parkwayshenton.com' WHERE Clinic_Name = 'Parkway Shenton Clinic';
UPDATE Clinic SET Email_Domain = 'onecare.com.sg' WHERE Clinic_Name = 'OneCare Family Clinic';
UPDATE Clinic SET Email_Domain = 'fullertonhealth.com' WHERE Clinic_Name = 'Fullerton Health Clinic';
