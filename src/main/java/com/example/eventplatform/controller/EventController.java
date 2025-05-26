package com.example.eventplatform.controller;

import com.example.eventplatform.model.Event;
import com.example.eventplatform.repository.EventRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/events")
public class EventController {

    private final EventRepository eventRepository;

    public EventController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @GetMapping
    public String listEvents(
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(value = "location", required = false) String location,
            Model model) {
        List<Event> events = eventRepository.findAll();

        // Фильтрация по дате
        if (date != null && !date.isEmpty()) {
            LocalDate filterDate = LocalDate.parse(date);
            events = events.stream()
                    .filter(event -> event.getDate() != null && event.getDate().equals(filterDate))
                    .collect(Collectors.toList());
        }

        // Фильтрация по локации
        if (location != null && !location.trim().isEmpty()) {
            String filterLocation = location.trim().toLowerCase();
            events = events.stream()
                    .filter(event -> event.getLocation() != null &&
                            event.getLocation().toLowerCase().contains(filterLocation))
                    .collect(Collectors.toList());
        }

        // Определяем текущую дату (26 мая 2025 года)
        LocalDate today = LocalDate.of(2025, 5, 26);

        // Создаём список мероприятий с их статусом
        List<EventWithStatus> eventsWithStatus = events.stream()
                .map(event -> new EventWithStatus(
                        event,
                        event.getDate().isBefore(today) ? "past" : "upcoming"
                ))
                .collect(Collectors.toList());

        model.addAttribute("eventsWithStatus", eventsWithStatus);
        model.addAttribute("filterDate", date);
        model.addAttribute("filterLocation", location);
        return "events";
    }

    @GetMapping("/new")
    public String showForm(Model model) {
        model.addAttribute("event", new Event());
        return "event-form";
    }

    @PostMapping("/new")
    public String saveEvent(@ModelAttribute Event event) {
        eventRepository.save(event);
        return "redirect:/events";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event Id: " + id));
        model.addAttribute("event", event);
        return "event-form";
    }

    @PostMapping("/edit/{id}")
    public String updateEvent(@PathVariable Long id, @ModelAttribute Event event) {
        Event existingEvent = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event Id: " + id));
        existingEvent.setTitle(event.getTitle());
        existingEvent.setDescription(event.getDescription());
        existingEvent.setLocation(event.getLocation());
        existingEvent.setDate(event.getDate());
        eventRepository.save(existingEvent);
        return "redirect:/events";
    }

    @GetMapping("/delete/{id}")
    public String deleteEvent(@PathVariable Long id) {
        eventRepository.deleteById(id);
        return "redirect:/events";
    }

    // Внутренний класс для передачи мероприятия и его статуса
    public static class EventWithStatus {
        private final Event event;
        private final String status;

        public EventWithStatus(Event event, String status) {
            this.event = event;
            this.status = status;
        }

        public Event getEvent() {
            return event;
        }

        public String getStatus() {
            return status;
        }
    }
}