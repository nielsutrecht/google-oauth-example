package com.nibado.example.googleoauth;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Properties;

public class WebServer {
    private final HttpServer server;
    private final Properties properties;
    private final RouteHandler handler;

    public WebServer(Properties properties) throws IOException {
        this.properties = properties;

        var port = Integer.parseInt(properties.getProperty("port"));

        handler = new RouteHandler(properties);
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", handler::handleIndex);
        server.createContext("/oauth", handler::handleOauth);

        System.out.printf("Started service on http://localhost:%s%n", port);
    }

    public void start() {
        server.start();
    }

    public static void main(String[] args) throws Exception {
        var properties = new Properties();
        properties.load(WebServer.class.getResourceAsStream("/app.properties"));

        new WebServer(properties).start();
    }
}
