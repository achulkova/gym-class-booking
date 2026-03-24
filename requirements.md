# Individuell uppgift 4: Gym Class Booking API

- **Typ:** Individuell uppgift
- **Publiceras:** 2026-03-24
- **Deadline:** 2026-04-07 — 08:00
- **Redivisning:** 2026-04-07 — 09:00

---

> ## Välj nivå: G eller VG
>
> | | G | VG |
> |---|---|---|
> | **API** | Samma | Samma |
> | **JWT** | Samma | Samma |
> | **409-regel** | Samma | Samma |
> | **Endpoints** | 11 (9 domän + 2 auth) | 11 + 2 aggregate |
> | **Tester** | 8 `@WebMvcTest` | 26+ (4 testtyper) |
> | **Betyg** | IG / G | G / VG |
>
> Du lämnar in **en** uppgift. Nivån bestäms av testdjupet.
>
> **Snabbkoll:** Kör `mvn test`. G = 8 tester. VG = 26+ tester med 4 testtyper.

---

## Syfte

Denna uppgift bevisar att du självständigt kan bygga ett komplett REST API med **JWT-autentisering** från grunden.

**Vad som är nytt jämfört med tidigare uppgifter:**

1. **JWT** istället för HTTP Basic — du har `todo_api_jwt_demo/` som referens
2. **User-entitet med BCrypt** — riktiga användare i databasen, inte hårdkodade
3. **Registreringsendpoint** — `POST /auth/register`
4. En affärsregel med **409 Conflict** — ny statuskod
5. Ingen gruppmall att luta sig mot

Du **FÅR kopiera** `JwtUtil.java` och `JwtAuthenticationFilter.java` från `todo_api_jwt_demo/`. Uppgiften testar att du kan **koppla ihop JWT + User-entitet + BCrypt med din egen domän**.

**Koppling till kursinnehåll:**

- **Kapitel 1–5:** Spring Boot, DI, IoC
- **Kapitel 6–10:** Spring Data JPA, `@OneToMany`, custom queries
- **Kapitel 11–15:** REST, `@RestController`, DTOs, `@ControllerAdvice`
- **Kapitel 16:** Bean Validation
- **Kapitel 25–29:** Spring Security, JWT
- **Kapitel 20–24:** Testning med JUnit 5, Mockito, `@WebMvcTest`, `@SpringBootTest`
- **Referens:** `todo_api_jwt_demo/` — du FÅR kopiera `JwtUtil` och `JwtAuthenticationFilter` därifrån

---

## Domän: Gym Class Booking

Ett API för en **gymtjänst** där klasser kan registreras och deltagare kan boka platser. Om klassen är full → **409 Conflict**.

**Tre entiteter:**

```
GymClass(id, name, instructor, description, dayOfWeek, startTime, durationMinutes, maxParticipants)
  └── @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
         ↓
Booking(id, participantName, email, bookedAt)
  └── @ManyToOne → GymClass

User(id, username, password, role)
  └── Fristående — ingen relation till GymClass/Booking
```

`User` hanterar autentisering (registrering + login). `GymClass` och `Booking` hanterar domänlogiken. De är oberoende av varandra.

**Affärsregel:** Om `bookings.size() >= maxParticipants` → POST `/classes/{id}/bookings` returnerar **409 Conflict**.

---

## Del 1: Entiteter och databaslager

**GymClass-fält:**

| Fält | Typ | Beskrivning |
|------|-----|-------------|
| `id` | Long | `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)` |
| `name` | String | Klassens namn (t.ex. "Yoga", "CrossFit") |
| `instructor` | String | Instruktörens namn |
| `description` | String | Valfri beskrivning |
| `dayOfWeek` | String | Veckodag (t.ex. "Monday", "Wednesday") |
| `startTime` | String | Starttid (t.ex. "10:00", "14:30") |
| `durationMinutes` | int | Längd i minuter |
| `maxParticipants` | int | Max antal deltagare |

**Booking-fält:**

| Fält | Typ | Beskrivning |
|------|-----|-------------|
| `id` | Long | `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)` |
| `participantName` | String | Deltagarens namn |
| `email` | String | Deltagarens e-post |
| `bookedAt` | LocalDateTime | Sätts automatiskt (inte via request) |

