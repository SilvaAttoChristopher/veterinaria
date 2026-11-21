package com.example.veterinaria.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatbotService {

    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    private static final String INSTRUCCIONES = """
        Eres el asistente virtual de CiruVet, una aplicación web veterinaria.

        Responde siempre en español, con claridad, amabilidad y de forma concisa.

        Puedes:
        - Explicar cómo utilizar las funciones de CiruVet.
        - Responder preguntas generales sobre el cuidado responsable
          de perros, gatos y otras mascotas.
        - Orientar al usuario para encontrar las secciones del sistema.

        No afirmes que has consultado citas, mascotas, vacunas ni datos
        de la base de datos: no tienes acceso a ellos.

        No inventes funciones, botones ni rutas que no conozcas.
        Si no sabes cómo funciona una sección concreta, dilo claramente.

        No sustituyas a un veterinario ni emitas diagnósticos definitivos.
        Si hay señales de urgencia, recomienda contactar con un veterinario
        de inmediato. No recomiendes medicamentos ni dosis sin evaluación
        profesional.

        No solicites contraseñas, claves API ni información personal
        innecesaria. No afirmes haber realizado acciones en la aplicación.
        """;

    public ChatbotService(
            ObjectMapper objectMapper,
            @Value("${GEMINI_API_KEY:}") String apiKey,
            @Value("${GEMINI_MODEL:gemini-3.1-flash-lite}") String model) {

        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
    }

    public record ChatMessage(String role, String content) {}

    public String responder(
            String mensaje,
            List<ChatMessage> historial) {

        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "El chatbot no está configurado. Falta la clave de Gemini.");
        }

        try {
            List<Map<String, Object>> contenidos = new ArrayList<>();

            // Solo se envían los últimos 10 mensajes anteriores.
            if (historial != null) {
                int inicio = Math.max(0, historial.size() - 10);

                for (ChatMessage anterior :
                        historial.subList(inicio, historial.size())) {

                    if (anterior == null
                            || anterior.content() == null
                            || anterior.content().isBlank()) {
                        continue;
                    }

                    String rol = anterior.role();

                    // El navegador no puede introducir roles del sistema.
                    if (!"user".equals(rol) && !"model".equals(rol)) {
                        continue;
                    }

                    String texto = anterior.content();

                    if (texto.length() > 2000) {
                        texto = texto.substring(0, 2000);
                    }

                    contenidos.add(Map.of(
                            "role", rol,
                            "parts", List.of(Map.of("text", texto))
                    ));
                }
            }

            // Añadir el mensaje actual.
            contenidos.add(Map.of(
                    "role", "user",
                    "parts", List.of(Map.of("text", mensaje))
            ));

            Map<String, Object> cuerpo = new LinkedHashMap<>();

            cuerpo.put("systemInstruction", Map.of(
                    "parts", List.of(Map.of("text", INSTRUCCIONES))
            ));

            cuerpo.put("contents", contenidos);

            cuerpo.put("generationConfig", Map.of(
                    "temperature", 0.5,
                    "maxOutputTokens", 700
            ));

            String url = "https://generativelanguage.googleapis.com/"
                    + "v1beta/models/" + model + ":generateContent";

            HttpRequest solicitud = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(45))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(
                            objectMapper.writeValueAsString(cuerpo)))
                    .build();

            HttpResponse<String> respuesta = httpClient.send(
                    solicitud,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (respuesta.statusCode() < 200
                    || respuesta.statusCode() >= 300) {

                // No devolver al navegador la respuesta interna de Google.
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Gemini no pudo procesar la consulta. "
                                + "Comprueba el modelo, la clave y la cuota.");
            }

            JsonNode raiz = objectMapper.readTree(respuesta.body());
            JsonNode partes = raiz.path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts");

            StringBuilder textoRespuesta = new StringBuilder();

            for (JsonNode parte : partes) {
                if (parte.hasNonNull("text")) {
                    textoRespuesta.append(parte.get("text").asText());
                }
            }

            if (textoRespuesta.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Gemini no devolvió una respuesta de texto.");
            }

            return textoRespuesta.toString().trim();

        } catch (ResponseStatusException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "La consulta fue interrumpida.");
        } catch (IOException | IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "No se pudo completar la consulta a Gemini.");
        }
    }
}
