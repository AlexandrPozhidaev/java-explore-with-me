package ru.practicum.dto;

public class ViewStatsDto {
    private final String app;
    private final String uri;
    private final long hits;

    public ViewStatsDto(String app, String uri, long hits) {
        this.app = app;
        this.uri = uri;
        this.hits = hits;
    }

    public String getApp() {
        return app;
    }

    public String getUri() {
        return uri;
    }

    public long getHits() {
        return hits;
    }
}
