# Projekat Mikroservisa: Studenti i Upisi

## Sadržaj

- [Pregled Projekta](#pregled-projekta)  
- [Arhitektura](#arhitektura)  
- [Korišćene Tehnologije](#korišćene-tehnologije)  
- [Moduli](#moduli)  
- [Konfiguracija Baze](#konfiguracija-baze)  
- [Pristup H2 Konzoli](#pristup-h2-konzoli)  
- [Otpornost i Tolerancija na Greške](#otpornost-i-tolerancija-na-greške)  
- [Pokretanje Projekta](#pokretanje-projekta)  
- [Endpoints / API](#endpoints--api)  
- [Validacija i Obrada Grešaka](#validacija-i-obrada-grešaka)  

---

## Pregled Projekta

Ovaj projekat je **sistem baziran na mikroservisima** za upravljanje studentima i njihovim upisima na kurseve. Pokazuje najbolje prakse u:

- Arhitekturi mikroservisa sa **otkrivanjem servisa** (Eureka)  
- **RESTful API-ji** za CRUD operacije  
- **Validacija** koristeći Jakarta Bean Validation (`@Valid`, `@NotNull`, `@Pattern`)  
- **Otpornost i tolerancija na greške** koristeći Resilience4j (Circuit Breaker + Retry)  
- In-memory **H2 bazu** za brzi razvoj i testiranje
- Testovi nad **kontrolerima**, **servisima** i **repozitorijumima**.

Sistem se sastoji od dva glavna servisa:

1. **Students Service** — upravlja podacima o studentima (puno ime, email, broj indeksa).  
2. **Enrollments Service** — upravlja upisima na kurseve i validira studente preko Feign poziva ka Students Service.  

## Korišćene Tehnologije

- **Java 17**  
- **Spring Boot 3.x**  
- **Spring Data JPA** sa Hibernate  
- **H2 Database** (in-memory za razvoj)  
- **Spring Cloud OpenFeign** za međuservisne pozive  
- **Resilience4j** za toleranciju na greške  
- **Spring Cloud Eureka** za otkrivanje servisa  
- **Jakarta Bean Validation** (`@NotNull`, `@Email`, `@Pattern`)  
- **Maven** za upravljanje zavisnostima  

---

## Moduli

### 1. Students Service
- Izlaže endpoint-e za **kreiranje, čitanje, ažuriranje i brisanje studenata**.   
- Vraća **odgovarajuće HTTP odgovore** u slučaju grešaka validacije ili konflikata.  

### 2. Enrollments Service
- Izlaže endpoint-e za **kreiranje, čitanje, ažuriranje i brisanje upisa**.  
- Validira **postojanje studenta** putem Feign klijenta ka Students Service.  
- Agregira podatke o studentu za detaljan prikaz (`EnrollmentDetails`).  
- Koristi **Resilience4j** za elegantno rukovanje nedostupnošću Students Service.  

---

## Konfiguracija Baze

Oba servisa koriste **in-memory H2 baze**:

| Servis               | JDBC URL                                         | Korisnik | Lozinka |
|---------------------|-------------------------------------------------|----------|---------|
| Students Service     | `jdbc:h2:mem:studentsdb;DB_CLOSE_DELAY=-1`     | sa       | (prazno)|
| Enrollments Service  | `jdbc:h2:mem:enrolldb;DB_CLOSE_DELAY=-1`       | sa       | (prazno)|

- Baza je **in-memory**, tako da se svi podaci gube kada servis prestane da radi.  
- Hibernate `ddl-auto: update` automatski kreira tabele pri pokretanju.  

---

## Pristup H2 Konzoli

Oba servisa izlažu H2 konzolu za pregled baze:

| Servis               | URL konzole                     |
|---------------------|---------------------------------|
| Students Service     | `http://localhost:9081/h2`      |
| Enrollments Service  | `http://localhost:9082/h2`      |

- **JDBC URL:** isti kao `spring.datasource.url`  
- **Korisnik:** `sa`  
- **Lozinka:** ostaviti prazno  
- Proverite da je **servis pokrenut** pre otvaranja konzole.  

---

## Otpornost i Tolerancija na Greške

- **Circuit Breaker:** sprečava kaskadne greške kada Students Service nije dostupan.  
- **Retry:** automatski pokušava ponovo neuspešne zahteve pre nego što se aktivira fallback.  
- **Fallbacks / rezervne opcije:**  
  - Za `create`/`update` u Enrollments Service, ako Students Service nije dostupan, vraća se **503 Service Unavailable**.  
  - Za `details` endpoint, vraća se delimičan odgovor sa poljima studenta označenim kao `"UNKNOWN"`.  

---

## Pokretanje Projekta

### Redosled pokretanja

1. DiscoveryServiceApplication
2. ApiGateWayApplication
3. StudentsServiceApplication
4. EnrollmentsServiceApplication
  
Pristupite H2 konzolama po potrebi.  
Koristite **REST klijent** (Postman, curl, itd.) za testiranje endpoint-a.  

---

## Endpoints / API

### Students Service (`/students`)
| Metod | Endpoint               | Opis                               |
|-------|-----------------------|-----------------------------------|
| GET   | `/students`            | Lista svih studenata              |
| GET   | `/students/{id}`       | Prikaži studenta po ID-u          |
| POST  | `/students`            | Kreiraj novog studenta            |
| PUT   | `/students/{id}`       | Ažuriraj studenta                 |
| DELETE| `/students/{id}`       | Obriši studenta                   |

### Enrollments Service (`/enrollments`)
| Metod | Endpoint                         | Opis                                         |
|-------|---------------------------------|---------------------------------------------|
| GET   | `/enrollments`                   | Lista svih upisa                            |
| GET   | `/enrollments/{id}`              | Prikaži upis po ID-u                         |
| POST  | `/enrollments`                   | Kreiraj novi upis                            |
| PUT   | `/enrollments/{id}`              | Ažuriraj upis                                |
| DELETE| `/enrollments/{id}`              | Obriši upis                                  |
| GET   | `/enrollments/{id}/details`      | Prikaži upis sa detaljima studenta          |

---

## Validacija i Obrada Grešaka

- **Students Service** validira:
  - Puno ime: samo slova i razmaci  
  - Email: validan email format  
  - Broj indeksa: format `XXX/YYYY`  

- **Enrollments Service** validira:
  - ID studenta: mora postojati i biti pozitivan  
  - Kod kursa: 2–4 velika slova + 2–4 cifre (npr. `DS101`)  
  - Semestar: format `1/2022`  

- **Poruke grešaka:**
  - `404 Not Found` → za nedostatak podataka  
  - `409 Conflict` → za duplirani email ili broj indeksa  
  - `400 Bad Request` → greške validacije  
  - `503 Service Unavailable` → kada zavisni servis nije dostupan
