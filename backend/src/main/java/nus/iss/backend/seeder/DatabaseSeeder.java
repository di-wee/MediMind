package nus.iss.backend.seeder;

import com.github.javafaker.Faker;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import nus.iss.backend.model.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;


@Component
public class DatabaseSeeder implements CommandLineRunner {

    @PersistenceContext
    private EntityManager entityManager;
    private final List<String> commonMeds = Arrays.asList(
            "Panadol", "Metformin", "Lipitor", "Amoxicillin", "Losartan",
            "Omeprazole", "Amlodipine", "Ventolin", "Atorvastatin", "Insulin"
    );
    private final Random random = new Random(20250808); //
    private final Faker faker = new Faker(new Locale("en-SG"), random);// fixed seed, same for all

    private final List<LocalTime> possibleTimes = Arrays.asList(
            LocalTime.of(8, 0),
            LocalTime.of(14, 0),
            LocalTime.of(20, 0)
    );
    private final LocalTime morningTime = LocalTime.of(8, 0);
    private final LocalTime eveningTime = LocalTime.of(20, 0);

    @Override
    @Transactional
    public void run(String... args) {
        System.out.println("Starting database seeding...");
        List<Clinic> clinics = seedClinicsIfEmpty();
        List<Doctor> doctors = seedDoctorsIfEmpty(clinics);
        List<Patient> patients = seedPatientsIfEmpty(clinics, doctors);

        // seed meds, schedules, and intake logs
        seedMedicationsAndSchedules(patients);

        System.out.println("Database seeding completed!");



    }


    //generic method to count rows for data entity
    private <T> long countEntities(Class<T> entityClass) {
        return entityManager.createQuery("SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e", Long.class)
                .getSingleResult();
    }

    //seed clinics if the table is empty
    private List<Clinic> seedClinicsIfEmpty() {
        if(countEntities(Clinic.class)> 0) {
            System.out.println("Clinic entries exist, skipping seeding...");
            return entityManager.createQuery("SELECT c FROM Clinic c", Clinic.class).getResultList();
        }

        List<String> clinicNames = Arrays.asList(
                "Raffles Medical Clinic",
                "Healthway Medical Centre",
                "Parkway Shenton Clinic",
                "OneCare Family Clinic",
                "Fullerton Health Clinic"
        );

        List<Clinic> clinics = new ArrayList<>();
        for (String name : clinicNames) {
            Clinic clinic = new Clinic();
            clinic.setClinicName(name);
            //saving to db
            entityManager.persist(clinic);
            clinics.add(clinic);
        }


        return clinics;
    }

    private List<Doctor> seedDoctorsIfEmpty(List<Clinic> clinics) {
        if (countEntities(Doctor.class) > 0) {
            return entityManager.createQuery("SELECT d FROM Doctor d", Doctor.class).getResultList();
        }

        List<Doctor> doctors = new ArrayList<>();
        for (Clinic clinic : clinics) {
            for (int i = 0; i < 3; i++) {
                Doctor doctor = new Doctor();
                doctor.setMcrNo("M" + faker.number().numberBetween(10000, 99999) + (char) ('A' + random.nextInt(26)));
                doctor.setPassword(faker.internet().password(8, 12));
                doctor.setFirstName(faker.name().firstName());
                doctor.setLastName(faker.name().lastName());

                // Fake email
                String domain = clinic.getClinicName().toLowerCase()
                        .replace(" ", "")
                        .replace("clinic", "")
                        .replace("centre", "");

                String firstNameClean = doctor.getFirstName().toLowerCase().replace(" ", "_");
                doctor.setEmail(firstNameClean + doctor.getLastName().charAt(0) + "@" + domain + ".com");

                doctor.setClinic(clinic);
                entityManager.persist(doctor);
                doctors.add(doctor);
            }
        }
        return doctors;
    }



