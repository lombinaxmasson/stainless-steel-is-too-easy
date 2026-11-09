package com.lombinaxmasson.ssite.chemistry;

import com.lombinaxmasson.ssite.media.RecordTrack;

import java.util.function.Function;
import java.util.function.Predicate;

/** Track selection shared by native recipe providers and registry-independent checks. */
final class RecordManufacturing {
    private RecordManufacturing() {}

    record Selection<T>(T source, RecordTrack track) {}

    static int sourceInput(String label) {
        return switch (label) {
            case "m8-01/cut-master", "m8-02/record-cdr" -> 1;
            case "m8-01/nickel-stamper" -> 0;
            case "m8-01/press-vinyl" -> 2;
            default -> -1;
        };
    }

    static <T> Selection<T> select(Iterable<T> inputs, Predicate<T> accepts,
                                   Function<T, RecordTrack> metadata,
                                   Function<String, RecordTrack> library) {
        for (T candidate : inputs) {
            if (candidate == null || !accepts.test(candidate)) continue;
            RecordTrack bound = metadata.apply(candidate);
            if (bound == null) continue;
            RecordTrack canonical = library.apply(bound.id());
            if (canonical != null && canonical.id().equals(bound.id())) return new Selection<>(candidate, canonical);
        }
        return null;
    }
}
