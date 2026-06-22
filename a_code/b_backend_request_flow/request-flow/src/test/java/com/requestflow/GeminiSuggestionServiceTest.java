package com.requestflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeminiSuggestionServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void returnsAiSuggestionWhenGeminiRespondsWithValidJson() throws Exception {
        Object service = serviceWithResponse(200, geminiResponse("""
                {"category":"BUG","priority":"HIGH","summary":"Falha no login"}
                """));

        Object suggestion = service.getClass().getMethod("suggest", String.class).invoke(service, "Erro no login");

        assertEquals("BUG", value(suggestion, "category"));
        assertEquals("HIGH", value(suggestion, "priority"));
        assertEquals("Falha no login", value(suggestion, "summary"));
        assertEquals("AI", value(suggestion, "source"));
    }

    @Test
    void throwsWhenGeminiReturnsHttpError() throws Exception {
        Object service = serviceWithResponse(500, "{}");

        assertThrows(Exception.class, () -> service.getClass().getMethod("suggest", String.class).invoke(service, "Erro no login"));
    }

    @Test
    void throwsWhenGeminiReturnsInvalidJsonText() throws Exception {
        Object service = serviceWithResponse(200, geminiResponse("texto fora de json"));

        assertThrows(Exception.class, () -> service.getClass().getMethod("suggest", String.class).invoke(service, "Erro no login"));
    }

    @Test
    void throwsWhenGeminiReturnsInvalidCategory() throws Exception {
        Object service = serviceWithResponse(200, geminiResponse("""
                {"category":"FINANCE","priority":"HIGH","summary":"Falha no login"}
                """));

        assertThrows(Exception.class, () -> service.getClass().getMethod("suggest", String.class).invoke(service, "Erro no login"));
    }

    @Test
    void payloadContainsOnlyDescriptionAndGenerationConfig() throws Exception {
        Object service = serviceWithResponse(200, geminiResponse("""
                {"category":"SUPPORT","priority":"MEDIUM","summary":"Ajuda solicitada"}
                """));

        String payload = (String) service.getClass()
                .getMethod("geminiPayload", String.class)
                .invoke(service, "Preciso de ajuda no sistema");
        JsonNode root = objectMapper.readTree(payload);

        assertTrue(root.has("contents"));
        assertTrue(root.has("generationConfig"));
        assertEquals(2, root.size());
        assertTrue(payload.contains("Preciso de ajuda no sistema"));
        assertFalse(payload.toLowerCase().contains("token"));
        assertFalse(payload.toLowerCase().contains("email"));
        assertFalse(payload.toLowerCase().contains("historico"));
        assertFalse(payload.toLowerCase().contains("comentarios"));
    }

    private Object serviceWithResponse(int statusCode, String body) throws Exception {
        Class<?> transportClass = Class.forName("com.requestflow.integration.GeminiHttpTransport");
        Object transport = Proxy.newProxyInstance(
                transportClass.getClassLoader(),
                new Class<?>[]{transportClass},
                (proxy, method, args) -> new FakeHttpResponse(statusCode, body, (HttpRequest) args[0])
        );

        Constructor<?> constructor = Class.forName("com.requestflow.integration.GeminiSuggestionService")
                .getConstructor(ObjectMapper.class, String.class, String.class, long.class, transportClass);
        return constructor.newInstance(objectMapper, "test-api-key", "gemini-2.5-flash", 3000L, transport);
    }

    private String value(Object target, String accessor) throws Exception {
        Object value = target.getClass().getMethod(accessor).invoke(target);
        return String.valueOf(value);
    }

    private String geminiResponse(String text) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "candidates", List.of(Map.of(
                            "content", Map.of(
                                    "parts", List.of(Map.of("text", text))
                            )
                    ))
            ));
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private record FakeHttpResponse(
            int statusCode,
            String body,
            HttpRequest request
    ) implements HttpResponse<String> {

        @Override
        public Optional<HttpResponse<String>> previousResponse() {
            return Optional.empty();
        }

        @Override
        public HttpHeaders headers() {
            return HttpHeaders.of(Map.of(), (first, second) -> true);
        }

        @Override
        public Optional<javax.net.ssl.SSLSession> sslSession() {
            return Optional.empty();
        }

        @Override
        public URI uri() {
            return request.uri();
        }

        @Override
        public HttpClient.Version version() {
            return HttpClient.Version.HTTP_1_1;
        }
    }
}
