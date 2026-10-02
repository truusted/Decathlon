package com.example.decathlon.core;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ScoringService {
    public enum Type { TRACK, FIELD }
    public record EventDef(String id, Type type, double A, double B, double C, String unit, double min, double max) {}

    private final Map<String, EventDef> events = Map.ofEntries(
            Map.entry("100m",           new EventDef("100m",           Type.TRACK, 25.4347,  18.0,  1.81,  "s",  5,     17.8)),
            Map.entry("longJump",       new EventDef("longJump",       Type.FIELD, 0.14354,  220.0, 1.4,   "cm", 250,   1000)),
            Map.entry("shotPut",        new EventDef("shotPut",        Type.FIELD, 51.39,    1.5,   1.05,  "m",  0.01,  30)),
            Map.entry("highJump",       new EventDef("highJump",       Type.FIELD, 0.8465,   75.0,  1.42,  "cm", 0.01,  100)),
            Map.entry("400m",           new EventDef("400m",           Type.TRACK, 1.53775,  82.0,  1.81,  "s",  20,    100)),
            Map.entry("110mHurdles",    new EventDef("110mHurdles",    Type.TRACK, 5.74352,  28.5,  1.92,  "s",  10,    28.5)),
            Map.entry("discusThrow",    new EventDef("discusThrow",    Type.FIELD, 12.91,    4.0,   1.1,   "m",  0.01,  85)),
            Map.entry("poleVault",      new EventDef("poleVault",      Type.FIELD, 0.2797,   100.0, 1.35,  "cm", 2,     1000)),
            Map.entry("javelinThrow",   new EventDef("javelinThrow",   Type.FIELD, 10.14,    7.0,   1.08,  "m",  0.01,  110)),
            Map.entry("1500m",          new EventDef("1500m",          Type.TRACK, 0.03768,  480.0, 1.85,  "s",  2,     7)),
            Map.entry("hep100mHurdles", new EventDef("hep100mHurdles", Type.TRACK, 9.23076,  26.7,  1.835, "s",  5,     26.4)),
            Map.entry("hepHighJump",    new EventDef("hepHighJump",    Type.FIELD, 1.84523,  75.0,  1.348, "cm", 75.7,  270)),
            Map.entry("hepShotPut",     new EventDef("hepShotPut",     Type.FIELD, 56.0211,  1.5,   1.05,  "m",  5,     100)),
            Map.entry("hep200m",        new EventDef("hep200m",        Type.TRACK, 4.99087,  42.5,  1.81,  "s",  14,    42.08)),
            Map.entry("hepLongJump",    new EventDef("hepLongJump",    Type.FIELD, 0.188807, 210.0, 1.41,  "cm", 0.01,  400)),
            Map.entry("hepJavelinThrow",new EventDef("hepJavelinThrow",Type.FIELD, 15.9803,  3.8,   1.04,  "m",  0.01,  100)),
            Map.entry("hep800m",        new EventDef("hep800m",        Type.TRACK, 0.11193,  254.0, 1.88,  "s",  70,    250.79))
    );

    public EventDef get(String id) { return events.get(id); }

    public int score(String eventId, double raw) {
        EventDef e = events.get(eventId);
        if (e == null) {
            throw new IllegalArgumentException("Unknown event: " + eventId);
        }
        if (raw < e.min() || raw > e.max()) {
            throw new IllegalArgumentException("Enter a " + eventId + " result between " + formatNumber(e.min()) + " and " + formatNumber(e.max()) + " " + e.unit() + ".");
        }

        double points;
        if (e.type() == Type.TRACK) {
            points = e.A() * Math.pow(e.B() - raw, e.C());
        } else {
            points = e.A() * Math.pow(raw - e.B(), e.C());
        }
        return (int) Math.floor(points);
    }

    private String formatNumber(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }
}