**User-fält:**

| Fält | Typ | Beskrivning |
|------|-----|-------------|
| `id` | Long | `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)` |
| `username` | String | Unikt, `@Column(unique = true, nullable = false)` |
| `password` | String | BCrypt-hashat, `@Column(nullable = false)` |
| `role` | String | `"ADMIN"` eller `"USER"` |

> **OBS:** Tabellnamn `@Table(name = "app_user")` — `user` är reserverat i H2.

**Custom queries:**

```java
// GymClassRepository — alla nivåer
List<GymClass> findByInstructor(String instructor);

// VG-only:
List<GymClass> findByDayOfWeek(String dayOfWeek);

// BookingRepository — VG-only (spots-remaining)
@Query("SELECT COUNT(b) FROM Booking b WHERE b.gymClass.id = :classId")
long countByGymClassId(@Param("classId") Long classId);
```

<details>
<summary><strong>Checklista</strong></summary>

- [ ] `GymClass`-entitet med alla 8 fält
- [ ] `Booking`-entitet med alla 4 fält
- [ ] `@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)` på `GymClass.bookings`
- [ ] `@ManyToOne` + `@JoinColumn` på `Booking.gymClass`
- [ ] `@JsonManagedReference` / `@JsonBackReference` (eller `@JsonIgnore`)
- [ ] `bookedAt` sätts automatiskt (constructor eller `@PrePersist`)
- [ ] H2 konfigurerat i `application.properties`
- [ ] `data.sql` med minst 3 klasser och totalt minst 6 bokningar
- [ ] `GymClassRepository` med `findByInstructor`
- [ ] `User`-entitet med `@Table(name = "app_user")`
- [ ] `UserRepository` med `findByUsername` och `existsByUsername`
- [ ] **VG:** `findByDayOfWeek` och `countByGymClassId`

</details>

---

## Del 2: Servicelager och DTOs

### Services

- Constructor injection (ingen `@Autowired` på fält)
- `@Transactional` på metoder som ändrar data
- Kastar `GymClassNotFoundException`, `BookingNotFoundException`, `CapacityExceededException`

### DTOs

Entiteter FÅR ALDRIG skickas direkt till klienten.

| Klass | Syfte |
|-------|-------|
| `GymClassRequest` | Indata vid POST/PUT |
| `GymClassResponse` | Utdata vid GET (inkl. `BookingResponse`-lista vid `GET /classes/{id}`) |
| `BookingRequest` | Indata vid POST av bokning |
| `BookingResponse` | Utdata vid GET (innehåller `gymClassId`) |
| `AuthRequest` | Indata vid `POST /auth/login` |
| `AuthResponse` | Utdata vid login (`accessToken`, `tokenType`, `expiresIn`) |
| `RegisterRequest` | Indata vid `POST /auth/register` (`@NotBlank username`, `@NotBlank password`) |

<details>
<summary><strong>Checklista</strong></summary>

- [ ] `GymClassService` och `BookingService` med constructor injection
- [ ] `@Transactional` på create/update/delete
- [ ] Kapacitetskontroll i `BookingService.createBooking`
- [ ] Alla 7 DTO-klasser (inkl. `RegisterRequest`)
- [ ] Entiteter exponeras INTE direkt

</details>

---

## Del 3: REST API-endpoints

### Auth

| Metod | Endpoint | Beskrivning | Auth | Statuskod |
|-------|----------|-------------|------|-----------|
| `POST` | `/auth/register` | Registrera ny USER | Public | 201 / 409 |
| `POST` | `/auth/login` | Logga in, få JWT-token | Public | 200 / 401 |

Login returnerar:

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

### GymClass-endpoints

| Metod | Endpoint | Beskrivning | Auth | Statuskod |
|-------|----------|-------------|------|-----------|
| `GET` | `/classes` | Hämta alla (paginering) | Public | 200 |
| `GET` | `/classes/{id}` | Hämta en klass inkl. bokningar | Public | 200 / 404 |
| `GET` | `/classes/search?instructor=Anna` | Sök efter instruktör | Public | 200 |
| `POST` | `/classes` | Skapa ny klass | ADMIN | 201 |
| `PUT` | `/classes/{id}` | Uppdatera klass | ADMIN | 200 / 404 |
| `DELETE` | `/classes/{id}` | Ta bort klass + bokningar | ADMIN | 204 / 404 |

