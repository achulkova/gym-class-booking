# Personlig reflektion: Gym Class Booking API (Capstone)

**Namn:** Anna Chulkova
**Datum:** 2026-04-09
**Kurs:** Enterprise Java (05)
**Nivå:** VG

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
Det var svårt för mig att förstå hur man självständigt skriver kod för JWT, eftersom det i jämförelse med den tidigare uppgiften tillkom fler filer 
och mer kod som hänger ihop med varandra. Jag behövde förstå hur alla delar fungerar tillsammans, inte bara var för sig.


Hur du löste det:
Jag valde att noggrant studera skillnaderna jämfört med den tidigare uppgiften och att fördjupa mig i de tekniska detaljerna.
```

---

### 2. Förklara med egna ord: vad händer steg för steg när en request med giltig Bearer-token når ditt API?

> Nämn JwtAuthenticationFilter, JwtUtil och SecurityContext.

```
Svar:
När en request med en giltig Bearer-token kommer till API:et händer ungefär så här:

1. Först passerar requesten genom JwtAuthenticationFilter.  
2. Den kollar om det finns en token i Authorization-headern.  
3. Om det finns en token, plockar den ut den.  
4. Sedan används JwtUtil för att kontrollera att tokenen är giltig och inte har gått ut, och för att hämta username och roller.  
5. Om allt är okej skapas ett Authentication-objekt med användarens information.  
6. Det objektet sparas i SecurityContext via JwtAuthenticationFilter så att systemet vet vem som gör requesten.  
7. Efter det går requesten vidare till controllern, och åtkomsten styrs av användarens roll.
```

---

### 3. Hur implementerade du 409 Conflict?

> Var sker kontrollen (klass, metod)? Hur kastas `CapacityExceededException`? Hur mappas den till 409?

```
Var kontrollen sker:
Kontrollen sker i BookingService, i metoden createBooking(). 
Där hämtas gymklassen först, och sedan kontrolleras om antalet bokningar redan har nått maxgränsen. 
I min kod är villkoret att gymClass.getBookings().size() jämförs med gymClass.getMaxParticipants().

Hur undantaget kastas:
Om klassen redan är full kastas en CapacityExceededException med ett tydligt felmeddelande som säger att klassen är full och hur många deltagare som max är tillåtna. 
Det sker direkt i BookingService.createBooking().

Hur det mappas till 409:
I GlobalExceptionHandler finns en egen @ExceptionHandler för CapacityExceededException. 
Där returneras ett standardiserat felsvar med statuskoden CONFLICT, alltså 409, tillsammans med message och timestamp.
```

---

### 4. Välj ett av dina `@WebMvcTest`-tester och förklara vad det bevisar.

> Beskriv vad testet gör, vilken auth-annotation (eller avsaknad), och varför statuskoden är korrekt.

```
Test (metod + klass):
POST /classes med ADMIN i GymClassControllerTest

Vad det bevisar:
Det testet bevisar att en användare med rollen ADMIN kan skapa en ny gymklass. 
När requesten skickas med ADMIN-roll returnerar API:et status 201 Created, vilket är korrekt för en lyckad POST.


Vad som händer om du ändrar auth-nivån:
Om rollen ändras till USER returneras 403 Forbidden. 
Om ingen autentisering används returneras 401 Unauthorized.
```

---

### 5. Vad tar du med dig?

> Om du skulle bygga ett nytt API med JWT imorgon — vad hade du gjort annorlunda?

```
Svar:
Om jag skulle bygga ett nytt API med JWT imorgon, hade jag börjat med en mer detaljerad plan för endpoints och vilka metoder som behövs för varje endpoint. 
Jag hade också varit mer noggrann med validering från början. 
Istället för att bygga hela lagret på en gång hade jag arbetat endpoint för endpoint och testat varje del direkt, både i generated-requests.http och via Swagger. 
```

---

## Del 2: Fördjupad reflektion (VG-only)

> **Besvara dessa BARA om du lämnar in på VG-nivå.**

### 6. `@WithMockUser` vs riktiga JWT-tokens

**Varför använder du `@WithMockUser` i `@WebMvcTest` men riktiga JWT-tokens i `@SpringBootTest`? Vad händer om du byter?**

```
Varför @WithMockUser i @WebMvcTest:
I @WebMvcTest används @WithMockUser eftersom vi inte kör hela security-konfigurationen. 
Det är ett snabbt sätt att simulera en användare utan att behöva skapa riktiga JWT-tokens.

Varför riktiga tokens i @SpringBootTest:
I @SpringBootTest körs hela applikationen, inklusive JwtAuthenticationFilter och SecurityConfig. 
Därför måste vi använda riktiga JWT-tokens för att testa hela autentiseringsflödet som i verkligheten.


Vad som händer om du använder @WithMockUser i @SpringBootTest:
Om man använder @WithMockUser i @SpringBootTest fungerar det inte korrekt, eftersom applikationen använder stateless JWT-autentisering. 
Då ignoreras mock-användaren och requesten blir oftast unauthorized (401).
```

---

### 7. 409-integrationstestet

**Beskriv steg för steg hur du testade kapacitetsregeln i ett integrationstest.**

> Hur satte du upp förutsättningarna (klass med maxParticipants), hur bokade du platser, och hur verifierade du 409?

```
Upplägg:
Jag skapade en klass med maxParticipants = 1. 
Jag gjorde en bokning som fungerade. 
Sedan försökte jag boka igen för samma klass och fick 409 Conflict.


Varför detta testar affärsregeln korrekt:
Det visar att när klassen är full går det inte att boka fler platser.
```

---

### 8. Vad lärde du dig om testtyper?

**Förklara med egna ord: vad testar `@DataJpaTest` som `@WebMvcTest` inte testar, och vice versa?**

```
@DataJpaTest testar:
@DataJpaTest testar databasen och repositories, till exempel queries och hur data sparas och hämtas.


@WebMvcTest testar:
@WebMvcTest testar controllers och HTTP-anrop, till exempel endpoints, statuskoder och validering.


När du väljer det ena framför det andra:
@DataJpaTest används när man vill testa databasen.
@WebMvcTest används när man vill testa API:et utan att starta hela applikationen.
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
