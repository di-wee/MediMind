package nus.iss.backend.seeder;

import com.github.javafaker.Faker;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import nus.iss.backend.model.Clinic;
import nus.iss.backend.model.Doctor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    @PersistenceContext
    private EntityManager entityManager;
    private final Faker faker = new Faker(new Locale("en-SG"));

    @Override
    @Transactional
    public void run(String... args) {
        System.out.println("Starting database seeding...");
        List<Clinic> clinics = seedClinicsIfEmpty();
        seedDoctorsIfEmpty(clinics);



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

    private void seedDoctorsIfEmpty(List<Clinic> clinics) {
        if (countEntities(Doctor.class) > 0)  {
            System.out.println("Doctor entries already exist, skipping seeding...");
            return;
        }

        if (clinics.isEmpty()) {
            System.out.println("Cannot seed Doctors because there are no clinics.");
            return;
        }

        for (Clinic clinic: clinics) {
            int doctorsPerClinics = 3;
            for (int i = 0; i< doctorsPerClinics; i++) {
                Doctor doctor = new Doctor();
                doctor.setMcrNo("M" + faker.number().numberBetween(10000,99999) + (char)('A' + faker.number().numberBetween(0,26)));
                doctor.setPassword(faker.internet().password(8,12));
                doctor.setFirstName(faker.name().firstName());
                doctor.setLastName(faker.name().lastName());

                String firstName = faker.name().firstName();
                String lastName = faker.name().lastName();
                String clinicName = clinic.getClinicName();

                String domain = clinicName.toLowerCase()
                        .replace(" ", "")             // remove spaces
                        .replace("clinic", "")        // remove "clinic"
                        .replace("centre", "");        // remove "centre"

                String email = firstName.toLowerCase() + lastName.charAt(0) + "@" + domain + ".com";

                doctor.setEmail(email);
                doctor.setClinic(clinic);
                entityManager.persist(doctor);
            }
        }

    System.out.println("Doctors seeded!");

    }

}


