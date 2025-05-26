package com.example.eventplatform.repository;

import com.example.eventplatform.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
}
