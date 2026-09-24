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

// Testar krav 2.3.2: att resultatlistan (standings) uppdateras när nya
// eller rättade resultat sparas. Testas här via API:et istället för
// Desktop-/Web-UI.

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class StandingsUpdateTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    @DisplayName("Resultatlistan uppdateras direkt efter ett nytt resultat")
    void resultatlistanUppdaterasDirektEfterNyttResultat() {
        rest.postForEntity("/api/competitors", Map.of("name", "Anna"), String.class);

        Map<String, Object> innan = hamtaStandings().get(0);
        Map<?, ?> scoresInnan = (Map<?, ?>) innan.get("scores");
        assertNull(scoresInnan.get("100m"), "Inget resultat ska finnas i grenen än");

        ResponseEntity<Map> svar = rest.postForEntity(
                "/api/score", Map.of("name", "Anna", "event", "100m", "raw", 11.0), Map.class);
        int poang = (int) svar.getBody().get("points");

        Map<String, Object> efter = hamtaStandings().get(0);
        Map<?, ?> scoresEfter = (Map<?, ?>) efter.get("scores");

        assertEquals(poang, scoresEfter.get("100m"),
                "Resultatlistan ska visa det nya resultatet direkt, utan extra steg");
    }

    @Test
    @DisplayName("Resultatlistan uppdateras direkt efter ett rättat resultat")
    void resultatlistanUppdaterasDirektEfterRattatResultat() {
        rest.postForEntity("/api/competitors", Map.of("name", "Anna"), String.class);

        ResponseEntity<Map> forsta = rest.postForEntity(
                "/api/score", Map.of("name", "Anna", "event", "100m", "raw", 11.0), Map.class);
        int forstaPoang = (int) forsta.getBody().get("points");

        Map<String, Object> innan = hamtaStandings().get(0);
        Map<?, ?> scoresInnan = (Map<?, ?>) innan.get("scores");
        assertEquals(forstaPoang, scoresInnan.get("100m"), "Det första resultatet ska synas innan rättelsen");

        ResponseEntity<Map> rattat = rest.postForEntity(
                "/api/score", Map.of("name", "Anna", "event", "100m", "raw", 10.5), Map.class);
        int rattadPoang = (int) rattat.getBody().get("points");

        Map<String, Object> efter = hamtaStandings().get(0);
        Map<?, ?> scoresEfter = (Map<?, ?>) efter.get("scores");

        assertEquals(rattadPoang, scoresEfter.get("100m"),
                "Resultatlistan ska visa det rättade värdet direkt, inte det gamla");
    }

    @Test
    @DisplayName("Totalpoängen uppdateras direkt vid ändring")
    void totalpoangenUppdaterasDirektVidAndring() {
        rest.postForEntity("/api/competitors", Map.of("name", "Anna"), String.class);

        ResponseEntity<Map> svar100m = rest.postForEntity(
                "/api/score", Map.of("name", "Anna", "event", "100m", "raw", 11.0), Map.class);
        int poang100m = (int) svar100m.getBody().get("points");

        Map<String, Object> efterForsta = hamtaStandings().get(0);
        assertEquals(poang100m, efterForsta.get("total"),
                "Totalpoängen ska stämma med det första resultatet");

        ResponseEntity<Map> svarLangdhopp = rest.postForEntity(
                "/api/score", Map.of("name", "Anna", "event", "longJump", "raw", 650.0), Map.class);
        int poangLangdhopp = (int) svarLangdhopp.getBody().get("points");

        Map<String, Object> efterAndra = hamtaStandings().get(0);
        assertEquals(poang100m + poangLangdhopp, efterAndra.get("total"),
                "Totalpoängen ska öka direkt och stämma med summan av båda grenarna");
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