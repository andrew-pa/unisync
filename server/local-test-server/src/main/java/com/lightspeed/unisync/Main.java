package com.lightspeed.unisync;

import com.google.gson.Gson;
import com.lightspeed.unisync.core.SyncService;
import com.lightspeed.unisync.core.generic.LastWriterWinsConflictResolver;
import com.lightspeed.unisync.core.generic.TrivialValidator;
import com.lightspeed.unisync.core.model.SyncRequest;
import com.lightspeed.unisync.core.model.Table;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.util.Map;

public class Main {
    static final Gson gson = new Gson();

    public static void main(String[] args) {
        HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress(3001), 24);
        } catch(IOException e) {
            System.out.println("Failed to create server: " + e);
            return;
        }

        var srv = new SyncService(
                new InMemoryDataStore(),
                new InMemoryIdentityService(),
                Map.of("contacts", new Table("contacts", new LastWriterWinsConflictResolver(), new TrivialValidator(), null)));

        try {
            server.createContext("/test", exchange -> {
                exchange.sendResponseHeaders(200, 0);
                exchange.close();
            });

            server.createContext("/sync", exchange -> {
                var request = gson.fromJson(new InputStreamReader(exchange.getRequestBody()), SyncRequest.class);
                System.out.println("Processing request: " + request.toString());
                var resp = srv.syncTable(request);
                System.out.println("Sending response: " + resp.toString());
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, 0);
                var writer = new OutputStreamWriter(exchange.getResponseBody());
                gson.toJson(resp, writer);
                writer.close();
                exchange.close();
            });
        } catch(Exception e) {
            System.out.println("Failed to initialize handlers: " + e);
            return;
        }

        System.out.println("Starting server on " + server.getAddress());
        server.start();
    }
}