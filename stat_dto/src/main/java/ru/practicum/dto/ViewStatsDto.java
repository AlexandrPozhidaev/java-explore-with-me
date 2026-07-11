package ru.practicum.dto;

public class ViewStatsDto {
    private String app;
    private String uri;
    private long hits;
    private long uniqueHits;

    public ViewStatsDto() {}

    public ViewStatsDto(String app, String uri, long hits, long uniqueHits) {
        this.app = app;
        this.uri = uri;
        this.hits = hits;
        this.uniqueHits = uniqueHits;
    }

    public String getApp() { return app; }
    public void setApp(String app) { this.app = app; }

    public String getUri() { return uri; }
    public void setUri(String uri) { this.uri = uri; }

    public long getHits() { return hits; }
    public void setHits(long hits) { this.hits = hits; }

    public long getUniqueHits() { return uniqueHits; }
    public void setUniqueHits(long uniqueHits) { this.uniqueHits = uniqueHits; }
}