    private List<Patient> seedPatientsIfEmpty(List<Clinic> clinics, List<Doctor> doctors) {
        if (countEntities(Patient.class) > 0) {
            return entityManager.createQuery("SELECT p FROM Patient p", Patient.class).getResultList();
        }

        List<String> emailDomains = Arrays.asList("gmail.com", "yahoo.com", "hotmail.com", "outlook.com");

        List<Patient> patients = new ArrayList<>();
        for (Clinic clinic : clinics) {
            List<Doctor> clinicDoctors = doctors.stream()
                    .filter(d -> d.getClinic().equals(clinic))
                    .toList();

            for (int i = 0; i < 10; i++) {
                Patient patient = new Patient();
                String firstName = faker.name().firstName();
                String lastName = faker.name().lastName();

                patient.setFirstName(firstName);
                patient.setLastName(lastName);

                // generate email based on patient first and last name
                String cleanFirst = firstName.toLowerCase().replace(" ", ".");
                String cleanLast = lastName.toLowerCase().replace(" ", ".");
                String domain = emailDomains.get(random.nextInt(emailDomains.size()));

                //20% chance to append a number for realism
                String email = cleanFirst + "." + cleanLast;
                if (random.nextInt(100) < 20) {
                    email += random.nextInt(100);
                }
                email += "@" + domain;
                patient.setEmail(email);

                patient.setPassword(faker.internet().password(8, 12));
                patient.setNric("S" + faker.number().numberBetween(1000000, 9999999) + faker.letterify("?").toUpperCase());
                patient.setGender(random.nextBoolean() ? "Male" : "Female");



                // convert to LocalDate to remove time
                LocalDate dob = faker.date().birthday(60, 90)// age 60-90
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                patient.setDob(dob);
                
                patient.setClinic(clinic);
                patient.setDoctor(clinicDoctors.get(random.nextInt(clinicDoctors.size())));

                // 70% chance to assign a doctor, 30% chance unassigned
                if (random.nextInt(100) < 70 && !clinicDoctors.isEmpty()) {
                    patient.setDoctor(clinicDoctors.get(random.nextInt(clinicDoctors.size())));
                } else {
                    patient.setDoctor(null);
                }

                entityManager.persist(patient);
                patients.add(patient);
            
            }
        }
        return patients;
    }

