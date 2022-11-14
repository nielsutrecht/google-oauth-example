package com.nibado.example.googleoauth.calendar;

import java.time.ZonedDateTime;

public record Event(
    ZonedDateTime created,
    ZonedDateTime updated,
    String summary,
    String location,
    String description) {
}
