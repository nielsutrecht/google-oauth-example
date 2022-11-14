package com.nibado.example.googleoauth.calendar;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nibado.example.googleoauth.oauth.Token;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class CalendarClient {
    private static final String BASE_URL = "https://www.googleapis.com/calendar/v3";
    private final OkHttpClient client;
    private final Properties properties;
    private final ObjectMapper mapper;

    public CalendarClient(Properties properties) {
        this.client = new OkHttpClient();
        this.properties = properties;
        this.mapper = new ObjectMapper()
            .findAndRegisterModules()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public List<Calendar> listCalendars(Token token) throws IOException {
        var request = new Request.Builder()
            .url(BASE_URL + "/users/me/calendarList")
            .header("Authorization", "Bearer " + token.accessToken())
            .build();

        try (var response = client.newCall(request).execute()) {
            return mapper.readValue(response.body().bytes(), CalendarList.class).items();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public List<Event> listEvents(Token token, String calendarId) throws IOException {
        var timeMin = "2022-10-01T00:00:00Z";
        var request = new Request.Builder()
            .url(BASE_URL + "/calendars/" + calendarId + "/events?timeMin=" + timeMin)
            .header("Authorization", "Bearer " + token.accessToken())
            .build();

        try (var response = client.newCall(request).execute()) {
            return mapper.readValue(response.body().bytes(), EventList.class).items();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }
}
