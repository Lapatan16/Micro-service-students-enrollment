# Projekat Mikroservisa: Studenti i Upisi

## Sadržaj

- [Pregled Projekta](#pregled-projekta)  
- [Arhitektura](#arhitektura)  
- [Korišćene Tehnologije](#korišćene-tehnologije)  
- [Moduli](#moduli)  
- [Tabela servisa](#tabela-servisa)  
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

## Šta je urađeno

### Obavezno

1. Service Discovery – Eureka Server
2. API Gateway – Spring Cloud Gateway
3. Dva mikroservisa
   - StudentsService
   - EnrollmentsService
4. Komunikacija servis–servis – OpenFeign
5. Otpornost – Resilience4j (Circuit Breaker + Retry)
6. Persistencija: H2 (in-memory) za oba servisa
7. Agregacioni endpoint
   - GET /enrollments/{id}/details

### Opciono

1. Jednostavna autentikacija na gateway-u putem API key filter

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

## Tabela Servisa

| Servis               | Port  | Rute / Endpoint-i                       | Odgovornosti                                  |
|---------------------|-------|----------------------------------------|-----------------------------------------------|
| API Gateway          | 8081  | `/api/**`, `/api/**`      | Rukovanje svim spoljnim zahtevima, prosleđivanje ka odgovarajućim mikroservisima |
| Students Service     | 9081  | `/students`, `/students/{id}`          | Upravljanje studentima (CRUD), validacija   |
| Enrollments Service  | 9082  | `/enrollments`, `/enrollments/{id}`, `/enrollments/{id}/details` | Upravljanje upisima (CRUD), agregacija podataka o studentima, validacija, otpornost na greške (Resilience4j) |
| Eureka Server        | 8761  | `/`                                     | Otkrivanje servisa (Service Registry)        |


---

## Pristup H2 Konzoli

Oba servisa izlažu H2 konzolu za pregled baze:

| Servis               | URL konzole                     |
|---------------------|---------------------------------|
| Students Service     | `http://localhost:9081/h2`      |
| Enrollments Service  | `http://localhost:9082/h2`      |
 
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

1. DiscoveryServiceApplication - port: 8761
2. ApiGateWayApplication - port: 8081
3. StudentsServiceApplication - port: 9081
4. EnrollmentsServiceApplication - port: 9082
  
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