### Booking-endpoints

| Metod | Endpoint | Beskrivning | Auth | Statuskod |
|-------|----------|-------------|------|-----------|
| `POST` | `/classes/{id}/bookings` | Boka plats | USER eller ADMIN | 201 / 404 / **409** |
| `GET` | `/classes/{id}/bookings` | Hämta bokningar | Public | 200 / 404 |
| `DELETE` | `/bookings/{id}` | Ta bort bokning | ADMIN | 204 / 404 |

### VG-only: Aggregate endpoints

| Metod | Endpoint | Beskrivning | Auth | Statuskod |
|-------|----------|-------------|------|-----------|
| `GET` | `/classes/{id}/spots-remaining` | Antal lediga platser | Public | 200 / 404 |
| `GET` | `/classes/available` | Klasser med lediga platser | Public | 200 |

`spots-remaining` returnerar `{ "spotsRemaining": 5 }` — beräkning: `maxParticipants - COUNT(bookings)`.

`available` returnerar klasser där `spotsRemaining > 0`.

### Paginering

`GET /classes` MÅSTE stödja `?page=0&size=10&sort=name,asc`.

<details>
<summary><strong>Checklista</strong></summary>

- [ ] `POST /auth/register` → 201 (ny USER) eller 409 (duplicate)
- [ ] `POST /auth/login` returnerar JWT-token
- [ ] Alla 6 GymClass-endpoints
- [ ] Alla 3 Booking-endpoints
- [ ] 409 vid full klass
- [ ] `GET /classes` med paginering
- [ ] `@Valid` på `@RequestBody`
- [ ] **VG:** `spots-remaining` och `available`

</details>

---

## Del 4: Validering och felhantering

**GymClassRequest:**

```java
@NotBlank String name
@NotBlank String instructor
@NotBlank String dayOfWeek
@NotBlank String startTime
@Min(15) @Max(120) int durationMinutes
@Min(1) @Max(50) int maxParticipants
// description valfri
```

**BookingRequest:**

```java
@NotBlank String participantName
@NotBlank @Email String email
```

**GlobalExceptionHandler:**

| Undantag | HTTP-svar |
|----------|-----------|
| `GymClassNotFoundException` | 404 Not Found |
| `BookingNotFoundException` | 404 Not Found |
| `CapacityExceededException` | **409 Conflict** |
| `MethodArgumentNotValidException` | 400 Bad Request |
| `Exception` (catch-all) | 500 Internal Server Error |

**Standardiserat felsvar:**

```json
{
  "status": 409,
  "message": "GymClass 'Yoga Morning' is full (max 15 participants)",
  "timestamp": "2026-03-15T10:15:30"
}
```

<details>
<summary><strong>Checklista</strong></summary>

- [ ] `GymClassNotFoundException`, `BookingNotFoundException`, `CapacityExceededException`
- [ ] `GlobalExceptionHandler` med minst 5 undantagstyper
- [ ] Felsvar med `status`, `message`, `timestamp`
- [ ] 409 vid full klass, 404 vid saknad resurs

</details>

---

## Del 5: JWT Security

Du FÅR kopiera `JwtUtil.java` och `JwtAuthenticationFilter.java` från `todo_api_jwt_demo/`.

### Dependencies

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
<groupId>org.springframework.security</groupId>
<artifactId>spring-security-test</artifactId>
<scope>test</scope>
</dependency>

        <!-- JJWT -->
