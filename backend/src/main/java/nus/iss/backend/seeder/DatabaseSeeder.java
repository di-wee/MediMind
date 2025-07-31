package nus.iss.backend.seeder;

import com.github.javafaker.Faker;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import nus.iss.backend.model.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
                patient.setDob(faker.date().birthday(60, 90)); // Age 60-90
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

        LocalDate now = LocalDate.now();
        for (Patient patient : patients) {
            for (int i = 0; i < 10; i++) {
                Medication med = new Medication();
                med.setMedicationName(commonMeds.get(random.nextInt(commonMeds.size())));
                med.setDosage((random.nextInt(2) + 1) + " tablet(s)");
                med.setIntakeQuantity(String.valueOf(random.nextInt(2) + 1));
                med.setFrequency(random.nextBoolean() ? "Once Daily" : "Twice Daily");
                med.setTiming(random.nextBoolean() ? "After Meal" : "Before Meal");
                med.setInstructions("Follow doctor’s instructions");
                med.setDoctorNotes(null);
                med.setActive(random.nextBoolean());
                med.setPatients(Collections.singletonList(patient));

                entityManager.persist(med);

                // Schedules
                int scheduleCount = random.nextInt(3) + 2; // 2-4 schedules
                for (int j = 0; j < scheduleCount; j++) {
                    Schedule schedule = new Schedule();
                    schedule.setPatient(patient);
                    schedule.setMedication(med);

                    // 80% current month, 20% last month
                    LocalDate date = random.nextDouble() < 0.8
                            ? now.minusDays(random.nextInt(now.getDayOfMonth()))
                            : now.minusDays(random.nextInt(30) + now.getDayOfMonth());

                    schedule.setScheduledTime(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                    entityManager.persist(schedule);

                    // IntakeHistory for active meds only
                    if (med.isActive()) {
                        int logCount = random.nextInt(3) + 1; // 1-3 logs
                        for (int k = 0; k < logCount; k++) {
                            IntakeHistory log = new IntakeHistory();
                            log.setPatient(patient);
                            log.setSchedule(schedule);
                            log.setLoggedDate(schedule.getScheduledTime());
                            log.setTaken(random.nextBoolean());
                            log.setDoctorNote(null);
                            entityManager.persist(log);
                        }
                    }
                }
            }
        }
    }

}


