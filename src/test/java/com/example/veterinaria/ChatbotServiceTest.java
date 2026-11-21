package com.example.veterinaria;

import com.example.veterinaria.controller.ChatbotController;
import com.example.veterinaria.service.ChatbotService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatbotControllerTest {

    @Mock
    private ChatbotService chatbotService;

    @InjectMocks
    private ChatbotController chatbotController;

    @Test
    void enviarMensajeDevuelveRespuestaCorrectamente() {
        when(chatbotService.responder(eq("Hola"), anyList()))
                .thenReturn("Hola, ¿en qué puedo ayudarte?");

        var solicitud = new ChatbotController.MessageRequest(
                "Hola", List.of());

        var respuesta = chatbotController.enviarMensaje(solicitud);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(
                "Hola, ¿en qué puedo ayudarte?",
                respuesta.getBody().get("reply"));

        verify(chatbotService).responder(eq("Hola"), anyList());
    }

    @Test
    void enviarMensajeVacioDevuelveError() {
        var solicitud = new ChatbotController.MessageRequest(
                "   ", List.of());

        var respuesta = chatbotController.enviarMensaje(solicitud);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                respuesta.getStatusCode());

        verifyNoInteractions(chatbotService);
    }
}
