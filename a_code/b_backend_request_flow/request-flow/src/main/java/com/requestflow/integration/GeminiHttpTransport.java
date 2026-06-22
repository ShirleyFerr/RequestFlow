package com.requestflow.integration;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@FunctionalInterface
public interface GeminiHttpTransport {

    HttpResponse<String> send(HttpRequest request) throws IOException, InterruptedException;
}
