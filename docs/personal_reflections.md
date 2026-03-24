# Personlig reflektion: Gym Class Booking API (Capstone)

**Namn:** _____________________
**Datum:** _____________________
**Kurs:** Enterprise Java (05)
**Nivå:** G / VG _(stryk det som inte gäller)_

---

> **Konfidentiellt:** Lämna INTE in i GitHub. Lämna in separat via Learnpoint.
>
> **Tips:** Korta, ärliga, konkreta svar är bättre än långa vaga generaliseringar. Ge specifika exempel från koden.

**Tid:** 10–15 min (G) / 15–20 min (VG)

---

## Del 1: Grundläggande reflektion (G + VG)

### 1. Vad var svårast med JWT-implementationen?

> T.ex. hur `JwtAuthenticationFilter` kopplas till `SecurityConfig`, hur tokenen genereras, eller hur `AuthController` skiljer sig från en vanlig controller.

```
Vad var svårt:


Hur du löste det:
```

---

### 2. Förklara med egna ord: vad händer steg för steg när en request med giltig Bearer-token når ditt API?

> Nämn JwtAuthenticationFilter, JwtUtil och SecurityContext.

```
Svar:
```

---

### 3. Hur implementerade du 409 Conflict?

> Var sker kontrollen (klass, metod)? Hur kastas `CapacityExceededException`? Hur mappas den till 409?

```
Var kontrollen sker:


Hur undantaget kastas:


Hur det mappas till 409:
```

---

### 4. Välj ett av dina `@WebMvcTest`-tester och förklara vad det bevisar.

> Beskriv vad testet gör, vilken auth-annotation (eller avsaknad), och varför statuskoden är korrekt.

```
Test (metod + klass):


Vad det bevisar:


Vad som händer om du ändrar auth-nivån:
```

---

### 5. Vad tar du med dig?

> Om du skulle bygga ett nytt API med JWT imorgon — vad hade du gjort annorlunda?

```
Svar:
```

---

## Del 2: Fördjupad reflektion (VG-only)

> **Besvara dessa BARA om du lämnar in på VG-nivå.**

### 6. `@WithMockUser` vs riktiga JWT-tokens

**Varför använder du `@WithMockUser` i `@WebMvcTest` men riktiga JWT-tokens i `@SpringBootTest`? Vad händer om du byter?**

```
Varför @WithMockUser i @WebMvcTest:


Varför riktiga tokens i @SpringBootTest:


Vad som händer om du använder @WithMockUser i @SpringBootTest:
```

---

### 7. 409-integrationstestet

**Beskriv steg för steg hur du testade kapacitetsregeln i ett integrationstest.**

> Hur satte du upp förutsättningarna (klass med maxParticipants), hur bokade du platser, och hur verifierade du 409?

```
Upplägg:


Varför detta testar affärsregeln korrekt:
```

---

### 8. Vad lärde du dig om testtyper?

**Förklara med egna ord: vad testar `@DataJpaTest` som `@WebMvcTest` inte testar, och vice versa?**

```
@DataJpaTest testar:


@WebMvcTest testar:


När du väljer det ena framför det andra:
```

---

## Bedömning

**G kräver:** Fråga 1–5 besvarade med konkreta exempel.

**VG kräver:** Fråga 1–8 besvarade. Del 2 kräver specifika testnamn och konkret analys av JWT-flöde och testisolering.

**Godkänt svar:**
> "Svårast var att förstå hur `JwtAuthenticationFilter` sätter `SecurityContext`. Jag kopierade filtret från demo-projektet men förstod inte varför `UsernamePasswordAuthenticationToken` skapas med tre argument (principal, null, authorities). Efter debugging insåg jag att det tredje argumentet är det som ger Spring Security rollinfo."

**Inte godkänt svar:**
> "JWT var lite krångligt men jag fixade det."
> (Inga konkreta exempel)

---

## Inlämning

- **Filnamn:** `personal_reflection_[DITTNAMN].pdf` eller `.md`
- **Plattform:** Learnpoint (INTE GitHub)
- **Deadline:** Samma som uppgiften, kl. 08:00

> **VIKTIGT:** Utan personlig reflektion → IG på hela uppgiften, även om koden är korrekt.
