package ru.practicum.mainsrvc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.mainsrvc.entity.Event;
import ru.practicum.mainsrvc.entity.EventStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByIdAndState(Long id, EventStatus state);

    @Query("SELECT e FROM Event e " +
            "WHERE (:states IS NULL OR e.state IN :states) " +
            "AND (:rangeStart IS NULL OR e.eventDate >= :rangeStart) " +
            "AND (:rangeEnd IS NULL OR e.eventDate <= :rangeEnd) " +
            "AND (:users IS NULL OR e.initiator.id IN :users) " +
            "AND (:categories IS NULL OR e.category.id IN :categories)")
    Page<Event> findByAdminFilters(
            @Param("states") List<String> states,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            @Param("users") List<Long> users,
            @Param("categories") List<Long> categories,
            Pageable pageable
    );

    @Query("SELECT e FROM Event e " +
            "WHERE e.state = 'PUBLISHED' " +
            "AND (:categoryIds IS NULL OR e.category.id IN :categoryIds) " +
            "AND (:paid IS NULL OR e.paid = :paid) " +
            "AND (:text IS NULL OR (LOWER(e.title) LIKE LOWER(:text) OR LOWER(e.annotation) LIKE LOWER(:text))) " +
            "AND (:rangeStart IS NULL OR e.eventDate >= :rangeStart) " +
            "AND (:rangeEnd IS NULL OR e.eventDate <= :rangeEnd)")
    Page<Event> findPublished(
            List<Long> categoryIds,
            Boolean paid,
            String text,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Pageable pageable
    );

    @Query("SELECT e FROM Event e WHERE e.id = :eventId AND e.initiator.id = :initiatorId")
    Optional<Event> findByIdAndInitiator(@Param("eventId") Long eventId, @Param("initiatorId") Long initiatorId);

    @Query("SELECT e FROM Event e WHERE e.initiator.id = :initiatorId")
    Page<Event> findAllByInitiatorId(@Param("initiatorId") Long initiatorId, Pageable pageable);
}
