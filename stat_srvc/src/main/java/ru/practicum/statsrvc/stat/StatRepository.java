package ru.practicum.statsrvc.stat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatRepository extends JpaRepository<Stat, Long> {

    @Query("""
        SELECT new ru.practicum.dto.ViewStatsDto(h.app, h.uri, COUNT(h))
        FROM Stat h
        WHERE (:uris IS NULL OR h.uri IN :uris)
          AND h.timestamp BETWEEN :start AND :end
        GROUP BY h.app, h.uri
        ORDER BY COUNT(h) DESC
        """)
    List<ViewStatsDto> findAllStats(
            @Param("uris") List<String> uris,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT new ru.practicum.dto.ViewStatsDto(h.app, h.uri, COUNT(DISTINCT h.ip))
        FROM Stat h
        WHERE (:uris IS NULL OR h.uri IN :uris)
          AND h.timestamp BETWEEN :start AND :end
        GROUP BY h.app, h.uri
        ORDER BY COUNT(DISTINCT h.ip) DESC
        """)
    List<ViewStatsDto> findUniqueStats(
            @Param("uris") List<String> uris,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
