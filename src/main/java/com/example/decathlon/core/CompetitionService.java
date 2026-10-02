package com.example.decathlon.core;

import com.example.decathlon.dto.EventResultDto;
import com.example.decathlon.dto.StandingDto;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CompetitionService {
    private final ScoringService scoring;

    public CompetitionService(ScoringService scoring) {
        this.scoring = scoring;
    }

    public static class Competitor {
        public final String name;
        public final Map<String, EventResultDto> results = new LinkedHashMap<>();

        public Competitor(String name) {
            this.name = name;
        }

        public int total() {
            return results.values().stream().mapToInt(EventResultDto::points).sum();
        }
    }

    private final Map<String, Competitor> competitors = new LinkedHashMap<>();

    public synchronized void addCompetitor(String name) {
        if (!competitors.containsKey(name)) {
            competitors.put(name, new Competitor(name));
        }
    }

    public synchronized int score(String name, String eventId, double raw) {
        int pts = scoring.score(eventId, raw);
        Competitor c = competitors.computeIfAbsent(name, Competitor::new);
        c.results.put(eventId, new EventResultDto(raw, pts));
        return pts;
    }

    public synchronized List<StandingDto> standings() {
        List<Competitor> sorted = new ArrayList<>(competitors.values());
        sorted.sort(Comparator.comparingInt(Competitor::total).reversed());

        List<StandingDto> result = new ArrayList<>();
        int place = 0;
        int previousTotal = Integer.MIN_VALUE;
        int competitorsSeen = 0;
        for (Competitor c : sorted) {
            competitorsSeen++;
            int total = c.total();
            if (total != previousTotal) {
                place = competitorsSeen;
                previousTotal = total;
            }
            result.add(new StandingDto(c.name, new LinkedHashMap<>(c.results), total, place));
        }
        return result;
    }

    public synchronized String exportCsv() {
        Set<String> eventIds = new LinkedHashSet<>();
        competitors.values().forEach(c -> eventIds.addAll(c.results.keySet()));
        List<String> header = new ArrayList<>();
        header.add("Name");
        header.addAll(eventIds);
        header.add("Total");

        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", header)).append("\n");
        for (Competitor c : competitors.values()) {
            List<String> row = new ArrayList<>();
            row.add(c.name);
            int sum = 0;
            for (String ev : eventIds) {
                EventResultDto r = c.results.get(ev);
                row.add(r == null ? "" : String.valueOf(r.points()));
                if (r != null) sum += r.points();
            }
            row.add(String.valueOf(sum));
            sb.append(String.join(",", row)).append("\n");
        }
        return sb.toString();
    }
}