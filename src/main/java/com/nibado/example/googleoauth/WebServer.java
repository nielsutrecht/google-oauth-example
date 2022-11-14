package com.nibado.example.googleoauth;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Properties;

public class WebServer {
    private final HttpServer server;
    private final Properties properties = new Properties();
    private final RouteHandler handler;

    public WebServer(int port) throws IOException {
        this.properties.load(WebServer.class.getResourceAsStream("/secrets.properties"));

        this.handler = new RouteHandler(properties);
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", handler::handleIndex);
        server.createContext("/oauth", handler::handleOauth);
    }

    public void start() {
        server.start();
    }

    public static void main(String[] args) throws Exception {
        new WebServer(8000).start();
    }
}
