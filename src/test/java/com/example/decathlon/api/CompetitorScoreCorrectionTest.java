package com.example.decathlon.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

// Testar krav 2.2.3: att tävlandedata ska kunna redigeras (t.ex. rätta ett
// resultat). Testas här via API:et istället för Desktop-/Web-UI.

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class CompetitorScoreCorrectionTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    @DisplayName("Rätta ett resultat via API")
    void rattaEttResultatViaApi() {
        rest.postForEntity("/api/competitors", Map.of("name", "Anna"), String.class);

        ResponseEntity<Map> forsta = rest.postForEntity(
                "/api/score", Map.of("name", "Anna", "event", "100m", "raw", 11.0), Map.class);
        int forstaPoang = (int) forsta.getBody().get("points");

        // Rätta samma tävlandes resultat i samma gren med ett nytt råvärde.
        ResponseEntity<Map> rattat = rest.postForEntity(
                "/api/score", Map.of("name", "Anna", "event", "100m", "raw", 10.5), Map.class);
        int rattadPoang = (int) rattat.getBody().get("points");

        assertNotEquals(forstaPoang, rattadPoang,
                "Ett snabbare (lägre) 100m-resultat ska ge andra poäng än det ursprungliga");

        Map<String, Object> anna = hamtaStandings().get(0);
        Map<?, ?> scores = (Map<?, ?>) anna.get("scores");
        assertEquals(rattadPoang, scores.get("100m"),
                "Resultatlistan ska visa det rättade värdet, inte det ursprungliga");
        assertEquals(rattadPoang, anna.get("total"),
                "Totalpoängen ska vara omräknad utifrån det rättade resultatet");
    }

    @Test
    @DisplayName("Lägga in resultat för en gren som saknas")
    void laggaInResultatForGrenSomSaknas() {
        rest.postForEntity("/api/competitors", Map.of("name", "Anna"), String.class);

        ResponseEntity<Map> svar = rest.postForEntity(
                "/api/score", Map.of("name", "Anna", "event", "100m", "raw", 11.0), Map.class);

        assertEquals(HttpStatus.OK, svar.getStatusCode());

        Map<String, Object> anna = hamtaStandings().get(0);
        Map<?, ?> scores = (Map<?, ?>) anna.get("scores");
        assertNotNull(scores.get("100m"), "Grenen ska nu finnas med bland tävlandens resultat");
        assertEquals(scores.get("100m"), anna.get("total"),
                "Totalpoängen ska räkna med det nytillagda resultatet");
    }

    @Test
    @DisplayName("Rätta resultat för en tävlande som inte finns")
    void rattaResultatForTavlandeSomInteFinns() {
        // "Okänd" har aldrig lagts till via POST /api/competitors.
        ResponseEntity<Map> svar = rest.postForEntity(
                "/api/score", Map.of("name", "Okänd", "event", "100m", "raw", 11.0), Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, svar.getStatusCode(),
                "Ett resultat ska inte gå att registrera/rätta för en tävlande som inte lagts till");
    }

    @Test
    @DisplayName("Går det att ändra namnet på en tävlande?")
    void garDetAttAndraNamnetPaEnTavlande() {
        rest.postForEntity("/api/competitors", Map.of("name", "Anna"), String.class);

        ResponseEntity<String> svar = rest.exchange(
                "/api/competitors/Anna",
                HttpMethod.PUT,
                new HttpEntity<>(Map.of("name", "Annika")),
                String.class);

        assertEquals(HttpStatus.OK, svar.getStatusCode(),
                "Det ska gå att ändra en tävlandes namn via API:et");
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