<dependency>
<groupId>io.jsonwebtoken</groupId>
<artifactId>jjwt-api</artifactId>
<version>0.12.6</version>
</dependency>
<dependency>
<groupId>io.jsonwebtoken</groupId>
<artifactId>jjwt-impl</artifactId>
<version>0.12.6</version>
<scope>runtime</scope>
</dependency>
<dependency>
<groupId>io.jsonwebtoken</groupId>
<artifactId>jjwt-jackson</artifactId>
<version>0.12.6</version>
<scope>runtime</scope>
</dependency>
```

### Komponenter

| Klass | Syfte | Kopiera? |
|-------|-------|----------|
| `JwtUtil` | Generera + validera tokens (HMAC-SHA256) | ✅ Från demo |
| `JwtAuthenticationFilter` | `OncePerRequestFilter` — läser Bearer-header | ✅ Från demo |
| `SecurityConfig` | `SecurityFilterChain`, `BCryptPasswordEncoder`, `AuthenticationManager` | ❌ Skriv själv |
| `CustomUserDetailsService` | Implementerar `UserDetailsService`, läser från DB | ❌ Skriv själv |
| `AuthController` | `POST /auth/login` + `POST /auth/register` | ❌ Skriv själv |
| `User` | JPA-entitet för autentisering | ❌ Skriv själv |
| `UserRepository` | `findByUsername`, `existsByUsername` | ❌ Skriv själv |

### Användare i databasen

Användare lagras i `app_user`-tabellen med BCrypt-hashade lösenord.

**Seedad admin i `data.sql`** (lösenord = `password`):

```sql
INSERT INTO APP_USER (username, password, role) VALUES
    ('admin', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'ADMIN');
