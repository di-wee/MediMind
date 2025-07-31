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
    private final Faker faker = new Faker(new Locale("en-SG"));
    private final List<String> commonMeds = Arrays.asList(
            "Panadol", "Metformin", "Lipitor", "Amoxicillin", "Losartan",
            "Omeprazole", "Amlodipine", "Ventolin", "Atorvastatin", "Insulin"
    );
    private final Random random = new Random();

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
                patient.setNric("S" + faker.number().numberBetween(1000000, 9999999) + faker.letterify("?"));
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
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);

        for (Patient patient : patients) {
            for (int i = 0; i < 10; i++) {
                Medication med = new Medication();
                med.setMedicationName(commonMeds.get(random.nextInt(commonMeds.size())));
                med.setDosage((random.nextInt(2) + 1) + " tablet(s)");
                med.setIntakeQuantity(String.valueOf(random.nextInt(2) + 1));
                boolean isOnceDaily = random.nextBoolean();
                med.setFrequency(isOnceDaily ? "Once Daily" : "Twice Daily");
                med.setTiming(random.nextBoolean() ? "After Meal" : "Before Meal");
                med.setInstructions("Follow doctor’s instructions");
                med.setDoctorNotes(null);
                med.setActive(random.nextBoolean());

                entityManager.persist(med);

                patient.getMedications().add(med);
                med.getPatients().add(patient);

                // Determine schedule times
                List<LocalTime> medTimes = new ArrayList<>();
                if (isOnceDaily) {
                    medTimes.add(possibleTimes.get(random.nextInt(possibleTimes.size())));
                } else {
                    medTimes.add(morningTime); // 8 AM
                    medTimes.add(eveningTime); // 8 PM
                }

                for (LocalTime time : medTimes) {
                    // Create schedule (only time)
                    Schedule schedule = new Schedule();
                    schedule.setPatient(patient);
                    schedule.setMedication(med);
                    schedule.setScheduledTime(time); // LocalTime only
                    entityManager.persist(schedule);

                    // Generate intake logs for the first 14 days of current month
                    if (med.isActive()) {
                        for (int day = 0; day < 14; day++) {
                            LocalDate logDate = firstDayOfMonth.plusDays(day);

                            // Avoid creating logs beyond today
                            if (logDate.isAfter(today)) break;

                            IntakeHistory log = new IntakeHistory();
                            log.setPatient(patient);
                            log.setSchedule(schedule);
                            log.setLoggedDate(logDate); // LocalDate only
                            log.setTaken(random.nextInt(100) < 80); // 80% chance taken
                            log.setDoctorNote(null);

                            entityManager.persist(log);
                        }
                    }
                }
            }
            entityManager.merge(patient);
        }
    }




}


