package com.requestflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AiProviderRouterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void usesFallbackWhenApiKeyIsEmpty() throws Exception {
        Object router = router(Optional.empty(), geminiService(200, geminiResponse("""
                {"category":"BUG","priority":"HIGH","summary":"Falha no login"}
                """)));

        Object suggestion = suggest(router, "Preciso de acesso ao sistema");

        assertEquals("FALLBACK", value(suggestion, "source"));
        assertEquals("ACCESS", value(suggestion, "category"));
    }

    @Test
    void usesGeminiWhenProviderAndApiKeyAreConfigured() throws Exception {
        Object router = router(Optional.of("test-api-key"), geminiService(200, geminiResponse("""
                {"category":"BUG","priority":"HIGH","summary":"Falha no login"}
                """)));

        Object suggestion = suggest(router, "Erro no login");

        assertEquals("AI", value(suggestion, "source"));
        assertEquals("BUG", value(suggestion, "category"));
    }

    @Test
    void fallsBackWhenGeminiThrows() throws Exception {
        Object router = router(Optional.of("test-api-key"), geminiService(200, geminiResponse("fora de json")));

        Object suggestion = suggest(router, "Ambiente de producao indisponivel");

        assertEquals("FALLBACK", value(suggestion, "source"));
        assertEquals("INCIDENT", value(suggestion, "category"));
        assertEquals("CRITICAL", value(suggestion, "priority"));
    }

    private Object router(Optional<String> apiKey, Object geminiSuggestionService) throws Exception {
        Object router = Class.forName("com.requestflow.integration.AiProviderRouter")
                .getDeclaredConstructor()
                .newInstance();
        setField(router, "provider", "gemini");
        setField(router, "apiKey", apiKey);
        setField(router, "fallbackService", Class.forName("com.requestflow.integration.AiFallbackService")
                .getDeclaredConstructor()
                .newInstance());
        setField(router, "geminiSuggestionService", geminiSuggestionService);
        return router;
    }

    private Object suggest(Object router, String description) throws Exception {
        Class<?> requestClass = Class.forName("com.requestflow.dto.ai.AiSuggestionRequestDTO");
        Object request = requestClass.getDeclaredConstructor(String.class).newInstance(description);
        return router.getClass().getMethod("suggest", requestClass).invoke(router, request);
    }

    private Object geminiService(int statusCode, String body) throws Exception {
        Class<?> transportClass = Class.forName("com.requestflow.integration.GeminiHttpTransport");
        Object transport = java.lang.reflect.Proxy.newProxyInstance(
                transportClass.getClassLoader(),
                new Class<?>[]{transportClass},
                (proxy, method, args) -> new FakeHttpResponse(statusCode, body, (HttpRequest) args[0])
        );

        Constructor<?> constructor = Class.forName("com.requestflow.integration.GeminiSuggestionService")
                .getConstructor(ObjectMapper.class, String.class, String.class, long.class, transportClass);
        return constructor.newInstance(objectMapper, "test-api-key", "gemini-2.5-flash", 3000L, transport);
    }

    private void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
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