    private void seedMedicationsAndSchedules(List<Patient> patients) {
        if (countEntities(Medication.class) > 0) {
            System.out.println("Medications already exist, skipping seeding...");
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);

        // Map realistic instructions and notes per medication
        Map<String, List<String>> instructionsMap = Map.of(
                "Panadol", List.of(
                        "Take with or after food to avoid stomach upset.",
                        "Do not exceed 8 tablets in 24 hours.",
                        "Avoid alcohol while taking Panadol."
                ),
                "Metformin", List.of(
                        "Take with meals to reduce stomach upset.",
                        "Do not crush or chew extended-release tablets.",
                        "Maintain a consistent meal schedule while on this medication."
                ),
                "Lipitor", List.of(
                        "Can be taken with or without food.",
                        "Avoid grapefruit juice while on Lipitor.",
                        "Take at the same time every day for best results."
                ),
                "Amoxicillin", List.of(
                        "Take every 8 hours for full effect.",
                        "Finish the entire course even if you feel better.",
                        "Can be taken with or without food; take with water."
                ),
                "Losartan", List.of(
                        "Can be taken with or without food.",
                        "Avoid potassium-rich salt substitutes unless instructed.",
                        "Take at the same time each day."
                ),
                "Omeprazole", List.of(
                        "Take 30 minutes before a meal, preferably breakfast.",
                        "Do not crush or chew the capsule.",
                        "Avoid alcohol and NSAIDs if possible."
                ),
                "Amlodipine", List.of(
                        "Can be taken with or without food.",
                        "Avoid grapefruit juice as it may increase side effects.",
                        "Take at the same time each day for consistent blood pressure control."
                ),
                "Ventolin", List.of(
                        "Use the inhaler as directed for wheezing or shortness of breath.",
                        "Shake the inhaler before use.",
                        "Rinse mouth after inhalation if using with a spacer."
                ),
                "Atorvastatin", List.of(
                        "Can be taken with or without food.",
                        "Avoid excessive alcohol while on this medication.",
                        "Take in the evening for best cholesterol-lowering effect."
                ),
                "Insulin", List.of(
                        "Inject subcutaneously as prescribed, rotate injection sites.",
                        "Monitor blood sugar regularly.",
                        "Do not skip meals after insulin injection."
                )
        );

        Map<String, List<String>> notesMap = Map.of(
                "Panadol", List.of(
                        "Do not combine with other medications containing paracetamol.",
                        "Seek medical attention if fever persists beyond 3 days."
                ),
                "Metformin", List.of(
                        "May cause mild gastrointestinal upset initially.",
                        "Report persistent vomiting or diarrhea to your doctor."
                ),
                "Lipitor", List.of(
                        "Report any unusual muscle pain or weakness.",
                        "Routine liver function tests may be required."
                ),
                "Amoxicillin", List.of(
                        "May cause mild diarrhea; take probiotics if recommended.",
                        "Inform your doctor if you develop a rash or breathing difficulty."
                ),
                "Losartan", List.of(
                        "May cause dizziness; get up slowly from sitting/lying position.",
                        "Monitor blood pressure regularly at home."
                ),
                "Omeprazole", List.of(
                        "Prolonged use may affect calcium absorption.",
                        "Inform your doctor if you experience persistent stomach pain."
                ),
                "Amlodipine", List.of(
                        "May cause ankle swelling in some patients.",
                        "Avoid sudden discontinuation without medical advice."
                ),
                "Ventolin", List.of(
                        "Excessive use may cause rapid heartbeat or tremors.",
                        "Seek medical attention if inhaler use increases suddenly."
                ),
                "Atorvastatin", List.of(
                        "Report any signs of liver problems (yellow eyes/skin, dark urine).",
                        "Do not consume large amounts of alcohol while on this medication."
                ),
                "Insulin", List.of(
                        "Keep a source of sugar nearby to manage hypoglycemia.",
                        "Store in a refrigerator; do not freeze."
                )
        );

        for (Patient patient : patients) {
            for (int i = 0; i < 10; i++) { // 10 meds per patient
                Medication med = new Medication();
                String medName = commonMeds.get(random.nextInt(commonMeds.size()));
                med.setMedicationName(medName);

                int quantity = random.nextInt(2) + 1; // 1-2 tablets
                med.setIntakeQuantity(quantity + " tablet" + (quantity > 1 ? "s" : ""));

                int frequency = random.nextInt(3) + 1;
                med.setFrequency(frequency);

                //  optional timing
                med.setTiming(random.nextInt(100) < 70 ?
                        (random.nextBoolean() ? "Every morning" : "Every night")
                        : null);

                // assign realistic instructions and notes from map
                med.setInstructions(getRandomItem(instructionsMap.getOrDefault(medName, List.of("Follow doctor's advice."))));
                med.setNotes(getRandomItem(notesMap.getOrDefault(medName, List.of("No special notes."))));
                med.setActive(random.nextBoolean());

                entityManager.persist(med);
                patient.getMedications().add(med);
                med.getPatients().add(patient);

                // generate realistic schedule times
                List<LocalTime> medTimes = new ArrayList<>();
                if (frequency == 1) {
                    medTimes.add(possibleTimes.get(random.nextInt(possibleTimes.size())));
                } else if (frequency == 2) {
                    medTimes.add(morningTime);
                    medTimes.add(eveningTime);
                } else {
                    medTimes.addAll(possibleTimes);
                }


                for (LocalTime time : medTimes) {
                    // schedule
                    Schedule schedule = new Schedule();
                    schedule.setPatient(patient);
                    schedule.setMedication(med);
                    schedule.setScheduledTime(time);

                    schedule.setIsActive(random.nextInt(100) < 80); // 80% active, 20% inactive
                    schedule.setCreationDate(LocalDateTime.now().minusDays(random.nextInt(30))); // created within last 30 days

                    entityManager.persist(schedule);

                    // intake logs for first 20 days of current month
                    if (med.isActive()) {
                        for (int day = 0; day < 20; day++) {
                            LocalDate logDate = firstDayOfMonth.plusDays(day);
                            if (logDate.isAfter(today)) break;

                            IntakeHistory log = new IntakeHistory();
                            log.setPatient(patient);
                            log.setSchedule(schedule);
                            log.setLoggedDate(logDate);
                            log.setTaken(random.nextInt(100) < 80); // 80% adherence
                            log.setDoctorNote(null);

                            entityManager.persist(log);
                        }
                    }
                }
            }
            entityManager.merge(patient);
        }
    }

    // helper generic function to select a random item from a list
    private <T> T getRandomItem(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }



}


