package com.example.decathlon.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

// Testar krav 2.3.1: att resultatlistan (standings) alltid går att hämta,
// och visar poäng per gren, totalpoäng och placering. Testas här via
// API:et istället för Desktop-/Web-UI.

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class StandingsApiTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    @DisplayName("Hämta resultatlistan innan några resultat finns")
    void hamtaResultatlistanInnanNagraResultatFinns() {
        rest.postForEntity("/api/competitors", Map.of("name", "Anna"), String.class);

        ResponseEntity<List> svar = rest.getForEntity("/api/standings", List.class);
        assertEquals(HttpStatus.OK, svar.getStatusCode());

        Map<String, Object> anna = hamtaStandings().get(0);
        Map<?, ?> scores = (Map<?, ?>) anna.get("scores");
        assertTrue(scores.isEmpty(), "Inga resultat är inrapporterade än, så grenlistan ska vara tom");
        assertEquals(0, anna.get("total"), "Totalpoängen ska vara 0 innan något resultat finns");
    }

    @Test
    @DisplayName("Resultatlistan visar rätt poäng per gren")
    void resultatlistanVisarRattPoangPerGren() {
        rest.postForEntity("/api/competitors", Map.of("name", "Anna"), String.class);

        ResponseEntity<Map> svar100m = rest.postForEntity(
                "/api/score", Map.of("name", "Anna", "event", "100m", "raw", 11.0), Map.class);
        ResponseEntity<Map> svarLangdhopp = rest.postForEntity(
                "/api/score", Map.of("name", "Anna", "event", "longJump", "raw", 650.0), Map.class);

        int poang100m = (int) svar100m.getBody().get("points");
        int poangLangdhopp = (int) svarLangdhopp.getBody().get("points");

        Map<String, Object> anna = hamtaStandings().get(0);
        Map<?, ?> scores = (Map<?, ?>) anna.get("scores");

        assertEquals(poang100m, scores.get("100m"), "Poängen för 100m ska stämma med det uträknade värdet");
        assertEquals(poangLangdhopp, scores.get("longJump"), "Poängen för längdhopp ska stämma med det uträknade värdet");
    }

    @Test
    @DisplayName("Resultatlistan visar rätt totalpoäng")
    void resultatlistanVisarRattTotalpoang() {
        rest.postForEntity("/api/competitors", Map.of("name", "Anna"), String.class);

        ResponseEntity<Map> svar100m = rest.postForEntity(
                "/api/score", Map.of("name", "Anna", "event", "100m", "raw", 11.0), Map.class);
        ResponseEntity<Map> svarLangdhopp = rest.postForEntity(
                "/api/score", Map.of("name", "Anna", "event", "longJump", "raw", 650.0), Map.class);

        int poang100m = (int) svar100m.getBody().get("points");
        int poangLangdhopp = (int) svarLangdhopp.getBody().get("points");
        int forvantadTotal = poang100m + poangLangdhopp;

        Map<String, Object> anna = hamtaStandings().get(0);

        assertEquals(forvantadTotal, anna.get("total"),
                "Totalpoängen ska vara summan av poängen i alla grenar");
    }

    @Test
    @DisplayName("Resultatlistan visar tävlandens placering")
    void resultatlistanVisarTavlandensPlacering() {
        rest.postForEntity("/api/competitors", Map.of("name", "Anna"), String.class);
        rest.postForEntity("/api/competitors", Map.of("name", "Bea"), String.class);
        rest.postForEntity("/api/competitors", Map.of("name", "Cim"), String.class);

        rest.postForEntity("/api/score", Map.of("name", "Anna", "event", "100m", "raw", 11.0), Map.class);
        rest.postForEntity("/api/score", Map.of("name", "Bea", "event", "100m", "raw", 12.0), Map.class);
        rest.postForEntity("/api/score", Map.of("name", "Cim", "event", "100m", "raw", 13.0), Map.class);

        Map<String, Object> forsta = hamtaStandings().get(0);

        assertTrue(
                forsta.containsKey("position") || forsta.containsKey("placering") || forsta.containsKey("rank"),
                "Krav 2.3.1 kräver att varje tävlandes placering ska visas, inte bara ordningen i listan"
        );
    }

    private List<Map<String, Object>> hamtaStandings() {
        ResponseEntity<List<Map<String, Object>>> response = rest.exchange(
                "/api/standings",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {});
        return response.getBody();
    }
}