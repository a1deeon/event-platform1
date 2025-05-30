package com.example.eventplatform.controller;

import com.example.eventplatform.model.Event;
import com.example.eventplatform.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/events")
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    @GetMapping("/")
    public String home() {
        return "redirect:/events";
    }

    @GetMapping
    public String listEvents(
            Model model,
            @RequestParam(value = "filter", required = false, defaultValue = "upcoming") String filter,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "date", required = false) LocalDate date
    ) {
        LocalDate currentDate = LocalDate.now();

        List<Event> allEvents;

        // Логика поиска
        if (location != null && !location.isEmpty() && date != null) {
            allEvents = eventRepository.findByLocationContainingIgnoreCaseAndDate(location, date);
        } else if (location != null && !location.isEmpty()) {
            allEvents = eventRepository.findByLocationContainingIgnoreCase(location);
        } else if (date != null) {
            allEvents = eventRepository.findByDate(date);
        } else {
            allEvents = eventRepository.findAll();
        }

        List<Event> eventsToDisplay;

        // Фильтрация по "предстоящие" или "прошедшие"
        if ("past".equals(filter)) {
            eventsToDisplay = allEvents.stream()
                    .filter(event -> event.getDate() != null && event.getDate().isBefore(currentDate))
                    .sorted(Comparator.comparing(Event::getDate).reversed())
                    .collect(Collectors.toList());
            model.addAttribute("isPastEventsView", true);
        } else { // "upcoming" или любой другой случай по умолчанию
            eventsToDisplay = allEvents.stream()
                    .filter(event -> event.getDate() != null && (event.getDate().isAfter(currentDate) || event.getDate().isEqual(currentDate)))
                    .sorted(Comparator.comparing(Event::getDate))
                    .collect(Collectors.toList());
            model.addAttribute("isPastEventsView", false);
        }

        model.addAttribute("eventsToDisplay", eventsToDisplay);

        // Логика для "популярных" мероприятий (карусель)
        List<Event> popularEventsForCarousel = eventRepository.findByIsPopularOrderByDateAsc(true).stream()
                .filter(event -> event.getDate() != null && (event.getDate().isAfter(currentDate) || event.getDate().isEqual(currentDate)))
                .limit(5)
                .collect(Collectors.toList());

        model.addAttribute("upcomingEvents", popularEventsForCarousel);

        // Передаем текущие параметры поиска в модель, чтобы форма их сохраняла
        model.addAttribute("location", location);
        model.addAttribute("date", date);
        model.addAttribute("filter", filter);

        return "events";
    }

    @GetMapping("/admin")
    public String adminListEvents(Model model) {
        LocalDate currentDate = LocalDate.now();

        model.addAttribute("upcomingEvents", eventRepository.findAll().stream()
                .filter(event -> event.getDate() != null && (event.getDate().isAfter(currentDate) || event.getDate().isEqual(currentDate)))
                .sorted(Comparator.comparing(Event::getDate))
                .collect(Collectors.toList()));
        model.addAttribute("pastEvents", eventRepository.findAll().stream()
                .filter(event -> event.getDate() != null && event.getDate().isBefore(currentDate))
                .sorted(Comparator.comparing(Event::getDate).reversed())
                .collect(Collectors.toList()));
        return "admin-events";
    }

    @GetMapping("/{id}")
    public String showEventDetails(@PathVariable("id") Long id, Model model) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event Id:" + id));
        model.addAttribute("event", event);
        return "event-details";
    }

    @GetMapping("/new")
    public String showEventForm(Model model) {
        model.addAttribute("event", new Event());
        return "event-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event Id:" + id));
        model.addAttribute("event", event);
        return "event-form";
    }

    @PostMapping("/new")
    public String createEvent(Event event) {
        long popularEventsCount = eventRepository.countByIsPopular(true);
        if (popularEventsCount < 5) {
            event.setPopular(true);
        } else {
            event.setPopular(false);
        }
        eventRepository.save(event);
        return "redirect:/events/admin";
    }

    @PostMapping("/edit/{id}")
    public String updateEvent(@PathVariable("id") Long id, Event event) {
        Event existingEvent = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event Id:" + id));
        event.setId(id);
        event.setPopular(existingEvent.isPopular());
        eventRepository.save(event);
        return "redirect:/events/admin";
    }

    @GetMapping("/delete/{id}")
    public String deleteEvent(@PathVariable("id") Long id) {
        eventRepository.deleteById(id);
        return "redirect:/events/admin";
    }
}