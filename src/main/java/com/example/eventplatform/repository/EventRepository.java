package com.example.eventplatform.repository;

import com.example.eventplatform.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    long countByIsPopular(boolean isPopular);
    List<Event> findByIsPopularOrderByDateAsc(boolean isPopular);

    // НОВОЕ: Методы для поиска по локации и дате
    List<Event> findByLocationContainingIgnoreCase(String location);
    List<Event> findByDate(LocalDate date);
    List<Event> findByLocationContainingIgnoreCaseAndDate(String location, LocalDate date);
}