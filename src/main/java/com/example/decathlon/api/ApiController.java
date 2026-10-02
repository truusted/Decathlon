package com.example.decathlon.api;

import com.example.decathlon.core.CompetitionService;
import com.example.decathlon.dto.ScoreReq;
import com.example.decathlon.dto.StandingDto;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final CompetitionService comp;

    public ApiController(CompetitionService comp) { this.comp = comp; }

    @PostMapping("/competitors")
    public ResponseEntity<?> add(@RequestBody Map<String,String> body) {
        String name = Optional.ofNullable(body.get("name")).orElse("").trim();

        if (name.isEmpty()) {
            return ResponseEntity.badRequest().body("Empty name");
        }

        comp.addCompetitor(name);
        return ResponseEntity.status(201).build();
    }

    @PostMapping("/score")
    public ResponseEntity<?> score(@RequestBody ScoreReq r) {
        try {
            int pts = comp.score(r.name(), r.event(), r.raw());
            return ResponseEntity.ok(Map.of("points", pts));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/standings")
    public List<StandingDto> standings() { return comp.standings(); }

    @GetMapping(value="/export.csv", produces = MediaType.TEXT_PLAIN_VALUE)
    public String export() { return comp.exportCsv(); }
}