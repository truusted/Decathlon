package com.example.decathlon.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

// Testar krav 2.2.2: att namn, råresultat, poäng, totalpoäng och placering
// sparas per tävlande.

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class CompetitorDataStorageTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    @DisplayName("Namn och poäng sparas per tävlande")
    void namnOchPoangSparasPerTavlande() {
        rest.postForEntity("/api/competitors", Map.of("name", "Anna"), String.class);
        rest.postForEntity("/api/score", Map.of("name", "Anna", "event", "100m", "raw", 11.0), Map.class);

        Map<String, Object> anna = hamtaStandings().get(0);

        assertEquals("Anna", anna.get("name"));
        Map<?, ?> scores = (Map<?, ?>) anna.get("scores");
        assertNotNull(scores.get("100m"), "Poängen för 100m ska finnas sparad");
        assertEquals(861, anna.get("total"), "Totalpoängen ska stämma med det inmatade resultatet");
    }

    @Test
    @DisplayName("Råresultat sparas per tävlande")
    void raResultatSparasPerTavlande() {
        rest.postForEntity("/api/competitors", Map.of("name", "Anna"), String.class);
        rest.postForEntity("/api/score", Map.of("name", "Anna", "event", "100m", "raw", 11.0), Map.class);

        Map<String, Object> anna = hamtaStandings().get(0);


        assertTrue(anna.containsKey("rawResults") || anna.containsKey("results"),
                "Krav 2.2.2 kräver att råresultatet per gren ska sparas per tävlande");
    }

    @Test
    @DisplayName("Placering returneras per tävlande")
    void placeringReturnerasPerTavlande() {
        rest.postForEntity("/api/competitors", Map.of("name", "Anna"), String.class);
        rest.postForEntity("/api/competitors", Map.of("name", "Bea"), String.class);
        rest.postForEntity("/api/competitors", Map.of("name", "Cim"), String.class);

        rest.postForEntity("/api/score", Map.of("name", "Anna", "event", "100m", "raw", 11.0), Map.class);
        rest.postForEntity("/api/score", Map.of("name", "Bea", "event", "100m", "raw", 12.0), Map.class);
        rest.postForEntity("/api/score", Map.of("name", "Cim", "event", "100m", "raw", 13.0), Map.class);

        Map<String, Object> forsta = hamtaStandings().get(0);

        assertTrue(forsta.containsKey("position") || forsta.containsKey("placering") || forsta.containsKey("rank"),
                "Krav 2.2.2 kräver att varje tävlandes placering ska sparas/visas");
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