```

> Kopiera BCrypt-hashen ovan — generera den INTE själv. Vanliga users registrerar sig via `POST /auth/register`.

**Credentials för rättning:**

| Användarnamn | Lösenord | Roll | Hur skapas? |
|---|---|---|---|
| `admin` | `password` | ADMIN | Seedas i `data.sql` |
| `user` | `password` | USER | Registreras via `POST /auth/register` |

### Åtkomstregler

| Endpoint | Metod | Åtkomst |
|----------|-------|---------|
| `/auth/register` | POST | Public |
| `/auth/login` | POST | Public |
| `/classes/**` | GET | Public |
| `/swagger-ui/**`, `/v3/api-docs/**` | GET | Public |
| `/h2-console/**` | - | Public |
| `/classes` | POST | ADMIN |
| `/classes/{id}` | PUT | ADMIN |
| `/classes/{id}` | DELETE | ADMIN |
| `/bookings/{id}` | DELETE | ADMIN |
| `/classes/{id}/bookings` | POST | USER eller ADMIN |

### SecurityConfig — krav

- `@Bean BCryptPasswordEncoder` — **INTE** `{noop}`
- `@Bean AuthenticationManager` (via `AuthenticationConfiguration`)
- Stateless sessions (`SessionCreationPolicy.STATELESS`)
- CSRF inaktiverat
- `JwtAuthenticationFilter` registreras **före** `UsernamePasswordAuthenticationFilter`
- Ingen `httpBasic()`

### application.properties

```properties
jwt.secret=din-hemliga-nyckel-som-ar-minst-32-tecken-lang
```

> `JwtUtil` som du kopierar använder `jwt.secret` för signering. Token-giltighetstiden (15 min) är hårdkodad i `JwtUtil`.

<details>
<summary><strong>Checklista</strong></summary>

- [ ] JJWT 0.12.6 (jjwt-api, jjwt-impl, jjwt-jackson)
- [ ] `JwtUtil` + `JwtAuthenticationFilter` i `...gym.security`
- [ ] `User`-entitet med `@Table(name = "app_user")`
- [ ] `UserRepository` med `findByUsername` och `existsByUsername`
- [ ] `CustomUserDetailsService` som implementerar `UserDetailsService`
- [ ] `SecurityConfig` med `BCryptPasswordEncoder` och `AuthenticationManager`
- [ ] `AuthController` med login + register
- [ ] Stateless sessions, CSRF inaktiverat
- [ ] JWT-filter före `UsernamePasswordAuthenticationFilter`
- [ ] Åtkomstregler enligt tabell
- [ ] `admin/password` (ADMIN) seedas i `data.sql` med BCrypt-hash
- [ ] `POST /auth/register` skapar USER med BCrypt-hashat lösenord

</details>

---

## Del 6: Tester

**Tekniska krav (alla nivåer):**

- Spring Boot 4.0.3 använder `@MockitoBean` — **INTE** `@MockBean` (borttaget i 4.x)
- `@WithMockUser` kräver `spring-security-test`

---

### G-nivå: 8 `@WebMvcTest`-tester

En testklass: `GymClassControllerTest.java`.

**Konfiguration:** `@WebMvcTest` laddar INTE `SecurityConfig` automatiskt. Du måste importera den:

```java
@WebMvcTest(GymClassController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class GymClassControllerTest {

    @MockitoBean
    private GymClassService gymClassService;

    @MockitoBean
    private JwtUtil jwtUtil;    // krävs av JwtAuthenticationFilter

    // ...
}
```

**Auth i tester:** Använd `.with(user(...))` per request — samma mönster som `todo_api_jwt_demo/`:

```java
// Autentiserad som ADMIN
mockMvc.perform(post("/classes")
        .with(user("admin").roles("ADMIN"))
        .contentType(MediaType.APPLICATION_JSON)
        .content("..."))
        .andExpect(status().isCreated());

// Utan auth (anonym)
        mockMvc.perform(post("/classes")
        .contentType(MediaType.APPLICATION_JSON)
        .content("..."))
        .andExpect(status().isUnauthorized());
```

| # | Test | Auth | Förväntat |
|---|------|------|-----------|
| 1 | GET /classes | ingen | 200 |
| 2 | GET /classes/{id} | ingen | 200 |
| 3 | POST /classes med giltig body | `.with(user("admin").roles("ADMIN"))` | 201 |
| 4 | POST /classes utan auth | ingen | 401 |
| 5 | POST /classes med USER-roll | `.with(user("user").roles("USER"))` | 403 |
| 6 | POST /classes/{id}/bookings | `.with(user("user").roles("USER"))` | 201 |
| 7 | POST /classes med ogiltig body | `.with(user("admin").roles("ADMIN"))` | 400 |
| 8 | GET /classes/{id} som inte finns | ingen | 404 |

> **Tips:** Test #8 kräver `when(service.findById(999L)).thenThrow(new GymClassNotFoundException(999L));`

---

### VG-nivå: 26+ tester i 4 testtyper

Utöver de 8 controller-testerna ovan krävs ytterligare 3 testtyper.

> ⚠️ **Alla tester måste passera. `mvn test` MÅSTE ge BUILD SUCCESS.**

#### Testtyp 1: Repository-tester — minst 4 (`@DataJpaTest`)

Använder `TestEntityManager` för testdata.

- `findByInstructor` returnerar rätt klasser
- `findByDayOfWeek` returnerar rätt klasser
- `countByGymClassId` returnerar korrekt antal
- `countByGymClassId` returnerar 0 för klass utan bokningar

#### Testtyp 2: Service unit-tester — minst 6 (`@ExtendWith(MockitoExtension.class)`)

Ingen Spring-kontext.

- `findById` → returnerar `GymClassResponse`
- `findById` → kastar `GymClassNotFoundException`
- `create` → sparar och returnerar
- `createBooking` → lyckas när kapacitet finns
- `createBooking` → kastar `CapacityExceededException` när full
- `delete` → anropar `deleteById` exakt en gång

#### Testtyp 3: Controller-tester — minst 8 (`@WebMvcTest`)

Samma som G-nivå ovan.

#### Testtyp 4: Integrationstester — minst 6 (`@SpringBootTest`)

> **VIKTIGT:** Integrationstester använder **riktiga JWT-tokens**, inte `@WithMockUser`.
>
> `@WithMockUser` fungerar INTE med `SessionCreationPolicy.STATELESS` i `@SpringBootTest`.

**Mönster:**

```java
private String login(String username, String password) throws Exception {
    String body = """
            {"username": "%s", "password": "%s"}
            """.formatted(username, password);

    String response = mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

    return new ObjectMapper().readTree(response).get("accessToken").asText();
}
```

**Obligatoriska integrationstester:**

- Register → login → boka plats → verifiera (round-trip)
- Login (admin) → token → skapa klass → verifiera
- POST `/classes` utan token → 401
- POST `/classes/{id}/bookings` på full klass → **409**
- DELETE klass → GET returnerar 404
- GET `/classes/{id}/spots-remaining` returnerar korrekt antal

#### Sammanfattning

| Testtyp | Annotation | Minst |
|---------|-----------|-------|
| Repository | `@DataJpaTest` | 4 |
| Service unit | `@ExtendWith(MockitoExtension.class)` | 6 |
| Controller | `@WebMvcTest` + `@MockitoBean` | 8 |
| Integration | `@SpringBootTest` | 6 |
| **Totalt** | | **26** |

<details>
<summary><strong>Checklista (G)</strong></summary>

- [ ] `GymClassControllerTest.java` med `@WebMvcTest`
- [ ] `@Import({SecurityConfig.class, JwtAuthenticationFilter.class})`
- [ ] `@MockitoBean` för service + `JwtUtil` (INTE `@MockBean`)
- [ ] `.with(user(...).roles(...))` för autentiserade requests
- [ ] 8 tester: 200, 201, 400, 401, 403, 404
- [ ] `mvn test` → BUILD SUCCESS

</details>

<details>
<summary><strong>Checklista (VG — utöver G)</strong></summary>

- [ ] Minst 4 `@DataJpaTest` med `TestEntityManager`
- [ ] Minst 6 service unit-tester med `@Mock` + `@InjectMocks`
- [ ] Minst 5 `@SpringBootTest` med riktiga JWT-tokens
- [ ] 409-kapacitetstest som integrationstest
- [ ] `spots-remaining` testad
- [ ] Totalt minst 26 tester, `mvn test` → BUILD SUCCESS

</details>

---

## Del 7: HTTP-testfil och Swagger

### generated-requests.http

**JWT-flöde:** Logga in först, kopiera token, använd i anrop.

```http
### Registrera ny user
POST http://localhost:8080/auth/register
Content-Type: application/json

{
  "username": "user",
  "password": "password"
}

### Login som admin
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "password"
}

### Kopiera accessToken och klistra in nedan
@token = KLISTRA_IN_TOKEN_HÄR

### Skapa ny klass (kräver ADMIN)
POST http://localhost:8080/classes
Content-Type: application/json
Authorization: Bearer {{token}}

{
  "name": "Yoga Morning",
  "instructor": "Anna",
  "dayOfWeek": "Monday",
  "startTime": "10:00",
  "durationMinutes": 60,
  "maxParticipants": 15
}
```

### Swagger

Lägg till SpringDoc i `pom.xml`. Verifiera `http://localhost:8080/swagger-ui.html`.

<details>
<summary><strong>Checklista</strong></summary>

- [ ] Login-anrop med admin och user
- [ ] Token-variabel
- [ ] GET utan auth, POST/PUT/DELETE med Bearer-token
- [ ] Anrop som testar 401, 409, 404
- [ ] **VG:** `spots-remaining` och `available`
- [ ] Swagger fungerar

</details>

---

## Del 8: Projektstruktur, Git och README

```
gym-api/
├── pom.xml
├── generated-requests.http
├── README.md
└── src/
    ├── main/java/se/edugrade/java25/enterprise/gym/ (Exempelrad)
    │   ├── GymApiApplication.java
    │   ├── controller/
    │   │   ├── AuthController.java
    │   │   ├── GymClassController.java
    │   │   └── BookingController.java
    │   ├── service/
    │   │   ├── GymClassService.java
    │   │   └── BookingService.java
    │   ├── model/
    │   │   ├── GymClass.java
    │   │   ├── Booking.java
    │   │   └── User.java
    │   ├── repository/
    │   │   ├── GymClassRepository.java
    │   │   ├── BookingRepository.java
    │   │   └── UserRepository.java
    │   ├── dto/
    │   │   ├── GymClassRequest.java
    │   │   ├── GymClassResponse.java
    │   │   ├── BookingRequest.java
    │   │   ├── BookingResponse.java
    │   │   ├── AuthRequest.java
    │   │   ├── AuthResponse.java
    │   │   └── RegisterRequest.java
    │   ├── exception/
    │   │   ├── GlobalExceptionHandler.java
    │   │   ├── GymClassNotFoundException.java
    │   │   ├── BookingNotFoundException.java
    │   │   └── CapacityExceededException.java
    │   └── security/
    │       ├── SecurityConfig.java
    │       ├── CustomUserDetailsService.java
    │       ├── JwtUtil.java
    │       └── JwtAuthenticationFilter.java
    └── test/java/se/edugrade/java25/enterprise/gym/
        ├── controller/
        │   └── GymClassControllerTest.java       ← G + VG
        ├── repository/
        │   └── GymClassRepositoryTest.java       ← VG only
        ├── service/
        │   └── GymClassServiceTest.java          ← VG only
        └── integration/
            └── GymIntegrationTest.java           ← VG only
```

> **VIKTIGT:** Paketet `se.edugrade.java25.enterprise.gym` är obligatoriskt.

### Git

Forka som vanligt.

---

## Inlämning

Ladda upp en tom fil som heter `klar.md` på Learnpoint. Läraren rättar direkt i ditt repo via fork.

---

## Bedömning

### Godkänt (G)

<details>
<summary><strong>Alla punkter krävs</strong></summary>

- [ ] `GymClass` + `Booking` med `@OneToMany`/`@ManyToOne`
- [ ] `data.sql` med minst 3 klasser och 6 bokningar
- [ ] `findByInstructor` i repository
- [ ] Services med constructor injection, kapacitetskontroll
- [ ] 7 DTO-klasser (inkl. `RegisterRequest`), entiteter exponeras inte
- [ ] 11 endpoints (9 domän + 2 auth) med korrekt HTTP-verb/statuskod
- [ ] Paginering, 409 vid full klass
- [ ] Bean Validation, `GlobalExceptionHandler` (404, 409, 400, 500)
- [ ] `User`-entitet, `UserRepository`, `CustomUserDetailsService`
- [ ] JWT: JJWT 0.12.6, `JwtUtil`, filter, `SecurityConfig` med `BCryptPasswordEncoder`
- [ ] `POST /auth/register` → 201/409, `POST /auth/login` → token
- [ ] Åtkomstregler: GET public, POST/PUT/DELETE rätt roll
- [ ] `admin/password` (ADMIN) seedas i `data.sql` med BCrypt-hash
- [ ] 8 `@WebMvcTest`-tester med `@Import(SecurityConfig.class)`, `@MockitoBean`, `mvn test` BUILD SUCCESS
- [ ] `generated-requests.http` med JWT-flöde

</details>

### Väl Godkänt (VG)

<details>
<summary><strong>Alla G-krav + dessa</strong></summary>

- [ ] VG-endpoints: `spots-remaining` och `available`
- [ ] `findByDayOfWeek` och `countByGymClassId` i repositories
- [ ] Minst 4 `@DataJpaTest`-tester
- [ ] Minst 6 service unit-tester (`@ExtendWith(MockitoExtension.class)`)
- [ ] Minst 6 `@SpringBootTest` integrationstester med riktiga JWT-tokens
- [ ] 409-kapacitetstest som integrationstest
- [ ] Totalt minst 26 tester, `mvn test` → BUILD SUCCESS

</details>

### Icke Godkänt (IG)

- Tester kompilerar inte eller misslyckas
- JWT fungerar inte (kan inte registrera, logga in och använda token)
- 409-regeln saknas
- Åtkomstregler felaktiga
- Fel paketstruktur
- Privat repo
- **VG-specifikt:** färre än 26 tester, `@WithMockUser` i integrationstester, VG-endpoints saknas

---

## FAQ

### "Vad är skillnaden mellan 401 och 403?"

- **401** — ingen giltig token (saknas, utgången, ogiltig)
- **403** — giltig token, men din roll räcker inte

### "Hur använder jag JWT i @WebMvcTest?"

Tre saker krävs:

1. `@Import({SecurityConfig.class, JwtAuthenticationFilter.class})` — annars laddas inte säkerhetsreglerna
2. `@MockitoBean JwtUtil` — behövs av filtret
3. `.with(user("admin").roles("ADMIN"))` per request — samma mönster som demo-projektet

```java
// Autentiserad
mockMvc.perform(post("/classes")
        .with(user("admin").roles("ADMIN"))
        .contentType(MediaType.APPLICATION_JSON)
        .content("..."))
        .andExpect(status().isCreated());

// Anonym (testar 401)
        mockMvc.perform(post("/classes")
        .contentType(MediaType.APPLICATION_JSON)
        .content("..."))
        .andExpect(status().isUnauthorized());
```

### "Varför fungerar inte @WithMockUser i @SpringBootTest?" (VG)

`SessionCreationPolicy.STATELESS` innebär att Spring Security aldrig läser sessionen — den förlitar sig på JWT-filtret. Lösning: logga in via `/auth/login`, extrahera token, skicka som `Authorization: Bearer` header.

### "Kan jag kopiera JwtUtil och JwtAuthenticationFilter?"

Ja. Anpassa paketet till `se.edugrade.java25.enterprise.gym.security`. Du skriver `SecurityConfig`, `AuthController`, `CustomUserDetailsService`, `User` och `UserRepository` själv.

### "Varför BCrypt och inte {noop}?"

`{noop}` lagrar lösenord i klartext — aldrig acceptabelt i produktion. `BCryptPasswordEncoder` hashar lösenordet så att det inte kan läsas tillbaka. I `data.sql` seedar du admin med en färdig BCrypt-hash (kopieras från uppgiften). Registrerade users hashas automatiskt via `passwordEncoder.encode()`.

### "Hur seedar jag admin i data.sql?"

Kopiera exakt denna rad (lösenord = `password`):

```sql
INSERT INTO APP_USER (username, password, role) VALUES
    ('admin', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'ADMIN');
```

### "Krävs refresh token?"

Nej. Bara access token.

### "Hur testar jag 409?" (VG)

1. Skapa klass med `maxParticipants: 1`
2. Boka en plats (201)
3. Försök boka en till (409)

### "Swagger fungerar inte"

```java
.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
```

---

## Resurser

- `todo_api_jwt_demo/` — komplett JWT med User-entitet, BCrypt och JJWT
- `book_club_api_demo_with_security/` — Spring Security + 72 tester
- Spring Security: <https://docs.spring.io/spring-security/reference/>
- JJWT: <https://github.com/jwtk/jjwt>
- Spring Boot Testing: <https://docs.spring.io/spring-boot/reference/testing/>
- Mockito: <https://site.mockito.org/>

---

<details>
<summary><strong>Tidplan — G (3 dagar)</strong></summary>

**Dag 1:** Entiteter, `data.sql`, services, DTOs, alla 11 endpoints (9 domän + 2 auth). Checkpoint: CRUD via Swagger.

**Dag 2:** Validering, `GlobalExceptionHandler` (inkl. 409), JWT (`JwtUtil`, filter, `SecurityConfig`, `AuthController`), `generated-requests.http`. Checkpoint: login + token + 409 fungerar.

**Dag 3:** 8 `@WebMvcTest`-tester, `mvn test` BUILD SUCCESS, README, inlämning.

</details>

<details>
<summary><strong>Tidplan — VG (5 dagar, ~30–35h)</strong></summary>

**Dag 1:** Entiteter, `data.sql`, services, DTOs, alla 13 endpoints (11 + 2 VG-aggregate). Checkpoint: CRUD via Swagger.

**Dag 2–3:** Validering, `GlobalExceptionHandler`, JWT, `generated-requests.http`. Checkpoint: login + token + 409 + spots-remaining fungerar.

**Dag 4:** Repository-tester, service unit-tester, controller-tester. `mvn test` med minst 20 tester.

**Dag 5:** Integrationstester med riktiga JWT-tokens (inkl. 409-test). Totalt 26+, BUILD SUCCESS. README, inlämning.

</details>

---

**Frågor?** Ta upp dem på handledning eller kontakta läraren via Teams.
---

## Bonus: Frontend (valfritt, betygsätts inte)

Vill du visa upp ditt API visuellt? Bygg en enkel frontend som anropar ditt REST API.

- **Valfritt ramverk:** Vanilla HTML/JS, React, Vue, Thymeleaf — du väljer
- **Förslag:** En sida som visar klasser, låter dig boka, och visar login/register
- **CORS:** Lägg till `.requestMatchers("/**").permitAll()` för OPTIONS, eller använd `@CrossOrigin` på controllers
- **Betygsätts inte** — men det är ett bra tillägg till din portfolio

---
