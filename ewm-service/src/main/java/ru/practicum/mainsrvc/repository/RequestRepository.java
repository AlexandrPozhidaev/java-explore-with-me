package ru.practicum.mainsrvc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.mainsrvc.entity.ParticipationRequest;
import ru.practicum.mainsrvc.entity.RequestStatus;

import java.util.List;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {

    List<ParticipationRequest> findAllByRequesterId(Long requesterId);

    List<ParticipationRequest> findAllByEventId(Long eventId);

    Page<ParticipationRequest> findAllByRequesterId(Long requesterId, Pageable pageable);

    Page<ParticipationRequest> findAllByRequesterIdAndEventId(
            Long requesterId, Long eventId, Pageable pageable);

    @Query("SELECT COUNT(r) FROM ParticipationRequest r WHERE r.event.id = :eventId AND r.status = 'CONFIRMED'")
    long countConfirmedByEventId(@Param("eventId") Long eventId);

    boolean existsByRequesterIdAndEventIdAndStatusNot(@Param("requesterId") Long requesterId,
                                                      @Param("eventId") Long eventId,
                                                      @Param("status") RequestStatus status);
}
