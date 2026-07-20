package ru.practicum.mainsrvc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.mainsrvc.entity.Compilation;

@Repository
public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    Page<Compilation> findByPinnedIsTrue(Pageable pageable);
    Page<Compilation> findByPinnedIsFalse(Pageable pageable);

    boolean existsByTitle(String title);
}