package ru.practicum.mainsrvc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.mainsrvc.entity.Event;
import ru.practicum.mainsrvc.entity.EventStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByIdAndState(Long id, EventStatus state);

    @Query("SELECT e FROM Event e " +
            "WHERE e.state = :state " +
            "AND (:categoryIds IS NULL OR e.category.id IN :categoryIds) " +
            "AND (:paid IS NULL OR e.paid = :paid) " +
            "AND (:text IS NULL OR (LOWER(e.title) LIKE LOWER(:text) OR LOWER(e.annotation) LIKE LOWER(:text)))")
    Page<Event> findPublished(
            List<Long> categoryIds,
            Boolean paid,
            String text,
            Pageable pageable
    );

    @Query("SELECT e FROM Event e WHERE e.id = :eventId AND e.initiator.id = :initiatorId")
    Optional<Event> findByIdAndInitiator(@Param("eventId") Long eventId, @Param("initiatorId") Long initiatorId);
}
