package com.lombinaxmasson.ssite.media;

/** Immutable metadata; audio bytes live in the server library and client cache. */
public record RecordTrack(String id, String title, int durationTicks, int bytes) {
    public RecordTrack {
        if (!validId(id)) throw new IllegalArgumentException("Invalid track hash");
        if (title == null || title.isBlank() || title.length() > 96
                || title.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Invalid track title");
        }
        if (durationTicks < 1 || durationTicks > RecordAudio.MAX_TICKS
                || bytes < 1 || bytes > RecordAudio.MAX_BYTES) {
            throw new IllegalArgumentException("Invalid track limits");
        }
    }

    public static boolean validId(String id) {
        if (id == null || id.length() != 64) return false;
        for (int i = 0; i < id.length(); i++) {
            char c = id.charAt(i);
            if ((c < '0' || c > '9') && (c < 'a' || c > 'f')) return false;
        }
        return true;
    }
}
