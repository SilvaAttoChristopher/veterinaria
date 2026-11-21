package com.example.veterinaria.controller;

import com.example.veterinaria.service.ChatbotService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/message")
    public ResponseEntity<Map<String, String>> enviarMensaje(
            @RequestBody MessageRequest solicitud) {

        if (solicitud == null
                || solicitud.message() == null
                || solicitud.message().isBlank()) {

            return ResponseEntity.badRequest().body(
                    Map.of("error", "Escribe un mensaje antes de enviarlo."));
        }

        String mensaje = solicitud.message().trim();

        if (mensaje.length() > 1000) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "El mensaje no puede superar los 1000 caracteres."));
        }

        List<ChatMessage> historial = solicitud.history() == null
                ? List.of()
                : solicitud.history();

        List<ChatbotService.ChatMessage> historialServicio =
                historial.stream()
                        .map(m -> new ChatbotService.ChatMessage(
                                m.role(), m.content()))
                        .toList();

        String respuesta = chatbotService.responder(
                mensaje, historialServicio);

        return ResponseEntity.ok(Map.of("reply", respuesta));
    }

    public record MessageRequest(
            String message,
            List<ChatMessage> history) {
    }

    public record ChatMessage(
            String role,
            String content) {
    }
}
