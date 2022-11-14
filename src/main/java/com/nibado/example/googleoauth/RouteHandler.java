package com.nibado.example.googleoauth;

import com.nibado.example.googleoauth.calendar.CalendarClient;
import com.nibado.example.googleoauth.oauth.OauthClient;
import com.nibado.example.googleoauth.oauth.Token;
import com.sun.net.httpserver.HttpExchange;
import okhttp3.HttpUrl;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public class RouteHandler {
    private final TemplateEngine templateEngine;
    private final Properties properties;
    private final OauthClient oauthClient;
    private final CalendarClient calendarClient;

    private Token currentToken;

    public RouteHandler(Properties properties) {
        this.templateEngine = createTemplateEngine();
        this.properties = properties;
        this.oauthClient = new OauthClient(properties);
        this.calendarClient = new CalendarClient(properties);
    }

    public void handleIndex(HttpExchange t) throws IOException {
        if(currentToken == null) {
            writeRedirect(t, oauthClient.buildRedirect());
        } else {

            if(currentToken.expires().isBefore(Instant.now().minusSeconds(60))) {
                currentToken = oauthClient.refresh(currentToken);
            }

            var calendars = calendarClient.listCalendars(currentToken);
            var events = calendarClient.listEvents(currentToken, properties.getProperty("primary-calendar"));

            writeTemplate(t, "index", Map.of("calendars", calendars, "events", events));
        }
    }

    public void handleOauth(HttpExchange t) throws IOException {
        var url = HttpUrl.parse("http://localhost:8000" + t.getRequestURI().toString());
        var code = url.queryParameter("code");

        currentToken = oauthClient.exchange(code);

        writeRedirect(t, "http://localhost:8000");
    }

    private void writeTemplate(HttpExchange t, String template, Map<String, Object> variables) throws IOException {
        var headers = t.getResponseHeaders();

        headers.add("Content-Type", "text/html;charset=UTF-8");
        headers.add("Pragma", "no-cache");
        headers.add("Cache-Control", "no-cache");
        headers.add("Expires", "0");

        var context = new Context(Locale.US, variables);
        var baos = new ByteArrayOutputStream();

        templateEngine.process(template, context, new OutputStreamWriter(baos));

        t.sendResponseHeaders(200, baos.size());
        t.getResponseBody().write(baos.toByteArray());
    }

    private void writeRedirect(HttpExchange t, String url) throws IOException {
        var headers = t.getResponseHeaders();
        headers.add("Location", url);

        t.sendResponseHeaders(302, 0);
    }

    private TemplateEngine createTemplateEngine() {
        var templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix("/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setCacheTTLMs(3600000L);
        templateResolver.setCacheable(true);

        var templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        return templateEngine;
    }
}
