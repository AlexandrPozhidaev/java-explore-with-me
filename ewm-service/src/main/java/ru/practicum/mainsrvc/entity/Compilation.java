package ru.practicum.mainsrvc.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "compilations")
public class Compilation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, unique = true)
    private String title;

    @Column(name = "description", length = 512)
    private String description;

    @Column(name = "pinned", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean pinned;

    @OneToMany(mappedBy = "compilation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CompilationEvent> events = new ArrayList<>();

    public Compilation(String title, String description, Boolean pinned) {
        this.title = title;
        this.description = description;
        this.pinned = pinned;
    }

    public Compilation() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getPinned() {
        return pinned;
    }

    public void setPinned(Boolean pinned) {
        this.pinned = pinned;
    }

    public List<CompilationEvent> getEvents() {
        return events;
    }

    public void setEvents(List<CompilationEvent> events) {
        this.events = events;
    }
}
