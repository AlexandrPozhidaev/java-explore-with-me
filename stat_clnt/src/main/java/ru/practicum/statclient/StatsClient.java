package ru.practicum.statclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.dto.StatDto;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StatsClient {
    private final RestTemplate restTemplate;
    private final String serverUrl;

    public StatsClient(RestTemplate restTemplate, @Value("${stats.server.url}") String serverUrl) {
        this.restTemplate = restTemplate;
        this.serverUrl = (serverUrl == null || serverUrl.isEmpty())
                ? ""
                : (serverUrl.endsWith("/") ? serverUrl : serverUrl + "/");
    }

    public StatDto hit(String uri, String app, String ip) {
        LocalDateTime timestamp = LocalDateTime.now();
        StatDto request = new StatDto(app, uri, ip);
        return restTemplate.postForObject(serverUrl + "hit", request, StatDto.class);
    }

    public List<ViewStatsDto> getStats(String start, String end, List<String> uris, boolean unique) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serverUrl + "stats")
                .queryParam("start", start)
                .queryParam("end", end)
                .queryParam("unique", unique);

        if (uris != null && !uris.isEmpty()) {
            uris.forEach(u -> builder.queryParam("uris", u));
        }

        var responseType = new org.springframework.core.ParameterizedTypeReference<List<ViewStatsDto>>() {};
        return restTemplate.exchange(
                builder.toUriString(),
                org.springframework.http.HttpMethod.GET,
                null,
                responseType
        ).getBody();
    }
}
