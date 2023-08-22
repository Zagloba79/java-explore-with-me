package ru.practicum.ewm.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "events")
@AllArgsConstructor
public class Event {
    private String annotation;
    private Integer category;
    private String description;
    private String eventDate;
    private Location Location;
    private boolean paid;
    private Integer participantLimit;
    private boolean requestModeration;
    private String title;
}
