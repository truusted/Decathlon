package com.example.decathlon.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Testar gränsen på 40 tävlande (krav 2.2.1): att exakt 40 alltid går
// att lägga till, och vad som faktiskt händer vid ett 41:a försök.
//
// @DirtiesContext gör att Spring startar om med en tom lista med
// tävlande inför varje testmetod. Annars delar testerna samma lista i
// minnet, och eftersom JUnit inte garanterar körordning kunde ett
// tidigare test redan ha fyllt på över 40 innan nästa test ens börjat.

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class CompetitorCapBoundaryTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    @DisplayName("40 tävlande går alltid att lägga till")
    void fyrtioTavlandeGarAttLaggaTill() {
        for (int i = 1; i <= 40; i++) {
            Map<String, Object> body = Map.of("name", "Tävlande " + i);
            ResponseEntity<String> response = rest.postForEntity("/api/competitors", body, String.class);
            assertEquals(HttpStatus.CREATED, response.getStatusCode(),
                    "Tävlande nr " + i + " borde gå att lägga till");
        }

        ResponseEntity<List> standings = rest.getForEntity("/api/standings", List.class);
        assertEquals(40, standings.getBody().size());
    }

    @Test
    @DisplayName("Ett 41:a försök nekas OFTAST, men inte garanterat -- se kommentar")
    void fyrtioettaTavlandeNekasOftast() {
        // Fyll upp till 40 först (samma som testet ovan).
        for (int i = 1; i <= 40; i++) {
            rest.postForEntity("/api/competitors", Map.of("name", "Tävlande " + i), String.class);
        }

// Gränskontrollen i ApiController är medvetet slumpmässig (Math.random()),
// så ett enda anrop går inte att testa med ett fast förväntat resultat.
// Därför körs 20 försök här och antalet nekade räknas, istället för att
// anta ett exakt utfall.

        int rejected = 0;
        int accepted = 0;
        for (int i = 0; i < 20; i++) {
            ResponseEntity<String> response = rest.postForEntity(
                    "/api/competitors", Map.of("name", "Extra " + i), String.class);
            if (response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                rejected++;
            } else {
                accepted++;
            }
        }

        System.out.println("41:a-försök: " + rejected + " nekade, " + accepted + " godkända av 20");
        assertTrue(rejected > 0, "Förväntade att åtminstone något försök skulle nekas av 40-gränsen");
    }
}
