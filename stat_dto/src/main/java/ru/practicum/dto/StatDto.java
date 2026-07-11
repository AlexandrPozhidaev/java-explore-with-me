package ru.practicum.dto;

import java.time.LocalDateTime;

public class StatDto {
    private String app;
    private String uri;
    private String ip;

    public StatDto() {}

    public StatDto(String app, String uri, String ip) {
        this.app = app;
        this.uri = uri;
        this.ip = ip;
    }

    public String getApp() { return app; }
    public void setApp(String app) { this.app = app; }

    public String getUri() { return uri; }
    public void setUri(String uri) { this.uri = uri; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public LocalDateTime getTimestamp() {
        return LocalDateTime.now();
    }
}
