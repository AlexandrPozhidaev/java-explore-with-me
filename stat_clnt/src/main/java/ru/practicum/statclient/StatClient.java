package ru.practicum.statclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.dto.StatDto;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatClient {
    private final RestTemplate restTemplate;
    private final String serverUrl;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatClient(RestTemplate restTemplate, @Value("${stats.server.url}") String serverUrl) {
        this.restTemplate = restTemplate;
        this.serverUrl = serverUrl.endsWith("/") ? serverUrl : serverUrl + "/";
    }


    public StatDto hit(String uri, String app, String ip) {
        StatDto request = new StatDto(app, uri, ip, LocalDateTime.now());
        return restTemplate.postForObject(serverUrl + "hit", request, StatDto.class);
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        String startStr = start.format(FORMATTER);
        String endStr = end.format(FORMATTER);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(serverUrl + "/stats")
                .queryParam("start", startStr)
                .queryParam("end", endStr)
                .queryParam("unique", unique);

        if (uris != null && !uris.isEmpty()) {
            for (String u : uris) {
                builder.queryParam("uris", u);
            }
        }

        ResponseEntity<List<ViewStatsDto>> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ViewStatsDto>>() {}
        );
        return response.getBody();
    }

    public Map<String, Long> getHits(List<String> uris) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusYears(10);
        LocalDateTime end = now.plusYears(10);

        List<ViewStatsDto> stats = getStats(start, end, uris, false);

        Map<String, Long> result = new HashMap<>();
        for (ViewStatsDto s : stats) {
            result.put(s.getUri(), s.getHits());
        }
        return result;
    }
}
