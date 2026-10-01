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

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

// Testar krav 2.5.1: att exporten innehåller namn, råresultat per gren,
// poäng per gren, totalpoäng och resultatplacering. Testas här via
// API:et (GET /api/export.csv) istället för Desktop-/Web-UI.

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ExportApiTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    @DisplayName("Export innehåller namn och poäng per gren")
    void exportInnehallerNamnOchPoangPerGren() {
        rest.postForEntity("/api/competitors", Map.of("name", "Anna"), String.class);
        ResponseEntity<Map> svar = rest.postForEntity(
                "/api/score", Map.of("name", "Anna", "event", "100m", "raw", 11.0), Map.class);
        int poang = (int) svar.getBody().get("points");

        ResponseEntity<String> export = rest.getForEntity("/api/export.csv", String.class);
        assertEquals(HttpStatus.OK, export.getStatusCode());

        String csv = export.getBody();
        assertTrue(csv.contains("Anna"), "Exporten ska innehålla tävlandens namn");
        assertTrue(csv.contains(String.valueOf(poang)), "Exporten ska innehålla poängen för grenen");
    }

    @Test
    @DisplayName("Export innehåller totalpoäng")
    void exportInnehallerTotalpoang() {
        rest.postForEntity("/api/competitors", Map.of("name", "Anna"), String.class);
        ResponseEntity<Map> svar100m = rest.postForEntity(
                "/api/score", Map.of("name", "Anna", "event", "100m", "raw", 11.0), Map.class);
        ResponseEntity<Map> svarLangdhopp = rest.postForEntity(
                "/api/score", Map.of("name", "Anna", "event", "longJump", "raw", 650.0), Map.class);

        int total = (int) svar100m.getBody().get("points") + (int) svarLangdhopp.getBody().get("points");

        ResponseEntity<String> export = rest.getForEntity("/api/export.csv", String.class);
        String csv = export.getBody();

        assertTrue(csv.contains(String.valueOf(total)),
                "Exporten ska innehålla totalpoängen (" + total + ")");
    }

    @Test
    @DisplayName("Export innehåller råresultat per gren")
    void exportInnehallerRaresultatPerGren() {
        rest.postForEntity("/api/competitors", Map.of("name", "Anna"), String.class);
        rest.postForEntity("/api/score", Map.of("name", "Anna", "event", "100m", "raw", 11.0), Map.class);

        ResponseEntity<String> export = rest.getForEntity("/api/export.csv", String.class);
        String csv = export.getBody();

        // Krav 2.5.1 kräver att råresultatet (t.ex. "11.0") finns med, inte
        // bara den uträknade poängen. Just nu sparas bara poängen i
        // CompetitionService, så det här testet förväntas fela.
        assertTrue(csv.contains("11.0") || csv.contains("11,0"),
                "Exporten ska innehålla råresultatet (11.0), inte bara poängen");
    }

    @Test
    @DisplayName("Export innehåller resultatplacering")
    void exportInnehallerResultatplacering() {
        rest.postForEntity("/api/competitors", Map.of("name", "Anna"), String.class);
        rest.postForEntity("/api/competitors", Map.of("name", "Bea"), String.class);
        rest.postForEntity("/api/competitors", Map.of("name", "Cim"), String.class);

        rest.postForEntity("/api/score", Map.of("name", "Anna", "event", "100m", "raw", 11.0), Map.class);
        rest.postForEntity("/api/score", Map.of("name", "Bea", "event", "100m", "raw", 12.0), Map.class);
        rest.postForEntity("/api/score", Map.of("name", "Cim", "event", "100m", "raw", 13.0), Map.class);

        ResponseEntity<String> export = rest.getForEntity("/api/export.csv", String.class);
        String header = export.getBody().split("\n")[0].toLowerCase();

        // Krav 2.5.1 kräver en egen kolumn för placering. Just nu bygger
        // headern bara "Name,<grenar>,Total", så det här testet förväntas
        // fela.
        assertTrue(header.contains("plac") || header.contains("position") || header.contains("rank"),
                "Exporten ska ha en egen kolumn för placering, header var: " + header);
    }
}